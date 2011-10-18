/*
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.

 * As a special exception to the terms and conditions of version 2.0 of 
 * the GPL, you may redistribute this Program in connection with Free/Libre 
 * and Open Source Software ("FLOSS") applications as described in Alfresco's 
 * FLOSS exception.  You should have received a copy of the text describing 
 * the FLOSS exception, and it is also available here: 
 * http://www.alfresco.com/legal/licensing"
 */

package org.alfresco.extension.bulkfilesystemimport.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.content.filestore.FileContentStore;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.util.Pair;


/**
 * Bulk Filesystem Importer that synchronously loads the source on the caller's thread (ie. the caller will block until the entire
 * import is complete).
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 */
public class SingleThreadedBulkFilesystemImporter
    extends AbstractBulkFilesystemImporter
{
    private final static Log log = LogFactory.getLog(SingleThreadedBulkFilesystemImporter.class);
    
    
    public SingleThreadedBulkFilesystemImporter(final ServiceRegistry      serviceRegistry,
                                                final BehaviourFilter      behaviourFilter,
                                                final ContentStore         configuredContentStore,
                                                final BulkImportStatusImpl importStatus)
    {
        super(serviceRegistry, behaviourFilter, configuredContentStore, importStatus);
    }

    
    /**
     * @see org.alfresco.extension.bulkfilesystemimport.impl.AbstractBulkFilesystemImporter#bulkImportImpl(org.alfresco.service.cmr.repository.NodeRef, java.io.File, boolean, org.alfresco.repo.content.filestore.FileContentStore)
     */
    @Override
    protected void bulkImportImpl(final NodeRef target, final File source, final boolean replaceExisting, final FileContentStore contentStore)
        throws Throwable
    {
        try
        {
            log.info("Bulk import started from '" + getFileName(source) + "'...");

            importStatus.startImport(getFileName(source), getRepositoryPath(target), getBatchWeight());
            bulkImportRecursively(target, getFileName(source), source, replaceExisting, contentStore);
            importStatus.stopImport();

            log.info("Bulk import from '" + getFileName(source) + "' succeeded.");
            logStatus(importStatus);
        }
        catch (final Throwable e)
        {
            log.error("Bulk import from '" + getFileName(source) + "' failed.", e);
            
            importStatus.stopImport(e);
            throw e;
        }
    }
    
    
    /**
     * Method that does the work of recursively importing a directory.  The only reason this method exists is to ensure subclasses can
     * cleanly override bulkImportImpl without messing up the mechanics of the recursion.
     * 
     * @param target         The target space to ingest the content into <i>(must not be null and must be a valid, writable space in the repository)</i>.
     * @param sourceRoot     The original directory from which this import was initiated <i>(must not be null)</i>.
     * @param source         The source directory on the local filesystem to read content from <i>(must not be null and must be a valid, readable directory on the local filesystem)</i>.
     * @param replaceExisting A flag indicating whether to replace (true) or skip (false) files that are already in the repository.
     */
    protected final void bulkImportRecursively(final NodeRef          target,
                                               final String           sourceRoot,
                                               final File             source,
                                               final boolean          replaceExisting,
                                               final FileContentStore contentStore)
    {
        List<Pair<NodeRef, File>> subDirectories = importDirectory(target, sourceRoot, source, replaceExisting, contentStore);

        // Recursively import sub directories
        for (final Pair<NodeRef, File> subDirectory : subDirectories)
        {
            if (subDirectory != null)
            {
                bulkImportRecursively(subDirectory.getFirst(), sourceRoot, subDirectory.getSecond(), replaceExisting, contentStore);
            }
        }
    }
    
}
