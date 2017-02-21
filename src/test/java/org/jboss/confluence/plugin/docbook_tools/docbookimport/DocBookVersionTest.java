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
 * Unit test for {@link DocBookVersion}.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DocBookVersionTest {

  @Test
  public void numOfEnumElements() {
    // if you add new element, add it to next tests too please!
    Assert.assertEquals(2, DocBookVersion.values().length);
  }

  @Test
  public void getXmlnsPrefix() {
    Assert.assertEquals("", DocBookVersion.DOCBOOK_4_3.getXmlnsPrefix());
    Assert.assertEquals("d:", DocBookVersion.DOCBOOK_5_0.getXmlnsPrefix());
  }

  @Test
  public void getFilenamePostfix() {
    Assert.assertEquals("_4_3", DocBookVersion.DOCBOOK_4_3.getFilenamePostfix());
    Assert.assertEquals("_5_0", DocBookVersion.DOCBOOK_5_0.getFilenamePostfix());
  }

}
