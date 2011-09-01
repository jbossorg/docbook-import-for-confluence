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
import java.io.FileInputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DocbookImporter} for DocBook 5.0 support.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DocbookImporter_DocBook_5_0_Test extends DocbookImporterTestBase {

  private static final Logger log = Logger.getLogger(DocbookImporter_DocBook_5_0_Test.class);

  private static final DocBookVersion TESTED_DOCBOOK_VERSION = DocBookVersion.DOCBOOK_5_0;

  @Test
  public void getDocStructure() throws Exception {

    File srcdir = prepareTestSourceDirectory("docbook-5.0-ok.zip");
    try {

      DocbookImporter tested = new DocbookImporter();
      File inFile = new File(srcdir, "Tree_Cache_Guide.xml");

      DocStructureItem ret = tested.getDocStructure(new FileInputStream(inFile), inFile.toURI().toString(),
          TESTED_DOCBOOK_VERSION);
      Assert.assertEquals("JBoss Cache Tree Cache", ret.getTitle());
      Assert.assertEquals(DocStructureItem.TYPE_BOOK, ret.getType());
      Assert.assertEquals(13, ret.getChilds().size());

      DocStructureItem chapter0 = assertChildDocStructureItem(ret, 0, "Introduction", "Introduction",
          DocStructureItem.TYPE_CHAPTER, 0, 0, 2);
      assertChildDocStructureItem(chapter0, 0, "What is a TreeCache?", "Introduction-What_is_a_TreeCache",
          DocStructureItem.TYPE_SECTION, 0, 0, 0);
      assertChildDocStructureItem(chapter0, 1, "TreeCache Basics", "Introduction-TreeCache_Basics",
          DocStructureItem.TYPE_SECTION, 0, 0, 0);

      assertChildDocStructureItem(ret, 1, "Architecture", "Architecture", DocStructureItem.TYPE_CHAPTER, 1, 0, 0);

      assertChildDocStructureItem(ret, 2, "Basic API", "Basic_API", DocStructureItem.TYPE_CHAPTER, 1, 0, 0);

      DocStructureItem chapter3 = assertChildDocStructureItem(ret, 3, "Clustered Caches", "Clustered_Caches",
          DocStructureItem.TYPE_CHAPTER, 0, 0, 3);
      assertChildDocStructureItem(chapter3, 0, "Local Cache", "Clustered_Caches-Local_Cache",
          DocStructureItem.TYPE_SECTION, 0, 0, 0);
      assertChildDocStructureItem(chapter3, 1, "Clustered Cache - Using Replication",
          "Clustered_Caches-Clustered_Cache___Using_Replication", DocStructureItem.TYPE_SECTION, 1, 0, 0);
      assertChildDocStructureItem(chapter3, 2, "Clustered Cache - Using Invalidation",
          "Clustered_Caches-Clustered_Cache___Using_Invalidation", DocStructureItem.TYPE_SECTION, 0, 0, 0);

      assertChildDocStructureItem(ret, 12, "Running JBoss Cache within JBoss Application Server",
          "Running_JBoss_Cache_within_JBoss_Application_Server", DocStructureItem.TYPE_APPENDIX, 0, 0, 1);

      // System.out.println(ret);
    } finally {
      FileUtils.deleteDirectoryRecursively(srcdir);
    }
  }

  @Test
  public void findMainDocBookBookFile() throws Exception {
    File srcdir = prepareTestSourceDirectory("docbook-5.0-ok.zip");
    try {

      DocbookImporter tested = new DocbookImporter();

      File ret = tested.findMainDocBookBookFile(srcdir);

      Assert.assertNotNull(ret);
      Assert.assertEquals("Tree_Cache_Guide.xml", ret.getName());

    } finally {
      FileUtils.deleteDirectoryRecursively(srcdir);
    }
  }

  @Test
  public void validateImageFilesExists() throws Exception {
    File srcdir = prepareTestSourceDirectory("docbook-5.0-ok.zip");
    File srcdir2 = prepareTestSourceDirectory("docbook-5.0-badimagereferences.zip");
    try {

      DocbookImporter tested = new DocbookImporter();

      File inFile = new File(srcdir, "Tree_Cache_Guide.xml");
      DocStructureItem docToImport = tested.getDocStructure(new FileInputStream(inFile), inFile.toURI().toString(),
          TESTED_DOCBOOK_VERSION);

      List<String> messages = new ArrayList<String>();
      tested.validateImageFilesExists(docToImport, srcdir, messages);

      Assert.assertTrue(messages.isEmpty());

      inFile = new File(srcdir2, "Tree_Cache_Guide.xml");
      docToImport = tested.getDocStructure(new FileInputStream(inFile), inFile.toURI().toString(),
          TESTED_DOCBOOK_VERSION);

      messages = new ArrayList<String>();
      tested.validateImageFilesExists(docToImport, srcdir2, messages);

      Assert.assertFalse(messages.isEmpty());
      Assert.assertEquals(1, messages.size());

    } finally {
      FileUtils.deleteDirectoryRecursively(srcdir);
      FileUtils.deleteDirectoryRecursively(srcdir2);
    }
  }

  @Test
  public void prepareChapterWIKIContent_chapterIndexingAndNoTitleTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/chapterIndexingAndNoTitleTest.xml");
    DocStructureItem chapter1 = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapter1, TESTED_DOCBOOK_VERSION);
    Assert.assertFalse(ret.contains("Chapter 1 title"));
    Assert.assertTrue(ret.contains("Chapter 1 content"));
    Assert.assertTrue(ret.contains("Section 1.1 title"));
    Assert.assertTrue(ret.contains("Section 1.1 content"));
    Assert.assertTrue(ret.contains("Section 1.2 title"));
    Assert.assertTrue(ret.contains("Section 1.2 content"));

    DocStructureItem chapter2 = new DocStructureItem(DocStructureItem.TYPE_CHAPTER);
    chapter1.getParent().addChild(chapter2);

    ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapter2, TESTED_DOCBOOK_VERSION);
    Assert.assertFalse(ret.contains("Chapter 2 title"));
    Assert.assertTrue(ret.contains("Chapter 2 content"));
    Assert.assertTrue(ret.contains("Section 2.1 title"));
    Assert.assertTrue(ret.contains("Section 2.1 content"));
    Assert.assertTrue(ret.contains("Section 2.2 title"));
    Assert.assertTrue(ret.contains("Section 2.2 content"));

    // test subsections
    DocStructureItem section11 = new DocStructureItem(DocStructureItem.TYPE_SECTION);
    chapter1.addChild(section11);
    DocStructureItem section12 = new DocStructureItem(DocStructureItem.TYPE_SECTION);
    chapter1.addChild(section12);
    ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapter1, TESTED_DOCBOOK_VERSION);
    Assert.assertFalse(ret.contains("Chapter 1 title"));
    Assert.assertTrue(ret.contains("Chapter 1 content"));
    Assert.assertFalse(ret.contains("Section 1.1 title"));
    Assert.assertFalse(ret.contains("Section 1.1 content"));
    Assert.assertFalse(ret.contains("Section 1.2 title"));
    Assert.assertFalse(ret.contains("Section 1.2 content"));
    ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), section11, TESTED_DOCBOOK_VERSION);
    Assert.assertFalse(ret.contains("Chapter 1 title"));
    Assert.assertFalse(ret.contains("Chapter 1 content"));
    Assert.assertFalse(ret.contains("Section 1.1 title"));
    Assert.assertFalse(ret.contains("h1."));
    Assert.assertTrue(ret.contains("Section 1.1 content"));
    Assert.assertFalse(ret.contains("Section 1.2 title"));
    Assert.assertFalse(ret.contains("Section 1.2 content"));
    ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), section12, TESTED_DOCBOOK_VERSION);
    Assert.assertFalse(ret.contains("Chapter 1 title"));
    Assert.assertFalse(ret.contains("Chapter 1 content"));
    Assert.assertFalse(ret.contains("Section 1.1 title"));
    Assert.assertFalse(ret.contains("Section 1.1 content"));
    Assert.assertFalse(ret.contains("Section 1.2 title"));
    Assert.assertFalse(ret.contains("h1."));
    Assert.assertTrue(ret.contains("Section 1.2 content"));

  }

  @Test
  public void prepareChapterWIKIContent_basicTextFormatingTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/basicTextFormatingTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    // we also test <para> here so double EOL after all sentences and other EOL removed from text!
    Assert.assertTrue(ret.contains("test with *bold* and *bold2* text at the end\n\n"));
    Assert.assertTrue(ret.contains("test with _italics_ and _italics2_ and _italics3_ text"));
    Assert.assertTrue(ret.contains("test with +underline+ and +underline2+ text"));
    Assert.assertTrue(ret.contains("test with -striked- text"));
    Assert.assertTrue(ret.contains("test with ^super script^ and ~sub script~ text"));
    Assert.assertTrue(ret.contains("test with {{code monospaced}} text"));
    // test no nesting of {{ }} pairs
    Assert.assertTrue(ret.contains("test with {{code}} {{filename}} {{monospaced}} text"));
    Assert.assertTrue(ret.contains("test with ??quoted?? text"));
    Assert.assertTrue(ret.contains("test with {quote}block quoted{quote}\n text"));
    // test no * escaping for noformat too
    Assert.assertTrue(ret.contains("test with {noformat}text in *screen* node{noformat}\n text"));
    Assert.assertTrue(ret.contains("h6. formalpara title\nformalpara content\n\n"));

  }

  @Test
  public void prepareChapterWIKIContent_sectionsToHeaderTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/sectionsToHeaderTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert.assertTrue(ret.contains("\nh1. Section 1 title\n"));
    Assert.assertTrue(ret.contains("\nh2. Section 1.1 title\n"));
    Assert.assertTrue(ret.contains("\nh2. Section 1.2 title\n"));
    Assert.assertTrue(ret.contains("\nh3. Section 1.2.1 title\n"));
    Assert.assertTrue(ret.contains("\nh2. Section 1.3 title\n"));
    Assert.assertTrue(ret.contains("\nh1. Section 2 title\n"));
  }

  @Test
  public void prepareChapterWIKIContent_programListingTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/programListingTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    // we also test special characters not escaped by presence of * in text!
    Assert.assertTrue(ret.contains("{code}\n    test program *\n    listing 1\n  {code}"));
    Assert.assertTrue(ret.contains("{code:lang=xml}\n    test program *\n    listing 2\n  {code}"));
    // we also test other text in example outside programlisting
    Assert
        .assertTrue(ret
            .contains("{code:lang=java|title=test program listing 3 title}\n      test program *\n      listing 3\n    {code}\nOther text in para *inside* example"));
    // we also test remove of other formating tags here
    Assert
        .assertTrue(ret
            .contains("{code:title=test program listing 4 title}\n      test program *\n      listing 4\n      test with bold and bold2 text in programlisting\n   {code}"));
  }

  @Test
  public void prepareChapterWIKIContent_admonitionsTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/admonitionsTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert.assertTrue(ret.contains("{warning}test warning no title\n\n{warning}"));
    Assert.assertTrue(ret.contains("{warning:title=test warning title}test warning with title\n\n{warning}"));

    Assert.assertTrue(ret.contains("{info}test important no title\n\n{info}"));
    Assert.assertTrue(ret.contains("{info:title=test important title}test important with title\n\n{info}"));
    Assert.assertTrue(ret.contains("{info} test important without para {info}"));

    Assert.assertTrue(ret.contains("{note}test note no title\n\n{note}"));
    Assert.assertTrue(ret.contains("{note:title=test note title}test note with title\n\n{note}"));

    Assert.assertTrue(ret.contains("{tip}test tip no title\n\n{tip}"));
    Assert.assertTrue(ret.contains("{tip:title=test tip title}test tip with title\n\n{tip}"));
  }

  @Test
  public void prepareChapterWIKIContent_listsTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/listsTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "para 1\n\n# item 1\n# item2 {code}\n  item2code\n{code}\nitem2aftercode\n# item3 {code}\n  item3code\n{code}\n\nitem3aftercodenewpara\nitem3aftercode2\n# item4\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_listsTestBasic() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/listsTestBasic.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "Bulleted list:\n\n* Item 1\n* Item 2\n* Item 3\n* Item 4\nNumbered list: \n# Item 1\n# Item 2\n# Item 3\n# Item 4\n\n\nNested lists:\n\n* Item 1\n** Item 11\n** Item 12\n** Item 13\n* Item 2\n*# Item 21\n*# Item 22\n*# Item 23\n* Item 3\n* Item 4\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_tableTestBasic() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/tableTestBasic.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "||Column 1 header||Column 2 header||Column 3 header||Column 4 header||\n|Read|X|X|X|\n|Write|*Bold text*|_Italic text_|X|\n|Export|||X|\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_tableTestBasic2() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/tableTestBasic2.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "||Column 1 header||Column 2 header||Column 3 header||Column 4 header||\n|Read|X|X|X|\n|Write|*Bold text*|_Italic text_|X|\n|Export|||X|\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_linkExternalTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/linkExternalTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "some 1 link [http://www.jboss.org] go\n\nsome 2 link [link *strong* text|http://www.jboss.org] go\n\nsome 3 link [link href|http://www.jboss.org] go\n\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_linkInternalTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/linkInternalTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert.assertEquals(
        "some 1 link [id-LinkPage] go\n\nsome 2 link [id-LinkPage2] go\n\nsome 3 link [go|id-LinkPage3]\n\n", ret);
  }

  @Test
  public void prepareChapterWIKIContent_imageTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/imageTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "text1\n\n!images/OnlyOneCacheLoader.png|title=figure title!\ntext2\n\n!images/OnlyOneCacheLoader.png|title=The Arquillian test infrastructure!\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_variablelistTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/variablelistTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "h6. Varlist title\n* {{publican.cfg}}\n{quote}Ensure you {{BRAND}} defined.\n{quote}\n* All XML files in the {{en\\-US}} directory\n{quote}Ensure you {{DOCTYPE}} defined.\npara 2 text\n{quote}\n",
            ret);
  }

  @Test
  public void prepareChapterWIKIContent_procedureTest() throws Exception {

    DocbookImporter tested = new DocbookImporter();

    URL url = getTestResourceFileURL("chapterwiki_docbook_5_0/procedureTest.xml");
    tested.normalizeDocBookXMLFileContent(new File(url.toURI()));

    DocStructureItem chapterInfo = prepareTestChapter1Info();
    String ret = tested.prepareNodeWIKIContent(url.openStream(), url.toString(), chapterInfo, TESTED_DOCBOOK_VERSION);
    log.debug(ret);
    Assert
        .assertEquals(
            "h6. procedure Title\n# *Step 1 Title*\nStep one info 1\nStep one info 2\n*formalpara title*\nformalpara content\n# *Step 2 Title*\nStep two *info* 1\nStep two *info* 2:\n## Sub\\-step one.\n## Sub\\-step two, etc.\n",
            ret);
  }

  @Test
  public void completeParsingTest() throws Exception {

    boolean internal = true;

    File srcdir = null;
    File inFile = null;

    if (internal) {
      srcdir = prepareTestSourceDirectory("docbook-5.0-ok.zip");
      inFile = new File(srcdir, "Tree_Cache_Guide.xml");
    } else {
      srcdir = new File("/home/velias/Tmp/Scroll_NewVersion/extracted");
      inFile = new File(srcdir, "docbook.xml");
    }

    try {
      DocbookImporter tested = new DocbookImporter();

      tested.normalizeAllDocBookXMLFilesContent(srcdir);

      DocStructureItem docToImport = tested.getDocStructure(new FileInputStream(inFile), inFile.toURI().toString(),
          TESTED_DOCBOOK_VERSION);
      log.debug(docToImport);

      completeParsingTestStruct(tested, docToImport, inFile);

    } finally {
      if (internal) {
        FileUtils.deleteDirectoryRecursively(srcdir);
      }
    }
  }

  protected void completeParsingTestStruct(DocbookImporter tested, DocStructureItem docToImport, File inFile)
      throws Exception {
    if (docToImport.getChilds() != null) {
      for (DocStructureItem chapterInfo : docToImport.getChilds()) {
        String c = tested.prepareNodeWIKIContent(inFile, chapterInfo, TESTED_DOCBOOK_VERSION);
        log.debug("Content for " + chapterInfo.getType() + "#" + chapterInfo.getId() + " is: " + c);
        completeParsingTestStruct(tested, chapterInfo, inFile);
      }
    }
  }

}
