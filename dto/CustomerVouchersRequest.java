package com.rhotels.crm.dto;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * Record Request đầu vào cho Tool getCustomerVouchers.
 */
public record CustomerVouchersRequest(
    @JsonPropertyDescription("Mã định danh duy nhất của khách hàng trong hệ thống CRM (ví dụ: KH888)")
    String customerId
) {}
