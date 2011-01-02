package com.kushaldave.diagram.servlets;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONException;

import com.kushaldave.diagram.data.DiagramData;
import com.kushaldave.diagram.data.DiagramData.Layout;
import com.kushaldave.diagram.render.DiagramRenderer;

/**
 * Generate a random ID and redirect to an editing UI for that diagram.
 * Only accept POST to keep robots from creating new diagrams.
 */
@SuppressWarnings("serial")
public class RenderServlet extends HttpServlet {

  private DiagramRenderer topdown;
  private DiagramRenderer collaboration;

  public RenderServlet(DiagramRenderer topdown, DiagramRenderer collaboration) {
    this.topdown = topdown;
    this.collaboration = collaboration;
  }

  protected void doGet(HttpServletRequest req, HttpServletResponse resp)
  throws ServletException, IOException {
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("text/html");
    resp.getWriter().write("<html><form method=POST><textarea name=sentences></textarea><input type=submit value=diagram></form></html>");
  }
  
  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse resp)
      throws ServletException, IOException {
    // TODO: Use Batik to generate SVG
    resp.setStatus(HttpServletResponse.SC_OK);
    resp.setContentType("image/png");
    DiagramData data;
    try {
      if (req.getParameter("sentences") != null) {
        String sentences = req.getParameter("sentences");
        data = new DiagramData();
        for (String sentence : sentences.split("\\.")) {
          data.addSentence(sentence);
        }
      } else {
        String json = req.getParameter("json");
        data = DiagramData.fromJSON(json);
      }
      DiagramRenderer renderer = data.getLayout() == Layout.COLLABORATION ? collaboration
          : topdown;
      renderer.renderToStream(data, resp.getOutputStream());
    } catch (JSONException e) {
      throw new IOException(e);
    }
  }
}
