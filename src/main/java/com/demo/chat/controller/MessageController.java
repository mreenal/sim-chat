package com.demo.chat.controller;

import com.demo.chat.service.MessageService;
import com.demo.chat.service.TypeIndicatorService;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Set;

@Controller
public class MessageController {

    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;
    private final TypeIndicatorService typeIndicatorService;

    public MessageController(SimpMessagingTemplate messagingTemplate, MessageService messageService, TypeIndicatorService typeIndicatorService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.typeIndicatorService = typeIndicatorService;
    }

    @MessageMapping("/message/rooms/{roomName}")
    @Description("Receive message sent from a room, logs the chat and send back the message with user name and timestamp")
    public void messaging(String message, Principal principal, @DestinationVariable("roomName") String roomName) throws Exception {
        String saveAndFormat = messageService.saveMessage(principal.getName(), roomName, message);
        messagingTemplate.convertAndSend("/rooms/" + roomName, saveAndFormat);
    }

    @MessageMapping("/type/rooms/{roomName}")
    @Description("Receive typing indicator, update typing user lists and send other user the list")
    public void typing(String message, Principal principal, @DestinationVariable("roomName") String roomName, StompHeaderAccessor headers) throws Exception {
        String currentUser = principal.getName();
        Set<String> users = typeIndicatorService.updateTypeInfo(roomName, principal.getName());
        users.forEach(user -> {
            if (!user.equals(currentUser)) {
                messagingTemplate.convertAndSendToUser(user, "/rooms/" + roomName, "typing");
            }
        });
    }

}
