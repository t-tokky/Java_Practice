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
    // 新規登録・更新の違いをidの有無で切り替える
    private Integer id;  
    
    @NotBlank (message = "必須項目です")
    private String includeTags;
    private String excludeTags;
    
    @NotBlank (message = "必須項目です")
    private String webhookName;
    
    @Pattern (regexp = "^(https://discord\\.com/api/webhooks/\\d+/[a-zA-Z0-9_-]+)?$", message = "正しいURLを入力してください")
    @Size(max = 255, message = "正しいURLを入力してください")
    private String webhookUrl;
    private Boolean isActive;  // 入力されない可能性もあるのでオブジェクト型
}
