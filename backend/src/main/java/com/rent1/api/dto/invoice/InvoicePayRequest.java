package com.rent1.api.dto.invoice;

import lombok.Data;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 账单核销请求DTO
 * 接口：PUT /api/income-invoices/{invoiceId}/pay
 */
@Data
public class InvoicePayRequest {

    /** 实际支付时间（可选，默认当前时间） */
    private LocalDateTime paidTime;
}