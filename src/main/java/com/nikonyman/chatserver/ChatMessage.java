package com.nikonyman.chatserver;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Luokka jonka avulla pidetään chat messaget muistissa //

public class ChatMessage {
    public LocalDateTime sent;
    public String nick;
    public String message;


    public ChatMessage(LocalDateTime time, String name, String ms){
    nick = name;
    message = ms;
    sent = time;

    }
    long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
}

    void setSent(long epoch) {
    sent = LocalDateTime.ofInstant(Instant.ofEpochMilli(epoch), ZoneOffset.UTC);
    }
}
