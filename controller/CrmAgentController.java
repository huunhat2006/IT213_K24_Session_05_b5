package com.rhotels.crm.controller;

import com.rhotels.crm.dto.CrmChatResponse;
import com.rhotels.crm.service.CrmVoucherService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST Controller cho CRM Support Agent tích hợp Tool Chaining và ChatMemory.
 */
@RestController
@RequestMapping("/api/crm")
public class CrmAgentController {

    private final ChatClient chatClient;

    public CrmAgentController(ChatClient.Builder builder, CrmVoucherService crmVoucherService, ChatMemory chatMemory) {
        this.chatClient = builder
                .defaultSystem("""
                    Bạn là Trợ lý Chăm sóc Khách hàng (CRM Support Agent) thông minh của hệ thống R-Hotels.
                    
                    NHIỆM VỤ CỦA BẠN:
                    1. Hỗ trợ khách hàng tra cứu mã giảm giá (voucher) và áp dụng voucher ưu đãi nhất vào đơn đặt phòng/hóa đơn.
                    
                    QUY TRÌNH XỬ LÝ (IMPORTANT REACT WORKFLOW):
                    - ĐỌC LỊCH SỬ CHAT (ChatMemory): Trước tiên hãy kiểm tra lịch sử trò chuyện xem thông tin danh tính khách hàng (như Mã khách hàng customerId, ví dụ: KH888) đã có hay chưa.
                    - NẾU CHƯA CÓ danh tính khách hàng: Lịch sự yêu cầu khách hàng cung cấp Mã khách hàng (customerId) hoặc số điện thoại đăng ký trước khi tiếp tục.
                    - NẾU ĐÃ CÓ customerId:
                      + Bước 1: Gọi Tool 'getCustomerVouchers' để lấy danh sách tất cả các voucher còn hiệu lực của khách hàng đó.
                      + Bước 2: Phân tích danh sách trả về, so sánh các mức chiết khấu và tự động chọn ra mã giảm giá mang lại lợi ích tốt nhất cho khách hàng (ví dụ: giảm % cao nhất hoặc số tiền giảm lớn nhất).
                      + Bước 3: Tự động gọi tiếp Tool 'applyVoucherToInvoice' để áp dụng mã giảm giá tốt nhất đó vào đơn đặt phòng/hóa đơn (invoiceId).
                      + Bước 4: Tổng hợp kết quả phản hồi cuối cùng và trả lời bằng ngôn ngữ tự nhiên thân thiện, thể hiện rõ số tiền đã được giảm và số tiền còn lại phải thanh toán.
                    
                    RÀNG BUỘC PHÒNG THỦ:
                    - Nếu bất kỳ Tool nào trả về 'isSuccess = false', hãy đọc lý do lỗi trong trường 'message' và giải thích lịch sự lý do cho khách hàng (ví dụ: hóa đơn đã thanh toán trước đó, mã không tồn tại...). KHÔNG NỔ EXCEPTION.
                    """)
                .defaultAdvisors(new MessageChatMemoryAdvisor(chatMemory))
                .defaultTools(crmVoucherService)
                .build();
    }

    /**
     * Endpoint chat tra cứu CRM & Áp dụng voucher tự động.
     *
     * @param message Yêu cầu của người dùng
     * @param conversationId Mã phiên hội thoại
     * @return CrmChatResponse chứa mã conversationId và tin nhắn từ AI Agent
     */
    @GetMapping("/chat")
    public ResponseEntity<CrmChatResponse> chat(
            @RequestParam String message,
            @RequestParam(required = false) String conversationId) {

        // Phòng thủ: Khởi tạo UUID duy nhất nếu là lượt chat đầu tiên
        String effectiveConversationId = (conversationId != null && !conversationId.isBlank())
                ? conversationId.trim()
                : UUID.randomUUID().toString();

        // Xử lý gọi Prompt với Advisor và ChatMemory
        String replyMessage = this.chatClient.prompt()
                .user(message)
                .advisors(advisorSpec -> advisorSpec
                        .param("chat_memory_conversation_id", effectiveConversationId)
                        .param("chat_memory_retrieve_size", 20)
                )
                .call()
                .content();

        return ResponseEntity.ok(new CrmChatResponse(effectiveConversationId, replyMessage));
    }
}
