package com.nikonyman;

import java.util.Hashtable;
import java.util.Map;

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
    if (users.containsKey(username)) {
      String pword = users.get(username).getPassword();
      if (pword.equals(password)) {

        return true;
      }
    }

    return false;

  }

  // Lisätään uusi käyttäjä mikäli käyttäjänimi ei ole varattu //
public boolean addUser(String username, String password, String email) {
  if (users.containsKey(username)) {
    return false;
  } else {
    User newuser = new User (username, password, email);
    users.put(username, newuser);
    return true;
  }
 }

}
