package com.nikonyman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.stream.Collectors;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

public class RegistrationHandler implements HttpHandler {

    private ChatAuthenticator authenticator;
    

    public RegistrationHandler(ChatAuthenticator auth) {
        authenticator = auth;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        int code = 200;
        String errorMessage = "";

        try {
            if (exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                // Handle POST requests (client sent new chat message)
                InputStream input = exchange.getRequestBody();
                String text = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8)).lines()
                        .collect(Collectors.joining("\n"));
                        String[] parts = text.split(":");
                        if(parts.length == 2){
                        String username = parts[0];
                        String password = parts[1];
                        
                if ((!username.isEmpty()) && (!password.isEmpty())) {
                    Boolean add = authenticator.addUser(username, password);
                    if(add == false){
                        code = 403;
                        errorMessage = "User already registered";
                    }
               
                }
            }
                input.close();
                exchange.sendResponseHeaders(code, -1);
            } else if (exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                code = 400;
                errorMessage = "Not supported";
            } else {
                code = 400;
                errorMessage = "Not supported";
            }
        } catch (Exception e) {
            code = 500;
            errorMessage = "Internal server error";
        }
        if (code >= 400) {
            byte[] bytes = errorMessage.getBytes("UTF-8");
            exchange.sendResponseHeaders(code, bytes.length);
            OutputStream stream = exchange.getResponseBody();
            stream.write(bytes);
            stream.close();

        }

    }
}
