package com.demo.chat.service;

import com.demo.chat.entity.RoomLog;
import com.demo.chat.entity.UserAction;
import com.demo.chat.repository.RoomRepository;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Description("Save when user joined or left a room and return currently joined user for the room")
    public List<String> updateRoomLogsAndGetUsers(String roomName, String userName, UserAction action) {
        RoomLog log = new RoomLog(userName, roomName, action);
        roomRepository.save(log);
        List<RoomLog> updatedLogs = roomRepository.findByRoomAndEvent(roomName, UserAction.JOINED_ROOM);
        return updatedLogs.stream().map(RoomLog::getUserName).collect(Collectors.toList());
    }

    @Description("on disconnect update all rooms user was subscribed and return updated joined user for the room")
    public Map<String, List<String>> updateOnDisconnect(String userName) {
        List<RoomLog> logs = roomRepository.findByUserName(userName);
        logs.forEach(log -> {
            log.setEvent(UserAction.LEFT_ROOM);
            roomRepository.save(log);
        });
        return roomRepository.findByEvent(UserAction.JOINED_ROOM)
                .stream().collect(Collectors.groupingBy(RoomLog::getRoom,
                        Collectors.mapping(RoomLog::getUserName, Collectors.toList())));
    }
}
