/*
 * Copyright (C) 2007-2013 Peter Monks.
 *               2010-2011 Ryan McVeigh Fixed issues #18 and #62.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.extension.bulkfilesystemimport.AnalysedDirectory;
import org.alfresco.extension.bulkfilesystemimport.DirectoryAnalyser;
import org.alfresco.extension.bulkfilesystemimport.ImportableItem;
import org.alfresco.extension.bulkfilesystemimport.ImportableItem.FileType;
import org.alfresco.extension.bulkfilesystemimport.MetadataLoader;


/**
 * This class provides the implementation for directory analysis, the process by
 * which a directory listing of files is broken up into ImportableItems.
 * 
 * @author Peter Monks (pmonks@alfresco.com)
 */
public final class DirectoryAnalyserImpl
    implements DirectoryAnalyser
{
    private final static Log log = LogFactory.getLog(DirectoryAnalyserImpl.class);
    
    private final static Pattern VERSION_SUFFIX_PATTERN = Pattern.compile(".+" + VERSION_SUFFIX_REGEX);

    private final MetadataLoader       metadataLoader;
    private final BulkImportStatusImpl importStatus;
    
    
    
    public DirectoryAnalyserImpl(final MetadataLoader       metadataLoader,
                                 final BulkImportStatusImpl importStatus)
    {
        this.metadataLoader = metadataLoader;
        this.importStatus   = importStatus;
    }
    

    /**
     * @see org.alfresco.extension.bulkfilesystemimport.DirectoryAnalyser#analyseDirectory(java.io.File)
     */
    public AnalysedDirectory analyseDirectory(final File directory)
    {
        final AnalysedDirectory        result          = new AnalysedDirectory();
        final Map<File,ImportableItem> importableItems = new HashMap<File,ImportableItem>();
        long                           start;
        long                           end;
        
        if (log.isDebugEnabled()) log.debug("Analysing directory " + AbstractBulkFilesystemImporter.getFileName(directory) + "...");

        start = System.nanoTime();
        result.originalListing = Arrays.asList(directory.listFiles());
        end = System.nanoTime();
        if (log.isTraceEnabled()) log.trace("List directory took: " + (float)(end - start) / (1000 * 1000 * 1000 )+ "s");

        start = System.nanoTime();
        // Build up the list of ImportableItems from the directory listing
        for (final File file : result.originalListing)
        {
            if (file.canRead())
            {
                if (isVersionFile(file))
                {
                    addVersionFile(importableItems, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else if (isMetadataFile(file))
                {
                    addMetadataFile(importableItems, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else
                {
                    boolean isDirectory = addParentFile(importableItems, file);
                    
                    if (isDirectory)
                    {
                        importStatus.incrementNumberOfFoldersScanned();
                    }
                    else
                    {
                        importStatus.incrementNumberOfFilesScanned();
                    }
                }
            }
            else
            {
                if (log.isWarnEnabled()) log.warn("Skipping unreadable file '" + AbstractBulkFilesystemImporter.getFileName(file) + "'.");
                
                importStatus.incrementNumberOfUnreadableEntries();
            }
        }
        end = System.nanoTime();
        if (log.isTraceEnabled()) log.trace("Build list of importable items took: " + (float)(end - start) / (1000 * 1000 * 1000 )+ "s");

        result.importableItems = new ArrayList<ImportableItem>(importableItems.values());

        start = System.nanoTime();
        // Finally, remove any items from the list that aren't valid (don't have either a
        // contentFile or a metadataFile)
        Iterator<ImportableItem> iter = result.importableItems.iterator();

        while (iter.hasNext())
        {
            ImportableItem importableItem = iter.next();

            if (!importableItem.isValid())
            {
                iter.remove();
            }
        }
        end = System.nanoTime();
        if (log.isTraceEnabled()) log.trace("Filter invalid importable items took: " + (float)(end - start) / (1000 * 1000 * 1000 )+ "s");

        if (log.isDebugEnabled()) log.debug("Finished analysing directory " + AbstractBulkFilesystemImporter.getFileName(directory) + ".");

        return(result);
    }


    private boolean isVersionFile(final File file)
    {
        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(file.getName());

        return(matcher.matches());
    }


    private boolean isMetadataFile(final File file)
    {
        boolean result = false;
        
        if (metadataLoader != null)
        {
            result = file.getName().endsWith(MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension());
        }
        
        return(result);
    }


    private void addVersionFile(final Map<File,ImportableItem> importableItems, final File versionFile)
    {
        File    parentContentFile = getParentOfVersionFile(versionFile);
        boolean isContentVersion  = false;

        if (isMetadataFile(parentContentFile))
        {
            parentContentFile = getParentOfMetadatafile(parentContentFile);
            isContentVersion  = false;
        }
        else
        {
            isContentVersion = true;
        }

        ImportableItem                             importableItem = findOrCreateImportableItem(importableItems, parentContentFile);
        int                                        version        = getVersionNumber(versionFile);
        ImportableItem.VersionedContentAndMetadata versionEntry   = findOrCreateVersionEntry(importableItem, version);

        if (isContentVersion)
        {
            versionEntry.setContentFile(versionFile);
        }
        else
        {
            versionEntry.setMetadataFile(versionFile);
        }
    }


    private void addMetadataFile(final Map<File,ImportableItem> importableItems, final File metadataFile)
    {
        final File parentContentfile = getParentOfMetadatafile(metadataFile);

        ImportableItem importableItem = findOrCreateImportableItem(importableItems, parentContentfile);

        importableItem.getHeadRevision().setMetadataFile(metadataFile);
    }


    private boolean addParentFile(final Map<File,ImportableItem> importableItems, final File contentFile)
    {
        ImportableItem importableItem = findOrCreateImportableItem(importableItems, contentFile);

        importableItem.getHeadRevision().setContentFile(contentFile);
        
        return(importableItem.getHeadRevision().getContentFileType() == FileType.DIRECTORY);
    }


    private ImportableItem findOrCreateImportableItem(final Map<File,ImportableItem> importableItems,
                                                      final File                     contentFile)
    {
        ImportableItem result = importableItems.get(contentFile);

        // We didn't find it, so create it
        if (result == null)
        {
            result = new ImportableItem();
            result.getHeadRevision().setContentFile(contentFile);
            importableItems.put(contentFile, result);
        }

        return(result);
    }


    private ImportableItem.VersionedContentAndMetadata findOrCreateVersionEntry(final ImportableItem importableItem, final int version)
    {
        ImportableItem.VersionedContentAndMetadata result = findVersionEntry(importableItem, version);

        if (result == null)
        {
            result = importableItem.new VersionedContentAndMetadata(version);
            
            importableItem.addVersionEntry(result);
        }

        return (result);
    }


    private ImportableItem.VersionedContentAndMetadata findVersionEntry(final ImportableItem importableItem, final int version)
    {
        ImportableItem.VersionedContentAndMetadata result = null;

        // Note: it would be better to store VersionEntries in a Map as well, and convert to a list later on, but in most
        // cases version histories are small enough that we can get away with the inefficiencies of finding items in lists.
        // See http://code.google.com/p/alfresco-bulk-filesystem-import/issues/detail?id=91 for a related problem.
        if (importableItem.hasVersionEntries())
        {
            for (final ImportableItem.VersionedContentAndMetadata versionEntry : importableItem.getVersionEntries())
            {
                if (version == versionEntry.getVersion())
                {
                    result = versionEntry;
                    break;
                }
            }
        }

        return(result);
    }


    private int getVersionNumber(final File versionFile)
    {
        int result = -1;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(AbstractBulkFilesystemImporter.getFileName(versionFile) + " is not a version file.");
        }

        Matcher matcher = VERSION_SUFFIX_PATTERN.matcher(versionFile.getName());
        String versionStr = null;

        if (matcher.matches())
        {
            versionStr = matcher.group(1);
        }
        else
        {
            throw new IllegalStateException("WTF?!?!?"); // ####TODO!!!!
        }

        result = Integer.parseInt(versionStr);

        return(result);
    }


    private File getParentOfVersionFile(final File versionFile)
    {
        File result = null;

        if (!isVersionFile(versionFile))
        {
            throw new IllegalStateException(AbstractBulkFilesystemImporter.getFileName(versionFile) + " is not a version file.");
        }

        String parentFilename = versionFile.getName().replaceFirst(VERSION_SUFFIX_REGEX, "");

        result = new File(versionFile.getParent(), parentFilename);
        
        return(result);
    }


    private File getParentOfMetadatafile(final File metadataFile)
    {
        File result = null;

        if (!isMetadataFile(metadataFile))
        {
            throw new IllegalStateException(AbstractBulkFilesystemImporter.getFileName(metadataFile) + " is not a metadata file.");
        }

        String name = metadataFile.getName();
        String contentName = name.substring(0, name.length() - (MetadataLoader.METADATA_SUFFIX + metadataLoader.getMetadataFileExtension()).length());

        result = new File(metadataFile.getParent(), contentName);

        return(result);
    }
}
