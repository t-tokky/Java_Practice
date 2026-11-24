package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.portfolio.qiita_summary_notifier.entity.NotificationSetting;
import com.portfolio.qiita_summary_notifier.form.SettingForm;
import com.portfolio.qiita_summary_notifier.helper.SettingHelper;
import com.portfolio.qiita_summary_notifier.security.CustomUserDetails;
import com.portfolio.qiita_summary_notifier.service.SettingsService;

import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping ("/settings")
@RequiredArgsConstructor 
public class SettingsController {

    private final SettingsService settingsService;

    // 新規設定と更新の二つをこのメソッドで行うために、idを用いた条件分岐
    @GetMapping("/form")
    public String formSetting(
        @AuthenticationPrincipal CustomUserDetails loginUser, 
        Model model
    ) {
        Integer currentUserId = loginUser.getId();

        if (currentUserId != null) {
            model.addAttribute("setting", settingsService.selectById(currentUserId));
        } else {
            model.addAttribute("setting", new SettingForm());
        }
        
        return "settings/form";
    }

    @PostMapping("/save")
    public String saveSetting(
        @ModelAttribute("setting") @Validated SettingForm form, 
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {    
        // 新規登録時のみwebhookUrlを必須にするメッセージを返す
        if (form.getId() == null) {
            if (form.getWebhookUrl() == null || form.getWebhookUrl().isEmpty()) {
                bindingResult.rejectValue("webhookUrl", "", "新規登録時はWebhookURLが必須です");
            }
        }

        if (bindingResult.hasErrors()) {
            return "settings/form";
        }

        String message =  (form.getId() == null) ? "設定を登録しました" : "設定を更新しました";
        NotificationSetting setting = SettingHelper.convertSetting(form);
        settingsService.saveSettings(setting);
    
        redirectAttributes.addFlashAttribute("message", message);

        return "redirect:/settings/detail";
    }

    // TODO: 処理を簡略化するためにuserIdとsettingIdを対応させているが、本来は想定していないので機能拡張時に変更する
    @GetMapping("/detail")
    public String detailSetting(
        @AuthenticationPrincipal CustomUserDetails loginUser, 
        Model model
    ) {
        Integer currentUserId = loginUser.getId();
        model.addAttribute("setting", settingsService.selectById(currentUserId));

        return "settings/detail";
    }
    
}
