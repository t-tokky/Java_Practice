package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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

    /**memo: 新規登録
     * フォーム画面から受け取ったformオブジェクトをコンバートしてentityに変換
     * O/R MapperでDBへ登録
     * フラッシュメッセージを出したりreturnをリダイレクトにしないといけないかも
     */
    @PostMapping("/save")
    public String createSetting(SettingForm form) {
        NotificationSetting setting = SettingHelper.convertSetting(form);
        settingMapper.insertSetting(setting);
        // フラッシュメッセージとリダイレクト？
        // htmlも合わせて修正
    }
    
}
