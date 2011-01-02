package com.kushaldave.diagram.render;

import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.kushaldave.diagram.data.DiagramData;
import com.kushaldave.diagram.data.DiagramData.Edge;

public class CollaborationRenderer extends DiagramRenderer {

  public void renderToStream(DiagramData data, OutputStream os)
      throws IOException {
    int width = data.getSize().width;
    int height = data.getSize().height;

    BufferedImage bi = new BufferedImage(width, height,
        BufferedImage.TYPE_INT_BGR);
    Graphics2D g = bi.createGraphics();

    prepareCanvas(g, width, height);

    // Assign the vertices to positions along X
    List<String> vertices = new ArrayList<String>();
    for (Edge edge : data.getEdges()) {
      if (!vertices.contains(edge.getStart())) {
        vertices.add(edge.getStart());
      }
      if (!vertices.contains(edge.getEnd())) {
        vertices.add(edge.getEnd());
      }
    }
    
    // Render vertices
    int pxPerVertex = (width - 100) / vertices.size();
    for (int i = 0; i < vertices.size(); i++) {
      int x = 50 + (pxPerVertex * i);
      g.drawLine(x, 30, x, height);
      
      String vertex = vertices.get(i);
      Rectangle2D labelBounds = getTextLength(vertex, g);
      float textX = (float) (x - labelBounds.getWidth() / 2);
      g.drawString(vertex, textX, 20);
    }
    
    // Render edges
    int y = 50;
    int yIncrement = (height - y) / data.getEdges().size();
    for (Edge edge : data.getEdges()) {
      int x1 = 50 + pxPerVertex * (vertices.indexOf(edge.getStart()));
      int x2 = 50 + pxPerVertex * (vertices.indexOf(edge.getEnd()));
      
      // Draw line
      g.drawLine(x1, y, x2, y);
      
      // Draw arrowhead
      AffineTransform arrowTransform =
          AffineTransform.getTranslateInstance(x2, y);
      arrowTransform.rotate(x2 > x1 ? 0 : Math.PI);
      Shape arrow = getNotchedArrow(8.0f, 15.0f, 5.0f);
      g.draw(arrowTransform.createTransformedShape(arrow));
      g.fill(arrowTransform.createTransformedShape(arrow));
      
      // Draw label
      int labelStartX = x1 + 5;
      if (x2 < x1) {
        Rectangle2D labelBounds = getTextLength(edge.getLabel(), g);
        labelStartX = (int) (x1 - 5 - labelBounds.getWidth());
      }
      g.drawString(edge.getLabel(), labelStartX, y - 5);
      
      y += yIncrement;
    }

    ImageIO.write(bi, "PNG", os);

  }

  private Rectangle2D getTextLength(String text, Graphics2D g) {
    LineMetrics lineMetrics = g.getFontMetrics().getLineMetrics("Ag", g);
    // Goal: have height / 2 = (reportedHeight / 2) - lineMetrics.getDescent()
    float height = lineMetrics.getHeight() - (lineMetrics.getDescent() * 2);
    Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, g);
    stringBounds.setRect(stringBounds.getX(), stringBounds.getY(), stringBounds
        .getWidth(), height);
    return stringBounds;
  }

    /**
   * Returns an arrowhead in the shape of an isosceles triangle with an
   * isoceles-triangle notch taken out of the base, with the specified base and
   * height measurements. It is placed with the vertical axis along the negative
   * x-axis, with its base centered on (0,0). From JUNG's ArrowFactory
   */
  public static GeneralPath getNotchedArrow(float base, float height,
      float notch_height) {
    GeneralPath arrow = new GeneralPath();
    arrow.moveTo(0, 0);
    arrow.lineTo(-height, base / 2.0f);
    arrow.lineTo(-(height - notch_height), 0);
    arrow.lineTo(-height, -base / 2.0f);
    arrow.lineTo(0, 0);
    return arrow;
  }

}
