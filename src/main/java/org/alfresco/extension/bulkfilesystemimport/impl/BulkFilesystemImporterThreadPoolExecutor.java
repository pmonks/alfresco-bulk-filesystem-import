/*
 * Copyright (C) 2007-2013 Peter Monks.
 *               2012      Alain Sahli - Fix for issue 109: http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=109.
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
    private final static int      DEFAULT_BLOCKING_QUEUE_SIZE    = 100000;
    
    
    public BulkFilesystemImporterThreadPoolExecutor()
    {
        this(DEFAULT_CORE_POOL_SIZE, DEFAULT_MAXIMUM_CORE_POOL_SIZE, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME_UNIT, DEFAULT_BLOCKING_QUEUE_SIZE);
    }
    
    public BulkFilesystemImporterThreadPoolExecutor(final int corePoolSize, final int maximumPoolSize)
    {
        this(corePoolSize, maximumPoolSize, DEFAULT_KEEP_ALIVE_TIME, DEFAULT_KEEP_ALIVE_TIME_UNIT, DEFAULT_BLOCKING_QUEUE_SIZE);
    }
    
    public BulkFilesystemImporterThreadPoolExecutor(final int      corePoolSize,
                                                    final int      maximumPoolSize,
                                                    final long     keepAliveTime,
                                                    final TimeUnit keepAliveTimeUnit,
                                                    final int      blockingQueueSize)
    {
        super(corePoolSize      <= 0    ? DEFAULT_CORE_POOL_SIZE         : corePoolSize,
              maximumPoolSize   <= 0    ? DEFAULT_MAXIMUM_CORE_POOL_SIZE : maximumPoolSize,
              keepAliveTime     <= 0    ? DEFAULT_KEEP_ALIVE_TIME        : keepAliveTime,
              keepAliveTimeUnit == null ? DEFAULT_KEEP_ALIVE_TIME_UNIT   : keepAliveTimeUnit,
              new LinkedBlockingQueue<Runnable>(blockingQueueSize <= 0 ? DEFAULT_BLOCKING_QUEUE_SIZE : blockingQueueSize),
              new BulkFilesystemImporterThreadFactory());

        // This won't work, since it allows for out-of-order execution, and also doesn't allow the thread pool to be cleanly shutdown
//        this.setRejectedExecutionHandler(new CallerRunsPolicy());
        
        if (log.isDebugEnabled()) log.debug("Creating new bulk import thread pool." +
                                            "\n\tcorePoolSize = " + corePoolSize +
                                            "\n\tmaximumPoolSize = " + maximumPoolSize +
                                            "\n\tkeepAliveTime = " + keepAliveTime + " " + String.valueOf(keepAliveTimeUnit) +
                                            "\n\tblockingQueueSize = " + blockingQueueSize);
    }
    
}
