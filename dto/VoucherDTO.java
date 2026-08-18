package com.rhotels.crm.dto;

/**
 * DTO mô tả thông tin mã giảm giá của khách hàng.
 */
public record VoucherDTO(
    String code,
    String description,
    double discountPercentage,
    double maxDiscountAmount,
    boolean isExpired
) {}
