package com.portfolio.qiita_summary_notifier.security;

import java.nio.charset.StandardCharsets;

import org.springframework.security.crypto.codec.Hex;
import org.springframework.security.crypto.encrypt.BytesEncryptor;
import org.springframework.security.crypto.encrypt.TextEncryptor;

/**memo: オリジナルのTextEncryptor
 * 暗号化に使うためのオブジェクトを作るクラス
 * 本来は作らなくてもTextEncryptorのBeanを作成できる(Encryptor.textなど)が
 * すでに非推奨となっており、代替するものがBytesEncryptorを実装したクラスだった
 * BytesEncryptorは入出力をbyte[]で行うので、テスト時に使いづらいことから
 * TextEncryptorを実装してラップしている
 */
public class MyHexEncodingTextEncryptor implements TextEncryptor{

    private final BytesEncryptor encryptor;

    public MyHexEncodingTextEncryptor(BytesEncryptor encryptor) {
        this.encryptor = encryptor;
    }

    // 入ってきた文字列(暗号化前のテキスト)をbyteに変換->暗号化して返す
    @Override 
    public String encrypt(String text) {
        byte[] encrypted = encryptor.encrypt(text.getBytes(StandardCharsets.UTF_8));
        return new String(Hex.encode(encrypted));
    }

    // 入ってきた文字列(暗号化後のテキスト)を復号->文字列に変換して返す
    @Override 
    public String decrypt(String encryptedText) {
        byte[] decrypted = encryptor.decrypt(Hex.decode(encryptedText));
        return new String(decrypted, StandardCharsets.UTF_8);
    }
}
