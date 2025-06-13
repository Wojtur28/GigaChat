package org.example.gigachat.core.conversation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.gigachat.core.user.UserRepository;
import org.example.gigachat.dto.request.CreateDirectConversationRequest;
import org.example.gigachat.dto.request.CreateGroupConversationRequest;
import org.example.gigachat.dto.response.ConversationResponse;
import org.example.gigachat.mapper.ConversationMapper;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversationService {

    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;
    private final ConversationMapper conversationMapper;

    public Mono<ConversationResponse> createDirectConversation(CreateDirectConversationRequest request) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(UserDetails.class)
                .doOnNext(userDetails -> log.info("Authenticated userDetails: {}", userDetails))
                .flatMap(userDetails ->
                        userRepository.findByEmail(userDetails.getUsername())
                                .doOnNext(user -> log.info("Current user entity: {}", user))
                                .switchIfEmpty(Mono.defer(() -> {
                                    log.warn("Current user not found in DB for email: {}", userDetails.getUsername());
                                    return Mono.error(new IllegalArgumentException("Current user not found"));
                                }))
                                .zipWith(
                                        userRepository.findByEmail(request.participantEmail())
                                                .doOnNext(user -> log.info("Participant user entity: {}", user))
                                                .switchIfEmpty(Mono.defer(() -> {
                                                    log.warn("Participant user not found for email: {}", request.participantEmail());
                                                    return Mono.error(new IllegalArgumentException("Participant user not found"));
                                                }))
                                )
                                .flatMap(tuple -> {
                                    String currentUserId = tuple.getT1().getId();
                                    String participantId = tuple.getT2().getId();
                                    log.info("currentUserId: {}, participantId: {}", currentUserId, participantId);
                                    List<String> participants = List.of(currentUserId, participantId);
                                    return conversationRepository
                                            .findByTypeAndParticipantIdsIn(ConversationType.DIRECT, participants)
                                            .hasElements()
                                            .doOnNext(exists -> log.info("Direct conversation exists: {}", exists))
                                            .flatMap(exists -> {
                                                if (exists) {
                                                    log.warn("Direct conversation already exists for: {}", participants);
                                                    return Mono.error(new IllegalArgumentException("Direct conversation already exists"));
                                                }
                                                var conversation = new Conversation();
                                                conversation.setType(ConversationType.DIRECT);
                                                conversation.setParticipantIds(participants);
                                                conversation.setCreatedAt(Instant.now());
                                                conversation.setUpdatedAt(Instant.now());
                                                log.info("Saving new conversation: {}", conversation);
                                                return conversationRepository.save(conversation)
                                                        .doOnNext(saved -> log.info("Saved conversation: {}", saved))
                                                        .map(conversationMapper::conversationToResponse);
                                            });
                                })
                );
    }

    public Mono<ConversationResponse> createGroupPrivateConversation(CreateGroupConversationRequest request) {
        return resolveParticipantIds(request.participantEmails())
                .flatMap(participantEmails -> {
                    var conversation = new Conversation();
                    conversation.setType(ConversationType.GROUP_PRIVATE);
                    conversation.setParticipantIds(participantEmails);
                    conversation.setName(request.name());
                    conversation.setCreatedAt(Instant.now());
                    conversation.setUpdatedAt(Instant.now());
                    return conversationRepository.save(conversation)
                            .map(conversationMapper::conversationToResponse);
                });
    }

    public Mono<ConversationResponse> createGroupPublicConversation(CreateGroupConversationRequest request) {
        return resolveParticipantIds(request.participantEmails())
                .flatMap(participantIds -> {
                    var conversation = new Conversation();
                    conversation.setType(ConversationType.GROUP_PUBLIC);
                    conversation.setParticipantIds(participantIds);
                    conversation.setName(request.name());
                    conversation.setCreatedAt(Instant.now());
                    conversation.setUpdatedAt(Instant.now());
                    return conversationRepository.save(conversation)
                            .map(conversationMapper::conversationToResponse);
                });
    }

    private Mono<List<String>> resolveParticipantIds(List<String> emails) {
        return Flux.fromIterable(emails)
                .flatMap(email -> userRepository.findByEmail(email).map(user -> user.getId()))
                .collectList();
    }

    public Mono<ConversationResponse> getConversationById(String conversationId) {
        return conversationRepository.findById(conversationId)
                .map(conversationMapper::conversationToResponse);
    }

    public Flux<ConversationResponse> getConversationsForCurrentUser() {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication().getPrincipal())
                .cast(UserDetails.class)
                .flatMapMany(userDetails ->
                        userRepository.findByEmail(userDetails.getUsername())
                                .flatMapMany(user ->
                                        conversationRepository.findAllByParticipantIdsContaining(user.getId())
                                                .map(conversationMapper::conversationToResponse)
                                )
                );
    }
}
