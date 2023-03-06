package com.demo.chat.controller;

import com.demo.chat.helper.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.context.TestPropertySource;
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

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
class MessageControllerTest {

    @LocalServerPort
    private Integer port;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new StringMessageConverter());
    }

    @AfterEach
    void end() {
        stompClient.stop();
    }

    @Test
    public void testChatSend() throws InterruptedException, ExecutionException, TimeoutException {
        //Given one user to subscribe to movie room
        String user1 = "mrinal";
        String URL1 = "ws://localhost:" + port + "/chat-ws?user=" + user1;
        StompSession stompSession1 = stompClient.connect(URL1, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
        BlockingQueue<String> result1 = new ArrayBlockingQueue<>(3);
        stompSession1.subscribe("/rooms/movie", new TestHelper.RoomUserListMessageHandler(result1));

        //When another user also subscribe to movie room
        String user2 = "tori";
        String URL2 = "ws://localhost:" + port + "/chat-ws?user=" + user2;
        StompSession stompSession2 = stompClient.connect(URL2, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
        BlockingQueue<String> result2 = new ArrayBlockingQueue<>(3);
        stompSession2.subscribe("/rooms/movie", new TestHelper.RoomUserListMessageHandler(result2));

        stompSession1.send("/app/message/rooms/movie", "Hello Everyone");

        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result1.poll(), containsString("Hello Everyone from " + user1)));
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result2.poll(), containsString("Hello Everyone from " + user1)));

    }


}