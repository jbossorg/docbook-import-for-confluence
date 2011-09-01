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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jboss.confluence.plugin.docbook_tools.utils.ConfluencePageTitleUniqEnabled;
import org.jboss.confluence.plugin.docbook_tools.utils.ConfluenceUtils;

/**
 * Value object used to hold parsed imported document structure.
 * 
 * @author Vlastimil Elias (velias at redhat dot com) (C) 2011 Red Hat Inc.
 * @see DocbookImporter#getDocStructure(java.io.InputStream, String)
 */
public class DocStructureItem implements ConfluencePageTitleUniqEnabled {

  public static final String TYPE_BOOK = "book";
  public static final String TYPE_CHAPTER = "chapter";
  public static final String TYPE_APPENDIX = "appendix";
  public static final String TYPE_SECTION = "section";

  /**
   * Basic constructor.
   */
  public DocStructureItem() {

  }

  /**
   * Constructor.
   * 
   * @param type to set, see {@link #type} doc.
   */
  public DocStructureItem(String type) {
    this.type = type;
  }

  /**
   * Filling constructor.
   * 
   * @param type to set, see {@link #type} doc.
   * @param id to set
   * @param title to set
   */
  public DocStructureItem(String type, String id, String title) {
    super();
    this.type = type;
    this.id = id;
    setTitle(title);
  }

  /**
   * Title
   */
  private String title;

  /**
   * Title normalized to may be used in Confluence Page title
   */
  private String confluencePageTitleBase;

  /**
   * Prefix for Confluence Page Title
   */
  private String confluencePageTitlePrefix;

  /**
   * Type of node, must be same as DocBook element name (book, chapter, appendix) because used in
   * {@link #getDocBookXPath()}! See <code>TYPE_xxx</code> constants.
   */
  private String type;

  /**
   * Id of node
   */
  private String id;

  /**
   * Parent node. <code>null</code> for root node.
   */
  private DocStructureItem parent;

  /**
   * List of child nodes.
   */
  private List<DocStructureItem> childs = new ArrayList<DocStructureItem>();

  /**
   * List of external file refs (http:// etc) in this node
   */
  private List<String> filerefsExternal = new ArrayList<String>();

  /**
   * List of local file refs (go to file in export zip) in this node (so filerefs to disk)
   */
  private List<String> filerefsLocal = new ArrayList<String>();

  /**
   * @return the title
   */
  public String getTitle() {
    return title;
  }

  /**
   * @param title the title to set
   */
  public void setTitle(String title) {
    if (StringUtils.isNotBlank(title)) {
      this.title = title;
      confluencePageTitleBase = ConfluenceUtils.normalizeTitleForConfluence(title);
    }
  }

  @Override
  public String getConfluencePageTitle() {
    if (confluencePageTitleBase == null)
      return null;

    if (confluencePageTitlePrefix == null)
      return confluencePageTitleBase;
    else
      return confluencePageTitlePrefix + "-" + confluencePageTitleBase;
  }

  @Override
  public void setConfluencePageTitlePrefix(String confluencePageTitlePrefix) {
    this.confluencePageTitlePrefix = confluencePageTitlePrefix;
  }

  /**
   * @return the childs
   */
  public List<DocStructureItem> getChilds() {
    return childs;
  }

  /**
   * Add child structure node to this node. {@link #type} must be set on this node!
   * 
   * @param child the child to add.
   */
  public void addChild(DocStructureItem child) {
    if (child.getType() == null) {
      throw new IllegalArgumentException("type must be set in child node");
    }
    child.parent = this;
    this.childs.add(child);
  }

  /**
   * @return the filerefs
   */
  public List<String> getFilerefsExternal() {
    return filerefsExternal;
  }

  /**
   * @return the filerefs
   */
  public List<String> getFilerefsLocal() {
    return filerefsLocal;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id the id to set
   */
  public void setId(String id) {
    if (StringUtils.isNotBlank(id)) {
      this.id = id;
    }
  }

  /**
   * Get type of node. Type must be same as DocBook element name (book, chapter, appendix) because used in
   * {@link #getDocBookXPath()} also!
   * 
   * @return the type
   */
  public String getType() {
    return type;
  }

  /**
   * Set type of node. Type must be same as DocBook element name (book, chapter, appendix) because used in
   * {@link #getDocBookXPath()}!
   * 
   * @param type the type to set
   */
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Get parent node.
   * 
   * @return the parent, may be null for root node
   */
  public DocStructureItem getParent() {
    return parent;
  }

  /**
   * Get root node of tree hierarchy this node is in.
   * 
   * @return root node of tree hierarchy.
   */
  public DocStructureItem getRoot() {
    if (parent == null)
      return this;
    else
      return parent.getRoot();
  }

  /**
   * Add file reference used in this part of DocBook content.
   * 
   * @param fileref to add into list
   * @see #getFilerefsLocal()
   * @see #getFilerefsExternal()
   */
  public void addFileref(String fileref) {
    if (fileref == null || fileref.trim().isEmpty()) {
      throw new IllegalArgumentException("fileref is null or empty");
    }
    int ind = fileref.indexOf("://");
    if (ind > 0 && ind < 10) {
      filerefsExternal.add(fileref);
    } else {
      filerefsLocal.add(fileref);
    }
  }

  /**
   * Get index of given child in me counted for same types.
   * 
   * @param child to count index for
   * @return index of child by type (zero based), -1 means it's not my child.
   */
  public int getChildTypeIndex(DocStructureItem child) {
    int idx = 0;
    for (DocStructureItem myChild : childs) {
      if (myChild == child) {
        return idx;
      } else if (myChild.getType().equals(child.getType())) {
        idx++;
      }
    }
    return -1;
  }

  /**
   * Get XPath for this node item
   * 
   * @param xmlnsPrefix to be used for path, must contain :, may be null
   * @return
   */
  public String getDocBookXPath(String xmlnsPrefix) {
    if (xmlnsPrefix == null) {
      xmlnsPrefix = "";
    }
    if (parent == null) {
      return xmlnsPrefix + type;
    } else {

      return parent.getDocBookXPath(xmlnsPrefix) + "/" + xmlnsPrefix + type + "["
          + (parent.getChildTypeIndex(this) + 1) + "]";
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("<DocStructureItem id='").append(id).append("' type='").append(type).append("' title='").append(title)
        .append("' filerefsLocal='").append(filerefsLocal).append("' filerefsExternal='").append(filerefsExternal);
    if (!childs.isEmpty()) {
      sb.append("'>\n");
      for (DocStructureItem i : childs) {
        sb.append(i);
        sb.append("\n");
      }
      sb.append("</DocStructureItem>");
    } else {
      sb.append("' />");
    }

    return sb.toString();
  }

}
