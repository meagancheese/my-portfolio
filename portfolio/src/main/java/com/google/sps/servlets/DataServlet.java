// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that returns some example content. TODO: modify this file to handle comments data */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private DatastoreService datastore;
  
  @Override
  public void init(){
    datastore = DatastoreServiceFactory.getDatastoreService();
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    PreparedQuery results = datastore.prepare(new Query("Comment"));
    List<String> messages = new ArrayList<String>();
    for(Entity entity : results.asIterable()){
      messages.add((String) entity.getProperty("text"));
    }
    final Gson gson = new Gson();
    String jsonMessages = gson.toJson(messages);
    response.setContentType("application/json;");
    response.getWriter().println(jsonMessages);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String comment = request.getParameter("comment");
    // TODO(meagancheese): Add Sanitization Step
    int max = getMax(response);
    if (max <= 0) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a valid integer number greater than zero.");
      return;
    }
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", comment);
    datastore.put(commentEntity);
    response.sendRedirect("/index.html");
  }
  
  private int getMax(HttpServletRequest request) {
    String maxString = request.getParameter("max");
    int max;
    try {
      max = Integer.parseInt(maxString);
    } catch(NumberFormatException e) {
      System.err.println("Could not convert to int: " + maxString);
      return -1;
    }
    return max;
  }
}