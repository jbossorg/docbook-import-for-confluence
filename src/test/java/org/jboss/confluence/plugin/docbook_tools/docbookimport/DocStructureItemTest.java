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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DocStructureItem}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DocStructureItemTest {

  @Test
  public void testFileRefs() {
    DocStructureItem tested = new DocStructureItem();

    Assert.assertEquals(0, tested.getFilerefsExternal().size());
    Assert.assertEquals(0, tested.getFilerefsLocal().size());

    tested.addFileref("http://myfile.com/ddd.gif");
    Assert.assertEquals(1, tested.getFilerefsExternal().size());
    Assert.assertEquals(0, tested.getFilerefsLocal().size());

    tested.addFileref("https://myfile.com/ddd.gif");
    Assert.assertEquals(2, tested.getFilerefsExternal().size());
    Assert.assertEquals(0, tested.getFilerefsLocal().size());

    tested.addFileref("download/ddd.gif");
    Assert.assertEquals(2, tested.getFilerefsExternal().size());
    Assert.assertEquals(1, tested.getFilerefsLocal().size());

    tested.addFileref("author/download/ddd.gif");
    Assert.assertEquals(2, tested.getFilerefsExternal().size());
    Assert.assertEquals(2, tested.getFilerefsLocal().size());

    tested.addFileref("something/download/ddd.gif");
    Assert.assertEquals(2, tested.getFilerefsExternal().size());
    Assert.assertEquals(3, tested.getFilerefsLocal().size());

    try {
      tested.addFileref(null);
      Assert.fail("No exception thrown");
    } catch (IllegalArgumentException e) {

    }

    try {
      tested.addFileref("  ");
      Assert.fail("No exception thrown");
    } catch (IllegalArgumentException e) {

    }

  }

  @Test
  public void testTitle() {
    DocStructureItem tested = new DocStructureItem();
    Assert.assertNull(tested.getTitle());
    tested.setTitle("My Title");
    Assert.assertEquals("My Title", tested.getTitle());

    tested.setTitle("");
    Assert.assertEquals("My Title", tested.getTitle());

    tested.setTitle(null);
    Assert.assertEquals("My Title", tested.getTitle());

  }

  @Test
  public void testId() {
    DocStructureItem tested = new DocStructureItem();
    Assert.assertNull(tested.getId());
    tested.setId("My ID");
    Assert.assertEquals("My ID", tested.getId());

    tested.setId("");
    Assert.assertEquals("My ID", tested.getId());

    tested.setId(null);
    Assert.assertEquals("My ID", tested.getId());
  }

  @Test
  public void testType() {
    DocStructureItem tested = new DocStructureItem();
    Assert.assertNull(tested.getType());
    tested.setType(DocStructureItem.TYPE_BOOK);
    Assert.assertEquals(DocStructureItem.TYPE_BOOK, tested.getType());
  }

  @Test
  public void testConstructors() {
    DocStructureItem tested = new DocStructureItem(DocStructureItem.TYPE_BOOK, "My ID", "My Title");
    Assert.assertEquals(DocStructureItem.TYPE_BOOK, tested.getType());
    Assert.assertEquals("My ID", tested.getId());
    Assert.assertEquals("My Title", tested.getTitle());
  }

  @Test
  public void testToString() {
    DocStructureItem tested = new DocStructureItem(DocStructureItem.TYPE_BOOK, "My ID", "My Title");

    String ts = tested.toString();
    Assert.assertTrue(ts.contains("'My ID'"));

    tested.addChild(new DocStructureItem(DocStructureItem.TYPE_APPENDIX));
    ts = tested.toString();
    Assert.assertTrue(ts.contains("'appendix'"));

  }

  @Test
  public void testChildsTree() {
    DocStructureItem root = new DocStructureItem(DocStructureItem.TYPE_BOOK);

    Assert.assertEquals(0, root.getChilds().size());
    Assert.assertNull(root.getParent());
    Assert.assertEquals(root, root.getRoot());

    DocStructureItem child1 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    root.addChild(child1);
    Assert.assertEquals(1, root.getChilds().size());
    Assert.assertEquals(root, child1.getParent());
    Assert.assertEquals(root, child1.getRoot());

    DocStructureItem child2 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    root.addChild(child2);
    Assert.assertEquals(2, root.getChilds().size());
    Assert.assertEquals(root, child2.getParent());
    Assert.assertEquals(root, child2.getRoot());

    DocStructureItem child22 = new DocStructureItem(DocStructureItem.TYPE_SECTION);
    child2.addChild(child22);
    Assert.assertEquals(2, root.getChilds().size());
    Assert.assertEquals(1, child2.getChilds().size());
    Assert.assertEquals(child2, child22.getParent());
    Assert.assertEquals(root, child22.getRoot());

    try {
      root.addChild(new DocStructureItem());
      Assert.fail("No IllegalArgumentException thrown");
    } catch (IllegalArgumentException e) {
      // OK
    }
  }

  @Test
  public void getDocBookXPath() {

    DocStructureItem tested = new DocStructureItem(DocStructureItem.TYPE_BOOK);
    Assert.assertEquals("book", tested.getDocBookXPath(null));

    DocStructureItem child = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child);
    Assert.assertEquals("book/chapter[1]", child.getDocBookXPath(null));

    DocStructureItem child2 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child2);
    Assert.assertEquals("book/chapter[2]", child2.getDocBookXPath(null));

    DocStructureItem child3 = new DocStructureItem(DocStructureItem.TYPE_APPENDIX);
    tested.addChild(child3);
    Assert.assertEquals("book/appendix[1]", child3.getDocBookXPath(null));

    DocStructureItem child4 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child4);
    Assert.assertEquals("book/chapter[3]", child4.getDocBookXPath(null));

    DocStructureItem child41 = new DocStructureItem(DocStructureItem.TYPE_SECTION);
    child4.addChild(child41);
    Assert.assertEquals("book/chapter[3]/section[1]", child41.getDocBookXPath(null));
    Assert.assertEquals("book/chapter[3]", child4.getDocBookXPath(null));

    DocStructureItem child42 = new DocStructureItem(DocStructureItem.TYPE_SECTION);
    child4.addChild(child42);
    Assert.assertEquals("d:book/d:chapter[3]/d:section[2]", child42.getDocBookXPath("d:"));
    Assert.assertEquals("book/chapter[3]/section[1]", child41.getDocBookXPath(null));
    Assert.assertEquals("book/chapter[3]", child4.getDocBookXPath(null));
  }

  @Test
  public void getChildTypeIndex() {

    DocStructureItem tested = new DocStructureItem(DocStructureItem.TYPE_BOOK);

    DocStructureItem child = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child);
    Assert.assertEquals(0, tested.getChildTypeIndex(child));

    DocStructureItem child2 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child2);
    Assert.assertEquals(1, tested.getChildTypeIndex(child2));

    DocStructureItem child3 = new DocStructureItem(DocStructureItem.TYPE_APPENDIX);
    tested.addChild(child3);
    Assert.assertEquals(0, tested.getChildTypeIndex(child3));

    DocStructureItem child4 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    tested.addChild(child4);
    Assert.assertEquals(2, tested.getChildTypeIndex(child4));

    DocStructureItem nochild = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    Assert.assertEquals(-1, tested.getChildTypeIndex(nochild));

  }

  @Test
  public void getConfluencePageTitle() {
    DocStructureItem tested = new DocStructureItem(DocStructureItem.TYPE_BOOK);
    Assert.assertNull(tested.getConfluencePageTitle());

    tested.setTitle("AAj1j_ff:@/\\|^#;[]{}<>$~");
    Assert.assertEquals("AAj1j_ff________________", tested.getConfluencePageTitle());

    tested.setConfluencePageTitlePrefix("AD");
    Assert.assertEquals("AD-AAj1j_ff________________", tested.getConfluencePageTitle());

    tested.setConfluencePageTitlePrefix(null);
    Assert.assertEquals("AAj1j_ff________________", tested.getConfluencePageTitle());

  }

}
