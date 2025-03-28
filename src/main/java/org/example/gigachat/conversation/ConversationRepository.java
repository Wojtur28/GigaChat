package org.example.gigachat.conversation;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface ConversationRepository extends ReactiveMongoRepository<Conversation, String> {

}
