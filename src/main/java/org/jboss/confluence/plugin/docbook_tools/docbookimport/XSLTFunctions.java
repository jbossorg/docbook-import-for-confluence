/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.confluence.plugin.docbook_tools.docbookimport;

/**
 * Functions used inside XSLT templates over Xalan extension mechanism.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class XSLTFunctions {

  /**
   * Patter used to match prefix in {@link #sectionTitlePrefixRemove(String)}
   */
  private static final String SECT_TITILE_PREFIX_PATTERN = "^[a-zA-Z0-9][a-zA-Z0-9]{1,3}-.+";

  /**
   * Remove prefix from section title. These prefixes are used due "page title unique" constraint in Confluence. Prefix
   * consist from two to four chars(letter or digit) followed by <code>-</code>. Space after prefix is trimmed if there.
   * 
   * @param title to remove prefix from.
   * @return title with removed prefix
   */
  public static String sectionTitlePrefixRemove(String title) {
    if (title == null)
      return "";
    if (title.matches(SECT_TITILE_PREFIX_PATTERN)) {
      int idx = title.indexOf("-");
      return title.substring(idx + 1).trim();
    } else {
      return title;
    }
  }

  /**
   * Pattern to match escaped characters in {@link #sectionTitlePrefixRemove(String)}. Capturing group must be used here
   * to copy content into result string. Matches start of some pair tag (bold, emphasis, underline, strike through, ...)
   * s to escape them, which is enough
   */
  private static final String ESCAPED_CHARS_PATTERN_PAIRTAGS = "([\\*\\-\\+\\_\\[\\(\\{])\\S";
  /**
   * Patterns to match escaped characters in {@link #sectionTitlePrefixRemove(String)}. Capturing group must be used
   * here to copy content into result string. Matches lists begin characters and escape them.
   */
  private static final String ESCAPED_CHARS_PATTERN_UOL = "^\\s*(\\*)\\s+";
  private static final String ESCAPED_CHARS_PATTERN_UOL2 = "^\\s*(\\-)\\s+";
  private static final String ESCAPED_CHARS_PATTERN_OL = "^\\s*(\\#)\\s+";

  /**
   * Escape text for Confluence WIKI - some special characters are escaped by backslash. <br>
   * Also removes all EOL characters from text.
   * 
   * @param text to escape characters in
   * @return escaped text
   */
  public static String escapeWIKICharsInContent(String text) {
    if (text == null)
      return "";
    text = text.replaceAll(ESCAPED_CHARS_PATTERN_PAIRTAGS, "\\\\$0");
    text = text.replaceAll(ESCAPED_CHARS_PATTERN_UOL, "\\\\* ");
    text = text.replaceAll(ESCAPED_CHARS_PATTERN_UOL2, "\\\\- ");
    text = text.replaceAll(ESCAPED_CHARS_PATTERN_OL, "\\\\# ");
    return removeAllEOL(text);
  }

  /**
   * Prepare monoscaped text for Confluence WIKI.
   * 
   * @param text content to make monoscape
   * @return monoscape WIKI text
   */
  public static String prepareMonospacedWIKIText(String text) {
    return surroundWIKIContentTextWithSequence(text, "{{", "}}");
  }

  /**
   * Surround given text with given prefix and postfix. Preserves spaces at the begin and end of text. Text is escaped
   * by {@link #escapeWIKICharsInContent(String)} before surrounded.
   * 
   * @param text to surround
   * @param prefix to use
   * @param suffix to use
   * @return surrounded text
   */
  static String surroundWIKIContentTextWithSequence(String text, String prefix, String suffix) {
    if (text == null || text.isEmpty()) {
      return "";
    }
    StringBuilder sb = new StringBuilder();
    if (text.startsWith(" ")) {
      sb.append(" ");
    }
    if (text.trim().length() > 0) {
      sb.append(prefix);
      sb.append(escapeWIKICharsInContent(text.trim()));
      sb.append(suffix);
    }
    if (text.length() > 1 && text.endsWith(" ")) {
      sb.append(" ");
    }
    return sb.toString();
  }

  /**
   * Trim text from XML elements with unwanted EOL around text to be used in Confluence WIKI syntax. <br>
   * Also removes all EOL characters from text.
   * 
   * @param text content to trim
   * @return trimmed WIKI text
   */
  public static String trimWIKIText(String text) {
    if (text == null)
      return "";
    // remove spaces at begin if text starts with EOL
    if (text.startsWith("\n")) {
      int idx = -1;
      for (int i = 0; i < text.length() && idx == -1; i++) {
        if (!Character.isWhitespace(text.charAt(i))) {
          idx = i;
        }
      }
      if (idx > -1) {
        text = text.substring(idx);
      }
    }
    // remove spaces at the end after last EOL
    if (text.contains("\n")) {
      int idx = -1;
      for (int i = text.length() - 1; i >= 0; i--) {
        char ch = text.charAt(i);
        if (Character.isWhitespace(ch) && ch == '\n') {
          idx = i;
          break;
        }
        if (!Character.isWhitespace(ch)) {
          break;
        }
      }
      if (idx > -1) {
        text = text.substring(0, idx);
      }
    }
    return removeAllEOL(text);
  }

  /**
   * Remove all EOL from text. Use space if space is not here. Remove spaces if there are more around EOL and leave here
   * only one.
   * 
   * @param text to normalize
   * @return normalized text
   */
  static String removeAllEOL(String text) {
    if (text == null || text.length() == 0)
      return "";
    text = text.replaceAll("(\\s+\\n\\s+)", " ");
    text = text.replaceAll("(\\s+\\n)", " ");
    text = text.replaceAll("(\\n\\s+)", " ");
    text = text.replaceAll("(\\s+\\r\\s+)", " ");
    text = text.replaceAll("(\\s+\\r)", " ");
    text = text.replaceAll("(\\r\\s+)", " ");
    return text.replaceAll("([\\n\\r])", " ");
  }

}
