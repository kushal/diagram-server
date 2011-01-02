package com.kushaldave.diagram.render;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.JSONException;

import com.kushaldave.diagram.data.DiagramData;

public class CachingRenderer extends DiagramRenderer {
  
  private LinkedHashMap<String, byte[]> cache;
  private DiagramRenderer renderer;

  @SuppressWarnings("serial")
  public CachingRenderer(DiagramRenderer renderer) {
    cache = new LinkedHashMap<String, byte[]>(10, .75f, true) {
      @Override
      protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
        return size() > 50;
      }
    };
    this.renderer = renderer;
  }

  @Override
  public void renderToStream(DiagramData data, OutputStream os)
      throws IOException {
    String dataJson;
    try {
      dataJson = data.toJSON();
    } catch (JSONException e) {
      throw new IOException(e);
    }
    
    byte[] bytes = cache.get(dataJson);
    if (bytes == null) {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      renderer.renderToStream(data, baos);
      bytes = baos.toByteArray();
      cache.put(dataJson, bytes);
    }
    os.write(bytes);
  }
}
