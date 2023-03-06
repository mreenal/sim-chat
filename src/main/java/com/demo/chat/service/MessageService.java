package com.demo.chat.service;

import com.demo.chat.entity.MessageLog;
import com.demo.chat.repository.MessageRepository;
import jdk.jfr.Description;
import org.springframework.stereotype.Service;

@Service
public class MessageService {

    private final MessageRepository messageRepository;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Description("save chat log information")
    public String saveMessage(String userName, String room, String message) {
        MessageLog messageLog = new MessageLog(userName, room, message);
        messageRepository.save(messageLog);
        return messageLog.formatSendMessage();
    }
}
