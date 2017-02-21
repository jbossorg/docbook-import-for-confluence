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
                xmlns:java="http://xml.apache.org/xalan/java"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:d="http://docbook.org/ns/docbook"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xsi:schemaLocation="http://docbook.org/ns/docbook http://docbook.org/xml/5.0/xsd/docbook.xsd"
  >

  <xsl:output method="text" indent="no" omit-xml-declaration="yes" />
  
  <xsl:strip-space elements="*" />

  <!-- render only specified chapter, appendix or section -->
  <xsl:template match="/">
    <xsl:apply-templates select="${1}" />
  </xsl:template>

  <xsl:template match="d:section/d:title" >
    <xsl:apply-templates /><xsl:text>
</xsl:text>
  </xsl:template>

  <!-- filter out title because used in separate field, not in content -->
  <xsl:template match="${1}/d:title" />

<!-- #############  basic text formating #################### -->
  <xsl:template match="d:emphasis[@role='strong']|d:bold">
    <xsl:text>*</xsl:text><xsl:apply-templates /><xsl:text>*</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:emphasis[@role='underline']|d:underline">
    <xsl:text>+</xsl:text><xsl:apply-templates /><xsl:text>+</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:emphasis[@role='strikethrough']">
    <xsl:text>-</xsl:text><xsl:apply-templates /><xsl:text>-</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:emphasis[@role='italics']|d:italics|d:emphasis">
    <xsl:text>_</xsl:text><xsl:apply-templates /><xsl:text>_</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:superscript">
    <xsl:text>^</xsl:text><xsl:apply-templates /><xsl:text>^</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:subscript">
    <xsl:text>~</xsl:text><xsl:apply-templates /><xsl:text>~</xsl:text>
  </xsl:template>
    
  <xsl:template match="d:screen">
    <xsl:text>{noformat}</xsl:text><xsl:apply-templates /><xsl:text>{noformat}
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:quote">
    <xsl:text>??</xsl:text><xsl:apply-templates /><xsl:text>??</xsl:text>
  </xsl:template>
  
   <xsl:template match="d:blockquote">
    <xsl:text>{quote}</xsl:text><xsl:apply-templates /><xsl:text>{quote}
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:formalpara/d:title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>
  
<!-- ############# sections to headers #################### -->
  <!-- filter out section info because not used-->
  <xsl:template match="d:section/d:info" />

  <xsl:template match="${1}/d:section">
    <xsl:text>
h1. </xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="${1}/d:section/d:section">
    <xsl:text>
h2. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/d:section/d:section/d:section">
    <xsl:text>
h3. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/d:section/d:section/d:section/d:section">
    <xsl:text>
h4. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/d:section/d:section/d:section/d:section/d:section">
    <xsl:text>
h5. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/d:section/d:section/d:section/d:section/d:section/d:section">
    <xsl:text>
h6. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <!-- filter out subsections in some cases -->
  ${2}


