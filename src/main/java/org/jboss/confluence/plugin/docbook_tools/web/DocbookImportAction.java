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
package org.jboss.confluence.plugin.docbook_tools.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.MimetypesFileTypeMap;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jboss.confluence.plugin.docbook_tools.docbookimport.DocBookVersion;
import org.jboss.confluence.plugin.docbook_tools.docbookimport.DocStructureItem;
import org.jboss.confluence.plugin.docbook_tools.docbookimport.DocbookImporter;
import org.jboss.confluence.plugin.docbook_tools.utils.ConfluenceUtils;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.springframework.web.util.HtmlUtils;

import com.atlassian.confluence.content.render.xhtml.DefaultConversionContext;
import com.atlassian.confluence.core.BodyContent;
import com.atlassian.confluence.core.BodyType;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.LabelManager;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.labels.Namespace;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.AttachmentManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.xhtml.api.XhtmlContent;
import com.atlassian.spring.container.ContainerManager;
import com.opensymphony.webwork.ServletActionContext;
import com.opensymphony.webwork.dispatcher.multipart.MultiPartRequestWrapper;

/**
 * Web Action used to import DocBook xml into Confluence.
 * 
 * @author Vlastimil Elias (velias at redhat dot com) (C) 2011-2017 Red Hat Inc.
 * @see DocbookImporter
 */
public class DocbookImportAction extends PageAvareActionBase {

	private Log logger;

	private PageManager pageManager;

	private LabelManager labelManager;

	private DocbookImporter importer;

	private String titlePrefixBase;

	private String docbookver;

	private String allSectionLevels;
	
	private final XhtmlContent xhtmlContent;

	private static MimetypesFileTypeMap mtftm = new MimetypesFileTypeMap();
	static {
		// register SVG image type because sometimes not registered
		String stest = mtftm.getContentType("test.svg");
		if (!stest.startsWith("image/")) {
			mtftm.addMimeTypes("image/svg svg SVG\n");
		}
	}

	/**
	 * Constructor to initialize.
	 */
	public DocbookImportAction() {
		super();
		logger = LogFactory.getLog(getClass());
		logger.debug("constructor called");
		pageManager = (PageManager) ContainerManager.getComponent("pageManager");
		labelManager = (LabelManager) ContainerManager.getComponent("labelManager");
		xhtmlContent = (XhtmlContent) ContainerManager.getComponent("xhtmlContent");
	}

	/**
	 * Form parameter.
	 * 
	 * @return the titlePrefixBase
	 */
	public String getTitlePrefixBase() {
		if (titlePrefixBase == null)
			return "";
		return titlePrefixBase;
	}

	/**
	 * Form parameter.
	 * 
	 * @param titlePrefixBase the titlePrefixBase to set
	 */
	public void setTitlePrefixBase(String titlePrefixBase) {
		if (titlePrefixBase != null) {
			titlePrefixBase = titlePrefixBase.trim();
			if (titlePrefixBase.isEmpty())
				titlePrefixBase = null;
		}

		this.titlePrefixBase = titlePrefixBase;
	}

	/**
	 * Form parameter.
	 * 
	 * @return the docbookver
	 */
	public String getDocbookver() {
		return docbookver;
	}

	/**
	 * Form parameter.
	 * 
	 * @param docbookver the docbookver to set
	 */
	public void setDocbookver(String docbookver) {
		this.docbookver = docbookver;
	}

	/**
	 * @return the allSectionLevels
	 */
	public String getAllSectionLevels() {
		return allSectionLevels;
	}

	/**
	 * @param allSectionLevels the allSectionLevels to set
	 */
	public void setAllSectionLevels(String allSectionLevels) {
		this.allSectionLevels = allSectionLevels;
	}

	/**
	 * Called to show import form page.
	 * 
	 * @return outcome
	 */
	public String configure() {
		logger.debug("configure called");
		if (!hasPermissionForSpace(PERMISSION_EXPORTSPACE))
			return "notpermitted";
		return "success";
	}

