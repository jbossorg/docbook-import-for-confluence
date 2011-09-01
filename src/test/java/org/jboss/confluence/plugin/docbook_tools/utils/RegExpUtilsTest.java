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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit test for {@link RegExpUtils}
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class RegExpUtilsTest {

  @Test
  public void escapeTextForRegexp() {

    Assert.assertEquals("", RegExpUtils.escapeTextForRegexp(null));
    Assert.assertEquals("", RegExpUtils.escapeTextForRegexp(""));
    Assert.assertEquals(" ", RegExpUtils.escapeTextForRegexp(" "));
    Assert.assertEquals("aa\\.bb", RegExpUtils.escapeTextForRegexp("aa.bb"));
    Assert.assertEquals("aa\\.bb\\.cc", RegExpUtils.escapeTextForRegexp("aa.bb.cc"));
    Assert.assertEquals("aa\\*bb\\*cc", RegExpUtils.escapeTextForRegexp("aa*bb*cc"));
    Assert.assertEquals("aa\\?bb\\?cc", RegExpUtils.escapeTextForRegexp("aa?bb?cc"));
    Assert.assertEquals("aa\\+bb\\+cc", RegExpUtils.escapeTextForRegexp("aa+bb+cc"));
    Assert.assertEquals("aa\\\\bb\\\\cc", RegExpUtils.escapeTextForRegexp("aa\\bb\\cc"));
    Assert.assertEquals("aa\\_bb\\_cc", RegExpUtils.escapeTextForRegexp("aa_bb_cc"));
  }

}
