package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.form.SettingForm;
import com.portfolio.qiita_summary_notifier.helper.SettingHelper;
import com.portfolio.qiita_summary_notifier.repository.NotificationSettingMapper;

import lombok.RequiredArgsConstructor;





@Controller
@RequestMapping ("/settings")
@RequiredArgsConstructor 
public class SettingsController {

    // DB操作のためのオブジェクトを注入
    private final NotificationSettingMapper settingMapper;

    /**memo:設定一覧の表示
     * ユーザーが持っている設定を一覧で表示する画面を作成して出力する
     * ユーザーごとのIDはspring securityを入れたタイミングで受け渡せるようになる
     * テスト段階ではidを1に固定しておき、security導入後で変更する
     * 引数に@AuthenticationPrincipalというアノテーションを付けたオブジェクトを指定するらしい
     */
    @GetMapping
    public String showSettings(Model model) {
        Integer userId = 1;  // 仮の値
        model.addAttribute("settings", settingMapper.selectByUserId(userId));

        return "settings/menu";
    }
    
    /**memo: 新規設定・設定更新
     * ここも一覧設定と同じようにsettingIdをダミーで持たせておく
     * settingIdはログインしているuserがshowSettings()で一覧取得した後に、
     * そこから個別の設定IDを取得する流れになると思う
     */
    @GetMapping("/form")
    public String formSetting(Model model) {
        Integer settingId = 1;  // 仮の値
        model.addAttribute("setting", settingMapper.selectById(settingId));

        return "settings/form";
    }

    /**memo: 設定の登録
     * フォーム画面から受け取ったformオブジェクトをコンバートしてentityに変換
     * 新規ではない&&入力がない場合は既存のurlを使うと解釈し、DBからデータを取り出してセット
     * DBに受け取ったデータを登録
     * フラッシュメッセージと合わせてsettings/detailにリダイレクト
     */

    /**TODO: 条件分岐のserviceクラスへの分離
     * そもそもControllerでRepositoryのメソッドを直接呼び出していることが間違い
     * Sesrviceクラスでメソッドを定義して、それをControllerが使う
     * Serviceの階層構造も修正して、implディレクトリを作成
     * メインのServiceは実装クラスの内容は知らなくていいので、インターフェースとだけ同じ階層にいればいい
     * 
     * 1. service/impl/を作る
     * 2. service/にrepositoryを操作するメソッドの定義(インターフェース)を作成
     * 3. service/impl/にインターフェースを実装した実装クラスを作成
     * 4. このControllerクラスの処理を、実装クラスのメソッドを使ったものに差し替え
     *      (saveメソッドの切り出しもここら辺)
     */
    @PostMapping("/save")
    public String saveSetting(SettingForm form, RedirectAttributes redirectAttributes) {
        // 既存URLを使う場合にformのwebhookUrlをセット
        if (form.getId() != null) {
            if (form.getWebhookUrl() == null || form.getWebhookUrl().isEmpty()) {
                NotificationSetting setting = settingMapper.selectById(form.getId());
                form.setWebhookUrl(setting.getWebhookUrl());
            }
        }

        NotificationSetting setting = SettingHelper.convertSetting(form);
        String message = "";
        if (form.getId() == null) {
            settingMapper.insertSetting(setting);
            message = "設定を登録しました";
        } else {
            settingMapper.updateSetting(setting);
            message = "設定を更新しました";
        }

        redirectAttributes.addFlashAttribute("message", message);
        return "redirect:/settings/detail";
    }

    @GetMapping("/detail")
    public String detailSetting(Model model) {
        Integer settingId = 1;
        model.addAttribute("setting", settingMapper.selectById(settingId));

        return "settings/detail";
    }
    
    
}
