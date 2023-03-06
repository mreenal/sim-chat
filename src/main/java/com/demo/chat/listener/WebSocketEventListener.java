package com.demo.chat.listener;

import com.demo.chat.service.RoomService;
import org.springframework.context.annotation.Description;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.demo.chat.entity.UserAction.JOINED_ROOM;
import static com.demo.chat.entity.UserAction.LEFT_ROOM;
import static com.demo.chat.util.RoomUtil.getRoomName;
import static com.demo.chat.util.RoomUtil.isRoom;

@Component
public class WebSocketEventListener {

    private final SimpMessagingTemplate messagingTemplate;
    private final RoomService roomService;

    public WebSocketEventListener(SimpMessagingTemplate messagingTemplate, RoomService roomService) {
        this.messagingTemplate = messagingTemplate;
        this.roomService = roomService;
    }

    @EventListener
    @Description("When a user joined a room, save the entry and send back the updated joined user")
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        String destination = (String) event.getMessage().getHeaders().get("simpDestination");
        String userName = Objects.requireNonNull(event.getUser()).getName();

        if (isRoom(destination)) {
            String roomName = getRoomName(destination);
            List<String> currentUsers = roomService.updateRoomLogsAndGetUsers(roomName, userName, JOINED_ROOM);
            messagingTemplate.convertAndSend(destination, currentUsers.toString());
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
            messagingTemplate.convertAndSend(destination, currentUsers.toString());
        }
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        Principal principal = event.getUser();
        System.out.println("Session connect " + sessionId + ": " + principal);

    }

    @EventListener
    @Description("when user disconnects remove update room user list and send to other users")
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getMessage().getHeaders().get("simpSessionId", String.class);
        Principal principal = event.getUser();
        System.out.println("Session disconnected " + sessionId + ": " + principal);
        String userName = Objects.requireNonNull(event.getUser()).getName();
        Map<String, List<String>> update = roomService.updateOnDisconnect(userName);
        update.forEach((room, users) -> {
            messagingTemplate.convertAndSend("/rooms/" + room, users.toString());
        });
    }

}
