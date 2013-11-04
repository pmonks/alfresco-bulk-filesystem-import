/*
 * Copyright (C) 2007-2013 Peter Monks.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * This file is part of an unsupported extension to Alfresco.
 * 
 */

package org.alfresco.extension.bulkfilesystemimport.impl;

import java.io.File;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.alfresco.extension.bulkfilesystemimport.BulkImportStatus;
import org.alfresco.extension.bulkfilesystemimport.util.DataDictionaryBuilder;
import org.alfresco.repo.content.ContentStore;
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
    
    
    public SingleThreadedBulkFilesystemImporter(final ServiceRegistry       serviceRegistry,
                                                final BehaviourFilter       behaviourFilter,
                                                final ContentStore          configuredContentStore,
                                                final BulkImportStatusImpl  importStatus,
                                                final DataDictionaryBuilder dataDictionaryBuilder)
    {
        super(serviceRegistry, behaviourFilter, configuredContentStore, importStatus, dataDictionaryBuilder);
    }


    /**
     * @see org.alfresco.extension.bulkfilesystemimport.impl.AbstractBulkFilesystemImporter#bulkImportImpl(org.alfresco.service.cmr.repository.NodeRef, java.io.File, boolean, boolean)
     */
    @Override
    protected void bulkImportImpl(final NodeRef target, final File source, final boolean replaceExisting, final boolean inPlaceImport)
        throws Throwable
    {
        try
        {
            log.info("Bulk import started from '" + getFileName(source) + "'...");

            importStatus.startImport(getFileName(source),
                                     getRepositoryPath(target),
                                     inPlaceImport ? BulkImportStatus.ImportType.IN_PLACE : BulkImportStatus.ImportType.STREAMING,
                                     getBatchWeight());
            bulkImportRecursively(target, getFileName(source), source, replaceExisting, inPlaceImport);
            importStatus.importSucceeded();

            log.info("Bulk import from '" + getFileName(source) + "' succeeded.");
            logStatus(importStatus);
        }
        catch (final Throwable e)
        {
            log.error("Bulk import from '" + getFileName(source) + "' failed.", e);
            
            importStatus.importFailed(e);
            throw e;
        }
    }
    
    
    /**
     * Method that does the work of recursively importing a directory.  The only reason this method exists is to ensure subclasses can
     * cleanly override bulkImportImpl without messing up the mechanics of the recursion.
     * 
     * @param target          The target space to ingest the content into <i>(must not be null and must be a valid, writable space in the repository)</i>.
     * @param sourceRoot      The original directory from which this import was initiated <i>(must not be null)</i>.
     * @param source          The source directory on the local filesystem to read content from <i>(must not be null and must be a valid, readable directory on the local filesystem)</i>.
     * @param replaceExisting A flag indicating whether to replace (true) or skip (false) files that are already in the repository.
     * @param inPlaceImport   A flag indicating whether this is an "in place" import (i.e. the source directory is already located inside the configured content store).
     */
    protected final void bulkImportRecursively(final NodeRef target,
                                               final String  sourceRoot,
                                               final File    source,
                                               final boolean replaceExisting,
                                               final boolean inPlaceImport)
        throws InterruptedException
    {
        List<Pair<NodeRef, File>> subDirectories = importDirectory(target, sourceRoot, source, replaceExisting, inPlaceImport);
        
        // Recursively import sub directories
        for (final Pair<NodeRef, File> subDirectory : subDirectories)
        {
            if (Thread.interrupted()) break;  // Exit ASAP if the thread has been interrupted
            
            if (subDirectory != null)
            {
                bulkImportRecursively(subDirectory.getFirst(), sourceRoot, subDirectory.getSecond(), replaceExisting, inPlaceImport);
            }
        }
    }
    
}
