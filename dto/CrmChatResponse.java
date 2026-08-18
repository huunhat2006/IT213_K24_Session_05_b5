package com.rhotels.crm.dto;

/**
 * Record Response trả về cho REST API endpoint chat của CRM Agent.
 */
public record CrmChatResponse(
    String conversationId,
    String replyMessage
) {}
