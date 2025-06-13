package org.example.gigachat.core.message;

import lombok.RequiredArgsConstructor;
import org.example.gigachat.core.conversation.ConversationRepository;
import org.example.gigachat.core.user.UserRepository;
import org.example.gigachat.dto.response.MessageResponse;
import org.example.gigachat.dto.request.SendMessageRequest;
import org.example.gigachat.mapper.MessageMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Comparator;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final Sinks.Many<Message> messageSink = Sinks.many().multicast().directAllOrNothing();
    private final Flux<Message> messageFlux = messageSink.asFlux().publish().autoConnect();
    private final MessageRepository messageRepository;
    private final ConversationRepository conversationRepository;
    private final MessageMapper messageMapper;
    private final UserRepository userRepository;

    public Flux<MessageResponse> getMessagesByConversation(String conversationId) {
        return messageRepository.findByConversationId(conversationId)
                .sort(Comparator.comparing(Message::getTimestamp))
                .map(messageMapper::messageToMessageResponse);
    }

    public Flux<MessageResponse> getMessagesForConversation(String conversationId) {
        return messageFlux.filter(msg -> conversationId.equals(msg.getConversationId()))
                .map(messageMapper::messageToMessageResponse);
    }

    public Mono<MessageResponse> sendMessage(SendMessageRequest request, String conversationId) {
        return ReactiveSecurityContextHolder.getContext()
                .map(securityContext -> securityContext.getAuthentication().getPrincipal())
                .cast(UserDetails.class)
                .flatMap(userDetails -> userRepository.findByEmail(userDetails.getUsername()))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Current user not found")))
                .flatMap(currentUser -> {
                    String authorId = currentUser.getId();
                    Message message = messageMapper.sendMessageRequestToMessage(request, conversationId, authorId);

                    return conversationRepository.findById(conversationId)
                            .switchIfEmpty(Mono.error(new IllegalArgumentException("Conversation not found")))
                            .filter(conversation -> conversation.getParticipantIds().contains(authorId))
                            .switchIfEmpty(Mono.error(new AccessDeniedException("You are not a participant of this conversation")))
                            .then(messageRepository.save(message)
                                    .doOnNext(messageSink::tryEmitNext)
                                    .map(messageMapper::messageToMessageResponse)
                            );
                });
    }


    public Mono<Void> deleteAllMessages() {
        return messageRepository.deleteAll();
    }
}
