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

import org.jboss.confluence.plugin.docbook_tools.docbookimport.DocStructureItem;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link ConfluenceUtils}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class ConfluenceUtilsTest {

  @Test
  public void normalizeTitleForConfluence() {
    Assert.assertEquals("AAj1j_ff", ConfluenceUtils.normalizeTitleForConfluence("AAj1j_ff"));
    Assert.assertEquals("AAj1j_ff________________",
        ConfluenceUtils.normalizeTitleForConfluence("AAj1j_ff:@/\\|^#;[]{}<>$~"));
  }

  ConfluenceUtils.PageManagerWrapper pageManagerMock = new ConfluenceUtils.PageManagerWrapper() {

    @Override
    public boolean pageExists(String spaceKey, String title) {
      return "title 3".equals(title);
    }

  };

  @Test
  public void handlePageTitleUniqueness() {

    // no duplicity test
    DocStructureItem nodeToProcess = new DocStructureItem(DocStructureItem.TYPE_BOOK, "0", null);
    nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "1", "title 1"));
    nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "2", "title 2"));

    ConfluenceUtils.handlePageTitleUniqueness(nodeToProcess, null, "SKEY", pageManagerMock);
    Assert.assertEquals("title 1", nodeToProcess.getChilds().get(0).getConfluencePageTitle());
    Assert.assertEquals("title 2", nodeToProcess.getChilds().get(1).getConfluencePageTitle());

    // duplicity tests
    nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "1", "title 1"));
    nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "3", "title 3"));
    nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "2", "title 1"));

    // no prefix available
    try {
      ConfluenceUtils.handlePageTitleUniqueness(nodeToProcess, null, "SKEY", pageManagerMock);
      Assert.fail("must fail due duplicity and not prefix base");
    } catch (IllegalStateException e) {
      // NTD
    }

    // with prefix all ok
    ConfluenceUtils.handlePageTitleUniqueness(nodeToProcess, "PB", "SKEY", pageManagerMock);
    Assert.assertEquals("title 1", nodeToProcess.getChilds().get(0).getConfluencePageTitle());
    Assert.assertEquals("title 2", nodeToProcess.getChilds().get(1).getConfluencePageTitle());
    Assert.assertEquals("PB-title 1", nodeToProcess.getChilds().get(2).getConfluencePageTitle());
    Assert.assertEquals("PB-title 3", nodeToProcess.getChilds().get(3).getConfluencePageTitle());
    Assert.assertEquals("PB0-title 1", nodeToProcess.getChilds().get(4).getConfluencePageTitle());

    // prefix exhausted exception
    nodeToProcess = new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "0", null);
    for (int i = 0; i <= 12; i++) {
      nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "1", "title 1"));
    }
    try {
      ConfluenceUtils.handlePageTitleUniqueness(nodeToProcess, "PRE", "SKEY", pageManagerMock);
      Assert.fail("must fail due exhausted prefix base");
    } catch (IllegalStateException e) {
      // NTD
    }
    nodeToProcess = new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "0", null);
    for (int i = 0; i <= 102; i++) {
      nodeToProcess.addChild(new DocStructureItem(DocStructureItem.TYPE_CHAPTER, "1", "title 1"));
    }
    try {
      ConfluenceUtils.handlePageTitleUniqueness(nodeToProcess, "PR", "SKEY", pageManagerMock);
      Assert.fail("must fail due exhausted prefix base");
    } catch (IllegalStateException e) {
      // NTD
    }

  }
}
