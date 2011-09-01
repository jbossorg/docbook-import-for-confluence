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
 * Version of DocBook to process.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public enum DocBookVersion {

  DOCBOOK_4_3("_4_3", null), DOCBOOK_5_0("_5_0", "d:");

  /**
   * Filename postfix used for resources (XSLT templates etc) for this DocBook version
   */
  private String filenamePostfix;

  /**
   * XML Namepace prefix used in XSLT files for this DocBook version
   */
  private String xmlnsPrefix = "";

  private DocBookVersion(String filenamePostfix, String xmlnsPrefix) {
    this.filenamePostfix = filenamePostfix;
    if (xmlnsPrefix != null)
      this.xmlnsPrefix = xmlnsPrefix;
  }

  /**
   * Get filename postfix used for resources (XSLT templates etc) for this DocBook version
   * 
   * @return filename postfix
   */
  public String getFilenamePostfix() {
    return filenamePostfix;
  }

  /**
   * Get XML Namepace prefix used in XSLT files for this DocBook version.
   * 
   * @return the xmlnsPrefix, never null
   */
  public String getXmlnsPrefix() {
    return xmlnsPrefix;
  }

}
