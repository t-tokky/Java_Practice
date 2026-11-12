package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import com.portfolio.qiita_summary_notifier.security.AccountDeletionService;
import com.portfolio.qiita_summary_notifier.security.CustomUserDetails;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;


@Controller 
@RequiredArgsConstructor 
public class AccountDeleteController {

    private final AccountDeletionService deletionService;

    @PostMapping("/user/delete")
    public String deleteAccount(
        @AuthenticationPrincipal CustomUserDetails loginUserDetails, 
        HttpServletRequest request, 
        HttpServletResponse response, 
        Authentication auth
    ) {
        Integer currentUserId = loginUserDetails.getId();
        deletionService.deleteAccount(currentUserId);

        // ログアウト処理のテンプレート  /logoutの内部ではこれが実行されている
        SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
        logoutHandler.logout(request, response, auth);
        
        return "redirect:/login?deleted";
    }
    
}
