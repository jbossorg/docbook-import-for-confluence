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

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 * Error listener for our SAX processing.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class SAXErrorHandler implements ErrorHandler {

  private StringBuilder errorLog = new StringBuilder();

  /**
   * Get exception if some occurred during transformation.
   * 
   * @return the exception if some occurred or null
   */
  public Exception getException() {
    if (errorLog.length() == 0)
      return null;
    return new Exception(errorLog.toString());
  }

  @Override
  public void warning(SAXParseException exception) throws SAXParseException {
    process(exception);
  }

  @Override
  public void fatalError(SAXParseException exception) throws SAXParseException {
    process(exception);
  }

  @Override
  public void error(SAXParseException exception) throws SAXParseException {
    process(exception);
  }

  private void process(SAXParseException exception) {
    String sid = exception.getSystemId();
    if (sid != null) {
      int idx = sid.lastIndexOf("/");
      if (idx > -1) {
        sid = sid.substring(idx);
      }
    }

    if (this.errorLog.length() > 0)
      this.errorLog.append(" causes ");
    this.errorLog.append("Error parsing file ");
    this.errorLog.append(sid);
    this.errorLog.append(" due '");
    this.errorLog.append(exception.getMessage());
    this.errorLog.append("'");
    Throwable e = exception.getCause();
    while (e != null) {
      this.errorLog.append(" due '");
      this.errorLog.append(e.getMessage());
      this.errorLog.append("'");
      e = e.getCause();
    }
  }

}