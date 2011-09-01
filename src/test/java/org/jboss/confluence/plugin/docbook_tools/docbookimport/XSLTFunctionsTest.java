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
 * Unit test for {@link XSLTFunctions}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class XSLTFunctionsTest {

  @Test
  public void sectionTitlePrefixRemove() {

    // leave as is
    Assert.assertEquals("test me", XSLTFunctions.sectionTitlePrefixRemove("test me"));
    Assert.assertEquals("-test me", XSLTFunctions.sectionTitlePrefixRemove("-test me"));
    Assert.assertEquals("-test me-", XSLTFunctions.sectionTitlePrefixRemove("-test me-"));
    Assert.assertEquals("test me-", XSLTFunctions.sectionTitlePrefixRemove("test me-"));
    // too short prefix
    Assert.assertEquals("A-test me-", XSLTFunctions.sectionTitlePrefixRemove("A-test me-"));
    // dash at end
    Assert.assertEquals("ABC-", XSLTFunctions.sectionTitlePrefixRemove("ABC-"));
    // too long prefix
    Assert.assertEquals("ABCDE-tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("ABCDE-tes-t me-"));
    // unwanted characters in prefix
    Assert.assertEquals("AB_D-tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("AB_D-tes-t me-"));

    // remove prefix
    Assert.assertEquals("tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("AB-tes-t me-"));
    Assert.assertEquals("tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("ABC-tes-t me-"));
    Assert.assertEquals("tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("ABCD-tes-t me-"));
    Assert.assertEquals("Not trimmed after prefix removed!", "tes-t me-",
        XSLTFunctions.sectionTitlePrefixRemove("ABCD- tes-t me-"));
    Assert.assertEquals("tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("1BCD-tes-t me-"));
    Assert.assertEquals("tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("123-tes-t me-"));
    Assert.assertEquals("CD-tes-t me-", XSLTFunctions.sectionTitlePrefixRemove("1B-CD-tes-t me-"));
  }

  @Test
  public void escapeWIKICharsInContent() {

    // leave as is
    Assert.assertEquals("", XSLTFunctions.escapeWIKICharsInContent(null));
    Assert.assertEquals("test me", XSLTFunctions.escapeWIKICharsInContent("test me"));

    // bold wiki escape
    Assert.assertEquals("test * me", XSLTFunctions.escapeWIKICharsInContent("test * me"));
    Assert.assertEquals("test* me", XSLTFunctions.escapeWIKICharsInContent("test* me"));
    Assert.assertEquals("test \\*me", XSLTFunctions.escapeWIKICharsInContent("test *me"));
    Assert.assertEquals("test   \\*me", XSLTFunctions.escapeWIKICharsInContent("test   *me"));
    Assert.assertEquals("\\*me and you*", XSLTFunctions.escapeWIKICharsInContent("*me and you*"));
    Assert.assertEquals("test \\*me and you* and others",
        XSLTFunctions.escapeWIKICharsInContent("test *me and you* and others"));
    Assert.assertEquals("test \\*me and you* and \\*others* too",
        XSLTFunctions.escapeWIKICharsInContent("test *me and you* and *others* too"));

    // unordered list wiki escape
    Assert.assertEquals("\\* me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("* me dfd dfdf"));
    Assert.assertEquals("\\* me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("   * me dfd dfdf"));

    // strike wiki escape
    Assert.assertEquals("test - me", XSLTFunctions.escapeWIKICharsInContent("test - me"));
    Assert.assertEquals("test- me", XSLTFunctions.escapeWIKICharsInContent("test- me"));
    Assert.assertEquals("test \\-me", XSLTFunctions.escapeWIKICharsInContent("test -me"));
    Assert.assertEquals("test   \\-me", XSLTFunctions.escapeWIKICharsInContent("test   -me"));
    Assert.assertEquals("\\-me and you-", XSLTFunctions.escapeWIKICharsInContent("-me and you-"));
    Assert.assertEquals("test \\-me and you- and others",
        XSLTFunctions.escapeWIKICharsInContent("test -me and you- and others"));
    Assert.assertEquals("test \\-me and you- and \\-others- too",
        XSLTFunctions.escapeWIKICharsInContent("test -me and you- and -others- too"));

    // unordered list wiki escape 2
    Assert.assertEquals("\\- me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("- me dfd dfdf"));
    Assert.assertEquals("\\- me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("   - me dfd dfdf"));

    // ordered list wiki escape
    Assert.assertEquals("\\# me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("# me dfd dfdf"));
    Assert.assertEquals("\\# me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("   # me dfd dfdf"));
    Assert.assertEquals("aaa # me dfd dfdf", XSLTFunctions.escapeWIKICharsInContent("aaa # me dfd dfdf"));

    // underline wiki escape
    Assert.assertEquals("test + me", XSLTFunctions.escapeWIKICharsInContent("test + me"));
    Assert.assertEquals("test+ me", XSLTFunctions.escapeWIKICharsInContent("test+ me"));
    Assert.assertEquals("test \\+me", XSLTFunctions.escapeWIKICharsInContent("test +me"));
    Assert.assertEquals("test   \\+me", XSLTFunctions.escapeWIKICharsInContent("test   +me"));
    Assert.assertEquals("\\+me and you+", XSLTFunctions.escapeWIKICharsInContent("+me and you+"));
    Assert.assertEquals("test \\+me and you+ and others",
        XSLTFunctions.escapeWIKICharsInContent("test +me and you+ and others"));
    Assert.assertEquals("test \\+me and you+ and \\+others+ too",
        XSLTFunctions.escapeWIKICharsInContent("test +me and you+ and +others+ too"));

    // emphasis wiki escape
    Assert.assertEquals("test _ me", XSLTFunctions.escapeWIKICharsInContent("test _ me"));
    Assert.assertEquals("test_ me", XSLTFunctions.escapeWIKICharsInContent("test_ me"));
    Assert.assertEquals("test \\_me", XSLTFunctions.escapeWIKICharsInContent("test _me"));
    Assert.assertEquals("test   \\_me", XSLTFunctions.escapeWIKICharsInContent("test   _me"));
    Assert.assertEquals("\\_me and you_", XSLTFunctions.escapeWIKICharsInContent("_me and you_"));
    Assert.assertEquals("test \\_me and you_ and others",
        XSLTFunctions.escapeWIKICharsInContent("test _me and you_ and others"));
    Assert.assertEquals("test \\_me and you_ and \\_others_ too",
        XSLTFunctions.escapeWIKICharsInContent("test _me and you_ and _others_ too"));

    // start of link wiki escape
    Assert.assertEquals("test [ me", XSLTFunctions.escapeWIKICharsInContent("test [ me"));
    Assert.assertEquals("test[ me", XSLTFunctions.escapeWIKICharsInContent("test[ me"));
    Assert.assertEquals("test \\[me", XSLTFunctions.escapeWIKICharsInContent("test [me"));
    Assert.assertEquals("test   \\[me", XSLTFunctions.escapeWIKICharsInContent("test   [me"));
    Assert.assertEquals("\\[me and you[", XSLTFunctions.escapeWIKICharsInContent("[me and you["));
    Assert.assertEquals("test \\[me and you] and others",
        XSLTFunctions.escapeWIKICharsInContent("test [me and you] and others"));
    Assert.assertEquals("test \\[me and you] and \\[others] too",
        XSLTFunctions.escapeWIKICharsInContent("test [me and you] and [others] too"));

    // start of emoticon ( wiki escape
    Assert.assertEquals("test ( me", XSLTFunctions.escapeWIKICharsInContent("test ( me"));
    Assert.assertEquals("test( me", XSLTFunctions.escapeWIKICharsInContent("test( me"));
    Assert.assertEquals("test \\(me", XSLTFunctions.escapeWIKICharsInContent("test (me"));
    Assert.assertEquals("test   \\(me", XSLTFunctions.escapeWIKICharsInContent("test   (me"));
    Assert.assertEquals("\\(me and you(", XSLTFunctions.escapeWIKICharsInContent("(me and you("));
    Assert.assertEquals("test \\(me and you) and others",
        XSLTFunctions.escapeWIKICharsInContent("test (me and you) and others"));
    Assert.assertEquals("test \\(me and you) and \\(others) too",
        XSLTFunctions.escapeWIKICharsInContent("test (me and you) and (others) too"));

    // start of macro { wiki escape
    Assert.assertEquals("test { me", XSLTFunctions.escapeWIKICharsInContent("test { me"));
    Assert.assertEquals("test{ me", XSLTFunctions.escapeWIKICharsInContent("test{ me"));
    Assert.assertEquals("test \\{me", XSLTFunctions.escapeWIKICharsInContent("test {me"));
    Assert.assertEquals("test   \\{me", XSLTFunctions.escapeWIKICharsInContent("test   {me"));
    Assert.assertEquals("\\{me and you{", XSLTFunctions.escapeWIKICharsInContent("{me and you{"));
    Assert.assertEquals("test \\{me and you} and others",
        XSLTFunctions.escapeWIKICharsInContent("test {me and you} and others"));
    Assert.assertEquals("test \\{me and you} and \\{others} too",
        XSLTFunctions.escapeWIKICharsInContent("test {me and you} and {others} too"));

    // EOL remove
    Assert.assertEquals("test \\{me me", XSLTFunctions.escapeWIKICharsInContent("test {me \n me"));
  }

  @Test
  public void prepareMonospacedWIKIText() {
    Assert.assertEquals("", XSLTFunctions.prepareMonospacedWIKIText(null));
    Assert.assertEquals("", XSLTFunctions.prepareMonospacedWIKIText(""));
    Assert.assertEquals(" ", XSLTFunctions.prepareMonospacedWIKIText(" "));
    Assert.assertEquals(" {{ahoj}}", XSLTFunctions.prepareMonospacedWIKIText(" ahoj"));
    Assert.assertEquals(" {{ahoj}} ", XSLTFunctions.prepareMonospacedWIKIText(" ahoj "));
    Assert.assertEquals("{{ahoj}} ", XSLTFunctions.prepareMonospacedWIKIText("ahoj "));

    // EOL remove
    Assert.assertEquals("{{ahoj boy}} ", XSLTFunctions.prepareMonospacedWIKIText("ahoj \n boy "));
  }

  @Test
  public void trimWIKIText() {
    Assert.assertEquals("", XSLTFunctions.trimWIKIText(null));
    Assert.assertEquals("", XSLTFunctions.trimWIKIText(""));
    Assert.assertEquals("  ", XSLTFunctions.trimWIKIText("  "));
    Assert.assertEquals("ahoj", XSLTFunctions.trimWIKIText("ahoj"));
    Assert.assertEquals(" ahoj ", XSLTFunctions.trimWIKIText(" ahoj "));
    Assert.assertEquals("ahoj", XSLTFunctions.trimWIKIText("\n   ahoj"));
    Assert.assertEquals("ahoj", XSLTFunctions.trimWIKIText("ahoj\n   "));
    Assert.assertEquals("ahoj", XSLTFunctions.trimWIKIText("\n   ahoj\n   "));
    Assert.assertEquals("ahoj ", XSLTFunctions.trimWIKIText("\n   ahoj \n    "));
    Assert.assertEquals("ahoj jedu ", XSLTFunctions.trimWIKIText("\n   ahoj\n jedu \n    "));
    Assert.assertEquals("ahoj jedu ", XSLTFunctions.trimWIKIText("\n   ahoj\n jedu \n"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.trimWIKIText("\nahoj\n jedu\n"));
    Assert.assertEquals(" ahoj jedu ", XSLTFunctions.trimWIKIText(" ahoj\njedu "));
  }

  @Test
  public void removeAllEOL() {
    Assert.assertEquals("", XSLTFunctions.removeAllEOL(null));
    Assert.assertEquals("", XSLTFunctions.removeAllEOL(""));
    Assert.assertEquals(" ", XSLTFunctions.removeAllEOL(" "));
    Assert.assertEquals("    ", XSLTFunctions.removeAllEOL("    "));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\njedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj    \njedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\n    jedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj  \n     jedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\rjedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj    \rjedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\r    jedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj  \r     jedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\r\njedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj    \r\njedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj\r\n    jedu"));
    Assert.assertEquals("ahoj jedu", XSLTFunctions.removeAllEOL("ahoj  \r\n     jedu"));

    Assert.assertEquals(" fdfsd asfasdf asdfasdf asdfasdf fsadfsadf ertwer ",
        XSLTFunctions.removeAllEOL(" fdfsd\nasfasdf\rasdfasdf\r\n asdfasdf  \nfsadfsadf  \rertwer "));
  }

}