	/**
	 * Called to process import form submit.
	 * 
	 * @return outcome
	 */
	@SuppressWarnings("rawtypes")
	public String perform() {
		logger.debug("perform called");
		if (!hasPermissionForSpace(PERMISSION_EXPORTSPACE))
			return "notpermitted";

		if (!validTitlePrefixBase()) {
			return "error";
		}

		MultiPartRequestWrapper multiWrapper = (MultiPartRequestWrapper) ServletActionContext.getRequest();
		if (multiWrapper.hasErrors()) {
			Collection errors = multiWrapper.getErrors();
			Iterator i = errors.iterator();
			while (i.hasNext()) {
				addActionError((String) i.next());
			}
			return "error";
		}

		File[] files = multiWrapper.getFiles("fileTopp");
		String[] fn = multiWrapper.getFileNames("fileTopp");
		if ((files == null || files.length == 0)) {
			addActionError("No DocBook zip file uploaded for import");
		} else {
			if (!fn[0].endsWith(".zip")) {
				addActionError("Uploaded file must be .zip");
			} else {
				File workingDir = null;
				try {
					importer = new DocbookImporter();

					workingDir = FileUtils.prepareWorkingDirectory("import-src-");
					FileUtils.unzip(FileUtils.openFileInputStream(files[0]), workingDir);
					File docBookFileToImport = importer.findMainDocBookBookFile(workingDir);
					if (docBookFileToImport == null) {
						addActionError("No main DocBook file containing <book> root element found.");
					} else {
						logger.debug("Main DocBook file to process: " + docBookFileToImport.getName());

						DocBookVersion docbookVersion = DocBookVersion.DOCBOOK_4_3;
						if ("5".equals(StringUtils.trimToNull(docbookver))) {
							docbookVersion = DocBookVersion.DOCBOOK_5_0;
						}

						boolean allSectionLevelsBool = Boolean.parseBoolean(allSectionLevels);

						DocStructureItem docToImport = importer.getDocStructure(new FileInputStream(docBookFileToImport),
								docBookFileToImport.toURI().toString(), docbookVersion, allSectionLevelsBool);

						if (validateReferencedLocalFilesExists(docToImport, workingDir)) {

							ConfluenceUtils.handlePageTitleUniqueness(docToImport, titlePrefixBase, getSpace().getKey(),
									new ConfluenceUtils.PageManagerWrapper() {

										@Override
										public boolean pageExists(String spaceKey, String title) {
											return pageManager.getPage(spaceKey, title) != null;
										}

									});

							importer.normalizeAllDocBookXMLFilesContent(workingDir);

							Page importRootPage = getPage();
							Page importRootPageOrig = (Page) importRootPage.clone();

							Page documentRootPage = importDocumentRoot(docToImport, importRootPage);

							importSubpages(docBookFileToImport, docToImport, documentRootPage, workingDir, docbookVersion);

							pageManager.saveContentEntity(documentRootPage, null);
							pageManager.saveContentEntity(importRootPage, importRootPageOrig, null);

							addActionMessage("Imported successfuly.");

							return "success";
						}
					}
				} catch (Exception e) {
					logger.error("Error during DocBook file import: " + e.getMessage(), e);
					addActionError("Error during DocBook file import: " + HtmlUtils.htmlEscape(e.getMessage()));
				} finally {
					if (!logger.isDebugEnabled()) {
						FileUtils.deleteDirectoryRecursively(workingDir);
					}
				}
			}
		}
		return "error";

	}

