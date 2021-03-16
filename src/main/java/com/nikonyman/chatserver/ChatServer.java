package com.nikonyman.chatserver;

import java.io.Console;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        // Avataan serveri try catch:in sisällä //
        try {
            ChatDatabase database = ChatDatabase.getInstance();
            String dbname = args[0];
            database.open(dbname);
            HttpsServer server = HttpsServer.create(new InetSocketAddress(8001), 0);
            SSLContext sslContext = chatServerSSLContext(args[1], args[2]);

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
            ExecutorService excr = Executors.newCachedThreadPool();
            server.setExecutor(excr);

            log("Starting Chatserver!");
            server.start();
            Console c = System.console();
            Boolean running = true;

            while (running) {
                if (c.readLine().equals("/quit")) {
                    server.stop(3);
                    database.close();
                }
            }

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

    private static SSLContext chatServerSSLContext(String args, String args2) throws Exception {
        char[] passphrase = args2.toCharArray();
        String fileName = args;
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(new FileInputStream(fileName), passphrase);

        KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
        kmf.init(ks, passphrase);

        TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
        tmf.init(ks);

        SSLContext ssl = SSLContext.getInstance("TLS");
        ssl.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return ssl;
    }

    public static void log(String message) {
        System.out.println(message);
    }
}