<!-- ############# program listings #################### -->

  <xsl:template match="d:example">
    <xsl:apply-templates>
      <xsl:with-param name="title" select="d:title"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- skip example title node because printed inside programlisting handler --> 
  <xsl:template match="d:example/d:title"/>
  

  <xsl:template match="d:programlisting[@language]">
    <xsl:param name="title"/>
    <xsl:text>{code:lang=</xsl:text><xsl:value-of select="@language"/>
    <xsl:if test="$title"><xsl:text>|title=</xsl:text><xsl:value-of select="$title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates />
    <xsl:text>{code}
</xsl:text>
  </xsl:template>

  <xsl:template match="d:programlisting">
    <xsl:param name="title"/>
    <xsl:text>{code</xsl:text>
    <xsl:if test="$title"><xsl:text>:title=</xsl:text><xsl:value-of select="$title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates />
    <xsl:text>{code}
</xsl:text>
  </xsl:template>

<!-- ############# Admonitions #################### -->
  <xsl:template match="d:warning">
    <xsl:text>{warning</xsl:text>
    <xsl:if test="d:title"><xsl:text>:title=</xsl:text><xsl:value-of select="d:title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{warning}
</xsl:text>
  </xsl:template>

  <xsl:template match="d:important">
    <xsl:text>{info</xsl:text>
    <xsl:if test="d:title"><xsl:text>:title=</xsl:text><xsl:value-of select="d:title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{info}
</xsl:text>
  </xsl:template>

<xsl:template match="d:note">
    <xsl:text>{note</xsl:text>
    <xsl:if test="d:title"><xsl:text>:title=</xsl:text><xsl:value-of select="d:title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{note}
</xsl:text>
  </xsl:template>  

<xsl:template match="d:tip">
    <xsl:text>{tip</xsl:text>
    <xsl:if test="d:title"><xsl:text>:title=</xsl:text><xsl:value-of select="d:title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{tip}
</xsl:text>
  </xsl:template>
  
  <!-- filter out title because used in separate field -->
  <xsl:template match="d:warning/d:title|d:important/d:title|d:note/d:title|d:tip/d:title" />

<!-- ############# lists #################### -->

<!-- list in para mainly starts on same row as some text, so we need to add row here so first list item starts on new line -->
  <xsl:template match="d:para/d:itemizedlist">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="d:para/d:orderedlist">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:itemizedlist/d:listitem">
    <xsl:text>* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:orderedlist/d:listitem">
    <xsl:text># </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:itemizedlist/d:listitem/d:itemizedlist/d:listitem">
    <xsl:text>** </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:orderedlist/d:listitem/d:orderedlist/d:listitem">
    <xsl:text>## </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:itemizedlist/d:listitem/d:orderedlist/d:listitem">
    <xsl:text>*# </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:orderedlist/d:listitem/d:itemizedlist/d:listitem">
    <xsl:text>#* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <!-- Only one EOL for para inside list because we need wiki list rows directly one after another  -->
  <xsl:template match="d:listitem/d:para">
    <xsl:apply-templates />
    <xsl:text>
</xsl:text>
  </xsl:template>

<!-- ############# tables #################### -->

  <!-- filter out title because used in separate field -->
  <xsl:template match="d:table/d:title" />

  <xsl:template match="d:table">
    <xsl:text>{scroll-title:title=</xsl:text><xsl:value-of select="d:title" /><xsl:text>}
</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{scroll-title}
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:entrytbl">
    <xsl:text>|{panel}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{panel}</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:thead/d:row/d:entry">
    <xsl:text>||</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:thead/d:row">
    <xsl:apply-templates/><xsl:text>||
</xsl:text>
  </xsl:template>

  <xsl:template match="d:thead/d:tr/d:td">
    <xsl:text>||</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:thead/d:tr">
    <xsl:apply-templates/><xsl:text>||
</xsl:text>
  </xsl:template>

  <xsl:template match="d:tbody/d:row/d:entry">
    <xsl:text>|</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:tbody/d:row">
    <xsl:apply-templates/><xsl:text>|
</xsl:text>
  </xsl:template>

  <xsl:template match="d:tbody/d:tr/d:td">
    <xsl:text>|</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:tbody/d:tr">
    <xsl:apply-templates/><xsl:text>|
</xsl:text>
  </xsl:template>
  
  <!-- No EOL for para inside table -->
  <xsl:template match="d:entry/d:para">
    <xsl:apply-templates />
  </xsl:template>
  <xsl:template match="d:td/d:para">
    <xsl:apply-templates />
  </xsl:template>
  
<!-- ############# variablelist #################### -->

  <xsl:template match="d:variablelist/d:title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="d:variablelist/d:varlistentry/d:term">
    <xsl:text>* </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:variablelist/d:varlistentry/d:listitem">
    <xsl:text>{quote}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{quote}
</xsl:text>
  </xsl:template>

<!-- ############# procedure #################### -->

<!-- Only one EOL for formalpara/title inside procedure steps -->
  <xsl:template match="d:procedure//d:formalpara/d:title">
    <xsl:text>*</xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>*
</xsl:text>
  </xsl:template>

<xsl:template match="d:procedure/d:title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>
<!-- No EOL for para inside procedure step -->
  <xsl:template match="d:procedure/d:step">
    <xsl:text># *</xsl:text><xsl:value-of select="d:title"/>
    <xsl:text>*
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>  
  
  <!-- skip procedure step title because printed before -->
  <xsl:template match="d:procedure/d:step/d:title"/>
  
  <xsl:template match="d:procedure/d:step/d:itemizedlist/d:listitem">
    <xsl:text>#* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="d:procedure/d:step/d:orderedlist/d:listitem">
    <xsl:text>## </xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="d:procedure/d:step//d:para">
    <xsl:apply-templates/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
<!-- ############# external links #################### -->

  <xsl:template match="d:ulink">
    <xsl:text>[</xsl:text>
    <xsl:if test="child::node()">
    <xsl:apply-templates />
    <xsl:text>|</xsl:text>
    </xsl:if>
    <xsl:value-of select="@url"/><xsl:text>]</xsl:text>
  </xsl:template>

<!-- ############# internal links - cross references #################### -->

  <xsl:template match="d:xref">
    <xsl:text>[</xsl:text>
    <xsl:if test="child::node()">
    <xsl:apply-templates />
    <xsl:text>|</xsl:text>
    </xsl:if>
    <xsl:value-of select="@linkend"/>
    <xsl:text>]</xsl:text>
  </xsl:template>
  
   <xsl:template match="d:link">
    <xsl:text>[</xsl:text>
    <xsl:if test="child::node()">
    <xsl:apply-templates />
    <xsl:text>|</xsl:text>
    </xsl:if>
    <xsl:value-of select="@linkend"/><xsl:value-of select="@xlink:href"/><xsl:text>]</xsl:text>
  </xsl:template>

<!-- ############# images #################### -->

  <xsl:template match="d:figure">
    <xsl:text>!</xsl:text>
    <xsl:apply-templates select="d:mediaobject/d:imageobject/d:imagedata"/>
    <xsl:if test="d:title"><xsl:text>|title=</xsl:text><xsl:value-of select="d:title"/></xsl:if><xsl:text>!
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:mediaobject">
    <xsl:text>!</xsl:text>
    <xsl:apply-templates select="d:imageobject[1]/d:imagedata"/>
    <xsl:if test="d:caption/d:para"><xsl:text>|title=</xsl:text>
    <xsl:value-of select="d:caption/d:para"/></xsl:if><xsl:text>!
</xsl:text>
  </xsl:template>
  
  <xsl:template match="d:inlinemediaobject">
    <xsl:text>!</xsl:text>
    <xsl:apply-templates select="d:imageobject[1]/d:imagedata"/><xsl:text>! </xsl:text>
  </xsl:template>
  
  <xsl:template match="d:imagedata">
    <xsl:value-of select="@fileref"/>
  </xsl:template>

<!-- ############# other #################### -->

<!-- Two EOL for para -->
  <xsl:template match="d:para">
    <xsl:apply-templates />
    <xsl:text>

</xsl:text>
  </xsl:template>

  <!-- Just copy any other elements - leave only inner text -->
  <xsl:template match="*">
    <xsl:apply-templates />
  </xsl:template>
  
  <!-- Just copy all elements for code and noformat wiki macros - leave only inner text without formating -->
  <xsl:template match="d:programlisting//*|d:screen//*">
    <xsl:apply-templates />
  </xsl:template>
  
  <!-- print escaped text elements -->
  <xsl:template match="*/text()" >
     <xsl:variable name="ttext" select="string(.)"/>
     <xsl:value-of select="java:org.jboss.confluence.plugin.docbook_tools.docbookimport.XSLTFunctions.escapeWIKICharsInContent($ttext)" />
  </xsl:template>

  <!-- print unescaped text elements for code and noformat wiki macros -->
  <xsl:template match="d:programlisting//text()|d:screen//text()" >
     <xsl:value-of select="." />
  </xsl:template>
  
  <xsl:template match="d:code/text()|d:filename/text()|d:package/text()|d:computeroutput/text()|d:command/text()|d:systemitem/text()|d:classname/text()|d:literal/text()|d:interface/text()|d:methodname/text()|d:option/text()|d:parameter/text()|d:type/text()|d:varname/text()|d:sgmltag/text()|d:guibutton/text()|d:guiicon/text()|d:guilabel/text()|d:guimenu/text()|d:guimenuitem/text()|d:guisubmenu/text()">
    <xsl:variable name="ttext" select="string(.)"/>
    <xsl:value-of select="java:org.jboss.confluence.plugin.docbook_tools.docbookimport.XSLTFunctions.prepareMonospacedWIKIText($ttext)" />
  </xsl:template>
  
  <!-- remove leading and trailing spaces if there is EOL in it --> 
  <xsl:template match="d:emphasis/text()|d:bold/text()|d:underline/text()|d:italics/text()|d:superscript/text()|d:subscript/text()|d:quote/text()|d:link/text()|d:xref/text()|d:ulink/text()">
    <xsl:variable name="ttext" select="string(.)"/>
    <xsl:value-of select="java:org.jboss.confluence.plugin.docbook_tools.docbookimport.XSLTFunctions.trimWIKIText($ttext)" />
  </xsl:template>
    
  <!-- filter out processing instructions if any -->
  <xsl:template match="processing-instruction()" />

</xsl:stylesheet>
