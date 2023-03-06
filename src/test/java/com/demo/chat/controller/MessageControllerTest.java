package com.demo.chat.controller;

import com.demo.chat.helper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.demo.chat.helper.TestHelper.createTransportClient;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class MessageControllerTest {

    @LocalServerPort
    private Integer port;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new StringMessageConverter());
    }


    @Test
    public void testChatSendToSameRoom() throws InterruptedException, ExecutionException, TimeoutException {
        //Given one user to subscribe to movie room
        String user1 = "mrinal";
        StompSession stompSession1 = createStompSession(user1);
        BlockingQueue<String> result1 = connectAndSubscribe(stompSession1, "movie");

        //And another user also subscribe to movie room
        String user2 = "tori";
        StompSession stompSession2 = createStompSession(user2);
        BlockingQueue<String> result2 = connectAndSubscribe(stompSession2, "movie");

        // When user 1 send message to room
        stompSession1.send("/app/message/rooms/movie", "Hello Everyone");

        // Then both user in the room gets the message
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result1.poll(), containsString("Hello Everyone from " + user1)));
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result2.poll(), containsString("Hello Everyone from " + user1)));

    }

    @Test
    public void testChatSendToDifferentRoom() throws InterruptedException, ExecutionException, TimeoutException {
        //Given one user to subscribe to movie room
        String user1 = "mrinal";
        StompSession stompSession1 = createStompSession(user1);
        BlockingQueue<String> result1 = connectAndSubscribe(stompSession1, "movie");

        //And another user subscribe to sports room
        String user2 = "tori";
        StompSession stompSession2 = createStompSession(user2);
        BlockingQueue<String> result2 = connectAndSubscribe(stompSession2, "sports");

        // When user 1 send message to movie room
        stompSession1.send("/app/message/rooms/movie", "Hello Everyone");

        // Then only user in movie room gets message
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result1.poll(), containsString("Hello Everyone from " + user1)));
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result2.poll(), not(containsString("Hello Everyone from " + user1))));

    }

    private BlockingQueue<String> connectAndSubscribe(StompSession stompSession, String roomName) throws InterruptedException, ExecutionException, TimeoutException {
        BlockingQueue<String> result = new ArrayBlockingQueue<>(3);
        stompSession.subscribe("/rooms/" + roomName, new TestHelper.RoomUserListMessageHandler(result));
        return result;
    }

    private StompSession createStompSession(String user) throws InterruptedException, ExecutionException, TimeoutException {
        String URL = "ws://localhost:" + port + "/chat-ws?user=" + user;
        return stompClient.connect(URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
    }


}