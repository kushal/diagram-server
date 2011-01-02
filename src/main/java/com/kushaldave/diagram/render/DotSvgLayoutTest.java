package com.kushaldave.diagram.render;

import java.awt.Dimension;
import java.io.IOException;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import com.kushaldave.diagram.data.DiagramData;

public class DotSvgLayoutTest extends TestCase {

  public void testGetXmlReaderDash() throws IOException, XMLStreamException,
      FactoryConfigurationError {
    DiagramData data = new DiagramData();
    data.addSentence("A eats A-b\u2122");
    Dimension d = new Dimension(100, 100);
    DotSvgLayout layout = new DotSvgLayout(data, d);
    layout.getXmlReader();
  }

}
