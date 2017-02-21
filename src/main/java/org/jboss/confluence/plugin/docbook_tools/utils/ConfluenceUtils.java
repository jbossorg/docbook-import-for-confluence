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

import java.util.HashSet;
import java.util.Set;

import com.atlassian.confluence.pages.PageManager;

/**
 * Distinct tools related to confluence.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ConfluenceUtils {

  private static final String CONFLUENCE_TITLE_BAD_CHARS = ":@/\\|^#;[]{}<>$~";

  /**
   * Normalize title so may be used as Confluence Page title
   * 
   * @param titleToNormalize
   * @return normalized title
   */
  public static String normalizeTitleForConfluence(String titleToNormalize) {
    if (titleToNormalize == null)
      return null;

    StringBuilder sb = new StringBuilder();
    for (char ch : titleToNormalize.toCharArray()) {
      if (CONFLUENCE_TITLE_BAD_CHARS.contains(Character.toString(ch))) {
        sb.append("_");
      } else {
        sb.append(ch);
      }

    }
    return sb.toString();
  }

  /**
   * Interface to wrap Confluence PageManager service for
   * {@link ConfluenceUtils#handlePageTitleUniqueness(ConfluencePageTitleUniqEnabled, String, String, PageManagerWrapper)}
   * so we can easily mock it for unit tests.
   * 
   */
  public static interface PageManagerWrapper {
    boolean pageExists(String spaceKey, String title);
  }

  /**
   * Prepare unique titles in given node and all subnodes.
   * 
   * @param nodeToProcess node to prepare unique title for and for all subnodes
   * @param titlePrefixBase base used to generate uniqueness prefix
   * @param spaceKey key for Confluence space to handle page title uniqueness for
   * @param pageManager to be used for current page titles checking. Use {@link PageManager#getPage(String, String)} to
   *          implement it.
   * @throws IllegalStateException if method is not able to find unique title for some page (prefix base not set or
   *           exhausted)
   */
  public static void handlePageTitleUniqueness(ConfluencePageTitleUniqEnabled nodeToProcess, String titlePrefixBase,
      String spaceKey, PageManagerWrapper pageManager) {
    Set<String> documentTitles = new HashSet<String>();
    handlePageTitleUniqueness(nodeToProcess, titlePrefixBase, spaceKey, documentTitles, pageManager);
  }

  private static void handlePageTitleUniqueness(ConfluencePageTitleUniqEnabled nodeToProcess, String titlePrefixBase,
      String spaceKey, Set<String> documentTitles, PageManagerWrapper pageManager) {

    boolean go = true;
    int counter = -1;
    while (go) {
      String title = nodeToProcess.getConfluencePageTitle();
      if (title != null && (documentTitles.contains(title) || pageManager.pageExists(spaceKey, title))) {
        if (titlePrefixBase == null) {
          throw new IllegalStateException(
              "Can't generate unique page title because 'Unique page title prefix base' is not provided.");
        }
        if ((counter > 9 && titlePrefixBase.length() > 2) || (counter > 99)) {
          throw new IllegalStateException(
              "Can't generate unique page title because 'Unique page title prefix base' is exhausted (use another please).");
        }

        String pref = titlePrefixBase;
        if (counter >= 0) {
          pref = titlePrefixBase + counter;
        }
        counter++;

        nodeToProcess.setConfluencePageTitlePrefix(pref);

      } else {
        go = false;
      }
    }
    documentTitles.add(nodeToProcess.getConfluencePageTitle());

    // process all children
    for (ConfluencePageTitleUniqEnabled child : nodeToProcess.getChilds()) {
      handlePageTitleUniqueness(child, titlePrefixBase, spaceKey, documentTitles, pageManager);
    }

  }

}
