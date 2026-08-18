package com.rhotels.crm.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Record Request đầu vào cho Tool applyVoucherToInvoice.
 */
public record ApplyVoucherRequest(
    @JsonPropertyDescription("Mã đơn đặt phòng / mã hóa đơn cần áp dụng mã giảm giá (ví dụ: HD999)")
    String invoiceId,

    @JsonPropertyDescription("Mã giảm giá được áp dụng (ví dụ: VIP20)")
    String voucherCode
) {}
