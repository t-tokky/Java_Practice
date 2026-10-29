# バリデーションのメモ

バリデーション関連のメモ

### Tips

- ValidationMessages.propertiesなどのファイルにアノテーションのメッセージを入れることでメンテナンス性が向上したり、メッセージ内容を多言語対応できる  
ValidationMessages.propertiesの記述：`NotBlank.message = 必ず入力してください`  
Javaにつけるアノテーション：`@NotBlank(message = "{NotBlank.message}")`  
