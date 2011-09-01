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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;

import org.apache.commons.digester.Digester;
import org.apache.log4j.Logger;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.jboss.confluence.plugin.docbook_tools.utils.RegExpUtils;
import org.jboss.confluence.plugin.docbook_tools.utils.SAXErrorHandler;
import org.jboss.confluence.plugin.docbook_tools.utils.XSLTErrorListener;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.ParserAdapter;

import com.atlassian.gzipfilter.org.tuckey.web.filters.urlrewrite.utils.StringUtils;

/**
 * Class used to import DocBook files with "JBoss Documentation Guide" structure to Confluence.
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class DocbookImporter {

  private static final Logger log = Logger.getLogger(DocbookImporter.class);

  static {
    log.info("Class loader: " + DocbookImporter.class.getClassLoader().getClass().getName());
  }

  // XInclude aware SAXParser
  protected static final SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
  static {
    printClassInfo(saxParserFactory.getClass(), "XML SAXParserFactory implementation from JAXP");
    saxParserFactory.setXIncludeAware(true);
    saxParserFactory.setNamespaceAware(true);
  }

  // XSLT engine
  protected static final javax.xml.transform.TransformerFactory transformerFact = javax.xml.transform.TransformerFactory
      .newInstance();
  static {
    printClassInfo(transformerFact.getClass(), "XSLT TransformerFactory implementation from JAXP");
  }

  /**
   * Constructor with some checks for necessary infrastructure.
   */
  public DocbookImporter() {
    String name = transformerFact.getClass().getName();
    if (!name.contains(".xalan.")) {
      log.error("We need Xalan XSLT processor! But JAXP returned: " + name);
      throw new IllegalStateException("We need Xalan XSLT processor returned from JAXP!");
    }
    try {
      saxParserFactory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
      SAXParser saxParser = saxParserFactory.newSAXParser();
      if (!saxParser.isXIncludeAware()) {
        throw new IllegalStateException("SAXParser returned by JAXP is not XInclude aware!");
      }
    } catch (ParserConfigurationException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    } catch (SAXException e) {
      log.error(e.getMessage());
      throw new RuntimeException(e);
    }
  }

  protected static final void printClassInfo(Class<?> clazz, String msg) {
    Package pack = clazz.getPackage();
    StringBuilder sb = new StringBuilder();
    sb.append(msg).append(": ").append(clazz.getName());
    sb.append(", Specification version: ").append(pack.getSpecificationVersion());
    sb.append(", Implementation version: ").append(pack.getImplementationVersion());
    log.info(sb.toString());
  }

  /**
   * Search for main DocBook <code>book</code> file in folder.
   * 
   * @param folder to search main DocBook <code>book</code> file in.
   * @return main file or null
   * @throws Exception
   */
  public File findMainDocBookBookFile(File folder) throws Exception {
    File[] files = folder.listFiles(FileUtils.FILTER_FILE_XML);
    if (files != null) {
      for (File f : files) {
        String content = FileUtils.readFileAsString(new FileInputStream(f));
        content = content.trim();
        if (content.indexOf("<book") > -1 && (content.endsWith("</book>") || content.endsWith("</ book>"))) {
          return f;
        }
      }
    }
    return null;
  }

  /**
   * Validate all images present in DocBook document structure are also present in input folder.
   * 
   * @param docToImport info about imported document. Local file references are taken from it
   * @param directory to search images in
   * @param messagesStore error message is added to this store for image not present in folder
   */
  public void validateImageFilesExists(DocStructureItem docToImport, File directory, List<String> messagesStore) {
    log.debug("Go to evaluate image file for title: " + docToImport.getTitle());
    List<String> files = docToImport.getFilerefsLocal();
    if (files != null && !files.isEmpty()) {
      for (String filename : files) {
        log.debug("Go to evaluate image file: " + filename);
        File f = new File(directory, filename);
        if (!f.exists()) {
          messagesStore.add("Missing image file: " + filename);
          log.debug("Missing image file: " + filename);
        }
      }
    }

    List<DocStructureItem> childs = docToImport.getChilds();
    if (childs != null && !childs.isEmpty()) {
      for (DocStructureItem child : childs) {
        validateImageFilesExists(child, directory, messagesStore);
      }
    }
  }

  /**
   * Get structure of titles and chapters from DocBook xml file to be used for further processing.
   * 
   * @param xmlToTransform
   * @param xmlToTransformURL URL of <code>xmlToTransform</code> file (may be <code>file://</code> too). We need it to
   *          correctly evaluate relative paths.
   * @param docbookVersion version of docbook to process
   * @return structure of titles
   * @throws Exception
   */
  public DocStructureItem getDocStructure(InputStream xmlToTransform, String xmlToTransformURL,
      DocBookVersion docbookVersion) throws Exception {

    InputStream xsltTemplate = getFileFromResources("getStructure" + docbookVersion.getFilenamePostfix() + ".xslt");
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    processXslt(xsltTemplate, xmlToTransform, xmlToTransformURL, out);

    if (log.isDebugEnabled())
      log.debug("DocStructureXML: " + out.toString("UTF-8"));

    Digester dig = new Digester();
    dig.setClassLoader(DocbookImporter.class.getClassLoader());
    dig.setValidating(false);

    // book info
    dig.addObjectCreate("node", DocStructureItem.class);
    dig.addCallMethod("node/title", "setTitle", 1);
    dig.addCallParam("node/title", 0);
    dig.addCallMethod("node/type", "setType", 1);
    dig.addCallParam("node/type", 0);

    // chapter or appendix info
    dig.addObjectCreate("node/node", DocStructureItem.class);
    dig.addSetNext("node/node", "addChild");
    dig.addCallMethod("node/node/title", "setTitle", 1);
    dig.addCallParam("node/node/title", 0);
    dig.addCallMethod("node/node/id", "setId", 1);
    dig.addCallParam("node/node/id", 0);
    dig.addCallMethod("node/node/type", "setType", 1);
    dig.addCallParam("node/node/type", 0);
    dig.addCallMethod("node/node/fileref", "addFileref", 1);
    dig.addCallParam("node/node/fileref", 0);

    // first level of sections info
    dig.addObjectCreate("node/node/node", DocStructureItem.class);
    dig.addSetNext("node/node/node", "addChild");
    dig.addCallMethod("node/node/node/title", "setTitle", 1);
    dig.addCallParam("node/node/node/title", 0);
    dig.addCallMethod("node/node/node/id", "setId", 1);
    dig.addCallParam("node/node/node/id", 0);
    dig.addCallMethod("node/node/node/type", "setType", 1);
    dig.addCallParam("node/node/node/type", 0);
    dig.addCallMethod("node/node/node/fileref", "addFileref", 1);
    dig.addCallParam("node/node/node/fileref", 0);

    try {
      DocStructureItem ret = (DocStructureItem) dig.parse(new ByteArrayInputStream(out.toByteArray()));
      validateDocStructure(ret);
      return ret;
    } catch (Exception e) {
      log.error(e.getMessage() + " in DocStructureXML file content: " + out.toString("UTF-8"));
      throw e;
    }
  }

  /**
   * Validate document structure data. Check for mandatory fields.
   * 
   * @param item to validate
   */
  protected void validateDocStructure(DocStructureItem item) {

    if (!DocStructureItem.TYPE_BOOK.equals(item.getType())) {
      if (StringUtils.isBlank(item.getTitle())) {
        throw new IllegalArgumentException("Item without title: " + item.getDocBookXPath(null));
      }
      if (StringUtils.isBlank(item.getId())) {
        throw new IllegalArgumentException("Item without id: " + item.getDocBookXPath(null));
      }
    }
    List<DocStructureItem> chl = item.getChilds();
    if (chl != null) {
      for (DocStructureItem ch : chl) {
        validateDocStructure(ch);
      }
    }
  }

  /**
   * Patch references in generated WIKI content.
   * <ul>
   * <li>internal cross references changed from DocBook xml id's to Confluence chapter titles
   * <li>local image relative paths changed to be only filenames because we import images as page attachments
   * </ul>
   * 
   * @param content to be patched
   * @param nodeStructure node structure to use {@link DocStructureItem#getFilerefsLocal()} and
   *          {@link DocStructureItem#getParent()} from.
   * @return patched content
   * @throws Exception
   */
  public String patchWIKIContentReferences(String content, DocStructureItem nodeStructure) throws Exception {
    List<String> frl = nodeStructure.getFilerefsLocal();
    if (frl != null && !frl.isEmpty()) {
      for (String fileref : frl) {
        content = content.replaceAll("\\!" + RegExpUtils.escapeTextForRegexp(fileref) + "(\\|.*)?\\!", "!"
            + getFilenameFromFilerefLocal(fileref) + "$1!");
      }
    }

    content = patchWIKIContentInternalCrossReferences(nodeStructure.getRoot(), content);

    return content;
  }

  /**
   * Patch internal cross references in generated WIKI content.
   * 
   * @param parent of hierarchy to patch references for - whole subtree traversed
   * @param content to patch
   * @return patched content
   */
  protected String patchWIKIContentInternalCrossReferences(DocStructureItem parent, String content) {
    for (DocStructureItem ch : parent.getChilds()) {
      content = content.replaceAll("\\[(.*\\|)?" + RegExpUtils.escapeTextForRegexp(ch.getId()) + "\\]",
          "[$1" + ch.getConfluencePageTitle() + "]");
      content = patchWIKIContentInternalCrossReferences(ch, content);
    }
    return content;
  }

  /**
   * Get filename from fileref local relative path.
   * 
   * @param fileref to convert
   * @return filename
   */
  public String getFilenameFromFilerefLocal(String fileref) {
    if (fileref == null || fileref.trim().isEmpty()) {
      return null;
    }
    int idx = fileref.lastIndexOf("/");
    if (idx > -1) {
      fileref = fileref.substring(idx + 1);
    }
    return fileref;
  }

  /**
   * Prepare WIKI content for defined node (chapter, appendix or section) from DocBook xml. If info about processed node
   * contains some children then only given node is rendered without subsections content.
   * 
   * @param xmlToTransform file to read DocBook xml <code>book</code> file from
   * @param nodeStructureInfo chapter structure
   * @param docbookVersion version of docbook to process
   * @return chapter WIKI content
   * @throws Exception
   */
  public String prepareNodeWIKIContent(File xmlToTransform, DocStructureItem nodeStructureInfo,
      DocBookVersion docbookVersion) throws Exception {
    try {
      return patchWIKIContentReferences(
          prepareNodeWIKIContent(new BufferedInputStream(new FileInputStream(xmlToTransform)), xmlToTransform.toURI()
              .toString(), nodeStructureInfo, docbookVersion), nodeStructureInfo);
    } catch (Exception e) {
      log.error("Error during WIKI content obtaining for " + nodeStructureInfo + ": " + e.getMessage());
      throw e;
    }
  }

  /**
   * Prepare WIKI content for defined node (chapter, appendix or section) from DocBook xml. If info about processed node
   * contains some children then only given node is rendered without subsections content.
   * 
   * @param xmlToTransform stream to read DocBook xml <code>book</code> file from (closed inside this method)
   * @param xmlToTransformURL URL of <code>xmlToTransform</code> file (may be <code>file://</code> too). We need it to
   *          correctly evaluate relative paths.
   * @param nodeStructureInfo info about processed node
   * @param docbookVersion version of docbook to process
   * @return chapter WIKI content
   * @throws Exception
   */
  protected String prepareNodeWIKIContent(InputStream xmlToTransform, String xmlToTransformURL,
      DocStructureItem nodeStructureInfo, DocBookVersion docbookVersion) throws Exception {
    String xslt = FileUtils.readFileAsString(getFileFromResources("prepareChapterWIKIContent"
        + docbookVersion.getFilenamePostfix() + ".xslt"));

    // customize XSLT template for this run
    xslt = xslt.replaceAll("\\$\\{1\\}", nodeStructureInfo.getDocBookXPath(docbookVersion.getXmlnsPrefix()));

    if (!nodeStructureInfo.getChilds().isEmpty()) {
      // we will render subsections later as subpages, so remove them from content now
      xslt = xslt.replace("${2}",
          "<xsl:template match=\"" + nodeStructureInfo.getDocBookXPath(docbookVersion.getXmlnsPrefix()) + "/"
              + docbookVersion.getXmlnsPrefix() + "section\" />");
    } else {
      xslt = xslt.replace("${2}", "");
    }

    // perform XSLT transformation
    InputStream xis = new ByteArrayInputStream(xslt.getBytes(FileUtils.CHARSET_UTF_8));
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    processXslt(xis, xmlToTransform, xmlToTransformURL, bos);
    return bos.toString(FileUtils.CHARSET_UTF_8);
  }

  /**
   * Process XSLT transformation.
   * 
   * @param xsltTemplate input stream with XSLT template file used to transform (closed inside this method)
   * @param xmlToTransform input stream with XML file to transform (closed inside this method)
   * @param xmlToTransformURL URL of <code>xmlToTransform</code> file (may be <code>file://</code> too). We need it to
   *          correctly evaluate relative paths.
   * @param output stream to write transformed output to
   * @throws javax.xml.transform.TransformerException
   */
  protected void processXslt(final InputStream xsltTemplate, final InputStream xmlToTransform,
      final String xmlToTransformURL, final OutputStream output) throws Exception {

    final XSLTErrorListener errorListener = new XSLTErrorListener();
    final SAXErrorHandler eh = new SAXErrorHandler();

    Thread th = new Thread(new Runnable() {

      public void run() {
        try {
          org.xml.sax.InputSource xmlSource = new org.xml.sax.InputSource(xmlToTransform);
          xmlSource.setSystemId(xmlToTransformURL);
          javax.xml.transform.Source xsltSource = new javax.xml.transform.stream.StreamSource(xsltTemplate);
          javax.xml.transform.Result result = new javax.xml.transform.stream.StreamResult(output);

          // prepare XInclude aware parser which resolves necessary entities correctly
          XMLReader reader = new ParserAdapter(saxParserFactory.newSAXParser().getParser());
          reader.setEntityResolver(new JDGEntityResolver(reader.getEntityResolver()));
          reader.setErrorHandler(eh);
          SAXSource xmlSAXSource = new SAXSource(reader, xmlSource);

          javax.xml.transform.Transformer trans = transformerFact.newTransformer(xsltSource);

          trans.setErrorListener(errorListener);
          trans.transform(xmlSAXSource, result);

        } catch (Exception e) {
          if (e instanceof TransformerException) {
            errorListener.setException((TransformerException) e);
          } else {
            errorListener.setException(new TransformerException(e));
          }
        } finally {
          FileUtils.closeInputStream(xmlToTransform);
          FileUtils.closeInputStream(xsltTemplate);
        }
      }
    });
    th.setName("DocbookImporter XSLT transformation thread");
    th.setDaemon(true);
    th.setContextClassLoader(DocbookImporter.class.getClassLoader());
    th.start();
    th.join();

    if (eh.getException() != null) {
      throw eh.getException();
    }

    if (errorListener.getException() != null) {
      throw errorListener.getException();
    }

  }

  /**
   * Normalizes DocBook xml file content for better processing. These normalizations are done:
   * <ul>
   * <li>all versions of EOL normalized to <code>\n</code>
   * <li><code>&lt;/ para&gt;</code>changed to <code>&lt;/para&gt;</code>
   * <li>leading and trailing white characters and EOL removed from &lt;para&gt; content
   * <li>white characters and EOL removed after &lt;/programlisting&gt; content
   * <li>leading and trailing white characters and EOL removed from table &lt;entry&gt; content
   * <li><code>&lt;/ entry&gt;</code>changed to <code>&lt;/entry&gt;</code>
   * <li>leading and trailing white characters and EOL removed from &lt;term&gt; content
   * <li><code>&lt;/ term&gt;</code>changed to <code>&lt;/term&gt;</code>
   * <li>leading and trailing white characters and EOL removed from &lt;link&gt; content
   * <li><code>&lt;/ link&gt;</code>changed to <code>&lt;/link&gt;</code>
   * </ul>
   * 
   * @param content to normalize
   * @return normalized content
   * @throws Exception
   */
  protected String normalizeDocBookXMLFileContent(String content) throws Exception {
    content = content.replaceAll("\\r\\n", "\n");
    content = content.replaceAll("\\r", "\n");
    content = content.replaceAll("<para(>| [^\\>^/]*>)\\s*\\n\\s*", "<para>");
    content = content.replaceAll("</ para>", "</para>");
    content = content.replaceAll("\\n\\s*</para>", "</para>");
    content = content.replaceAll("</ programlisting>", "</programlisting>");
    content = content.replaceAll("</programlisting>\\n\\s*", "</programlisting>");
    content = content.replaceAll("<entry(>| [^\\>^/]*>)\\s*\\n\\s*", "<entry>");
    content = content.replaceAll("</ entry>", "</entry>");
    content = content.replaceAll("\\n\\s*</entry>", "</entry>");
    content = content.replaceAll("<term(>| [^\\>^/]*>)\\s*\\n\\s*", "<term>");
    content = content.replaceAll("</ term>", "</term>");
    content = content.replaceAll("\\n\\s*</term>", "</term>");
    return content;
  }

  /**
   * Normalizes DocBook xml file content for better processing. See {@link #normalizeDocBookXMLFileContent(String)}.
   * 
   * @param file to normalize content inside
   * @throws Exception
   */
  public void normalizeDocBookXMLFileContent(File file) throws Exception {
    String content = FileUtils.readFileAsString(new BufferedInputStream(new FileInputStream(file)));
    content = normalizeDocBookXMLFileContent(content);
    FileUtils.writeStringContentToFile(file, content);
  }

  /**
   * Normalizes all DocBook xml file content for better processing in given folder and all subfolders. See
   * {@link #normalizeDocBookXMLFileContent(String)}.
   * 
   * @param folder to normalize content inside
   * @throws Exception
   */
  public void normalizeAllDocBookXMLFilesContent(File folder) throws Exception {
    File[] files = folder.listFiles(FileUtils.FILTER_FILE_XML);
    if (files != null) {
      for (File file : files) {
        normalizeDocBookXMLFileContent(file);
      }
    }
    File[] subfolders = folder.listFiles(FileUtils.FILTER_DIRECTORY);
    if (subfolders != null) {
      for (File subfolder : subfolders) {
        normalizeAllDocBookXMLFilesContent(subfolder);
      }
    }
  }

  /**
   * Get stream to read file from resources.
   * 
   * @param fileName of file to get stream for
   * @return stream to read given file from
   * @throws Exception
   */
  protected InputStream getFileFromResources(String fileName) throws Exception {
    String resName = fileName;
    InputStream is = getClass().getResourceAsStream(resName);
    if (is == null) {
      throw new Exception("Resource not found for name: " + resName);
    }
    return is;
  }

}
