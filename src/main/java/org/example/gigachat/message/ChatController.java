package org.example.gigachat.message;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
@AllArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @GetMapping("/rooms/{roomId}/messages")
    public Flux<Message> getMessagesByRoom(@PathVariable String roomId) {
        return chatService.getMessagesByRoom(roomId);
    }

    @PostMapping("/messages")
    public Mono<Message> sendMessage(@RequestBody Message message) {
        return chatService.sendMessage(message);
    }

}
