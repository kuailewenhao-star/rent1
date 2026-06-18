package com.rent1.domain.billing;

import lombok.Getter;

/**
 * 费用类型枚举 - 计费规则核心费用项
 * 覆盖收入账单全部9类费用类型
 * 系统固定项：RENT租金、DEPOSIT押金，不可删除
 * 杂费项：WATER至OTHER，可自由配置
 */
@Getter
public enum FeeType {

    RENT("RENT", "租金", true),
    DEPOSIT("DEPOSIT", "押金", true),
    WATER("WATER", "水费", false),
    ELECTRIC("ELECTRIC", "电费", false),
    GAS("GAS", "燃气费", false),
    BROADBAND("BROADBAND", "宽带费", false),
    PROPERTY("PROPERTY", "物业费", false),
    TRASH("TRASH", "垃圾清运费", false),
    OTHER("OTHER", "其他杂费", false);

    private final String code;
    private final String name;
    private final boolean immutable;

    FeeType(String code, String name, boolean immutable) {
        this.code = code;
        this.name = name;
        this.immutable = immutable;
    }

    /**
     * 根据code获取对应费用类型
     */
    public static FeeType fromCode(String code) {
        if (code == null) {
            return null;
        }
        for (FeeType type : values()) {
            if (type.getCode().equalsIgnoreCase(code)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 是否为系统固定项（租金/押金）
     */
    public boolean isSystemFixed() {
        return this.immutable;
    }
}
