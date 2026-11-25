package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.portfolio.qiita_summary_notifier.form.RegisterForm;
import com.portfolio.qiita_summary_notifier.security.CustomUserDetails;

@Controller 
public class PageController {   

    @GetMapping("/")
    public String showTop() {
        return "index";
    }

    @GetMapping ("/demo")
    public String showDemo() {
        return "demo-notification";
    }

    @GetMapping("/qa")
    public String showHelp() {
        return "qa";
    }
    
    @GetMapping("/terms")
    public String showTerms() {
        return "terms";
    } 

    @GetMapping("/login")
    public String showLogin(@AuthenticationPrincipal CustomUserDetails loginUser) {
        // ログイン済みユーザーに2重にログインをさせない
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
