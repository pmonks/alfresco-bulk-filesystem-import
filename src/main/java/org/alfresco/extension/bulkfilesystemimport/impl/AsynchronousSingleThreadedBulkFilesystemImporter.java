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
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.extension.bulkfilesystemimport.BulkImportStatus;
import org.alfresco.extension.bulkfilesystemimport.util.DataDictionaryBuilder;
import org.alfresco.repo.content.ContentStore;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.repo.security.authentication.AuthenticationUtil.RunAsWork;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;


/**
 * Bulk Filesystem Importer that asynchronously loads the source on a single background thread (ie. the caller
 * immediately returns, and can then poll the status via the getImportStatus method).
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 */
public class AsynchronousSingleThreadedBulkFilesystemImporter
    extends SingleThreadedBulkFilesystemImporter
{
    private final static Log log = LogFactory.getLog(AsynchronousSingleThreadedBulkFilesystemImporter.class);
    
    
    private final ThreadFactory threadFactory;
    
    public AsynchronousSingleThreadedBulkFilesystemImporter(final ServiceRegistry       serviceRegistry,
                                                            final BehaviourFilter       behaviourFilter,
                                                            final ContentStore          configuredContentStore,
                                                            final BulkImportStatusImpl  importStatus,
                                                            final DataDictionaryBuilder dataDictionaryBuilder,
                                                            final ThreadFactory         threadFactory)
    {
        super(serviceRegistry, behaviourFilter, configuredContentStore, importStatus, dataDictionaryBuilder);
        
        this.threadFactory = threadFactory;
    }
    

    /**
     * @see org.alfresco.extension.bulkfilesystemimport.impl.SingleThreadedBulkFilesystemImporter#bulkImportImpl(org.alfresco.service.cmr.repository.NodeRef, java.io.File, boolean, boolean)
     */
    @Override
    protected void bulkImportImpl(final NodeRef target,
                                  final File    source,
                                  final boolean replaceExisting,
                                  final boolean inPlaceImport)
        throws Throwable
    {
        Runnable     backgroundLogic  = null;
        Thread       backgroundThread = null;
        final String currentUser      = AuthenticationUtil.getFullyAuthenticatedUser();

        backgroundLogic = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    if (log.isDebugEnabled()) log.debug("Background BulkFilesystemImporter thread started.");
                    
                    AuthenticationUtil.runAs(new RunAsWork<Object>()
                    {
                        @Override
                        public Object doWork()
                            throws Exception
                        {
                            try
                            {
                                log.info("Bulk import started from '" + getFileName(source) + "'...");

                                importStatus.startImport(getFileName(source),
                                                         getRepositoryPath(target),
                                                         inPlaceImport ? BulkImportStatus.ImportType.IN_PLACE : BulkImportStatus.ImportType.STREAMING,
                                                         getBatchWeight());
                                bulkImportRecursively(target, getFileName(source), source, replaceExisting, inPlaceImport);
                                importStatus.stopImport();

                                log.info("Bulk import from '" + getFileName(source) + "' succeeded.");
                                logStatus(importStatus);
                            }
                            catch (final Throwable t)
                            {
                                log.error("Bulk import from '" + getFileName(source) + "' failed.", t);
                                
                                importStatus.stopImport(t);
                                
                                // Ugh Java's checked exceptions are the pits!
                                if (t instanceof Exception)
                                {
                                    throw (Exception)t;
                                }
                                else
                                {
                                    throw new Exception(t);
                                }
                            }
                            return(null);
                        }
                    }, currentUser);
                }
                catch (final Exception e)
                {
                    // Log exception and swallow
                    if (log.isErrorEnabled()) log.error("Background BulkFilesystemImporter thread threw unexpected exception.", e);
                }
                finally
                {
                    if (log.isDebugEnabled()) log.debug("Background BulkFilesystemImporter thread complete.");
                }
            }
        };
        
        backgroundThread = threadFactory.newThread(backgroundLogic);
        backgroundThread.start();
    }
    
}
