package com.demo.chat.listener;

import com.demo.chat.helper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.test.annotation.DirtiesContext;
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
import static org.hamcrest.Matchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application.yml")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WebSocketEventListenerTest {

    @LocalServerPort
    private Integer port;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        stompClient = new WebSocketStompClient(new SockJsClient(createTransportClient()));
        stompClient.setMessageConverter(new StringMessageConverter());
    }

    @Test
    public void testTowUserSubscribed() throws InterruptedException, ExecutionException, TimeoutException {
        //Given one user to subscribe to movie room
        String user1 = "mrinal";
        BlockingQueue<String> result1 = connectAndSubscribe(user1, "movie");

        //When another user also subscribe to movie room
        String user2 = "tori";
        BlockingQueue<String> result2 = connectAndSubscribe(user2, "movie");

        // Then both user in the room get updated user list
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result1.poll(), allOf(containsString(user1), containsString(user2))));

        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result2.poll(), allOf(containsString(user1), containsString(user2))));

    }

    @Test
    public void testTowUserSubscribedDifferentRoom() throws InterruptedException, ExecutionException, TimeoutException {
        //Given one user to subscribe to movie room
        String user1 = "mrinal";
        BlockingQueue<String> result1 = connectAndSubscribe(user1, "movie");

        //When another user also subscribe to sports room
        String user2 = "tori";
        BlockingQueue<String> result2 = connectAndSubscribe(user2, "sports");

        // Then different room user should not get notification
        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result1.poll(), allOf(containsString(user1), not(containsString(user2)))));

        await()
                .atMost(3, SECONDS)
                .untilAsserted(() -> assertThat(result2.poll(), allOf(not(containsString(user1)), containsString(user2))));

    }

    private BlockingQueue<String> connectAndSubscribe(String user, String roomName) throws InterruptedException, ExecutionException, TimeoutException {
        String URL = "ws://localhost:" + port + "/chat-ws?user=" + user;
        StompSession stompSession = stompClient.connect(URL, new StompSessionHandlerAdapter() {
        }).get(1, SECONDS);
        BlockingQueue<String> result = new ArrayBlockingQueue<>(2);
        stompSession.subscribe("/rooms/" + roomName, new TestHelper.RoomUserListMessageHandler(result));
        return result;
    }


}