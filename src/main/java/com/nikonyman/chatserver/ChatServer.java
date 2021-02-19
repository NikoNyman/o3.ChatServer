package com.nikonyman.chatserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManagerFactory;

import com.nikonyman.ChatAuthenticator;
import com.nikonyman.ChatHandler;
import com.nikonyman.RegistrationHandler;
import com.sun.net.httpserver.*;

// Main class //

public class ChatServer {

    public static void main(String[] args) {
        try {
            ChatDatabase database = ChatDatabase.getInstance();
            String dbname = "C:\\Users\\nikop\\Desktop\\ChatServer\\chatserver\\03-chat-db";
            database.open(dbname);
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = chatServerSSLContext();
            

            server.setHttpsConfigurator(new HttpsConfigurator(sslContext) {
                public void configure(HttpsParameters params) {
                    InetSocketAddress remote = params.getClientAddress();
                    SSLContext c = getSSLContext();
                    SSLParameters sslparams = c.getDefaultSSLParameters();
                    params.setSSLParameters(sslparams);
                }
            });
            ChatAuthenticator aut = new ChatAuthenticator();
            HttpContext chatContext = server.createContext("/chat", new ChatHandler());
            chatContext.setAuthenticator(aut);
            server.createContext("/registration", new RegistrationHandler(aut));
            server.setExecutor(null);
            
            log("Starting Chatserver!");
            server.start();

        } catch (FileNotFoundException e) {
            // Certificate file not found!
            System.out.println("Certificate not found! ");
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

    }

    private static SSLContext chatServerSSLContext() throws Exception {
        char[] passphrase = "Ohj3lm01nt134v41n".toCharArray();
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream("keystore.jks"), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }


    public static void log(String message){
        System.out.println(message);
    }
}
