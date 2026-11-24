package com.portfolio.qiita_summary_notifier.security;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.springframework.security.crypto.encrypt.TextEncryptor;

import com.portfolio.qiita_summary_notifier.helper.SpringContextHolder;

import lombok.NoArgsConstructor;

// DB登録時に暗号化処理を挟むため、継承して拡張する
@NoArgsConstructor 
public class CryptoTypeHandler extends BaseTypeHandler<String> {

    private TextEncryptor textEncryptor;

    // MyBatisによってインスタンス化されるので、DIによる取得ができない
    // そのため、初回実行時にDIコンテナから直接取得する
    private TextEncryptor getTextEncryptor() {
        if (this.textEncryptor == null) {
            this.textEncryptor = SpringContextHolder.getBean(TextEncryptor.class);
        }
        return this.textEncryptor;
    }

    // DB保存時に暗号化してから保存
    @Override 
    public void setNonNullParameter(
        PreparedStatement ps, 
        int i, 
        String parameter, 
        JdbcType jdbcType
    ) throws SQLException {
        ps.setString(i, getTextEncryptor().encrypt(parameter));
    }

    // DBから取り出すときに復号してから取り出す
    @Override 
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        String value = rs.getString(columnName);
        return (value == null) ? null : getTextEncryptor().decrypt(value);
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        String value = rs.getString(columnIndex);
        return (value == null) ? null : getTextEncryptor().decrypt(value);
    }

    @Override 
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        String value = cs.getString(columnIndex);
        return (value == null) ? null : getTextEncryptor().decrypt(value);
    }
}
