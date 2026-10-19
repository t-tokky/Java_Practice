package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**memo: このクラスの説明
 * このクラスはページ管理(htmlの切り替え)のみで使う
 * 画面を切り替えないものはNotificatinoApiControllerで作る
 */
@Controller 
@RequestMapping ("/home")
public class PageController {   

    @GetMapping ("/demo")
    public String demo() {
        return "/demo-notification";
    }
}
