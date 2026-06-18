package com.rent1.domain.common;

/**
 * 联系人关系枚举
 */
public enum ContactRelationship {
    RELATIVE("RELATIVE", "亲属"),
    FRIEND("FRIEND", "朋友"),
    COLLEAGUE("COLLEAGUE", "同事"),
    OTHER("OTHER", "其他");

    private final String code;
    private final String name;

    ContactRelationship(String code, String name) {
        this.code = code;
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
