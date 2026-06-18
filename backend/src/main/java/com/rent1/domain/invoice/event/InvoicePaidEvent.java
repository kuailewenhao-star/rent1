package com.rent1.domain.invoice.event;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 账单核销事件 - 账单核销（已支付）后触发
 * 下游消费方：
 * 1. 消息域：停止催收提醒
 * 2. 统计域：更新营收统计数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoicePaidEvent {

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

    /** 已支付金额 */
    private BigDecimal paidAmount;

    /** 实际支付时间 */
    private LocalDateTime paidTime;

    /** 事件生成时间 */
    private LocalDateTime eventTime;

    /**
     * 初始化事件
     */
    public void initialize() {
        this.eventId = "EVT_PAY_" + System.currentTimeMillis();
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
     * 判断是否为押金账单
     */
    public boolean isDepositInvoice() {
        return "DEPOSIT".equals(feeType);
    }
}