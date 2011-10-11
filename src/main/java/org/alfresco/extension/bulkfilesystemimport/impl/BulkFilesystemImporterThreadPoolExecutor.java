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

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class provides a simplified <code>ThreadPoolExecutor</code> that uses sensible defaults for the bulk filesystem import tool.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 */
public class BulkFilesystemImporterThreadPoolExecutor
    extends ThreadPoolExecutor
{
    private final static Log log = LogFactory.getLog(BulkFilesystemImporterThreadPoolExecutor.class);
    
    private final static int      DEFAULT_CORE_POOL_SIZE         = Runtime.getRuntime().availableProcessors();
    private final static int      DEFAULT_MAXIMUM_CORE_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;   // We naively assume 50+% of time is spent blocked on I/O
    private final static long     DEFAULT_KEEP_ALIVE_TIME        = 1L;
    private final static TimeUnit DEFAULT_KEEP_ALIVE_TIME_UNIT   = TimeUnit.MINUTES;
    
    
    public BulkFilesystemImporterThreadPoolExecutor()
    {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_CORE_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME_UNIT);
    }
    
    public BulkFilesystemImporterThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize)
    {
        this(corePoolSize, maximumPoolSize, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME_UNIT);
    }
    
    public BulkFilesystemImporterThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize, final long keepAliveTime, final TimeUnit keepAliveTimeUnit)
    {
        super(corePoolSize, maximumPoolSize, keepAliveTime, keepAliveTimeUnit, new LinkedBlockingQueue<Runnable>(), new BulkFilesystemImporterThreadFactory());
        
        if (log.isDebugEnabled()) log.debug("Creating new bulk import thread pool." +
                                            "\n\tcorePoolSize = " + corePoolSize +
                                            "\n\tmaximumPoolSize = " + maximumPoolSize +
                                            "\n\tkeepAliveTime = " + keepAliveTime + " " + String.valueOf(keepAliveTimeUnit));
    }
    
}
