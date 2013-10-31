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
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class is a DTO that represents an "importable item" - a series of files
 * that represent a single node (content OR space) in the repository.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 */
public final class ImportableItem
{
    private final String parentFilename;
    
    private ContentAndMetadata                     headRevision   = new ContentAndMetadata();
    private SortedSet<VersionedContentAndMetadata> versionEntries = null;
    
    
    public ImportableItem(final String parentFilename)
    {
        // PRECONDITIONS
        assert parentFilename != null             : "parentFilename must not be null.";
        assert parentFilename.trim().length() > 0 : "parentFilename must not be blank or empty.";
        
        // Body
        this.parentFilename = parentFilename;
    }

    
    public String getParentFilename()
    {
        return(parentFilename);
    }
    
    public FileType getFileType()
    {
        FileType result = FileType.UNKNOWN;
        
        if (headRevision.contentFileExists())
        {
            result = headRevision.getContentFileType();
        }
        else if (hasVersionEntries())
        {
            for (final VersionedContentAndMetadata versionEntry : versionEntries)
            {
                if (versionEntry.contentFileExists())
                {
                    result = versionEntry.getContentFileType();
                }
            }
        }
        
        return(result);
    }
    
    
    public boolean isValid()
    {
        return(headRevision.contentFileExists() || headRevision.metadataFileExists() || hasVersionEntries());
    }
    
    public ContentAndMetadata getHeadRevision()
    {
        return(headRevision);
    }
    
    
    /**
     * @return The "weight" of this importable item.  This is an approximation of how expensive it will
     * be to write this item into the repository.
     */
    public int weight()
    {
        int totalWeightOfVersions = 0;
        
        if (hasVersionEntries())
        {
            for (final VersionedContentAndMetadata versionEntry : versionEntries)
            {
                totalWeightOfVersions += versionEntry.weight();
            }
        }
        
        return(headRevision.weight() + totalWeightOfVersions);
    }

    /**
     * @return True if this ImportableItem has version entries.
     */
    public boolean hasVersionEntries()
    {
        return(versionEntries != null && versionEntries.size() > 0);
    }
    
    /**
     * @param versionLabel The version label to search for <i>(must not be null, empty or blank)</i>.
     * @return The version entry corresponding to that versionLabel <i>(may be null)</i>.
     */
    public VersionedContentAndMetadata getVersionEntry(final String versionLabel)
    {
        VersionedContentAndMetadata result = null;
    
        if (hasVersionEntries())
        {
            for (final ImportableItem.VersionedContentAndMetadata versionEntry : versionEntries)
            {
                if (versionEntry.getVersionLabel().equals(versionLabel))
                {
                    result = versionEntry;
                    break;
                }
            }
        }
    
        return(result);
    }
    
    
    /**
     * @return A read-only copy of all version entries in this importable item, in increasing version label order.
     */
    public Set<VersionedContentAndMetadata> getVersionEntries()
    {
        return(Collections.unmodifiableSet(versionEntries));
    }
    
    
    /**
     * @param versionEntry The version entry to add to the set of version entries in this importable item.
     */
    public void addVersionEntry(final VersionedContentAndMetadata versionEntry)
    {
        if (versionEntry != null)
        {
            if (versionEntries == null)
            {
                versionEntries = new TreeSet<VersionedContentAndMetadata>();
            }
                
            versionEntries.add(versionEntry);
        }
    }
    
    @Override
    public String toString()
    {
        return(new ToStringBuilder(this)
               .append("headRevision", headRevision)
               .append("versions",     versionEntries)
               .toString());
    }
    
    public class ContentAndMetadata
    {
        private File     contentFile           = null;
        private boolean  contentFileExists     = false;
        private boolean  contentFileIsReadable = false;
        private FileType contentFileType       = FileType.UNKNOWN;
        private long     contentFileSize       = -1;
        private Date     contentFileCreated    = null;
        private Date     contentFileModified   = null;
        private File     metadataFile          = null;
        private long     metadataFileSize      = -1;

        
        
        public final String getParentFileName()
        {
            return(parentFilename);
        }
        
        public final File getContentFile()
        {
            return(contentFile);
        }
        
