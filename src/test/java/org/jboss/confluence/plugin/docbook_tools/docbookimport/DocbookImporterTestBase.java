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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.apache.log4j.Logger;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.junit.Assert;

/**
 * Base class for Unit tests for {@link DocbookImporter}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public abstract class DocbookImporterTestBase {

  @SuppressWarnings("unused")
  private static final Logger log = Logger.getLogger(DocbookImporterTestBase.class);

  /**
   * Base folder to load test resources from
   */
  protected static final String TEST_RESOURCES_BASE_FOLDER = "/docbookimport/";

  /**
   * Get URL of test resource.
   * 
   * @param name of resource. Relative to {@link #TEST_RESOURCES_BASE_FOLDER} value {@value #TEST_RESOURCES_BASE_FOLDER}
   * @return URL to resource
   */
  protected URL getTestResourceFileURL(String name) {
    return getClass().getResource(TEST_RESOURCES_BASE_FOLDER + name);
  }

  /**
   * Get InputStream of test resource file.
   * 
   * @param name of file. Relative to {@link #TEST_RESOURCES_BASE_FOLDER} value {@value #TEST_RESOURCES_BASE_FOLDER}
   * @return URL to resource
   */
  protected InputStream getTestResourceFileInputStream(String name) throws IOException {
    return getTestResourceFileURL(name).openStream();
  }

  /**
   * Prepare working directory with content ezpanded from given ZIP file to be used as input for tests.
   * 
   * @param filename subpath to ZIP file with content for tests. Relative to {@link #TEST_RESOURCES_BASE_FOLDER} value
   *          {@value #TEST_RESOURCES_BASE_FOLDER}.
   * 
   * @return directory with expanded content for tests
   */
  protected File prepareTestSourceDirectory(String filename) throws Exception {
    File dir = FileUtils.prepareWorkingDirectory("testsource-");
    FileUtils.unzip(getTestResourceFileInputStream(filename), dir);
    return dir;
  }

  /**
   * Search for child DocStructureItem in given root by index, and then assert it exists with given values.
   * 
   * @param parent DocStructureItem to search for child in
   * @param childIndex index of child in parent to be asserted
   * @param assertTitle
   * @param assertId
   * @param assertType
   * @param assertNumberOfFilerefsLocal
   * @param assertNumberOfFilerefsExternal
   * @param assertNumberOfSubChilds
   * 
   * @return asserted child DocStructureItem so asserting may be chained
   */
  protected DocStructureItem assertChildDocStructureItem(DocStructureItem parent, int childIndex, String assertTitle,
      String assertId, String assertType, int assertNumberOfFilerefsLocal, int assertNumberOfFilerefsExternal,
      int assertNumberOfSubChilds) {
    DocStructureItem childToAssert = parent.getChilds().get(childIndex);
    Assert.assertNotNull("Child not found at index " + childIndex + " in " + parent, childToAssert);
    Assert.assertEquals("Bad parent field in child at index " + childIndex + " in " + parent, parent,
        childToAssert.getParent());
    Assert.assertEquals("Bad title for child at index " + childIndex + " in " + parent, assertTitle,
        childToAssert.getTitle());
    Assert.assertEquals("Bad id for child at index " + childIndex + " in " + parent, assertId, childToAssert.getId());
    Assert.assertEquals("Bad type for child at index " + childIndex + " in " + parent, assertType,
        childToAssert.getType());
    Assert.assertEquals("Bad number of filerefsLocal for child at index " + childIndex + " in " + parent,
        assertNumberOfFilerefsLocal, childToAssert.getFilerefsLocal().size());
    Assert.assertEquals("Bad number of filerefsExternal for child at index " + childIndex + " in " + parent,
        assertNumberOfFilerefsExternal, childToAssert.getFilerefsExternal().size());
    Assert.assertEquals("Bad number of subchilds for child at index " + childIndex + " in " + parent,
        assertNumberOfSubChilds, childToAssert.getChilds().size());
    return childToAssert;
  }

  /**
   * Prepare root of DocStructureItem tree with one book and one chapter in it.
   * 
   * @return DocStructureItem representing chapter 1.
   */
  protected DocStructureItem prepareTestChapter1Info() {
    DocStructureItem book = new DocStructureItem(DocStructureItem.TYPE_BOOK);
    DocStructureItem chapter = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    book.addChild(chapter);
    return chapter;
  }

}
