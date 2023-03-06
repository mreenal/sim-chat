package com.demo.chat.service;

import com.demo.chat.entity.TypingIndicator;
import org.springframework.context.annotation.Description;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Service
public class TypeIndicatorService {

    private final ConcurrentMap<String, Set<TypingIndicator>> typeInfo = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;

    public TypeIndicatorService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Description("keep the list updated who is writing")
    public Set<String> updateTypeInfo(String room, String userName) {
        Set<TypingIndicator> users = typeInfo.getOrDefault(room, new HashSet<>());
        users.add(new TypingIndicator(userName));
        typeInfo.put(room, users);
        return users.stream().map(TypingIndicator::getName).collect(Collectors.toSet());
    }

    @Scheduled(fixedRate = 5000)
    @Description("It will run in every 5 sec and cleanup any idle users from the list")
    public void cleanupTypeInfo() {
        LocalDateTime now = LocalDateTime.now();
        typeInfo.forEach((room, users) -> {
            users.removeIf(user -> Duration.between(user.getTimestamp(), now).getSeconds() >= 5);

            users.forEach(user -> messagingTemplate
                    .convertAndSendToUser(user.getName(), "/rooms/" + room, "typing")
            );

        });
    }

}
