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
package org.jboss.confluence.plugin.docbook_tools.utils;

import java.util.regex.Pattern;

/**
 * Utility classes for regexp.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class RegExpUtils {

  /**
   * Patter used to match escaped characters in {@link #escapeTextForRegexp(String)}. Capturing group must be used here
   * to copy content into result string.
   */
  private static final String ESCAPED_CHARS_PATTERN = "([\\*\\+\\.\\?\\_\\\\])";

  /**
   * Escape text to be safely used in {@link Pattern} regexp. So escape all regexp danger characters.
   * 
   * @param text
   * @return
   */
  public static final String escapeTextForRegexp(String text) {
    if (text == null)
      return "";
    return text.replaceAll(ESCAPED_CHARS_PATTERN, "\\\\$0");
  }

}
