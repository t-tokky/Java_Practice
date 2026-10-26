package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

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
     * ユーザーごとのidの渡し方がピンと来てない
     * ユーザーはこの画面に入る前にログインをしているので、どこかにはユーザー情報がある...はず?
     */
    @GetMapping
    public String settingsList(Model model) {
        model.addAttribute("settings", settingMapper.selectByUserId(1));
        return "settings/menu";
    }
    

    @GetMapping("/form")
    public String se() {
        return "form";
    }
}
