package com.kushaldave.diagram.render;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.font.LineMetrics;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;
import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import com.kushaldave.diagram.data.DiagramData;

/**
 * Call out to DotSvgLayout and convert the resulting SVG into PNG.
 */
public class SvgRenderer extends DiagramRenderer {

  public void renderToStream(DiagramData data, OutputStream os)
      throws IOException {
    int width = data.getSize().width;
    int height = data.getSize().height;
    
    Dimension dimension = new Dimension(width, height);
    DotSvgLayout layout = new DotSvgLayout(data, dimension);

    BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
    Graphics2D g = bi.createGraphics();

    prepareCanvas(g, width, height);

    try {
      XMLEventReader xmlReader = layout.getXmlReader();
      while (xmlReader.hasNext()) {
        XMLEvent event = xmlReader.nextEvent();
        
        if (event.isStartElement()) {
            StartElement element = event.asStartElement();
            String name = element.getName().getLocalPart();
            if (name.equals("g")) {
              String transform = getAttribute(element, "transform");
              if (transform != null) {
                Pattern translatePattern = Pattern.compile("translate\\(([-\\d\\.]+) ([-\\d\\.]+)\\)");
                Matcher matcher = translatePattern.matcher(transform);
                matcher.find();
                g.translate(Float.parseFloat(matcher.group(1)), Float.parseFloat(matcher.group(2)));
              }
            } else if (name.equals("path")) {
              String d = getAttribute(element, "d");
              String lastStart = null;
              d = d.replace("C", " C");
              StringTokenizer st = new StringTokenizer(d);
              GeneralPath p = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
              while (st.hasMoreTokens()) {
                String token = st.nextToken();
                if (!token.matches("[MLC].*")) {
                  token = lastStart + token;
                }
                lastStart = token.substring(0, 1);
                if (token.startsWith("M")) {
                  String[] points = token.substring(1).split(",");
                  p.moveTo(Float.parseFloat(points[0]), Float.parseFloat(points[1]));
                } else if (token.startsWith("L")) {
                  String[] points = token.substring(1).split(",");
                  p.lineTo(Float.parseFloat(points[0]), Float.parseFloat(points[1]));                        
                } else if (token.startsWith("C")) {
                  float[] cp1 = toPoints(token.substring(1));
                  float[] cp2 = toPoints(st.nextToken());
                  float[] endPoint = toPoints(st.nextToken());
                  p.curveTo(cp1[0], cp1[1], cp2[0], cp2[1], endPoint[0], endPoint[1]);
                } 
              }
              g.draw(p);
            } else if (name.equals("polygon")) {
              String points = getAttribute(element, "points");
              String stroke = getAttribute(element, "stroke");
              String fill = getAttribute(element, "fill");
              if ("white".equals(stroke)) {
                continue;
              }
              Polygon p = new Polygon();
              StringTokenizer st = new StringTokenizer(points);
              while (st.hasMoreTokens()) {
                String[] point = st.nextToken().split(",");
                p.addPoint((int) Float.parseFloat(point[0]), (int) Float.parseFloat(point[1]));
              }
              g.draw(p);
              if ("black".equals(fill)) {
                g.fill(p);
              }
              // dropshadow?
            } else if (name.equals("text")) {
              // this is the middle?
              StringBuilder sb = new StringBuilder();
              for (XMLEvent textEvent = xmlReader.nextEvent();
                   textEvent.getEventType() == XMLEvent.CHARACTERS;
                   textEvent = xmlReader.nextEvent()) {
                sb.append(textEvent.asCharacters().getData());
              }
              String text = sb.toString();
              Rectangle2D textLength = getTextLength(text, g);

              // Adjust for font size diff
              float x = Float.parseFloat(getAttribute(element, "x")) - (float) textLength.getWidth() / 2f + 2; 
              float y = Float.parseFloat(getAttribute(element, "y")) + (float) textLength.getHeight() / 2f - 4; 
              g.drawString(text, x, y);
            }
          }
        
      }
      
      ImageIO.write(bi, "PNG", os);
    } catch (XMLStreamException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (FactoryConfigurationError e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private float[] toPoints(String value) {
    String[] valueSplit = value.split(",");
    return new float[] {
      Float.parseFloat(valueSplit[0]),
      Float.parseFloat(valueSplit[1])
    };
  }
  
  private Rectangle2D getTextLength(String text, Graphics2D g) {
    LineMetrics lineMetrics = g.getFontMetrics().getLineMetrics("Ag", g);
    // Goal: have height / 2 = (reportedHeight / 2) - lineMetrics.getDescent()
    float height = lineMetrics.getHeight() - (lineMetrics.getDescent() * 2);
    Rectangle2D stringBounds = g.getFontMetrics().getStringBounds(text, g);
    stringBounds.setRect(stringBounds.getX(), stringBounds.getY(), stringBounds.getWidth(), height);
    return stringBounds;
  }
  
  private String getAttribute(StartElement element, String attributeName) {
    Attribute attribute = element.getAttributeByName(new QName(attributeName));
    if (attribute == null) {
      Attribute style = element.getAttributeByName(new QName("style"));
      if (style != null) {
        String[] values = style.getValue().split(";");
        for (String value : values) {
          if (value.startsWith(attributeName)) {
            return value.split(":")[1];
          }
        }
      }
    }
    return attribute != null ? attribute.getValue() : null;
  }
}



