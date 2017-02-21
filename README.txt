This is plugin used to import DocBook xml into Atlassian Confluence.

For more info look http://community.jboss.org/wiki/DocBookImportForConfluence
For changes look changelog.txt

**** License ****
*****************
GNU Lesser General Public License Version 2.1
http://www.gnu.org/licenses/lgpl-2.1-standalone.html

**** How to install/update ****
*******************************
Simply use Confluence 'Admin > Configuration > Plugins' to install or update plugin.
Plugin can be downloaded from http://community.jboss.org/wiki/DocBookImportForConfluence also.

**** How to use ****
********************
To import content from DocBook XML into a space, a user must have the 'Space - Export' permission within the space.

1. Place your DocBook XML files into a zip file. The DocBook XML file containing the <book> root element must be in the top-level directory, not in a sub-directory. The zip file must also contain all relatively referenced resources (e.g. XIncluded XML files, images and entity files) in the correct relative paths as the import process is XInclude aware. 
2. Go to the main page of the Space you want to import into.
3. Select JBoss DocBook Importer from the Tools menu
4. Use form on this page, follow instructions on this page.

**** How to develop this plugin ****
************************************
Build is based on Maven 2 build tool, so build is simple.
Repository contains files for Eclipse IDE too so it's simple to import it as existing project into Eclipse.

Heart of plugin are XSLT files used to transform part of DocBook xml intended for one page into Confluence WIKI syntax:
/src/main/resources/org/jboss/confluence/plugin/docbook_tools/docbookimport/prepareChapterWIKIContent_4_3.xslt
/src/main/resources/org/jboss/confluence/plugin/docbook_tools/docbookimport/prepareChapterWIKIContent_5_0.xslt
So update these file if you want to change mapping of DocBook xml structures into WIKI syntax.
