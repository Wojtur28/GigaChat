package org.example.gigachat.core.conversation;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.dto.request.CreateDirectConversationRequest;
import org.example.gigachat.dto.request.CreateGroupConversationRequest;
import org.example.gigachat.dto.response.ConversationResponse;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/conversation")
@RequiredArgsConstructor
public class ConversationController {

    private final ConversationService conversationService;

    @GetMapping
    public Flux<ConversationResponse> getAllConversationsForCurrentUser() {
        return conversationService.getConversationsForCurrentUser();
    }

    @GetMapping("/{conversationId}")
    public Mono<ConversationResponse> getConversationById(@PathVariable String conversationId) {
        return conversationService.getConversationById(conversationId);
    }

    @PostMapping("/direct")
    public Mono<ConversationResponse> createDirectConversation(@RequestBody CreateDirectConversationRequest request) {
        return conversationService.createDirectConversation(request);
    }

    @PostMapping("/group-private")
    public Mono<ConversationResponse> createGroupPrivateConversation(@RequestBody CreateGroupConversationRequest request) {
        return conversationService.createGroupPrivateConversation(request);
    }

    @PostMapping("/group-public")
    public Mono<ConversationResponse> createGroupPublicConversation(@RequestBody CreateGroupConversationRequest request) {
        return conversationService.createGroupPublicConversation(request);
    }
}
