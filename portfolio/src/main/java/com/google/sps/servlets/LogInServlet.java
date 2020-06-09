package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@WebServlet("/login")
public class LogInServlet extends HttpServlet {
  
  final String ROOT_URL = "/";
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    UserService userService = UserServiceFactory.getUserService();
    boolean loggedIn = userService.isUserLoggedIn();
    LoginInfo toSend;
    if (loggedIn) {
      toSend = new LoginInfo(loggedIn, userService.createLogoutURL(ROOT_URL));
    } else {
      toSend = new LoginInfo(loggedIn, userService.createLoginURL(ROOT_URL));
    }
    response.getWriter().println(new Gson().toJson(toSend));
  }  
  
  private class LoginInfo {
    private final boolean loggedIn;
    private final String changeLogInStatusURL;
    
    public LoginInfo(boolean loggedIn, String changeLogInStatusURL) {
      this.loggedIn = loggedIn;
      this.changeLogInStatusURL = changeLogInStatusURL;
    }
  }
}
