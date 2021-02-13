package com.nikonyman.chatserver;

import java.time.LocalDateTime;

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
}


