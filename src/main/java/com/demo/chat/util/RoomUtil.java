package com.demo.chat.util;

public class RoomUtil {

    public static String getRoomName(String destination) {
        int index = destination.lastIndexOf('/');
        return destination.substring(index + 1);
    }

    public static boolean isRoom(String destination) {
        return destination != null && destination.startsWith("/rooms");
    }
}
