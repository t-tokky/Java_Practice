package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.form.SettingForm;
import com.portfolio.qiita_summary_notifier.helper.SettingHelper;
import com.portfolio.qiita_summary_notifier.service.SettingsService;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping ("/settings")
@RequiredArgsConstructor 
public class SettingsController {

    // DB操作のためのオブジェクトを注入
    private final SettingsService settingsService;

    /**memo:設定一覧の表示
     * ユーザーが持っている設定を一覧で表示する画面を作成して出力する
     * ユーザーごとのIDはspring securityを入れたタイミングで受け渡せるようになる
     * テスト段階ではidを1に固定しておき、security導入後で変更する
     * 引数に@AuthenticationPrincipalというアノテーションを付けたオブジェクトを指定するらしい
     */
    @GetMapping
    public String showSettings(Model model) {
        Integer userId = 1;  // 仮の値
        model.addAttribute("settings", settingsService.selectByUserId(userId));

        return "settings/menu";
    }
    
    /**memo: 新規設定・設定更新
     * 設定の分岐はRequestParamで受け取る
     * 値があればそのIDを使ってDBから取得
     * 値がなければ空のSettingFormオブジェクトを渡す
     */
    @GetMapping("/form")
    public String formSetting(@RequestParam(name = "id", required = false) Integer settingId, Model model) {
        if (settingId != null) {
            model.addAttribute("setting", settingsService.selectById(settingId));
        } else {
            model.addAttribute("setting", new SettingForm());
        }
        
        return "settings/form";
    }

    /**memo: 設定の登録
     * フォーム画面から受け取ったformオブジェクトをコンバートしてentityに変換
     * 新規ではない&&入力がない場合は既存のurlを使うと解釈し、DBからデータを取り出してセット
     * DBに受け取ったデータを登録
     * フラッシュメッセージと合わせてsettings/detailにリダイレクト
     */
    @PostMapping("/save")
    public String saveSetting(
        @ModelAttribute @Validated SettingForm form, 
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {    
        // 受け渡し方があっているのかよく分からない
        if (bindingResult.hasErrors()) {
            model.addAttribute("setting", form);
            return "setting/form";
        }

        String message =  (form.getId() == null) ? "設定を登録しました" : "設定を更新しました";
        NotificationSetting setting = SettingHelper.convertSetting(form);
        settingsService.saveSettings(setting);
    
        redirectAttributes.addFlashAttribute("message", message);
        // urlの末尾にパラメーターを追加するメソッド(detail?id=値の形)
        redirectAttributes.addAttribute("id", setting.getId());
        return "redirect:/settings/detail";
    }

    @GetMapping("/detail")
    public String detailSetting(@RequestParam(name = "id") Integer settingId, Model model) {
        model.addAttribute("setting", settingsService.selectById(settingId));

        return "settings/detail";
    }
    
    
}
