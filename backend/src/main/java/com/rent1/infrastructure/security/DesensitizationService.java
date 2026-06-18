package com.rent1.infrastructure.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 数据脱敏服务（基础设施层 - 无业务逻辑）
 * <p>
 * 对手机号、身份证号、真实姓名等敏感数据按规则进行展示前脱敏，
 * 确保小程序端、管理后台展示符合隐私保护要求。
 * 管理员可通过 {@link #setAdminMode(boolean)} 或调用不脱敏的原始方法获取明文。
 * </p>
 * 对应任务：
 * <ul>
 *     <li>SEC-010 手机号脱敏展示：前3位+****+后4位</li>
 *     <li>SEC-011 身份证号脱敏展示：前6位+********+后4位</li>
 *     <li>SEC-012 真实姓名脱敏展示：保留首字，其余以 * 隐藏</li>
 * </ul>
 */
@Slf4j
@Service
public class DesensitizationService {

    /** 管理员模式：true 时返回明文，用于平台内部查看场景 */
    private static final ThreadLocal<Boolean> ADMIN_MODE = ThreadLocal.withInitial(() -> Boolean.FALSE);

    /**
     * 设置当前线程是否为管理员查看模式。
     * 管理员查看完成后必须调用 {@link #clearAdminMode()} 清除。
     */
    public void setAdminMode(boolean admin) {
        ADMIN_MODE.set(admin);
    }

    /** 清除当前线程管理员模式标记 */
    public void clearAdminMode() {
        ADMIN_MODE.remove();
    }

    /**
     * 手机号脱敏：138****5678
     */
    public String maskPhone(String phone) {
        if (Boolean.TRUE.equals(ADMIN_MODE.get())) {
            return phone;
        }
        return staticMaskPhone(phone);
    }

    /**
     * 身份证号脱敏：440101********1234
     */
    public String maskIdCard(String idCard) {
        if (Boolean.TRUE.equals(ADMIN_MODE.get())) {
            return idCard;
        }
        return staticMaskIdCard(idCard);
    }

    /**
     * 真实姓名脱敏：张**、李**
     */
    public String maskName(String name) {
        if (Boolean.TRUE.equals(ADMIN_MODE.get())) {
            return name;
        }
        return staticMaskName(name);
    }

    /**
     * 通用脱敏入口 - 按类型选择合适的脱敏策略
     */
    public String mask(String value, SensitiveType type) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        if (Boolean.TRUE.equals(ADMIN_MODE.get())) {
            return value;
        }
        if (type == null) {
            type = SensitiveType.DEFAULT;
        }
        switch (type) {
            case PHONE:
                return staticMaskPhone(value);
            case ID_CARD:
                return staticMaskIdCard(value);
            case NAME:
                return staticMaskName(value);
            default:
                return "***";
        }
    }

    // ==================== 静态工具方法（非 Spring 场景使用） ====================

    public static String staticMaskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        if (phone.length() == 11) {
            return phone.substring(0, 3) + "****" + phone.substring(7);
        }
        int visiblePrefix = Math.min(3, phone.length() / 2);
        int visibleSuffix = Math.min(4, phone.length() - visiblePrefix);
        return phone.substring(0, visiblePrefix) + "****" + phone.substring(phone.length() - visibleSuffix);
    }

    public static String staticMaskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 10) {
            return idCard;
        }
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        // 15位身份证
        return idCard.substring(0, 6) + "*****" + idCard.substring(idCard.length() - 4);
    }

    public static String staticMaskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return name + "*";
        }
        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0));
        for (int i = 1; i < name.length(); i++) {
            masked.append("*");
        }
        return masked.toString();
    }

    /** 脱敏类型枚举 */
    public enum SensitiveType {
        PHONE,
        ID_CARD,
        NAME,
        DEFAULT
    }
}
