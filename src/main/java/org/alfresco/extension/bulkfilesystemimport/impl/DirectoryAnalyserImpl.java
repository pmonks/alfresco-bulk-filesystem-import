/*
 * Copyright (C) 2005-2011 Alfresco Software Limited.
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
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
 * @version $Id: DirectoryAnalyserImpl.java 116 2011-08-03 23:55:41Z pmonks@gmail.com $
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
        AnalysedDirectory result = new AnalysedDirectory();
        
        if (log.isDebugEnabled()) log.debug("Analysing directory " + AbstractBulkFilesystemImporter.getFileName(directory) + "...");

        result.originalListing = Arrays.asList(directory.listFiles());
        result.importableItems = new ArrayList<ImportableItem>();

        // Build up the list of ImportableItems from the directory listing
        for (final File file : result.originalListing)
        {
            if (log.isTraceEnabled()) log.trace("Scanning file " + AbstractBulkFilesystemImporter.getFileName(file) + "...");
            
            if (file.canRead())
            {
                if (isVersionFile(file))
                {
                    addVersionFile(result.importableItems, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else if (isMetadataFile(file))
                {
                    addMetadataFile(result.importableItems, file);
                    importStatus.incrementNumberOfFilesScanned();
                }
                else
                {
                    boolean isDirectory = addParentFile(result.importableItems, file);
                    
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
                if (log.isWarnEnabled()) { log.warn("Skipping unreadable file '" + AbstractBulkFilesystemImporter.getFileName(file) + "'."); }
                
                importStatus.incrementNumberOfUnreadableEntries();
            }
        }

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


    private void addVersionFile(final List<ImportableItem> importableItems, final File versionFile)
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


    private void addMetadataFile(final List<ImportableItem> importableItems, final File metadataFile)
    {
        final File parentContentfile = getParentOfMetadatafile(metadataFile);

        ImportableItem importableItem = findOrCreateImportableItem(importableItems, parentContentfile);

        importableItem.getHeadRevision().setMetadataFile(metadataFile);
    }


    private boolean addParentFile(final List<ImportableItem> importableItems, final File contentFile)
    {
        ImportableItem importableItem = findOrCreateImportableItem(importableItems, contentFile);

        importableItem.getHeadRevision().setContentFile(contentFile);
        
        return(importableItem.getHeadRevision().getContentFileType() == FileType.DIRECTORY);
    }


    private ImportableItem findOrCreateImportableItem(final List<ImportableItem> importableItems,
                                                      final File                 contentFile)
    {
        ImportableItem result = findImportableItem(importableItems, contentFile);

        // We didn't find it, so create it
        if (result == null)
        {
            result = new ImportableItem();
            result.getHeadRevision().setContentFile(contentFile);
            importableItems.add(result);
        }

        return(result);
    }


    private ImportableItem findImportableItem(final List<ImportableItem> importableItems, final File contentFile)
    {
        ImportableItem result = null;

        if (contentFile == null)
        {
            throw new IllegalStateException("Cannot call findOrCreateImportableItem with null key");
        }

        for (final ImportableItem importableItem : importableItems)
        {
            if (contentFile.equals(importableItem.getHeadRevision().getContentFile()))
            {
                result = importableItem;
                break;
            }
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
