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

import org.apache.log4j.Logger;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link DocbookImporter} for common code not DocBook version dependant.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DocbookImporter_CommonTest extends DocbookImporterTestBase {

	@SuppressWarnings("unused")
	private static final Logger log = Logger.getLogger(DocbookImporter_CommonTest.class);

	@Test
	public void normalizeDocBookXMLFileContent_String() throws Exception {
		DocbookImporter tested = new DocbookImporter();

		// leading <para> tests
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>text</para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>\ntext</para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>\n     text</para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>    \n     text</para>"));
		Assert
				.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para attr=\"cosi\">\ntext</para>"));

		// trailing </para> tests
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>text\n</para>"));
		Assert
				.assertEquals("<para>text      </para>", tested.normalizeDocBookXMLFileContent("<para>\ntext      \n</para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>\n     text\n     </para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>\n     text\n     </ para>"));

		// multiple <para> occurrences tests
		Assert.assertEquals("<para>text</para>\n<para>text2</para>",
				tested.normalizeDocBookXMLFileContent("<para>\n     text\n     </para>\n<para>\n     text2\n     </para>"));
		// <para> subtags test
		Assert
				.assertEquals(
						"<para>text <strong>strongtext</strong> text</para>\n<para>text2</para>",
						tested
								.normalizeDocBookXMLFileContent("<para>\n     text <strong>strongtext</strong> text\n     </para>\n<para>\n     text2\n     </para>"));
		Assert
				.assertEquals(
						"<para>text <parameter>strongtext</parameter> text</para>\n<para>text2</para>",
						tested
								.normalizeDocBookXMLFileContent("<para>\n     text <parameter>strongtext</parameter> text\n     </para>\n<para>\n     text2\n     </para>"));

		// distinct EOL types tests and </ para> test
		Assert.assertEquals("<para>text</para>",
				tested.normalizeDocBookXMLFileContent("<para>\r\n     text\r\n     </ para>"));
		Assert.assertEquals("<para>text</para>", tested.normalizeDocBookXMLFileContent("<para>\r     text\r     </ para>"));

		// leave <para /> as is test
		Assert.assertEquals("text <para />\n test", tested.normalizeDocBookXMLFileContent("text <para />\n test"));
		Assert.assertEquals("text <para/>\n test", tested.normalizeDocBookXMLFileContent("text <para/>\n test"));

		// </programlisting> tests
		Assert
				.assertEquals(
						"<programlisting>text</programlisting>text1a\n<programlisting>\n  text2\n     </programlisting>text2a",
						tested
								.normalizeDocBookXMLFileContent("<programlisting>text</programlisting>\n  text1a\n<programlisting>\n  text2\n     </ programlisting>\n   text2a"));

		// <entry> tests
		Assert
				.assertEquals("<entry>text</entry>\n<entry>text2</entry>", tested
						.normalizeDocBookXMLFileContent("<entry>\n     text\n     </entry>\n<entry>\n     text2\n     </ entry>"));
		Assert
				.assertEquals(
						"<entry>XML Book Component</entry>\n            <entry align=\"center\">Description</entry>\n            <entry>Order of Component in the Book</entry>",
						tested
								.normalizeDocBookXMLFileContent("<entry align=\"center\">\n XML Book Component</entry>\n            <entry align=\"center\">Description</entry>\n            <entry>Order of Component in the Book</entry>"));
		// leave <entry /> as is test
		Assert.assertEquals("text <entry />\n test", tested.normalizeDocBookXMLFileContent("text <entry />\n test"));
		Assert.assertEquals("text <entry/>\n test", tested.normalizeDocBookXMLFileContent("text <entry/>\n test"));

		// <term> tests
		Assert
				.assertEquals(
						"  <term>text2</term>\n   <term>text</term>\n<term>text2</term>",
						tested
								.normalizeDocBookXMLFileContent("  <term>text2</term>\n   <term>\n     text\n     </term>\n<term>\n     text2\n     </ term>"));
		// leave <term /> as is test
		Assert.assertEquals("text <term />\n test", tested.normalizeDocBookXMLFileContent("text <term />\n test"));
		Assert.assertEquals("text <term/>\n test", tested.normalizeDocBookXMLFileContent("text <term/>\n test"));

	}

	@Test
	public void normalizeDocBookXMLFileContent_File() throws Exception {

		File file = File.createTempFile("docbooktools_test", null);
		FileUtils.writeStringContentToFile(file, "<para>\n     text\n     </ para>");

		DocbookImporter tested = new DocbookImporter();

		tested.normalizeDocBookXMLFileContent(file);
		Assert.assertEquals("<para>text</para>", FileUtils.readFileAsString(new FileInputStream(file)));

	}

	@Test
	public void getFilenameFromFilerefLocal() {
		DocbookImporter tested = new DocbookImporter();

		Assert.assertNull(tested.getFilenameFromFilerefLocal(null));
		Assert.assertNull(tested.getFilenameFromFilerefLocal(""));
		Assert.assertEquals("test.gif", tested.getFilenameFromFilerefLocal("test.gif"));
		Assert.assertEquals("test.gif", tested.getFilenameFromFilerefLocal("aa/test.gif"));
		Assert.assertEquals("test.gif", tested.getFilenameFromFilerefLocal("bb/aa/test.gif"));

	}

	@Test
	public void patchWIKIContentReferences() throws Exception {
		DocbookImporter tested = new DocbookImporter();

		DocStructureItem parentStructure = new DocStructureItem();
		parentStructure.setType(DocStructureItem.TYPE_BOOK);

		DocStructureItem chapter1Structure = new DocStructureItem();
		chapter1Structure.setTitle("Chapter 1");
		chapter1Structure.setId("Chapter_1");
		chapter1Structure.setType(DocStructureItem.TYPE_CHAPTER);
		parentStructure.addChild(chapter1Structure);

		String content = "text1\n\n!images/OnlyOneCacheLoader.png|thumbnail,title=figure title!\ntext2\n!a/images/OnlyOneCacheLoader2.png|thumbnail,title=figure title!\n[Chapter_2][Section_21]";

		// first check - nothing changed
		Assert.assertEquals(content, tested.patchWIKIContentReferences(content, chapter1Structure));

		// prepare data for second check to change something
		DocStructureItem chapter2Structure = new DocStructureItem();
		chapter2Structure.setTitle("Chapter 2");
		chapter2Structure.setId("Chapter_2");
		chapter2Structure.setType(DocStructureItem.TYPE_CHAPTER);
		parentStructure.addChild(chapter2Structure);

		chapter1Structure.addFileref("images/OnlyOneCacheLoader.png");
		chapter1Structure.addFileref("a/images/OnlyOneCacheLoader2.png");
		Assert
				.assertEquals(
						"text1\n\n!OnlyOneCacheLoader.png|thumbnail,title=figure title!\ntext2\n!OnlyOneCacheLoader2.png|thumbnail,title=figure title!\n[Chapter 2][Section_21]",
						tested.patchWIKIContentReferences(content, chapter1Structure));

		// text images without titles
		String content2 = "!images/OnlyOneCacheLoader.png! aaa !ertertert|a!";
		Assert.assertEquals("!OnlyOneCacheLoader.png! aaa !ertertert|a!",
				tested.patchWIKIContentReferences(content2, chapter1Structure));

		// prepare data for third check to change something in deeper
		DocStructureItem section21Structure = new DocStructureItem();
		section21Structure.setTitle("Section 21#");
		section21Structure.setId("Section_21");
		section21Structure.setType(DocStructureItem.TYPE_CHAPTER);
		chapter2Structure.addChild(section21Structure);

		Assert
				.assertEquals(
						"text1\n\n!OnlyOneCacheLoader.png|thumbnail,title=figure title!\ntext2\n!OnlyOneCacheLoader2.png|thumbnail,title=figure title!\n[Chapter 2][Section 21#]",
						tested.patchWIKIContentReferences(content, chapter1Structure));

		// test more complex reference links
		content = "text1\n[Title of link|Chapter_2][Section_21]";
		Assert.assertEquals("text1\n[Title of link|Chapter 2][Section 21#]",
				tested.patchWIKIContentReferences(content, chapter1Structure));

		// test no failure when id in some DocStructureItem is null or empty
		content = "text1\n\n!images/OnlyOneCacheLoader.png|thumbnail,title=figure title!\ntext2\n!a/images/OnlyOneCacheLoader2.png|thumbnail,title=figure title!\n[Chapter_2][Section_21]";
		DocStructureItem section22Structure = new DocStructureItem();
		section22Structure.setTitle("Section 22");
		section22Structure.setId(null);
		section22Structure.setType(DocStructureItem.TYPE_CHAPTER);
		chapter2Structure.addChild(section22Structure);
		DocStructureItem section23Structure = new DocStructureItem();
		section23Structure.setTitle("Section 23");
		section23Structure.setId(" ");
		section23Structure.setType(DocStructureItem.TYPE_CHAPTER);
		chapter2Structure.addChild(section23Structure);
		Assert
				.assertEquals(
						"text1\n\n!OnlyOneCacheLoader.png|thumbnail,title=figure title!\ntext2\n!OnlyOneCacheLoader2.png|thumbnail,title=figure title!\n[Chapter 2][Section 21#]",
						tested.patchWIKIContentReferences(content, chapter1Structure));

	}

}
