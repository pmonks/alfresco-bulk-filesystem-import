/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

package org.alfresco.extension.bulkfilesystemimport.impl;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;


/**
 * This ThreadFactory provides human-readable names for threads initiated by the Bulk Filesystem Importer.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @version $Id$
 *
 */
public class BulkFilesystemImporterThreadFactory
    implements ThreadFactory
{
    private final static String THREAD_NAME_PREFIX = "BulkFilesystemImportWorkerThread";
    
    private final static AtomicLong currentThreadNumber = new AtomicLong();
    
    
    /**
     * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
     */
    @Override
    public Thread newThread(final Runnable runnable)
    {
        final Thread result = Executors.defaultThreadFactory().newThread(runnable);
        
        result.setName(THREAD_NAME_PREFIX + currentThreadNumber.incrementAndGet());
        result.setDaemon(true);
        
        return(result);
    }

}
