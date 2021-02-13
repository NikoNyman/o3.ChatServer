package com.nikonyman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpHandler;

import org.json.JSONException;
import org.json.JSONObject;

import com.nikonyman.chatserver.ChatServer;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

// Rekistöinin suoritus //

public class RegistrationHandler implements HttpHandler {

    private ChatAuthenticator auth = null;

    public RegistrationHandler(ChatAuthenticator authenticator) {
        auth = authenticator;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int code = 200;
        String errorMessage = "";

        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
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

                }
                if (contentType.equalsIgnoreCase("application/json")) {
                    InputStream input = exchange.getRequestBody();
                    String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                            .collect(Collectors.joining("\n"));
                    ChatServer.log(text);
                    input.close();

                    // Lisätäään tiedot JSONObjectiin //

                    JSONObject registrationMsg = new JSONObject(text);
                    String username = registrationMsg.get("username").toString();
                    String password = registrationMsg.getString("password");
                    String email = registrationMsg.getString("email");

                    if (auth.addUser(username, password, email)) {
                        exchange.sendResponseHeaders(code, -1);
                        ChatServer.log("User added");
                    } else {
                        code = 400;
                        errorMessage = "Invalid user credentials";

                    }
                } else {
                    code = 411;
                    errorMessage = "Content-type must be text/plain.";
                    ChatServer.log(errorMessage);
                }

            } else {
                code = 400;
                errorMessage = "Not supported";
            }
        } catch (IOException e) {
            code = 500;
            errorMessage = "Error in handling the request: " + e.getMessage();
       
        } catch (JSONException e) {
            e.printStackTrace();
            errorMessage = "JSON file not valid";
            code = 400;

        } catch (Exception e) {
            code = 500;
            errorMessage = "Internal server error: " + e.getMessage();
        

        }
        if (code < 200 || code > 299) {
            ChatServer.log("*** Error in /registration: " + code + " " + errorMessage);
            byte[] bytes = errorMessage.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();

        }
    }
}
