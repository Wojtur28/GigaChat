package org.example.gigachat.mapper;

import org.example.gigachat.core.conversation.Conversation;
import org.example.gigachat.dto.response.ConversationResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ConversationMapper {

    @Mapping(target = "participantEmails", source = "participantIds")
    ConversationResponse conversationToResponse(Conversation conversation);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "DIRECT")
    @Mapping(target = "participantIds", expression = "java(List.of(currentUserId, participantId))")
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Conversation createDirectToConversation(@MappingTarget Conversation conversation, String currentUserId, String participantId);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "GROUP_PRIVATE")
    @Mapping(target = "participantIds", expression = "java(new java.util.ArrayList<>(participantIds))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Conversation createGroupPrivateToConversation(@MappingTarget Conversation conversation, List<String> participantIds, String name);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", constant = "GROUP_PUBLIC")
    @Mapping(target = "participantIds", expression = "java(new java.util.ArrayList<>(participantIds))")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Conversation createGroupPublicToConversation(@MappingTarget Conversation conversation, List<String> participantIds, String name);
}
