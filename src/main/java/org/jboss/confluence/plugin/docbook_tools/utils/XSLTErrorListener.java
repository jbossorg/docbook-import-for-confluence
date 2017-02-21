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

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import org.apache.log4j.Logger;

/**
 * Error listener for our XSLT processing.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class XSLTErrorListener implements ErrorListener {

  private static final Logger log = Logger.getLogger(XSLTErrorListener.class);

  private TransformerException exception;

  /**
   * Get exception if some occured during transformation.
   * 
   * @return the exception if some occured or null
   */
  public TransformerException getException() {
    return exception;
  }

  /**
   * @param exception the exception to set
   */
  public void setException(TransformerException exception) {
    this.exception = exception;
  }

  @Override
  public void warning(TransformerException exception) throws TransformerException {
    log.warn(exception.getMessage());
  }

  @Override
  public void fatalError(TransformerException exception) throws TransformerException {
    this.exception = exception;
  }

  @Override
  public void error(TransformerException exception) throws TransformerException {
    this.exception = exception;
  }
}