# sim-chat
A simple chat application. User can join any room and send a message to the room. The room will get updated list of connected users. 
All message logs are saved in database. user can send messages to subscribed room. Client can also send user typing indicator. 
All other user will get list of typing users. A schedule job will cleanup idle user from typing user list. 
No user authentication is needed, and it is assumed that user id is unique.

#### Run locally
- Build with openjdk 11+ and maven 3.6+
- `mvn clean install`
- `mvn spring-boot:run`
- socket opens on port `8080` 
- Using Intellij Run `ChatApplication` from run menu

#### Connect to socket from client
- User connect to the socket `http://{host}:{port}/chat-ws?user=${userId}`
- User subscribe to room by `/rooms/{roomName}`
- User sends message to room `/app/message/rooms/{roomName}`
- Send type indicator `/app/type/rooms/{roomName}`

#### Test
- `mvn test` 

#### TODO
- use DTO for sending message objects
- add unit tests 
- logging
- Error handling