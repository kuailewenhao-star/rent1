package com.rent1.api.dto.invoice;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 公摊费用分摊结果响应DTO
 */
@Data
public class SharedInvoiceResponse {

    /** 分摊账单数量 */
    private Integer splitCount;

    /** 分摊账单明细列表 */
    private List<SplitInvoiceDetail> invoices;

    @Data
    public static class SplitInvoiceDetail {
        /** 房间ID */
        private String roomId;
        
        /** 分摊金额 */
        private BigDecimal amount;
        
        /** 分摊比例 */
        private BigDecimal ratio;
        
        /** 租客姓名（脱敏） */
        private String tenantName;
    }
}