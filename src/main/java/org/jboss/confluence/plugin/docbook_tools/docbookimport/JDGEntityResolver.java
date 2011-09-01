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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.jboss.confluence.plugin.docbook_tools.utils.FileUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Our custom Entity resolver to handle:
 * <ul>
 * <li><code>/Common_Content/</code> placed resources.
 * <li>Cache external (<code>http://</code> loaded) DocBook resources for better performance in memory (memCache) and on
 * filesystem (fsCache).
 * <li>Calls wrapped resolver in other cases
 * </ul>
 * 
 * @author Vlastimil Elias (velias at redhat dot com)
 */
public class JDGEntityResolver implements EntityResolver {

  private static final Logger log = Logger.getLogger(JDGEntityResolver.class);

  /**
   * Max number of entries in memCache.
   */
  private static final int MAX_CACHE_ENTRIES = 100;

  /**
   * memCache structure - LRU type cache
   */
  private static final Map<String, byte[]> memCache = Collections.synchronizedMap(new LinkedHashMap<String, byte[]>(
      MAX_CACHE_ENTRIES, .75F, true) {
    protected boolean removeEldestEntry(Map.Entry<String, byte[]> eldest) {
      return size() > MAX_CACHE_ENTRIES;
    }
  });

  /**
   * Directory for filesystem cache. Leave null to disable fsCache.
   */
  private static File fsCacheDir = FileUtils.prepareDirectoryInTempArea("JDGEntityResolverCache");

  /**
   * Flag if memCache may be used or not
   */
  private static final boolean useMemCache = MAX_CACHE_ENTRIES > 0;

  private EntityResolver wrapped;

  /**
   * Constructor.
   * 
   * @param wrapped resolver to be wrapped. Called if
   */
  public JDGEntityResolver(EntityResolver wrapped) {
    super();
    this.wrapped = wrapped;
  }

  @Override
  public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
    if (systemId.contains("Common_Content/")) {
      // simulate valid XML
      if (systemId.toLowerCase().endsWith(".xml")) {
        return new InputSource(new ByteArrayInputStream("<?xml version='1.0'?><a></a>".getBytes()));
      }
      // empty for others
      return new InputSource(new ByteArrayInputStream(new byte[] {}));
    }

    if (systemId != null) {

      if (useMemCache && memCache.containsKey(systemId)) {
        log.debug("MemCache hit for external resource: " + systemId);
        InputSource is = new InputSource(new ByteArrayInputStream(memCache.get(systemId)));
        is.setSystemId(systemId);
        return is;
      }
      if (systemId.toLowerCase().startsWith("http://")) {

        byte[] res_is = null;

        String fsCacheKey = generateFsCacheKey(systemId);
        res_is = getFromFSCache(fsCacheKey);

        if (res_is != null) {
          log.debug("FsCache hit for external resource: " + systemId);
          if (useMemCache) {
            memCache.put(systemId, res_is);
            log.debug("MemCached fsCache loaded external resource: " + systemId);
          }
        } else {
          InputStream is = null;
          try {
            URL url = new URL(systemId);
            is = url.openStream();

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            int count;
            byte buf[] = new byte[2048];
            while ((count = is.read(buf, 0, 2048)) != -1) {
              os.write(buf, 0, count);
            }

            res_is = os.toByteArray();
            writeToFSCache(fsCacheKey, new ByteArrayInputStream(res_is));

            if (useMemCache) {
              memCache.put(systemId, res_is);
              log.debug("MemCached external resource: " + systemId);
            }
          } catch (Exception e) {
            String msg = "Error retrieving external resource from URL " + systemId + " with message: " + e.getMessage();
            log.warn(msg);
            throw new IOException(msg);
          } finally {
            if (is != null) {
              is.close();
            }
          }
        }

        if (res_is != null) {
          InputSource cis = new InputSource(new ByteArrayInputStream(res_is));
          cis.setSystemId(systemId);
          return cis;
        }
      }
    }

    if (wrapped != null) {
      return wrapped.resolveEntity(publicId, systemId);
    }
    return null;
  }

  private byte[] getFromFSCache(String fsCacheKey) throws IOException {
    if (fsCacheDir != null) {
      File f = new File(fsCacheDir, fsCacheKey);
      if (f.exists() && f.canRead()) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        FileUtils.copyFile(new FileInputStream(f), bos);
        return bos.toByteArray();
      }
    }
    return null;
  }

  private void writeToFSCache(String fsCacheKey, InputStream is) {
    if (fsCacheDir != null) {
      File f = new File(fsCacheDir, fsCacheKey);
      try {
        FileUtils.copyFile(is, new FileOutputStream(f));
      } catch (Exception e) {
        log.warn("Error writing external resource to FSCache" + e.getMessage());
      }
    }
  }

  private String generateFsCacheKey(String systemId) {
    return systemId.replaceAll("\\W", "_");
  }

}