        public final void setContentFile(final File contentFile)
        {
            this.contentFile = contentFile;
            
            if (contentFile != null)
            {
                // stat the file, to find out a few key details
                contentFileExists = contentFile.exists();
                
                if (contentFileExists)
                {
                    contentFileIsReadable = contentFile.canRead();
                    contentFileSize       = contentFile.length();
                    contentFileModified   = new Date(contentFile.lastModified());
                    contentFileCreated    = contentFileModified;    // TODO: determine proper file creation time (awaiting JDK 1.7 NIO2 library)
                    
                    if (contentFile.isFile())
                    {
                        contentFileType = FileType.FILE;
                    }
                    else if (contentFile.isDirectory())
                    {
                        contentFileType = FileType.DIRECTORY;
                    }
                    else
                    {
                        contentFileType = FileType.OTHER;
                    }
                }
            }
        }
        
        public final boolean contentFileExists()
        {
            return(contentFileExists);
        }
        
        public final boolean isContentFileReadable()
        {
            return(contentFileIsReadable);
        }
        
        public final FileType getContentFileType()
        {
            return(contentFileType);
        }
        
        public final long getContentFileSize()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file size if content file doesn't exist.");
            }
            
            return(contentFileSize);
        }
        
        public final Date getContentFileCreatedDate()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file creation date if content file doesn't exist.");
            }
            
            return(contentFileCreated);
        }
        
        public final Date getContentFileModifiedDate()
        {
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file modification date if content file doesn't exist.");
            }
            
            return(contentFileModified);
        }
        
        public final boolean metadataFileExists()
        {
            return(metadataFile != null);
        }
        
        public final File getMetadataFile()
        {
            return(metadataFile);
        }
        
        public final void setMetadataFile(final File metadataFile)
        {
            if (metadataFile != null && metadataFile.exists())
            {
                this.metadataFile     = metadataFile;
                this.metadataFileSize = metadataFile.length();
            }
        }
        
        public final long getMetadataFileSize()
        {
            if (!metadataFileExists())
            {
                throw new IllegalStateException("Cannot determine metadata file size if metadata file doesn't exist.");
            }
            
            return(metadataFileSize);
        }
        
        public final int weight()
        {
            return((contentFile  == null || !contentFileExists ? 0 : 1) +
                   (metadataFile == null ? 0 : 1));
        }

        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("contentFile",  (contentFileExists    ? contentFile.getAbsolutePath()  : null))
                   .append("metadatafile", (metadataFile != null ? metadataFile.getAbsolutePath() : null))
                   .toString());
        }
    }
    
    
    public class VersionedContentAndMetadata
        extends ContentAndMetadata
        implements Comparable<VersionedContentAndMetadata>
    {
        private final Pattern VERSION_NUMBER_PATTERN = Pattern.compile(DirectoryAnalyser.VERSION_LABEL_REGEX);
        
        private final int majorVersion;
        private final int minorVersion;


        public VersionedContentAndMetadata(final String versionLabel)
        {
            Matcher m = VERSION_NUMBER_PATTERN.matcher(versionLabel);
            
            if (!m.matches())
            {
                throw new IllegalArgumentException(versionLabel + " is not a valid version label.");
            }
            
            String majorVersionStr = m.group(1);
            String minorVersionStr = m.group(3);
            
            majorVersion = Integer.parseInt(majorVersionStr);
            
            if (minorVersionStr != null)
            {
                minorVersion = Integer.parseInt(minorVersionStr);
            }
            else
            {
                minorVersion = 0;
            }
        }
        
        public VersionedContentAndMetadata(final int majorVersion,
                                           final int minorVersion)
        {
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
        }
        
        public final int getMajorVersion()
        {
            return(majorVersion);
        }
        
        public final int getMinorVersion()
        {
            return(minorVersion);
        }
        
        public final String getVersionLabel()
        {
            return(majorVersion + "." + minorVersion);
        }
        
        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("version", getVersionLabel())
                   .appendSuper("")
                   .toString());
        }

        public int compareTo(final VersionedContentAndMetadata other)
        {
            return(this.majorVersion < other.majorVersion ? -1 :
                   this.majorVersion > other.majorVersion ?  1 :
                   this.minorVersion < other.minorVersion ? -1 :
                   this.minorVersion > other.minorVersion ?  1 : 0);
        }

        @Override
        public boolean equals(final Object other)
        {
            if (this == other)
            {
                return(true);
            }

            if (!(other instanceof VersionedContentAndMetadata))
            {
                return(false);
            }

            VersionedContentAndMetadata otherVCAM = (VersionedContentAndMetadata)other;

            return(this.majorVersion == otherVCAM.majorVersion &&
                   this.minorVersion == otherVCAM.minorVersion);
        }

        @Override
        public int hashCode()
        {
            return(majorVersion * 17 + minorVersion);
        }
    }

    
    public enum FileType
    {
        FILE,
        DIRECTORY,
        OTHER,
        UNKNOWN
    }
    
}
