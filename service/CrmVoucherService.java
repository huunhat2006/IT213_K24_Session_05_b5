package com.rhotels.crm.service;

import com.rhotels.crm.dto.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service tích hợp các Tool tra cứu CRM và áp dụng Voucher với lập trình phòng thủ.
 */
@Service
public class CrmVoucherService {

    /**
     * Tool 1: Truy vấn danh sách voucher của khách hàng từ hệ thống CRM.
     */
    @Tool(description = "Truy vấn danh sách các mã giảm giá (voucher) còn hạn của khách hàng dựa trên customerId (Mã khách hàng CRM, ví dụ: KH888). " +
                        "Chỉ gọi công cụ này khi đã xác định được mã khách hàng.")
    public CustomerVouchersResponse getCustomerVouchers(CustomerVouchersRequest request) {
        // Phòng thủ Lớp 1: Check null request
        if (request == null) {
            return CustomerVouchersResponse.error("Yêu cầu không hợp lệ: Request payload bị null.");
        }

        // Phòng thủ Lớp 2: Check null / blank customerId
        if (request.customerId() == null || request.customerId().isBlank()) {
            return CustomerVouchersResponse.error("Mã khách hàng (customerId) không được để trống. Vui lòng hỏi lại khách hàng.");
        }

        String customerIdStr = request.customerId().trim().toUpperCase();

        // Giả lập tra cứu CRM Database
        if ("KH888".equals(customerIdStr)) {
            List<VoucherDTO> vouchers = List.of(
                new VoucherDTO("VIP20", "Giảm 20% cho khách hàng VIP (Giảm tối đa 500.000 VNĐ)", 20.0, 500000.0, false),
                new VoucherDTO("WELCOME10", "Giảm 10% chào mừng thành viên mới (Giảm tối đa 200.000 VNĐ)", 10.0, 200000.0, false)
            );
            return CustomerVouchersResponse.success(customerIdStr, vouchers, 
                "Tìm thấy 2 mã giảm giá khả dụng cho khách hàng " + customerIdStr);
        } else if ("KH777".equals(customerIdStr)) {
            List<VoucherDTO> vouchers = List.of(
                new VoucherDTO("WELCOME10", "Giảm 10% chào mừng thành viên mới", 10.0, 200000.0, false)
            );
            return CustomerVouchersResponse.success(customerIdStr, vouchers, 
                "Tìm thấy 1 mã giảm giá khả dụng cho khách hàng " + customerIdStr);
        } else {
            return CustomerVouchersResponse.error("Không tìm thấy hồ sơ khách hàng mã '" + customerIdStr + "' trong hệ thống CRM.");
        }
    }

    /**
     * Tool 2: Áp dụng mã giảm giá vào hóa đơn / đơn đặt phòng trong Database.
     */
    @Tool(description = "Áp dụng một mã giảm giá (voucherCode) cụ thể vào hóa đơn/đơn đặt phòng (invoiceId). " +
                        "Công cụ này sẽ cập nhật trực tiếp số tiền phải thanh toán vào database hệ thống.")
    public ApplyVoucherResponse applyVoucherToInvoice(ApplyVoucherRequest request) {
        // Phòng thủ Lớp 1: Check null request
        if (request == null) {
            return ApplyVoucherResponse.error("Yêu cầu không hợp lệ: Request payload bị null.");
        }

        // Phòng thủ Lớp 2: Check null / blank tham số
        if (request.invoiceId() == null || request.invoiceId().isBlank()) {
            return ApplyVoucherResponse.error("Mã hóa đơn (invoiceId) không được để trống.");
        }
        if (request.voucherCode() == null || request.voucherCode().isBlank()) {
            return ApplyVoucherResponse.error("Mã giảm giá (voucherCode) không được để trống.");
        }

        String invoiceIdStr = request.invoiceId().trim().toUpperCase();
        String voucherCodeStr = request.voucherCode().trim().toUpperCase();

        // Giả lập kiểm tra trạng thái hóa đơn từ Database
        if ("HD888".equals(invoiceIdStr)) {
            // Trường hợp lỗi nghiệp vụ: Hóa đơn đã thanh toán
            return ApplyVoucherResponse.error("Lỗi nghiệp vụ: Hóa đơn " + invoiceIdStr + " đã được thanh toán từ trước. Không thể áp dụng mã giảm giá.");
        }

        if (!"HD999".equals(invoiceIdStr)) {
            return ApplyVoucherResponse.error("Không tìm thấy đơn đặt phòng / hóa đơn mã '" + invoiceIdStr + "' trong hệ thống.");
        }

        // Giả lập hóa đơn HD999 chưa thanh toán, tổng tiền gốc 4.000.000 VNĐ
        double originalAmount = 4000000.0;
        double discountPercentage = 0.0;
        double maxDiscount = 0.0;

        if ("VIP20".equals(voucherCodeStr)) {
            discountPercentage = 20.0;
            maxDiscount = 500000.0;
        } else if ("WELCOME10".equals(voucherCodeStr)) {
            discountPercentage = 10.0;
            maxDiscount = 200000.0;
        } else {
            return ApplyVoucherResponse.error("Mã giảm giá '" + voucherCodeStr + "' không hợp lệ hoặc đã hết hạn sử dụng.");
        }

        // Tính toán số tiền giảm thực tế
        double rawDiscount = originalAmount * (discountPercentage / 100.0);
        double actualDiscount = Math.min(rawDiscount, maxDiscount);
        double finalAmount = originalAmount - actualDiscount;

        // Cập nhật Database thành công
        return ApplyVoucherResponse.success(
            invoiceIdStr, 
            voucherCodeStr, 
            originalAmount, 
            actualDiscount, 
            finalAmount, 
            String.format("Áp dụng mã %s thành công cho hóa đơn %s. Đã giảm %,.0f VNĐ. Số tiền cần thanh toán còn lại: %,.0f VNĐ.",
                    voucherCodeStr, invoiceIdStr, actualDiscount, finalAmount)
        );
    }
}
