<?xml version="1.0" encoding="UTF-8"?>
<!--
  ~ JBoss, Home of Professional Open Source.
  ~ Copyright 2011, Red Hat, Inc., and individual contributors
  ~ as indicated by the @author tags.
  ~
  ~ This is free software; you can redistribute it and/or modify it
  ~ under the terms of the GNU Lesser General Public License as
  ~ published by the Free Software Foundation; either version 2.1 of
  ~ the License, or (at your option) any later version.
  ~
  ~ This software is distributed in the hope that it will be useful,
  ~ but WITHOUT ANY WARRANTY; without even the implied warranty of
  ~ MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  ~ Lesser General Public License for more details.
  ~
  ~ You should have received a copy of the GNU Lesser General Public
  ~ License along with this software; if not, write to the Free
  ~ Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  ~ 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  -->
<xsl:stylesheet version="1.0" 
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
  xmlns:d="http://docbook.org/ns/docbook"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
  xsi:schemaLocation="http://docbook.org/ns/docbook http://docbook.org/xml/5.0/xsd/docbook.xsd"
  exclude-result-prefixes="d"
  >

  <xsl:output method="xml" indent="yes" encoding="UTF-8" />
  
  <xsl:template match="/">
    <xsl:apply-templates select="d:book" />
  </xsl:template>

  <!-- print book - title and subsections -->  
  <xsl:template match="d:book">
  <node>
    <type><xsl:text>book</xsl:text></type>
    <xsl:if test="d:bookinfo/d:title"><title><xsl:value-of select="d:bookinfo/d:title" /></title></xsl:if>
    <xsl:if test="d:info/d:title"><title><xsl:value-of select="d:info/d:title" /></title></xsl:if>
    <xsl:apply-templates select="d:chapter" />
    <xsl:apply-templates select="d:appendix" />
  </node>  
  </xsl:template>

  <!-- print chapter titles and picture refs inside  -->
  <xsl:template match="d:book/d:chapter">
    <node>
    <type><xsl:text>chapter</xsl:text></type>
    <title><xsl:value-of select="d:title" /></title>
    <xsl:if test="@id"><id><xsl:value-of select="@id" /></id></xsl:if>
    <xsl:if test="@xml:id"><id><xsl:value-of select="@xml:id" /></id></xsl:if>
    <xsl:apply-templates />
    </node>
  </xsl:template>

  <!-- print appendix titles and picture refs inside  -->
  <xsl:template match="d:book/d:appendix">
    <node>
    <type><xsl:text>appendix</xsl:text></type>
    <title><xsl:value-of select="d:title" /></title>
    <xsl:if test="@id"><id><xsl:value-of select="@id" /></id></xsl:if>
    <xsl:if test="@xml:id"><id><xsl:value-of select="@xml:id" /></id></xsl:if>
    <xsl:apply-templates />
    </node>
  </xsl:template>
  
  <!-- print first level section in chapter titles and picture refs inside  -->
  <xsl:template match="d:book/d:chapter/${1}d:section">
    <sectnode>
    <type><xsl:text>section</xsl:text></type>
    <title><xsl:value-of select="d:title" /></title>
    <xsl:if test="@id"><id><xsl:value-of select="@id" /></id></xsl:if>
    <xsl:if test="@xml:id"><id><xsl:value-of select="@xml:id" /></id></xsl:if>
    <label><xsl:value-of select="@remap" /></label>
    <xsl:apply-templates />
    </sectnode>
  </xsl:template>

  <!-- print first level section in appendix titles and picture refs inside  -->
  <xsl:template match="d:book/d:appendix/${1}d:section">
    <sectnode>
    <type><xsl:text>section</xsl:text></type>
    <title><xsl:value-of select="d:title" /></title>
    <xsl:if test="@id"><id><xsl:value-of select="@id" /></id></xsl:if>
    <xsl:if test="@xml:id"><id><xsl:value-of select="@xml:id" /></id></xsl:if>
    <label><xsl:value-of select="@remap" /></label>
    <xsl:apply-templates />
    </sectnode>
  </xsl:template>

  <!-- print filerefs -->
  <xsl:template match="*[@fileref]">
    <fileref><xsl:value-of select="@fileref" /></fileref>
  </xsl:template>
  
  <!-- ignore all others elements, only apply templates so subelements are resolved -->
  <xsl:template match="*">
    <xsl:apply-templates />
  </xsl:template>
  
  <!-- print no text elements -->
  <xsl:template match="*/text()" >
  </xsl:template>
  
  <!-- filter out processing instructions if any -->
  <xsl:template match="processing-instruction()" />
  
  <!-- filter out comments if any -->
  <xsl:template match="comment()" />

</xsl:stylesheet>
