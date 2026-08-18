package com.rhotels.crm.dto;

/**
 * Record Response đóng gói kết quả áp dụng voucher vào hóa đơn.
 */
public record ApplyVoucherResponse(
    boolean isSuccess,
    String invoiceId,
    String voucherCode,
    double originalAmount,
    double discountAmount,
    double finalAmount,
    String message
) {
    public static ApplyVoucherResponse error(String message) {
        return new ApplyVoucherResponse(false, null, null, 0.0, 0.0, 0.0, message);
    }

    public static ApplyVoucherResponse success(String invoiceId, String voucherCode, double originalAmount, double discountAmount, double finalAmount, String message) {
        return new ApplyVoucherResponse(true, invoiceId, voucherCode, originalAmount, discountAmount, finalAmount, message);
    }
}
