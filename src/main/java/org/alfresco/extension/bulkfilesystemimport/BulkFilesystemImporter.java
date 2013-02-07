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

package org.alfresco.extension.bulkfilesystemimport;

import java.io.File;

import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Interface defining a bulk filesystem importer.
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 */
public interface BulkFilesystemImporter
{
    /**
     * Initiates a bulk filesystem import.  getStatus().inProgress() must be false prior to calling this method or an Exception will be thrown.
     * 
     * @param target         The target space to ingest the content into <i>(must not be null and must be a valid, writable space in the repository)</i>.
     * @param source         The source directory on the local filesystem to read content from <i>(must not be null and must be a valid, readable directory on the local filesystem)</i>.
     * @param replaceExisting A flag indicating whether to replace (true) or skip (false) files that are already in the repository.
     */
    void bulkImport(NodeRef target, File source, boolean replaceExisting) throws Throwable;
    
    /**
     * Stops an import, if one is in progress (which can be determined by calling getStatus().inProgress().
     * Note that this is done asynchronously - it may take a little while for in-progress transactions to complete.
     */
    void stopImport();
    
    /**
     * @return A status object that describes the current state of the bulk filesystem importer.
     */
    BulkImportStatus getStatus();
}
