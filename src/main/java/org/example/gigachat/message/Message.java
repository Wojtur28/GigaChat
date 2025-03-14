package org.example.gigachat.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "messages")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Message {

    @Id
    private String id;

    private String roomId;

    private String sender;

    private String content;

    private Instant timestamp;
}
