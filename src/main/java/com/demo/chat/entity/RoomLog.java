package com.demo.chat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "room_log")
public class RoomLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String userName;
    private String room;
    @Enumerated(EnumType.STRING)
    private UserAction event;
    private LocalDateTime timestamp;

    public RoomLog() {
    }

    public RoomLog(String userName, String room, UserAction event) {
        this.userName = userName;
        this.room = room;
        this.event = event;
        this.timestamp = LocalDateTime.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public UserAction getEvent() {
        return event;
    }

    public void setEvent(UserAction event) {
        this.event = event;
    }

}
