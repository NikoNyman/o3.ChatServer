package com.nikonyman.chatserver;

import java.io.File;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.apache.commons.codec.digest.Crypt;

public class ChatDatabase {

    private static ChatDatabase singleton = null;
    public Connection con;
    private SecureRandom secureRandom = new SecureRandom();

    public static synchronized ChatDatabase getInstance() {
        if (null == singleton) {
            singleton = new ChatDatabase();
        }
        return singleton;
    }

    private ChatDatabase() {
    }

    public void open(String dbName) throws SQLException {
        Boolean fex;
        File f = new File(dbName);
        if (f.exists() && !f.isDirectory()) {
            fex = true;
        } else {
            fex = false;
        }
        String cAddres = "jdbc:sqlite:" + dbName;
        con = DriverManager.getConnection(cAddres);
        if (!fex) {
            initializeDatabase();
        }
    }

    private boolean initializeDatabase() throws SQLException {
        if (null != con) {
            String createUsersString = "CREATE TABLE USER(UNAME VARCHAR2(30) PRIMARY KEY,PWORD VARCHAR2(30),EMAIL VARCHAR2(30), SALT VARCHAR2(90))";
            String createMessageString = "CREATE TABLE MESSAGE(UNAME VARCHAR2(30), MSG VARCHAR2(100), SENT NUMERIC, PRIMARY KEY(UNAME, SENT))";
            Statement createStatement = con.createStatement();
            createStatement.executeUpdate(createUsersString);
            createStatement.executeUpdate(createMessageString);
            createStatement.close();
            return true;
        }
        return false;
    }

    public boolean insertUserToDatabase(String username, String password, String email) {
        try {
            byte bytes[] = new byte[13];
            secureRandom.nextBytes(bytes);
            String saltBytes = new String(Base64.getEncoder().encode(bytes));
            String salt = "$6$" + saltBytes;
            String hashedPassword = Crypt.crypt(password, salt);
            String createUserString = "insert into USER " + "VALUES('" + username + "','" + hashedPassword + "','" + email + "','" + salt
                    + "')";
            Statement createStatement = con.createStatement();
            createStatement.executeUpdate(createUserString);
            createStatement.close();

            return true;
        } catch (SQLException e) {
            System.out.println("Primary key already used" + e.getMessage());
            return false;
        }
    }

    public boolean insertMessageToDatabase(String username, String message, long time) {
        try {

            String createUserString = "insert into MESSAGE " + "VALUES('" + username + "','" + message + "','" + time
                    + "')";
            Statement createStatement = con.createStatement();
            createStatement.executeUpdate(createUserString);
            createStatement.close();

            return true;
        } catch (SQLException e) {
            System.out.println("Error putting message to database");
            return false;
        }
    }

    public ArrayList<ChatMessage> readChatmessages() throws SQLException {

        ArrayList<ChatMessage> messagestoread = new ArrayList<ChatMessage>();
        String readMessages = "select * from MESSAGE order by SENT desc limit 100";
        Statement createStatement = con.createStatement();
        ResultSet rs = createStatement.executeQuery(readMessages);

        while (rs.next()) {
            String uName = rs.getString("UNAME");
            String msg = rs.getString("MSG");
            long sent = rs.getLong("SENT");
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(sent), ZoneOffset.UTC);
            System.out.println(time);
            ChatMessage newMessage = new ChatMessage(time, uName, msg);
            messagestoread.add(newMessage);
        }
        return (messagestoread);

    }

    public boolean checkUserFromDatabase(String username, String password) {
        try {
            String checkUserString = "select UNAME, PWORD from USER where UNAME='" + username + "'";
            Statement createStatement = con.createStatement();
            ResultSet rs = createStatement.executeQuery(checkUserString);
            
            if (rs.isBeforeFirst()) {
                String hashedPassword = rs.getString("PWORD");
                if (hashedPassword.equals(Crypt.crypt(password, hashedPassword))) {
                createStatement.close();
                return true;
                }else {
                    System.out.println("tassa virhe");
                    createStatement.close();
                    return false;
                }
                
                
            } else {
                System.out.println("Result set is not empty");
                createStatement.close();
                return false;
            }

        } catch (SQLException e) {
            System.out.println("ERROR" + e.getMessage());
            return false;
        }
    }

    public ArrayList<ChatMessage> getMessages(long since) throws SQLException {
        ArrayList<ChatMessage> messagesToGet = new ArrayList<ChatMessage>();
        String readMessages = "select UNAME, SENT, MSG from MESSAGE where SENT >" + since + " order by SENT";
        Statement createStatement = con.createStatement();
        ResultSet rs = createStatement.executeQuery(readMessages);

        while (rs.next()) {
            String uName = rs.getString("UNAME");
            String msg = rs.getString("MSG");
            long sent = rs.getLong("SENT");
            LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(sent), ZoneOffset.UTC);
            System.out.println(time);
            ChatMessage newMessage = new ChatMessage(time, uName, msg);
            messagesToGet.add(newMessage);

        }
        return messagesToGet;
}     


    public void close(){
        try {
            con.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }
}