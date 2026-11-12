package com.portfolio.qiita_summary_notifier.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class RegisterForm {

    @NotBlank (message = "名前は必須です")
    @Size (min = 3, max = 255, message = "名前が短すぎます")
    private String username;

    @NotBlank (message = "パスワードは必須です")
    @Size (min = 8, max = 255, message = "パスワードは8文字以上にしてください")
    private String password;

    @NotBlank (message = "確認用パスワードは必須です")
    private String passwordConfirm;
}
