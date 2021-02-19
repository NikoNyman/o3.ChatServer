package com.nikonyman.chatserver;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ChatDatabase {

    private static ChatDatabase singleton = null;
    public Connection con;

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
            String createUsersString = "CREATE TABLE USER(UNAME VARCHAR2(30) PRIMARY KEY,PWORD VARCHAR2(30),EMAIL VARCHAR2(30))";
            Statement createStatement = con.createStatement();
            createStatement.executeUpdate(createUsersString);
            createStatement.close();
            return true;
        }
        return false;
    }

    public boolean insertToDatabase(String username, String password, String email){
            try{
                String createUserString = "insert into USER " + "VALUES('" + username + "','" + password + "','" + email + "')";
                Statement createStatement = con.createStatement();
                createStatement.executeUpdate(createUserString);
                createStatement.close();
                  
                  return true;
      } catch(SQLException e){
         System.out.println("Primary key already used");
         return false;
      }
        }



        public boolean checkUserFromDatabase(String username, String password) throws SQLException{
            String createUserString = "select UNAME, PWORD from USER where UNAME='" + username + "' and PWORD='" + password + "'";
            Statement createStatement = con.createStatement();
            ResultSet rs = createStatement.executeQuery(createUserString);
            createStatement.close();
            if(!rs.isBeforeFirst()){
                return true;
            }
            else {
                System.out.println("Result set is not empty");
                return false;
            } 
            

        }
    }
