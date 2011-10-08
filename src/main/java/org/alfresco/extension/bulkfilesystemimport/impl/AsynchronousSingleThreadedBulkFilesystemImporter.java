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
import java.util.concurrent.ThreadFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
 * @version $Id: AsynchronousSingleThreadedBulkFilesystemImporter.java 116 2011-08-03 23:55:41Z pmonks@gmail.com $
 */
public class AsynchronousSingleThreadedBulkFilesystemImporter
    extends SingleThreadedBulkFilesystemImporter
{
    private final static Log log = LogFactory.getLog(AsynchronousSingleThreadedBulkFilesystemImporter.class);
    
    
    private final ThreadFactory threadFactory;
    
    public AsynchronousSingleThreadedBulkFilesystemImporter(final ServiceRegistry      serviceRegistry,
                                                            final BehaviourFilter      behaviourFilter,
                                                            final BulkImportStatusImpl importStatus,
                                                            final ThreadFactory        threadFactory)
    {
        super(serviceRegistry, behaviourFilter, importStatus);
        
        this.threadFactory = threadFactory;
    }
    

    /**
     * @see org.alfresco.extension.bulkfilesystemimport.impl.AbstractBulkFilesystemImporter#bulkImportImpl(org.alfresco.service.cmr.repository.NodeRef, java.io.File, boolean)
     */
    @Override
    protected void bulkImportImpl(final NodeRef target,
                                  final File    source,
                                  final boolean replaceExisting)
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

                                importStatus.startImport(getFileName(source), getRepositoryPath(target), getBatchWeight());
                                bulkImportRecursively(target, getFileName(source), source, replaceExisting);
                                importStatus.stopImport();

                                log.info("Bulk import from '" + getFileName(source) + "' succeeded.");
                                
                                if (log.isDebugEnabled())
                                {
                                    log.debug("Bulk Import Statistics:" +
                                              "Scanned:" +
                                              "\n\tFiles:                  " + importStatus.getNumberOfFilesScanned() +
                                              "\n\tFolders:                " + importStatus.getNumberOfFoldersScanned() +
                                              "Read:" +
                                              "\n\tContent files:          " + importStatus.getNumberOfContentFilesRead() +
                                              " (" +importStatus.getNumberOfContentBytesRead() + " bytes)" +
                                              "\n\tMetadata files:         " + importStatus.getNumberOfMetadataFilesRead() +
                                              " (" + importStatus.getNumberOfMetadataBytesRead() + " bytes)" +
                                              "\n\tContent version files:  " + importStatus.getNumberOfContentVersionFilesRead() +
                                              " (" + importStatus.getNumberOfContentVersionBytesRead() + " bytes)" +
                                              "\n\tMetadata version files: " + importStatus.getNumberOfMetadataVersionFilesRead() +
                                              " (" + importStatus.getNumberOfMetadataVersionBytesRead() + " bytes)" +
                                              "Written:" +
                                              "\n\tContent nodes created:  " + importStatus.getNumberOfContentNodesCreated() +
                                              "\n\tContent nodes replaced: " + importStatus.getNumberOfContentNodesReplaced() +
                                              "\n\tContent nodes skipped:  " + importStatus.getNumberOfContentNodesSkipped() +
                                              "\n\tSpace nodes created:    " + importStatus.getNumberOfSpaceNodesCreated() +
                                              "\n\tSpace nodes replaced:   " + importStatus.getNumberOfSpaceNodesReplaced() +
                                              "\n\tSpace nodes skipped:    " + importStatus.getNumberOfSpaceNodesSkipped());
                                }
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
