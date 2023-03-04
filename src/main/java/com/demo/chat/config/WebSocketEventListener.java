package com.demo.chat.config;

import com.demo.chat.service.RoomService;
import org.springframework.context.annotation.Description;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.Objects;

import static com.demo.chat.entity.UserAction.JOINED_ROOM;
import static com.demo.chat.entity.UserAction.LEFT_ROOM;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }


    @EventListener
    @Description("When a user joined a room, save the entry and send back the updated jined user")
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        String userName = Objects.requireNonNull(event.getUser()).getName();

        if (destination != null && destination.startsWith("/rooms")) {
            String roomName = getRoomName(destination);
            List<String> currentUsers = roomService.updateRoomLogsAndGetUsers(roomName, userName, JOINED_ROOM);
            messagingTemplate.convertAndSend(destination, currentUsers);
        }
    }

    @EventListener
    @Description("When a user leaves a room, save the entry and send back the updated user")
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        String userName = Objects.requireNonNull(event.getUser()).getName();

        if (destination != null && destination.startsWith("/rooms")) {
            String roomName = getRoomName(destination);
            List<String> currentUsers = roomService.updateRoomLogsAndGetUsers(roomName, userName, LEFT_ROOM);
            messagingTemplate.convertAndSend(destination, currentUsers);
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        Principal principal = event.getUser();
        System.out.println("Session connect " + sessionId + ": " + principal);

    }

    private String getRoomName(String destination) {
        int index = destination.lastIndexOf('/');
        return destination.substring(index + 1);
    }
}
