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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Key;
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

@WebServlet("/data")
public class DataServlet extends HttpServlet {
  
  private DatastoreService datastore;
  
  @Override
  public void init(){
    datastore = DatastoreServiceFactory.getDatastoreService();
  }
  
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String jsonMessages = "";
    List<Entity> results = datastore.prepare(new Query("Comment").addSort("timestamp_millis", SortDirection.DESCENDING))
      .asList(FetchOptions.Builder.withDefaults());
    List<String> messages = new ArrayList<String>();
    for(Entity entity : results){
      String text = (String)entity.getProperty("text");
      String name = (String)entity.getProperty("name");
      String comment = String.format("\"%s\" - %s", text, name);
      messages.add(comment);
    }
    response.setContentType("application/json;");
    response.getWriter().println(new Gson().toJson(messages));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    long timestampMillis = System.currentTimeMillis();
    String comment = request.getParameter("comment");
    String name = request.getParameter("name");
    if(name.equals("")){
      name = "Anonymous";
    }
    // TODO(meagancheese): Add Sanitization Step
    Entity commentEntity = new Entity("Comment");
    commentEntity.setProperty("text", comment);
    commentEntity.setProperty("timestamp_millis", timestampMillis);
    commentEntity.setProperty("name", name);
    Transaction txn = datastore.beginTransaction();
    datastore.put(txn, commentEntity);
    txn.commit();
    response.sendRedirect("/index.html");
  }
}
