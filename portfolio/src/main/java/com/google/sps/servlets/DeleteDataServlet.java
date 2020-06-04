// I might not need all of these, but they're the imports from my other servlet

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
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

@WebServlet("/delete-data")
public class DeleteDataServlet extends HttpServlet {
  
  private DatastoreService datastore;
  
  @Override
  public void init(){
    datastore = DatastoreServiceFactory.getDatastoreService();
  }
  
  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response) throws IOException {
    List<Key> keys = new ArrayList<Key>();
    for(Entity entity : datastore.prepare(new Query("Comment")).asIterable()){
      keys.add(entity.getKey());
    }
    for(Key key : keys){
      datastore.delete(key);
    }
  }
}
