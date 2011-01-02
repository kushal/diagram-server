package com.kushaldave.diagram.data;

import org.junit.Assert;
import junit.framework.TestCase;

public class SentenceParserTest extends TestCase {

  public void testStandardSentence() {
    Assert.assertArrayEquals(
        new String[] { "harry\u2122", "loves", "sal-ly"},
        SentenceParser.parseSentence("Harry\u2122 loves Sal-ly"));
    Assert.assertArrayEquals(
        new String[] { "harry", "really loves", "sally"},
        SentenceParser.parseSentence("Harry really loves Sally."));
  }

  public void testQuotedSentences() {
    Assert.assertArrayEquals(
        new String[] { "Harry Houdini", "loves", "sally"},
        SentenceParser.parseSentence("\"Harry Houdini\" loves Sally"));
    Assert.assertArrayEquals(
        new String[] { "harry", "really loves", "Sally"},
        SentenceParser.parseSentence("Harry really loves \"Sally\"."));
  }
}
