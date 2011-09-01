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
                xmlns:java="http://xml.apache.org/xalan/java">

  <xsl:output method="text" indent="no" omit-xml-declaration="yes" />
  
  <xsl:strip-space elements="*" />

  <!-- render only specified chapter, appendix or section -->
  <xsl:template match="/">
    <xsl:apply-templates select="${1}" />
  </xsl:template>

  <xsl:template match="section/title" >
    <xsl:apply-templates /><xsl:text>
</xsl:text>
  </xsl:template>

  <!-- filter out title because used in separate field, not in content -->
  <xsl:template match="${1}/title" />

<!-- #############  basic text formating #################### -->
  <xsl:template match="emphasis[@role='strong']|bold">
    <xsl:text>*</xsl:text><xsl:apply-templates /><xsl:text>*</xsl:text>
  </xsl:template>
  
  <xsl:template match="emphasis[@role='underline']|underline">
    <xsl:text>+</xsl:text><xsl:apply-templates /><xsl:text>+</xsl:text>
  </xsl:template>
  
  <xsl:template match="emphasis[@role='strikethrough']">
    <xsl:text>-</xsl:text><xsl:apply-templates /><xsl:text>-</xsl:text>
  </xsl:template>
  
  <xsl:template match="emphasis[@role='italics']|italics|emphasis">
    <xsl:text>_</xsl:text><xsl:apply-templates /><xsl:text>_</xsl:text>
  </xsl:template>
  
  <xsl:template match="superscript">
    <xsl:text>^</xsl:text><xsl:apply-templates /><xsl:text>^</xsl:text>
  </xsl:template>
  
  <xsl:template match="subscript">
    <xsl:text>~</xsl:text><xsl:apply-templates /><xsl:text>~</xsl:text>
  </xsl:template>
    
  <xsl:template match="screen">
    <xsl:text>{noformat}</xsl:text><xsl:apply-templates /><xsl:text>{noformat}
</xsl:text>
  </xsl:template>
  
  <xsl:template match="quote">
    <xsl:text>??</xsl:text><xsl:apply-templates /><xsl:text>??</xsl:text>
  </xsl:template>
  
   <xsl:template match="blockquote">
    <xsl:text>{quote}</xsl:text><xsl:apply-templates /><xsl:text>{quote}
</xsl:text>
  </xsl:template>
  
  <xsl:template match="formalpara/title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>
  
<!-- ############# sections to headers #################### -->
  <!-- filter out section info because not used-->
  <xsl:template match="section/info" />

  <xsl:template match="${1}/section">
    <xsl:text>
h1. </xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="${1}/section/section">
    <xsl:text>
h2. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/section/section/section">
    <xsl:text>
h3. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/section/section/section/section">
    <xsl:text>
h4. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/section/section/section/section/section">
    <xsl:text>
h5. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>
  
  <xsl:template match="${1}/section/section/section/section/section/section">
    <xsl:text>
h6. </xsl:text>
    <xsl:apply-templates />
  </xsl:template>

  <!-- filter out subsections in some cases -->
  ${2}


