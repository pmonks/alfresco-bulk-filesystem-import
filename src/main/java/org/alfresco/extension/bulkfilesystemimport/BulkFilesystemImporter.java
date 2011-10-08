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
     * @return A status object that describes the current state of the bulk filesystem importer.
     */
    BulkImportStatus getStatus();
}
