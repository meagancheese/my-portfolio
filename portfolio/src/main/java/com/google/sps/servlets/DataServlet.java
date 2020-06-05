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
    // System.out.println("Calls doGet"); DEBUG Tool
    /*
    // Testing 1 2 3 
    for(int i = 0; i < 10; i++){
      Entity testEnt = new Entity("Test");
      testEnt.setProperty("runNumber", i);
      Transaction txn = datastore.beginTransaction();
      datastore.put(txn, testEnt);
      txn.commit();
      System.out.println("TEST: This P U T run: " + testEnt.getProperty("runNumber"));
      for(Entity entity : datastore.prepare(new Query("Test")).asIterable()){
        System.out.println("TEST: This GOT run: " + entity.getProperty("runNumber"));
      }
    }
   List<Entity> testList = new ArrayList<>();
   for(Entity test : datastore.prepare(new Query("Test")).asIterable()){
     testList.add(test);
   }
   
    for(Entity test : testList){
      Transaction txn = datastore.beginTransaction();
      datastore.delete(txn, test.getKey());
      txn.commit();
      System.out.println(test.getProperty("runNumber"));
    }
    
  /*  try {
          java.lang.Thread.sleep(200);
         } catch (Exception e) {
            System.out.println(e);
         }
         
    System.out.println("After delete, datastore sees these: ");
    for(Entity entity : datastore.prepare(new Query("Test")).asIterable()){
      System.out.println(entity.getProperty("runNumber"));
    }
    try {
          java.lang.Thread.sleep(1000);
         } catch (Exception e) {
            System.out.println(e);
         }
         
    System.out.println("After delete, datastore sees these: ");
    for(Entity entity : datastore.prepare(new Query("Test")).asIterable()){
      System.out.println(entity.getProperty("runNumber"));
    }
    */
    //End Testing
    
    
    // Temporarily comments out the base of my code to just test doGet placing and retrieving info from datastore
    //if(true){ //Comments out code
    //  return; //
    //}         //
    
    String jsonMessages = "";
    int max = getMax(request);
    // for(int i=0; i < 10; i++){ // DEBUG Tool
    if (max < 0) {
      max = 5; // Default
    }
    List<Entity> results = datastore.prepare(new Query("Comment").addSort("timestamp_millis", SortDirection.ASCENDING))
      .asList(FetchOptions.Builder.withLimit(max));
      
    try {
      java.lang.Thread.sleep(1200);
    } catch (Exception e) {
      System.out.println(e);
      }
      
    List<String> messages = new ArrayList<String>();
    for(Entity entity : results){
      String text = (String)entity.getProperty("text");
      String name = (String)entity.getProperty("name");
      String comment = "\"" + text + "\" - " + name;
      messages.add(comment);
    }
    final Gson gson = new Gson();
    jsonMessages = gson.toJson(messages);
     // System.out.println("DEBUG: " + i + " " + jsonMessages); // DEBUG Tool
    // } // DEBUG Tool
    response.setContentType("application/json;");
    response.getWriter().println(jsonMessages);
    
    
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
    // System.out.println("DEBUG: Sent to datastore " + comment); // DEBUG Tool
    response.sendRedirect("/index.html");
  }
  
  private int getMax(HttpServletRequest request) {
    String maxString = request.getParameter("max");
    if(maxString == null){
      return 5; // Default
    }
    try {
      return Integer.parseInt(maxString);
    } catch(NumberFormatException e) {
      return -1;
    }
  }
}
