package com.nikonyman.chatserver;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

// Luokka jonka avulla pidetään chat messaget muistissa //

public class ChatMessage {
    public LocalDateTime sent;
    public String nick;
    public String message;

    public ChatMessage(LocalDateTime time, String name, String ms) {
        nick = name;
        message = ms;
        sent = time;

    }

    public long dateAsInt() {
        return sent.toInstant(ZoneOffset.UTC).toEpochMilli();
    }



}