	protected boolean validTitlePrefixBase() {
		if (titlePrefixBase == null)
			return true;

		if (titlePrefixBase.length() < 2 || titlePrefixBase.length() > 3) {
			addActionError("'Unique page title prefix base' must be two or three characters long");
			return false;
		} else {
			for (char ch : titlePrefixBase.toCharArray()) {
				if (!Character.isLetterOrDigit(ch)) {
					addActionError("'Unique page title prefix base' must contain only letters or digits");
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Import document root into {@link Page}. Both returned object and <code>importRootPage</code> must be persisted
	 * later using {@link PageManager}!
	 * 
	 * @param docToImport info about document to read chapters from
	 * @param importRootPage to add document root page as children to
	 * @return {@link Page} object for document root
	 */
	private Page importDocumentRoot(DocStructureItem docToImport, Page importRootPage) {
		Page documentRootPage = preparePageObjectBase(docToImport, importRootPage);
		return documentRootPage;
	}

	/**
	 * Prepare base for {@link Page} object. Title, Space and parent Page is set into returned object. Returned object is
	 * added as child to parent object too.
	 * <p>
	 * Both returned object and <code>parentPage</code> must be persisted later using {@link PageManager}!
	 * 
	 * @param pageStructureInfo info about page structure
	 * @param parentPage parent page
	 * @return prepared Page object
	 */
	private Page preparePageObjectBase(DocStructureItem pageStructureInfo, Page parentPage) {
		Page page = new Page();
		String title = pageStructureInfo.getConfluencePageTitle();
		if (StringUtils.isBlank(title)) {
			throw new RuntimeException(
					"Title for book/chapters/sections must be defined, check your docbook xml structure please: "
							+ pageStructureInfo.toString(false));
		}
		page.setTitle(title);
		page.setSpace(getSpace());
		page.setParentPage(parentPage);
		parentPage.addChild(page);
		return page;
	}

	/**
	 * Import childs of given DocStructureItem (chapters or sections) as subpages for given rootPage.
	 * 
	 * @param docBookFileToImport DocBook xml <code>book</code> file used to import.
	 * @param docToImport info about node to read childs from
	 * @param rootPage root node page to add childs as subpages to
	 * @param workDir to load referenced images from
	 * @throws Exception
	 */
	private void importSubpages(File docBookFileToImport, DocStructureItem docToImport, Page rootPage, File workDir,
			DocBookVersion docbookVersion) throws Exception {
		int chapterPosition = 0;
		for (DocStructureItem chapterInfo : docToImport.getChilds()) {
			Page chapterPage = preparePageObjectBase(chapterInfo, rootPage);
			chapterPage.setPosition(chapterPosition++);
			
			String wikiContent = importer.prepareNodeWIKIContent(docBookFileToImport, chapterInfo, docbookVersion);
			List<RuntimeException> conversionErrors = new ArrayList<>();
            BodyContent bc = new BodyContent(chapterPage, xhtmlContent.convertWikiToStorage(wikiContent, new DefaultConversionContext( chapterPage.toPageContext()), conversionErrors ), BodyType.XHTML);
            if(!conversionErrors.isEmpty()){
                logger.warn("Errors from wiki content conversion during DocBook import: " + conversionErrors);
            }
			chapterPage.setBodyContent(bc);
			importPageAttachments(chapterInfo, workDir, chapterPage);

			// import subpages recursively
			importSubpages(docBookFileToImport, chapterInfo, chapterPage, workDir, docbookVersion);

			pageManager.saveContentEntity(chapterPage, null);

			Set<String> labels = chapterInfo.getLabels();
			if (labels != null && !labels.isEmpty()) {
				for (String lb : labels) {
					Label label = new Label(lb, Namespace.GLOBAL);
					labelManager.addLabel((Labelable) chapterPage, label);
				}
			}

		}
	}

	/**
	 * Import attachments for Page.
	 * 
	 * @param chapterInfo to import attachments for
	 * @param workDir to read attachments from
	 * @param chapterPage to add attachments to
	 * @throws Exception
	 */
	private void importPageAttachments(DocStructureItem chapterInfo, File workDir, Page chapterPage) throws Exception {
		List<String> localRefs = chapterInfo.getFilerefsLocal();
		if (localRefs != null && !localRefs.isEmpty()) {
			AttachmentManager attachmentManager = pageManager.getAttachmentManager();
			Set<String> uniqueName = new HashSet<String>();
			for (String ref : localRefs) {
				if (!uniqueName.contains(ref)) {
					uniqueName.add(ref);
					File af = new File(workDir, ref);
					Attachment attachment = new Attachment();
					attachment.setFileName(importer.getFilenameFromFilerefLocal(ref));
					attachment.setFileSize(af.length());
					attachment.setContent(chapterPage);
					attachment.setContentType(mtftm.getContentType(af));
					InputStream is = new FileInputStream(af);
					try {
						attachmentManager.saveAttachment(attachment, null, is);
					} finally {
						if (is != null) {
							is.close();
						}
					}
					chapterPage.addAttachment(attachment);
				}
			}
		}
	}

	private boolean validateReferencedLocalFilesExists(DocStructureItem docToImport, File directory) {
		List<String> messages = new ArrayList<String>();
		importer.validateImageFilesExists(docToImport, directory, messages);

		if (messages.isEmpty()) {
			return true;
		} else {
			for (String msg : messages) {
				addActionError(msg);
			}
			return false;
		}
	}

}
