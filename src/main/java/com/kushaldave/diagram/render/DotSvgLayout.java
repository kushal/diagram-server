package com.kushaldave.diagram.render;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.kushaldave.diagram.data.DiagramData;
import com.kushaldave.diagram.data.DiagramData.Edge;
import com.kushaldave.diagram.util.Constants;
import com.kushaldave.diagram.util.Util;

public class DotSvgLayout {
  
  private static final XMLInputFactory xmlFactory = XMLInputFactory.newInstance();

  static {
    xmlFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);
    xmlFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
    xmlFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
  }
  private Map<String, Point2D> locations = new HashMap<String, Point2D>();

  private DiagramData data;

  private Dimension dimension;
  
  DotSvgLayout(DiagramData data, Dimension dimension) throws IOException {
    this.data = data;
    this.dimension = dimension;
  }
  
  public XMLEventReader getXmlReader() throws IOException, XMLStreamException, FactoryConfigurationError {
    
    String[] vertices = data.getVertices().toArray(new String[0]);
    Process p = Runtime.getRuntime().exec("/usr/local/bin/dot -y -Tsvg");
    
    // Map vertices to numbers
    Map<String, Integer> numbers = new HashMap<String, Integer>();
    for (int i = 0; i < vertices.length; i++) {
      numbers.put(vertices[i], i);
    }

    PrintWriter osWriter = new PrintWriter(p.getOutputStream());
    if (!Constants.DEBUG) {
      writeGraph(data, numbers, osWriter);
      osWriter.close();
      System.err.println(Util.streamToString(p.getErrorStream()));
      return xmlFactory.createXMLEventReader(new BufferedReader(
          new InputStreamReader(p.getInputStream())));
    } else {
      StringWriter w = new StringWriter();
      writeGraph(data, numbers, w);
      osWriter.write(w.toString());
      osWriter.close();
      System.err.println(Util.streamToString(p.getErrorStream()));
      String result = Util.streamToString(p.getInputStream());
      return xmlFactory.createXMLEventReader(new StringReader(result));
    }
  }

  private void writeGraph(DiagramData data,
      Map<String, Integer> numbers, Writer pwriter) throws IOException {
    BufferedWriter writer = new BufferedWriter(pwriter);
    writer.write("digraph G { compound=true ");
    String dimensionString = dimension.getWidth() + "," + dimension.getHeight();
    writer.write("graph [viewport=\"" + dimensionString + ",1\" center=true] ");
    writer.write("node [ shape=box ] ");
    writer.newLine();

    Set<String> subgraphVertices = Sets.newHashSet();
    // Add all subgraphs and remember used nodes
    for (String subgraph : data.getSubgraphs().keySet()) {
      writer.write("subgraph cluster_" + numbers.get(subgraph) + " {");
      writer.newLine();
      writer.write("label=\"" + subgraph + "\";");
      writer.newLine();
      subgraphVertices.add(subgraph);
      for (String vertex : data.getSubgraphs().get(subgraph)) {
        subgraphVertices.add(vertex);
        writer.write("n" + numbers.get(vertex) + " [label=\"" + vertex + "\"];");
        writer.newLine();
      }
      writer.newLine();
      writer.write("}");
      writer.newLine();
    }
    
    for (String vertex : data.getVertices()) {
      if (!subgraphVertices.contains(vertex)) {
        writer.write("n" + numbers.get(vertex) + " [label=\"" + vertex + "\"];");
        writer.newLine();
      }
    }
    for (Edge edge : data.getEdgesMinusSubgraphs()) {
      String start = edge.getStart();
      String end = edge.getEnd();
      String ltail = "";
      String lhead = "";
      Multimap<String, String> subgraphs = data.getSubgraphs();
      if (subgraphs.containsKey(start)) {
        // Pick an arbitrary node inside of the subgraph as start but set logical head to subgraph
        lhead = ",ltail=cluster_" + numbers.get(start);
        start = subgraphs.get(start).iterator().next();
      }
      if (subgraphs.containsKey(end)) {
        ltail = ",lhead=cluster_" + numbers.get(end);
        end = subgraphs.get(end).iterator().next();
      }
      writer.write("n" + numbers.get(start) + "->n" + numbers.get(end) + " [label=\"" + edge.getLabel() + "\"" + ltail + lhead + "];");
      writer.newLine();
    }
    
    writer.write("}");
    writer.close();
  }

  public Point2D transform(String v) {
    return locations.get(v);
  }
}