<!-- ############# program listings #################### -->

  <xsl:template match="example">
    <xsl:apply-templates>
      <xsl:with-param name="title" select="title"/>
    </xsl:apply-templates>
  </xsl:template>

  <!-- skip example title node because printed inside programlisting handler --> 
  <xsl:template match="example/title"/>
  

  <xsl:template match="programlisting[@language]">
    <xsl:param name="title"/>
    <xsl:text>{code:lang=</xsl:text><xsl:value-of select="@language"/>
    <xsl:if test="$title"><xsl:text>|title=</xsl:text><xsl:value-of select="$title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates />
    <xsl:text>{code}
</xsl:text>
  </xsl:template>

  <xsl:template match="programlisting">
    <xsl:param name="title"/>
    <xsl:text>{code</xsl:text>
    <xsl:if test="$title"><xsl:text>:title=</xsl:text><xsl:value-of select="$title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates />
    <xsl:text>{code}
</xsl:text>
  </xsl:template>

<!-- ############# Admonitions #################### -->
  <xsl:template match="warning">
    <xsl:text>{warning</xsl:text>
    <xsl:if test="title"><xsl:text>:title=</xsl:text><xsl:value-of select="title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{warning}
</xsl:text>
  </xsl:template>

  <xsl:template match="important">
    <xsl:text>{info</xsl:text>
    <xsl:if test="title"><xsl:text>:title=</xsl:text><xsl:value-of select="title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{info}
</xsl:text>
  </xsl:template>

<xsl:template match="note">
    <xsl:text>{note</xsl:text>
    <xsl:if test="title"><xsl:text>:title=</xsl:text><xsl:value-of select="title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{note}
</xsl:text>
  </xsl:template>  

<xsl:template match="tip">
    <xsl:text>{tip</xsl:text>
    <xsl:if test="title"><xsl:text>:title=</xsl:text><xsl:value-of select="title" /></xsl:if>
    <xsl:text>}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{tip}
</xsl:text>
  </xsl:template>
  
   <!-- filter out title because used in separate field -->
  <xsl:template match="warning/title|important/title|note/title|tip/title" />

<!-- ############# lists #################### -->

<!-- list in para mainly starts on same row as some text, so we need to add row here so first list item starts on new line -->
  <xsl:template match="para/itemizedlist">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  <xsl:template match="para/orderedlist">
    <xsl:text>
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="itemizedlist/listitem">
    <xsl:text>* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="orderedlist/listitem">
    <xsl:text># </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="itemizedlist/listitem/itemizedlist/listitem">
    <xsl:text>** </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="orderedlist/listitem/orderedlist/listitem">
    <xsl:text>## </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="itemizedlist/listitem/orderedlist/listitem">
    <xsl:text>*# </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="orderedlist/listitem/itemizedlist/listitem">
    <xsl:text>#* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <!-- Only one EOL for para inside list because we need wiki list rows directly one after another  -->
  <xsl:template match="listitem/para">
    <xsl:apply-templates />
    <xsl:text>
</xsl:text>
  </xsl:template>

<!-- ############# tables #################### -->

  <!-- filter out title because used in separate field -->
  <xsl:template match="table/title" />

  <xsl:template match="table">
    <xsl:text>{scroll-title:title=</xsl:text><xsl:value-of select="title" /><xsl:text>}
</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{scroll-title}
</xsl:text>
  </xsl:template>

  <xsl:template match="entrytbl">
    <xsl:text>|{panel}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{panel}</xsl:text>
  </xsl:template>
  
  <xsl:template match="thead/row/entry">
    <xsl:text>||</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="thead/row">
    <xsl:apply-templates/><xsl:text>||
</xsl:text>
  </xsl:template>

  <xsl:template match="tbody/row/entry">
    <xsl:text>|</xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="tbody/row">
    <xsl:apply-templates/><xsl:text>|
</xsl:text>
  </xsl:template>
  
  <!-- No EOL for para inside table -->
  <xsl:template match="entry/para">
    <xsl:apply-templates />
  </xsl:template>
  
<!-- ############# variablelist #################### -->

  <xsl:template match="variablelist/title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>

  <xsl:template match="variablelist/varlistentry/term">
    <xsl:text>* </xsl:text>
    <xsl:apply-templates/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
  <xsl:template match="variablelist/varlistentry/listitem">
    <xsl:text>{quote}</xsl:text>
    <xsl:apply-templates/>
    <xsl:text>{quote}
</xsl:text>
  </xsl:template>

<!-- ############# procedure #################### -->

<!-- Only one EOL for formalpara/title inside procedure steps -->
  <xsl:template match="procedure//formalpara/title">
    <xsl:text>*</xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>*
</xsl:text>
  </xsl:template>

<xsl:template match="procedure/title">
    <xsl:text>h6. </xsl:text><xsl:apply-templates select="child::node()" /><xsl:text>
</xsl:text>
  </xsl:template>
<!-- No EOL for para inside procedure step -->
  <xsl:template match="procedure/step">
    <xsl:text># *</xsl:text><xsl:value-of select="title"/>
    <xsl:text>*
</xsl:text>
    <xsl:apply-templates/>
  </xsl:template>  
  
  <!-- skip procedure step title because printed before -->
  <xsl:template match="procedure/step/title"/>
  
  <xsl:template match="procedure/step/itemizedlist/listitem">
    <xsl:text>#* </xsl:text><xsl:apply-templates/>
  </xsl:template>

  <xsl:template match="procedure/step/orderedlist/listitem">
    <xsl:text>## </xsl:text><xsl:apply-templates/>
  </xsl:template>
  
  <xsl:template match="procedure/step//para">
    <xsl:apply-templates/>
    <xsl:text>
</xsl:text>
  </xsl:template>
  
<!-- ############# external links #################### -->

  <xsl:template match="ulink">
    <xsl:text>[</xsl:text>
    <xsl:if test="child::node()">
    <xsl:apply-templates />
    <xsl:text>|</xsl:text>
    </xsl:if>
    <xsl:value-of select="@url"/><xsl:text>]</xsl:text>
  </xsl:template>

<!-- ############# internal links - cross references #################### -->

  <xsl:template match="xref">
    <xsl:text>[</xsl:text>
    <xsl:value-of select="@linkend"/>
    <xsl:text>]</xsl:text>
  </xsl:template>

<!-- ############# images #################### -->

  <xsl:template match="figure">
    <xsl:text>!</xsl:text>
    <xsl:apply-templates select="mediaobject/imageobject/imagedata"/>
    <xsl:text>|title=</xsl:text>
    <xsl:value-of select="title"/><xsl:text>!
</xsl:text>
  </xsl:template>
  
  <xsl:template match="mediaobject">
    <xsl:text>!</xsl:text>
    <xsl:apply-templates select="imageobject[1]/imagedata"/>
    <xsl:text>|title=</xsl:text>
    <xsl:value-of select="caption/para"/><xsl:text>!
</xsl:text>
  </xsl:template>
  
  <xsl:template match="imagedata">
    <xsl:value-of select="@fileref"/>
  </xsl:template>

<!-- ############# other #################### -->

<!-- Two EOL for para -->
  <xsl:template match="para">
    <xsl:apply-templates />
    <xsl:text>

</xsl:text>
  </xsl:template>

  <!-- Just copy any other elements - leave only inner text -->
  <xsl:template match="*">
    <xsl:apply-templates />
  </xsl:template>
  
  <!-- Just copy all elements for code and noformat wiki macros - leave only inner text without formating -->
  <xsl:template match="programlisting//*|screen//*">
    <xsl:apply-templates />
  </xsl:template>
  
  <!-- print escaped text elements -->
  <xsl:template match="*/text()" >
     <xsl:variable name="ttext" select="string(.)"/>
     <xsl:value-of select="java:org.jboss.confluence.plugin.docbook_tools.docbookimport.XSLTFunctions.escapeWIKICharsInContent($ttext)" />
  </xsl:template>

  <!-- print unescaped text elements for code and noformat wiki macros -->
  <xsl:template match="programlisting//text()|screen//text()" >
     <xsl:value-of select="." />
  </xsl:template>
  
  <xsl:template match="code/text()|filename/text()|package/text()|computeroutput/text()|command/text()|systemitem/text()|classname/text()|literal/text()|interface/text()|methodname/text()|option/text()|parameter/text()|type/text()|varname/text()|sgmltag/text()|guibutton/text()|guiicon/text()|guilabel/text()|guimenu/text()|guimenuitem/text()|guisubmenu/text()">
    <xsl:variable name="ttext" select="string(.)"/>
    <xsl:value-of select="java:org.jboss.confluence.plugin.docbook_tools.docbookimport.XSLTFunctions.prepareMonospacedWIKIText($ttext)" />
  </xsl:template>
  
    
  <!-- filter out processing instructions if any -->
  <xsl:template match="processing-instruction()" />

</xsl:stylesheet>
