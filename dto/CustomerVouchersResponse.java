package com.rhotels.crm.dto;

import java.util.List;

/**
 * Record Response đóng gói danh sách voucher của khách hàng.
 */
public record CustomerVouchersResponse(
    boolean isSuccess,
    String customerId,
    List<VoucherDTO> vouchers,
    String message
) {
    public static CustomerVouchersResponse error(String message) {
        return new CustomerVouchersResponse(false, null, List.of(), message);
    }

    public static CustomerVouchersResponse success(String customerId, List<VoucherDTO> vouchers, String message) {
        return new CustomerVouchersResponse(true, customerId, vouchers, message);
    }
}
