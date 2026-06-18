package com.rent1.infrastructure.security.typehandler;

import com.rent1.infrastructure.security.CryptoService;
import com.rent1.infrastructure.security.SpringContextHolder;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * 敏感字段 MyBatis TypeHandler - 数据库层面自动加解密
 * <p>
 * 使用 {@link CryptoService.Mode#STABLE} 稳定加密模式：
 * 1. 写入数据库：自动加密
 * 2. 读取数据库：自动解密
 * 3. 查询（WHERE 字段 = ?）：将传入明文加密为稳定密文，
 *    与数据库中已存密文比较匹配，从而支持等值查询与 UNIQUE 约束。
 * </p>
 * 使用方式：在实体字段上添加
 * <pre>{@code @TableField(typeHandler = SensitiveStringTypeHandler.class)}</pre>
 * 适用字段：
 * <ul>
 *     <li>member.phone_encrypted - 手机号</li>
 *     <li>member.real_name_encrypted - 真实姓名</li>
 *     <li>member.id_card_encrypted - 身份证号</li>
 *     <li>member_contact.name_encrypted - 联系人姓名</li>
 *     <li>member_contact.phone_encrypted - 联系人手机号</li>
 * </ul>
 * 对应任务：SEC-001 敏感字段数据库加密存储
 */
@MappedTypes(String.class)
public class SensitiveStringTypeHandler extends BaseTypeHandler<String> {

    private CryptoService cryptoService;

    private CryptoService getCryptoService() {
        if (cryptoService == null) {
            cryptoService = SpringContextHolder.getBean(CryptoService.class);
        }
        return cryptoService;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        if (parameter == null || parameter.isEmpty()) {
            ps.setString(i, parameter);
            return;
        }
        // 已加密格式不再二次加密
        String value = getCryptoService().isEncrypted(parameter)
                ? parameter
                : getCryptoService().encryptStable(parameter);
        ps.setString(i, value);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return decrypt(rs.getString(columnName));
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return decrypt(rs.getString(columnIndex));
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return decrypt(cs.getString(columnIndex));
    }

    private String decrypt(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        try {
            return getCryptoService().decrypt(value);
        } catch (Exception e) {
            // 兼容历史明文数据：解密失败则直接返回原值
            return value;
        }
    }
}
