# 概要

後で見返して学習ノートとしてまとめるためのメモの断片  
とりあえずここに避難して、時間の空いたタイミングでまとめていく

## 断片

    /**memo: Beanに同じ型が複数あるとき
     * 以前は@RequiredArgsConstructorを使っていたが、DIするBeanに判断がつかずアプリが起動できなかった
     * lombokのアノテーションで作られたコンストラクタは、元のBeanにアノテーションがついていても
     * 必ずしも引数に@Qualifierが付くわけではない
     * 同じ型のBeanが複数ありコンストラクタがその型のBeanを引数とするときは、
     * 明示的に自分で書く必要がある
     */

    /**memo: デモボタン追加によるオーバーライドメソッドの追加
     * デモボタンを実装しようとしていろいろ試してメソッドをオーバーロードすることにした
     * 場当たり感があるのでリファクタリングは必要
     */

    /**メモ: stream関連
    * このメソッドは取得した記事リストから、通知していない新着記事のみにしたい
    * 取得したarticlesをstreamで流していく
    * settingのidは共通なので、このタイミングで取得しておく
    * filter:  articleを受け取って記事が通知済みでないかを確認している
    *          exists...()メソッドの引数にarticle.getId()をすることで記事のidを取得して渡している
    * toList:  ArrayListに変換するメソッド  Java16から実装された
    *          イミュータブルなオブジェクトなので注意
    *          collect(Collectors.toList())であればミュータブルなリストとして扱える
    */

    /**memo: 記事がなかった時
    * この地点や次の地点で記事が取得できない場合
    * 要素数0のリストオブジェクトはstreamやforを使うと、処理をスキップする
    * 例外は出ないので、分岐させて早めに通知メソッドを呼ぶ必要はない
    */

    /**memo: エンコーダー
    * パスワードのハッシュ化(encode())や検証(matches())を行うのがこのメソッドが返すオブジェクト
    * 今回のPasswordEncoderオブジェクトは、ハッシュ値の先頭にアルゴリズム名を付与して保存する({bcrypt}$2a$10...)
    * これによって別アルゴリズムに移行しても接頭辞から識別が可能
    */

    /**memo: オリジナルのTextEncryptor
    * 暗号化に使うためのオブジェクトを作るクラス
    * 本来は作らなくてもTextEncryptorのBeanを作成できる(Encryptor.textなど)が
    * すでに非推奨となっており、代替するものがBytesEncryptorを実装したクラスだった
    * BytesEncryptorは入出力をbyte[]で行うので、テスト時に使いづらいことから
    * TextEncryptorを実装してラップしている
    */

    /**memo: 認証の流れ
    * 引数にusernameを受け取ってそれをもとにユーザーを検索
    * 合致したレコードを取り出し、usernameとpassword、authorities(許可)をUserオブジェクトに入れて返す
    * このSecurityが用意するUserオブジェクトに入れないとSecurityは読めない
    * (今回はUserを継承したカスタムクラスを作って返している)
    */

    /**memo: TypeHandlerの解説
    * TypeHandlerはDBとJavaでデータの出し入れをするときの型変換を行うクラス
    * MyBatisはTypeHandlerを自動で使用して型変換を行う
    * その時使うTypeHandlerをカスタマイズして拡張したのがこのクラス
    */

    // ログアウト処理のテンプレート  /logoutの内部ではこれが実行されている
    SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
    logoutHandler.logout(request, response, auth);

    /**memo: 設定の登録
     * フォーム画面から受け取ったformオブジェクトをコンバートしてentityに変換
     * 新規ではない&&入力がない場合は既存のurlを使うと解釈し、DBからデータを取り出してセット
     * DBに受け取ったデータを登録
     * フラッシュメッセージと合わせてsettings/detailにリダイレクト
     */
