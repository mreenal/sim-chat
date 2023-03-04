package com.demo.chat.controller;

import com.demo.chat.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.util.HtmlUtils;

import javax.websocket.server.PathParam;

@Controller
public class MessageController {

    @MessageMapping("/hello")
    @SendTo("/topic/sports")
    public String greeting(ChatMessage message) throws Exception {
        Thread.sleep(1000); // simulated delay
        return ("Hello, " + HtmlUtils.htmlEscape(message.getMessageBody()) + "!");
    }

    @MessageMapping("/message/room/{roomName}")
    @SendTo("/topic/sports")
    public String chat(String message, @DestinationVariable("roomName") String roomName) throws Exception {
        Thread.sleep(1000); // simulated delay
        return ("Hello, " +  roomName  + HtmlUtils.htmlEscape(message) + "!");
    }
}
