package com.demo.chat.repository;

import com.demo.chat.entity.RoomLog;
import com.demo.chat.entity.UserAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<RoomLog, Integer> {

    List<RoomLog> findByRoomAndEvent(String room, UserAction event);

    List<RoomLog> findByUserName(String userName);

    List<RoomLog> findByEvent(UserAction event);

}
