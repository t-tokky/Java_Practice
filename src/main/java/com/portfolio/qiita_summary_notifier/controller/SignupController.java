package com.portfolio.qiita_summary_notifier.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.portfolio.qiita_summary_notifier.form.RegisterForm;
import com.portfolio.qiita_summary_notifier.repository.UserMapper;
import com.portfolio.qiita_summary_notifier.security.SignupService;

import lombok.RequiredArgsConstructor;


@Controller 
@RequiredArgsConstructor 
public class SignupController {

    private final UserMapper userMapper;
    private final SignupService signupService;

    @PostMapping("/signup")
    public String signup(
        @ModelAttribute("registerForm") @Validated RegisterForm form, 
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String username = form.getUsername();
        String password = form.getPassword();
        String passwordConfirm = form.getPasswordConfirm();

        if (bindingResult.hasErrors()) {  // バリデーション
            return "signup";
        }

        // 確認用パスワードの一致を確認
        if (!password.equals(passwordConfirm)) {
            model.addAttribute("message", "確認用パスワードが一致しません");

            return "signup";
        }

        // 名前が使われているかを確認
        if (userMapper.existsByUsername(username)) {
            model.addAttribute("message", "その名前は既に使われています");

            return "signup";
        }

        signupService.registerUser(username, password);
        redirectAttributes.addFlashAttribute("message", "登録が完了しました");
        
        return "redirect:/login";
    }
    
}
