package com.kushaldave.diagram.data;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * Single object containing user-provided sentences and configuration
 * as well as the sentences parsed into Edges and Vertices.
 * TODO(kushal): Move into contained objects
 */
public class DiagramData {

  public static class Edge {
    private String start;
    private String end;
    private String label;

    public Edge(String start, String end, String label) {
      this.start = start;
      this.end = end;
      this.label = label;
    }

    public String getStart() {
      return start;
    }

    public String getEnd() {
      return end;
    }

    public String getLabel() {
      return label;
    }
    
    @Override
    public boolean equals(Object obj) {
      if (obj == this) {
        return true;
      }
      if (obj instanceof Edge) {
        Edge e2 = (Edge) obj;
        return e2.end.equals(this.end) &&
          e2.start.equals(this.start) &&
          e2.label.equals(this.label);
      }
      return false;
    }
    
    @Override
    public int hashCode() {
      /// todo
      return super.hashCode();
    }
  }
  
  public static enum Layout {
    TOP_DOWN,
    COLLABORATION
  };

  private Collection<String> vertices = new LinkedHashSet<String>();
  private Collection<Edge> edges = new ArrayList<Edge>();
  private Multimap<String, String> subgraphs = HashMultimap.create();
  private Collection<Edge> edgesMinusSubgraphs = new ArrayList<Edge>();
  private List<String> sentences = new ArrayList<String>();
  private Dimension size = new Dimension(300, 300);
  private Layout layout = Layout.TOP_DOWN;
  private String title = "";
  
  Edge addEdge(String label, String start, String end) {
    vertices.add(start);
    vertices.add(end);
    Edge edge = new Edge(start, end, label);
    if (!edges.contains(edge)) {
      edges.add(edge);
      return edge;
    }
    return null;
  }

  void rebuildGraph() {
    vertices.clear();
    edges.clear();
    ArrayList<String> sentenceCopy = new ArrayList<String>();
    sentenceCopy.addAll(sentences);
    sentences.clear();
    for (String sentence : sentenceCopy) {
      addSentence(sentence);
    }
  }

  public Collection<Edge> getEdges() {
    return edges;
  }

  public Collection<String> getVertices() {
    return vertices;
  }

  public List<String> getSentences() {
    return sentences;
  }

  public Dimension getSize() {
    return size;
  }

  public Layout getLayout() {
    return layout;
  }

  public Multimap<String, String> getSubgraphs() {
    return subgraphs;
  }

  public Collection<Edge> getEdgesMinusSubgraphs() {
    return edgesMinusSubgraphs;
  }

  public void addSentence(String sentence) {
    if (sentence != null && !"".equals(sentence)) {
      try {
        String[] pieces = SentenceParser.parseSentence(sentence);
        Edge edge = addEdge(pieces[1], pieces[0], pieces[2]);
        if (pieces[1].equals("is in")) {
          subgraphs.put(pieces[2], pieces[0]);
        } else if (edge != null){
          edgesMinusSubgraphs.add(edge);
        }
        sentences.add(sentence);
      } catch (Exception e) {}
    }
  }
  
  public void setSize(Dimension size) {
    this.size = size;
  }
  
  public void setLayout(Layout layout) {
    this.layout = layout;
  }
  
  public void deleteSentence(String sentence) {
    sentences.remove(sentence);
    rebuildGraph();
  }
  
  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void moveUp(String sentence) {
    int newIndex = sentences.indexOf(sentence) - 1;
    if (newIndex > -1) {
      sentences.remove(sentence);
      sentences.add(newIndex, sentence);
    }
  }

  public void moveDown(String sentence) {
    int newIndex = sentences.indexOf(sentence) + 1;
    if (newIndex < sentences.size()) {
      sentences.remove(sentence);
      sentences.add(newIndex, sentence);      
    }
  }

  public void changeLayout() {
    if (layout == Layout.COLLABORATION) {
      layout = Layout.TOP_DOWN;
    } else {
      layout = Layout.COLLABORATION;
    }
  }

  public void changeSize(String width, String height) {
    this.size = new Dimension(
        Integer.parseInt(width),
        Integer.parseInt(height));
  }

  public String toJSON() throws JSONException {
    JSONObject json = new JSONObject();
    json.put("width", getSize().getWidth());
    json.put("height", getSize().getHeight());
    json.put("layout", getLayout().toString());

    json.put("title", getTitle().toString());

    for (String sentence : getSentences()) {
      json.append("sentences", sentence);
    }
    return json.toString();
  }
  
  public static DiagramData fromJSON(String s) throws JSONException {
    JSONObject json = new JSONObject(s);
    DiagramData data = new DiagramData();
    try {
      data.setSize(new Dimension(json.getInt("width"), json.getInt("height")));
    } catch (JSONException e) {
    }

    try {
      data.setLayout(Layout.valueOf(json.getString("layout")));
    } catch (JSONException e) {
    }

    try {
      data.setTitle(json.getString("title"));
    } catch (JSONException e) {
    }

    if (json.has("sentences")) {
      JSONArray sentences = json.getJSONArray("sentences");
      for (int i = 0; i < sentences.length(); i++) {
        data.addSentence(sentences.getString(i));
      }
    }

    return data;
  }

  public static void main(String[] args) {
  }
}
