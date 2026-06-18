package com.rent1.infrastructure.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 数据权限上下文（线程本地存储）
 * <p>
 * 在 {@link com.rent1.api.interceptor.AuthInterceptor} 解析登录身份后
 * 写入当前登录用户的 memberId 与 memberType，Repository 层可据此自动
 * 注入 landlord_member_id = ? 或 tenant_member_id = ? 过滤条件，
 * 从而实现跨接口一致的数据权限隔离。
 * </p>
 * 对应任务：SEC-021 数据权限隔离 - 房东只能看自有房源
 */
public class DataScopeContext {

    private static final ThreadLocal<DataScope> HOLDER = ThreadLocal.withInitial(DataScope::new);

    public static void set(String memberId, String memberType) {
        HOLDER.set(new DataScope(memberId, memberType));
    }

    public static DataScope get() {
        return HOLDER.get();
    }

    public static String getMemberId() {
        return HOLDER.get().getMemberId();
    }

    public static String getMemberType() {
        return HOLDER.get().getMemberType();
    }

    public static boolean isLandlord() {
        return "LANDLORD".equals(HOLDER.get().getMemberType());
    }

    public static boolean isTenant() {
        return "TENANT".equals(HOLDER.get().getMemberType());
    }

    public static boolean isAdmin() {
        return "ADMIN".equals(HOLDER.get().getMemberType());
    }

    public static void clear() {
        HOLDER.remove();
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataScope {
        private String memberId;
        private String memberType;
    }
}
