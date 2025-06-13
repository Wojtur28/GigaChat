package org.example.gigachat.core.conversation;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.List;

public interface ConversationRepository extends ReactiveMongoRepository<Conversation, String> {

    Flux<Conversation> findAllByParticipantIdsContaining(String userId);

    Flux<Conversation> findByTypeAndParticipantIdsIn(ConversationType type, List<String> participantIds);


}
