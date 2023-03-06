package com.demo.chat.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "message_log")
public class MessageLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    private String userName;
    private String room;
    private String message;
    private LocalDateTime timestamp;

    public MessageLog() {
    }

    public MessageLog(String userName, String room, String message) {
        this.userName = userName;
        this.room = room;
        this.message = message;
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

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String formatSendMessage() {
        return this.message + " from " + this.userName + " at " + this.timestamp.toLocalTime().toString();
    }
}
