package com.rent1.domain.invoice.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 账单生成事件 - 账单生成后触发
 * 下游消费方：
 * 1. 消息域：触发消息通知（租金日催缴、杂费单次提醒）
 * 2. 统计域：更新统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceGeneratedEvent {

    /** 事件ID */
    private String eventId;

    /** 账单ID */
    private String invoiceId;

    /** 合约ID */
    private String contractId;

    /** 租客会员ID */
    private String tenantMemberId;

    /** 房东会员ID */
    private String landlordMemberId;

    /** 房间ID */
    private String roomId;

    /** 房源ID */
    private String houseSourceId;

    /** 费用类型 */
    private String feeType;

    /** 金额 */
    private BigDecimal amount;

    /** 计费周期起始时间 */
    private LocalDate cycleStart;

    /** 计费周期结束时间 */
    private LocalDate cycleEnd;

    /** 应付日期 */
    private LocalDate dueDate;

    /** 是否手动录入 */
    private Boolean isManual;

    /** 事件生成时间 */
    private LocalDateTime eventTime;

    /**
     * 初始化事件
     */
    public void initialize() {
        this.eventId = "EVT_INV_" + System.currentTimeMillis();
        this.eventTime = LocalDateTime.now();
    }

    /**
     * 获取费用类型名称
     */
    public String getFeeTypeName() {
        switch (feeType) {
            case "RENT": return "租金";
            case "DEPOSIT": return "押金";
            case "WATER": return "水费";
            case "ELECTRIC": return "电费";
            case "GAS": return "燃气费";
            case "BROADBAND": return "宽带费";
            case "PROPERTY": return "物业费";
            case "TRASH": return "垃圾清运费";
            case "OTHER": return "其他杂费";
            default: return feeType;
        }
    }

    /**
     * 判断是否为租金账单（需要每日催收）
     */
    public boolean isRentInvoice() {
        return "RENT".equals(feeType);
    }

    /**
     * 判断是否为杂费账单（仅单次提醒）
     */
    public boolean isMiscFeeInvoice() {
        return !"RENT".equals(feeType) && !"DEPOSIT".equals(feeType);
    }
}