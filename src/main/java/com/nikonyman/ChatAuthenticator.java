package com.nikonyman;

import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Map;

import com.nikonyman.chatserver.ChatDatabase;
import com.sun.net.httpserver.BasicAuthenticator;

// Luokka jolla tarkistetaan voiko käyttäjä chätätä //

public class ChatAuthenticator extends BasicAuthenticator {

  private Map<String,User> users;

  public ChatAuthenticator() {
    super("chat");
    users = new Hashtable<String, User>();
    
  }

  
// Tarkistetaan onko käyttäjä tiedot oikeat //
  @Override
  public boolean checkCredentials(String username, String password) {
    ChatDatabase database = ChatDatabase.getInstance();
      try {
      if (database.checkUserFromDatabase(username, password)) {
        return true;
        
      }else{
        return false;
      }
    } catch (SQLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;

  }

  // Lisätään uusi käyttäjä mikäli käyttäjänimi ei ole varattu //
public boolean addUser(String username, String password, String email) {
  ChatDatabase database = ChatDatabase.getInstance();
    if(database.insertToDatabase(username,password,email)){
      return true;
    }
    else{
      return false;
    }
}

}
