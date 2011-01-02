package com.kushaldave.diagram.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {

  public static String streamToString(InputStream is) throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(is));
    StringBuilder sb = new StringBuilder();
    String line;
    while (null != (line = br.readLine())) {
      sb.append(line);
    }
    is.close();
    return sb.toString();
  }
}
