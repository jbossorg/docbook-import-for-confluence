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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link FileUtils}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com) (C) 2011 Red Hat Inc.
 */
public class FileUtilsTest {

  @Test
  public void readFileAsString() throws Exception {
    String TSC = " test stream content \n errtewr ertwertwer \n fwerw wertwert ";
    Assert.assertEquals(TSC,
        FileUtils.readFileAsString(new ByteArrayInputStream(TSC.getBytes(FileUtils.CHARSET_UTF_8))));
  }

  @Test
  public void copyFileWithReplace() throws Exception {

    String in = "ahoj ${1} ok ${2} no ${1} go";

    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    Properties replacements = new Properties();
    replacements.put("\\$\\{1\\}", "rep laced");
    replacements.put("\\$\\{2\\}", "kk");

    FileUtils.copyFileWithReplace(new ByteArrayInputStream(in.getBytes(FileUtils.CHARSET_UTF_8)), replacements, bos);

    Assert.assertEquals("ahoj rep laced ok kk no rep laced go", bos.toString(FileUtils.CHARSET_UTF_8));

  }

}
