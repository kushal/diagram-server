package com.kushaldave.diagram;


import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import com.kushaldave.diagram.render.CachingRenderer;
import com.kushaldave.diagram.render.CollaborationRenderer;
import com.kushaldave.diagram.render.DiagramRenderer;
import com.kushaldave.diagram.render.SvgRenderer;
import com.kushaldave.diagram.servlets.RenderServlet;
import com.kushaldave.diagram.util.Constants;

public class DiagramServer {
  public static void main(String[] args) throws Exception {
    Server server = new Server(7070);
    
    ServletContextHandler rootCH = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
    rootCH.setContextPath("/");
    server.setHandler(rootCH);
    
    DiagramRenderer topDown = new SvgRenderer();
    DiagramRenderer collaboration = new CollaborationRenderer();
 
    if (!Constants.DEBUG) {
      topDown = new CachingRenderer(topDown);
      collaboration = new CachingRenderer(collaboration);
    } else {
      System.err.println("Caches enabled");
    }
 
    rootCH.addServlet(new ServletHolder(new RenderServlet(topDown, collaboration)), "/render");
    
    server.start();
    server.join();
  }
}
