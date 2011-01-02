package com.kushaldave.diagram.render;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.IOException;
import java.io.OutputStream;

import com.kushaldave.diagram.data.DiagramData;

public abstract class DiagramRenderer {

  protected void prepareCanvas(Graphics2D g, int width, int height) {
    g.setBackground(Color.WHITE);
    g.fillRect(0, 0, width, height);
    g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
    g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    //g.setFont(new Font("Lucida Sans", Font.PLAIN, 12));
    g.setFont(new Font("Helvetica", Font.PLAIN, 12));
    g.setColor(Color.black);  
  }
  
  public abstract void renderToStream(DiagramData data, OutputStream os) throws IOException;
}
