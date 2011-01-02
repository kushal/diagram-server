package com.kushaldave.diagram.data;

public class SentenceParser {

  /***
   * Parse provided sentence into 3 pieces.
   */
  public static String[] parseSentence(String s) {
    return new SentenceParser(s).parse();
  }

  private String s;

  public SentenceParser(String s) {
    s = s.trim(); // just in case
    if (s.endsWith(".")) {
      s = s.substring(0, s.length() - 1);
    }
    this.s = s;
  }
  
  public String[] parse() {
    String firstToken = getFirstToken();
    String lastToken = getLastToken();
    return new String[] { firstToken, s, lastToken };
  }

  private String getLastToken() {
    String lastToken;
    if (s.endsWith("\"")) {
      // If trails with " or .", read to preceding "
      int lastQuote = s.lastIndexOf("\"", s.length() - 2);
      lastToken = s.substring(lastQuote + 1, s.length() - 1);
      s = s.substring(0, lastQuote - 1);
    } else {
      // Otherwise, break at last space
      int lastSpace = s.lastIndexOf(" ");
      lastToken = s.substring(lastSpace + 1).toLowerCase();
      s = s.substring(0, lastSpace);
    }
    return lastToken;
  }

  private String getFirstToken() {
    String firstToken;
    if (s.startsWith("\"")) {
      // If the leading character is a ", break at the next "
      int nextQuote = s.indexOf("\"", 1);
      firstToken = s.substring(1, nextQuote);
      s = s.substring(nextQuote + 2);
    } else {
      // Otherwise, break at first space
      int spaceIndex = s.indexOf(" ");
      firstToken = s.substring(0, spaceIndex).toLowerCase();
      s = s.substring(spaceIndex + 1);
    }
    return firstToken;
  }
}
