package org.example.gigachat.mapper;

import org.example.gigachat.core.message.Message;
import org.example.gigachat.dto.response.MessageResponse;
import org.example.gigachat.dto.request.SendMessageRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageResponse messageToMessageResponse(Message entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversationId", source = "conversationId")
    @Mapping(target = "authorId", source = "authorId")
    @Mapping(target = "timestamp", expression = "java(java.time.Instant.now())")
    Message sendMessageRequestToMessage(
        SendMessageRequest request,
        String conversationId,
        String authorId
    );
}
