package com.portfolio.qiita_summary_notifier.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor 
@AllArgsConstructor 
public class SettingForm {
    private Integer id;  // 新規登録/更新の違いを受け取ったIDの有無で切り替える
    private String includeTags;
    private String excludeTags;
    private String webhookName;
    private String webhookUrl;
    private Boolean isActive;  // 入力されない可能性もあるのでオブジェクト型
}
