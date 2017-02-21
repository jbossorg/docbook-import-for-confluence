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

import java.util.List;

/**
 * Interface for value objects holding Confluence page title which need to be made unique by
 * {@link ConfluenceUtils#handlePageTitleUniqueness(ConfluencePageTitleUniqEnabled, String, String, com.atlassian.confluence.pages.PageManager)}
 * .
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 * 
 */
public interface ConfluencePageTitleUniqEnabled {

  /**
   * Get Confluence Page title for this node (normalized, with prefix if set).
   * 
   * @return the confluencePageTitleBase
   * @see #setConfluencePageTitlePrefix(String)
   * @see ConfluenceUtils#normalizeTitleForConfluence(String)
   */
  public String getConfluencePageTitle();

  /**
   * Set prefix for Confluence Page title to make it unique
   * 
   * @param confluencePageTitlePrefix the confluencePageTitlePrefix to set
   * @see #getConfluencePageTitle()
   */
  public void setConfluencePageTitlePrefix(String confluencePageTitlePrefix);

  /**
   * Get list of subpages for this page so we can traverse whole structure.
   * 
   * @return list of subpages, never null
   */
  public List<? extends ConfluencePageTitleUniqEnabled> getChilds();

}