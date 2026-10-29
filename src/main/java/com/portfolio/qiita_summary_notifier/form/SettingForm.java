package com.portfolio.qiita_summary_notifier.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class SettingForm {
    private Integer id;  // 新規登録・更新の違いを受け取ったIDの有無で切り替える
    @NotBlank (message = "必須項目です")
    private String includeTags;
    private String excludeTags;
    @NotBlank (message = "必須項目です")
    private String webhookName;
    @NotBlank (message = "必須項目です")
    @Pattern (regexp = "^https://discord\\.com/api/webhooks/\\d+/[a-zA-Z0-9_-]+$", message = "正しいURLを入力してください")
    @Size(max = 255, message = "正しいURLを入力してください")
    private String webhookUrl;
    private Boolean isActive;  // 入力されない可能性もあるのでオブジェクト型
}
