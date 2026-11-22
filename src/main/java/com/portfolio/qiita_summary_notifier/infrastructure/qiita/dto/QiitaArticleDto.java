package com.portfolio.qiita_summary_notifier.infrastructure.qiita.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.Data;
import lombok.NoArgsConstructor;

// ここにあるフィールドの値だけ格納するためのアノテーション
@JsonIgnoreProperties (ignoreUnknown = true)
@Data
@NoArgsConstructor 
public class QiitaArticleDto {
    private String title;
    private String url;
    private String body;
    private String id;  // 重複管理に必要 Qiitaの記事idは文字列入り

    // Qiitaのjsonに対応させるためにインナークラスで表現
    private User user;
    private List<Tag> tags;

    @JsonIgnoreProperties (ignoreUnknown = true)
    @Data 
    @NoArgsConstructor 
    public static class User {
        private String id;
        private String name;

        // Qiitaユーザーの名前が設定されていない場合、idの値を使う
        public String getName() {
            return (this.name != null && !this.name.isBlank())
                ? this.name : this.id;
        }
    }

    @JsonIgnoreProperties (ignoreUnknown = true)
    @Data 
    @NoArgsConstructor 
    public static class Tag {
        private String name;
    }
}
