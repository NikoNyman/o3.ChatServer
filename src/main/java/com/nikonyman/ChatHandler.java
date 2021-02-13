package com.nikonyman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.stream.Collectors;

import com.nikonyman.chatserver.ChatMessage;
import com.nikonyman.chatserver.ChatServer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import org.json.JSONArray;
import org.json.JSONObject;

// Luokka jossa hoidetaan POST ja GET //

public class ChatHandler implements HttpHandler {

    String errorMessage = "";

    private ArrayList<ChatMessage> messages = new ArrayList<ChatMessage>();

    // Handlessä otetaan selvää clientin pyyntö ja sitten siirrytään hoitamaan se //
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int code = 200;
        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                code = handleChatMessageFromClient(exchange);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                code = handleGetRequestFromClient(exchange);
            } else {
                code = 400;
                errorMessage = "Not supported";
            }
        } catch (IOException e) {
            code = 500;
            errorMessage = "Error in handling the request: " + e.getMessage();

        } catch (Exception e) {
            code = 500;
            errorMessage = "Internal server error: " + e.getMessage();

        }
        if (code < 200 || code > 299) {
            ChatServer.log("*** Error in /chat: " + code + " " + errorMessage);
            byte[] bytes = errorMessage.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();

        }

    }
        // Hoidetaan POST //
    private int handleChatMessageFromClient(HttpExchange exchange) throws Exception {
        int code = 200;
        Headers headers = exchange.getRequestHeaders();
        int contentLength = 0;
        String contentType = "";

        if (headers.containsKey("Content-Length")) {
            contentLength = Integer.valueOf(headers.get("Content-Length").get(0));
        } else {
            errorMessage = "Content-Length not specified";
            code = 411;
        }
        if (headers.containsKey("Content-Type")) {
            contentType = headers.get("Content-Type").get(0);
        } else {
            code = 400;
            errorMessage = "No content type in request";
            return code;
        }
        if (contentType.equalsIgnoreCase("application/json")) {
            InputStream input = exchange.getRequestBody();
            String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                    .collect(Collectors.joining("\n"));
            ChatServer.log(text);
            input.close();

            // Luodaan ja lisätään viestin tiedot JSONObjetiin. //
            JSONObject ChatMessage = new JSONObject(text);

            String dateStr = ChatMessage.getString("sent");
            OffsetDateTime odt = OffsetDateTime.parse(dateStr);

            LocalDateTime sent = odt.toLocalDateTime();
            String nickName = ChatMessage.get("user").toString();
            String message = ChatMessage.getString("message");

            ChatMessage newMessage = new ChatMessage(sent, nickName, message);
            if (!text.isEmpty()) {
                messages.add(newMessage);
                exchange.sendResponseHeaders(code, -1);
                ChatServer.log("New message saved");

            } else {
                code = 400;
                errorMessage = "No content in request";
                ChatServer.log(errorMessage);

            }
        } else {
            code = 411;
            errorMessage = "Content-Type must be application/json";
            ChatServer.log(errorMessage);
        }
        return code;

    }
        // Hoidetaan GET pyynnöt //
    private int handleGetRequestFromClient(HttpExchange exchange) throws IOException, SQLException {
        int code = 200;

        if (messages.isEmpty()) {
            ChatServer.log("No new messages to deliver to client");
            code = 204;
            exchange.sendResponseHeaders(code, -1);
            return code;
        }
        Collections.sort(messages, 
        (ChatMessage lhs, ChatMessage rhs) -> 
        lhs.sent.compareTo(rhs.sent));

        JSONArray responseMessages = new JSONArray();

        // Hoidetaan viestien päivämäärät //

        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("UTC"));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        String dateText = now.format(formatter);
        // For Loop jolla viestit lisätään JSONArrayhyn //
        for (ChatMessage message : messages) {
            JSONObject msg = new JSONObject();
;
            msg.put("sent", dateText);
            msg.put("user", message.nick);
            msg.put("message", message.message);
            responseMessages.put(msg);
        }
        ChatServer.log("Delivering" + messages.size() + " messages to client");
        
        String JSON = responseMessages.toString();
        byte[] bytes = JSON.getBytes("UTF-8");
        ChatServer.log(JSON);
        exchange.sendResponseHeaders(code, bytes.length);
        OutputStream stream = exchange.getResponseBody();
        stream.write(bytes);
        stream.flush();
        stream.close();
        return code;

    }

    

}
