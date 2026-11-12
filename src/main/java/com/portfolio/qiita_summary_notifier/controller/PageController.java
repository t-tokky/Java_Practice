package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.portfolio.qiita_summary_notifier.form.RegisterForm;
import com.portfolio.qiita_summary_notifier.security.CustomUserDetails;







/**memo: このクラスの説明
 * このクラスはページ管理(htmlの切り替え)のみで使う
 * 画面を切り替えないものはNotificatinoApiControllerで作る
 */
@Controller 
public class PageController {   

    @GetMapping("/")
    public String top() {
        return "index";
    }
    

    @GetMapping ("/demo")
    public String demo() {
        return "demo-notification";
    }

    @GetMapping("/qa")
    public String help() {
        return "qa";
    }
    
    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
    

    @GetMapping("/login")
    public String showLogin(@AuthenticationPrincipal CustomUserDetails loginUser) {
        // ログイン済みならdetailに飛ばす
        if (loginUser != null) {
            return "redirect:/settings/detail";
        }
        return "account/login";
    }

    @GetMapping("/signup")
    public String showSignup(@ModelAttribute("registerForm") RegisterForm form) {
        return "account/signup";
    } 

    @GetMapping("/user/delete/confirm")
    public String showDeleteConfirm() {
        return "account/delete-confirm";
    }
    
}
