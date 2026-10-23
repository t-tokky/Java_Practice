package com.portfolio.qiita_summary_notifier.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.infrastructure.gemini.exception.GeminiSummaryFailedException;
import com.portfolio.qiita_summary_notifier.repository.NotificationLogMapper;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;

import lombok.extern.slf4j.Slf4j;

// このクラスでインフラの3つのメソッドを使って出力する
// ここで出力内容も整理すればいい...はず

@Slf4j 
@Service
public class QiitaNotificationService {

    private final ArticleProvider articleProvider;
    private final NotificationSender notificationSender;
    private final Summarizer summarizer;

    private final NotificationSettingMapper notificationSettingMapper;
    private final NotificationLogMapper notificationLogMapper;

    private final RetryTemplate dbRetryTemplate;

    /**memo: Beanに同じ型が複数あるとき
     * 以前は@RequiredArgsConstructorを使っていたが、DIするBeanに判断がつかずアプリが起動できなかった
     * lombokのアノテーションで作られたコンストラクタは、元のBeanにアノテーションがついていても
     * 必ずしも引数に@Qualifierが付くわけではない
     * 同じ型のBeanが複数ありコンストラクタがその型のBeanを引数とするときは、
     * 明示的に自分で書く必要がある
     */
    public QiitaNotificationService(
        ArticleProvider articleProvider,
        NotificationSender notificationSender,
        Summarizer summarizer,
        NotificationSettingMapper notificationSettingMapper,
        NotificationLogMapper notificationLogMapper,
        @Qualifier("dbRetryTemplate") RetryTemplate dbRetryTemplate
    ) {
        this.articleProvider = articleProvider;
        this.notificationSender = notificationSender;
        this.summarizer = summarizer;
        this.notificationSettingMapper = notificationSettingMapper;
        this.notificationLogMapper = notificationLogMapper;
        this.dbRetryTemplate = dbRetryTemplate;
    }
    
    /**memo: ループ処理
     * DBはクラウドを使用しているので、retry処理を入れている
     * ここで記事が取得できなければ処理が動かない
     */
    // 有効な設定を全件取得してループで回す
    // @Scheduled (cron = "0 30 7 * * *", zone = "Asia/Tokyo")
    public void execute() {
        try {
            dbRetryTemplate.invoke(() -> {
                List<NotificationSetting> settingsList = 
                    notificationSettingMapper.selectActiveSettings();
                for (NotificationSetting setting : settingsList) {
                    deliverArticles(setting);
                }
            });
        } catch(DataAccessException e) {
            log.error("定期処理が実行できませんでした。\nDBにアクセスできません。\n{}", e);
            return;
        }
    }

    /**memo: でもボタン追加によるオーバーライドメソッドの追加
     * でもボタンを実装しようとしていろいろ試してメソッドをオーバーロードすることにした
     * 場当たり感があるのでリファクタリングは必要
     */
    public void deliverArticles(NotificationSetting setting) {
        deliverArticles(setting, false);
    }

    // 1つの設定に対しての記事取得から通知までの処理
    public boolean deliverArticles(NotificationSetting setting, boolean isDemo) {
        // 設定のタグリストを使って最新記事を取得
        String includeTags = setting.getIncludeTags();
        String excludeTags = setting.getExcludeTags();
        List<Article> articles = articleProvider.fetchArticles(includeTags, excludeTags);
        
        /**メモ: stream関連
         * このメソッドは取得した記事リストから、通知していない新着記事のみにしたい
         * 取得したarticlesをstreamで流していく
         * settingのidは共通なので、このタイミングで取得しておく
         * filter:  articleを受け取って記事が通知済みでないかを確認している
         *          exists...()メソッドの引数にarticle.getId()をすることで記事のidを取得して渡している
         * toList:  ArrayListに変換するメソッド  Java16から実装された
         *          イミュータブルなオブジェクトなので注意
         *          collect(Collectors.toList())であればミュータブルなリストとして扱える
         */
        // まだ送信していない記事のみに振り分ける
        Integer settingId = setting.getId();
        List<Article> latestArticles = articles.stream()
                .filter(article -> !notificationLogMapper.existsBySettingIdAndArticleId(settingId, article.getId()))
                .toList();
        /**memo: 記事がなかった時
         * この地点や次の地点で記事が取得できない場合
         * 要素数0のリストオブジェクトはstreamやforを使うと、処理をスキップする
         * 例外は出ないので、分岐させて早めに通知メソッドを呼ぶ必要はない
         */

        // 要約を取り出して、記事データと合わせてオブジェクトにする
        // 要約は都度呼び出すことで、例外発生時にスキップできるようになった
        List<ArticleWithSummary> unformattedList = new ArrayList<>();
        for (Article article : latestArticles) {
            /**memo: 例外処理の内容
             * geminiから要約が返ってこなかった場合ここでキャッチ
             * 中身を見て空か確認するよりも例外をキャッチした方が早いし不具合も起きないはず
             */
            try {
                ArticleWithSummary unformattedData = 
                    new ArticleWithSummary(article, summarizer.fetchSummaryOfArticle(article));
                unformattedList.add(unformattedData);
            } catch (GeminiSummaryFailedException e) {
                log.error("GeminiApiエラー: {}", e);
            }

        }

        // Discordへ通知
        boolean isCompleteNotify = notificationSender.notifyNewArticles(
            setting.getWebhookUrl(), unformattedList
        );

        // 通知履歴をDBへ保存
        if (isCompleteNotify && !isDemo) {
            try {
                dbRetryTemplate.invoke(() -> {
                    for (Article sentArticle : latestArticles) {
                        notificationLogMapper.insertLog(settingId, sentArticle.getId());
                    }
                });
            } catch (DataAccessException e) {
                log.error("通知履歴の保存でエラー発生: {}", e);
            }

        }

        // 通知が完了したかが重要なのでこれでいいはず
        return isCompleteNotify;
    }
}
