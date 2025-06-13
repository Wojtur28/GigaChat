package org.example.gigachat.core.message;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.dto.response.MessageResponse;
import org.example.gigachat.dto.request.SendMessageRequest;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping("/rooms/{conversationId}/messages")
    public Flux<MessageResponse> getMessagesByRoom(@PathVariable String conversationId) {
        return messageService.getMessagesByConversation(conversationId);
    }

    @PostMapping("/messages")
    public Mono<MessageResponse> sendMessage(
            @RequestBody SendMessageRequest request,
            @RequestParam String conversationId) {
        return messageService.sendMessage(request, conversationId);
    }

    @DeleteMapping("/messages")
    public Mono<Void> deleteAllMessages() {
        return messageService.deleteAllMessages();
    }
}
