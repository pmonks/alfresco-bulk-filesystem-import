/*
 * Copyright (C) 2007-2012 Peter Monks.
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

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * This class is a DTO that represents an "importable item" - a series of files
 * that represent a single node (content OR space) in the repository.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 */
public final class ImportableItem
{
    private ContentAndMetadata                     headRevision   = new ContentAndMetadata();
    private SortedSet<VersionedContentAndMetadata> versionEntries = null;
    
    
    public boolean isValid()
    {
        return(headRevision.contentFileExists() || headRevision.metadataFileExists());
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
    
    public Set<VersionedContentAndMetadata> getVersionEntries()
    {
        return(Collections.unmodifiableSet(versionEntries));
    }
    
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
               .append("HeadRevision", headRevision)
               .append("Versions", versionEntries)
               .toString());
    }
    
    public class ContentAndMetadata
    {
        private File     contentFile           = null;
        private boolean  contentFileExists     = false;
        private boolean  contentFileIsReadable = false;
        private FileType contentFileType       = null;
        private long     contentFileSize       = -1;
        private Date     contentFileCreated    = null;
        private Date     contentFileModified   = null;
        private File     metadataFile          = null;
        private long     metadataFileSize      = -1;

        
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
                    contentFileCreated    = contentFileModified;    //TODO: determine proper file creation time (awaiting JDK 1.7 NIO2 library)
                    
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
            if (!contentFileExists())
            {
                throw new IllegalStateException("Cannot determine content file type if content file doesn't exist.");
            }
            
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
            return((contentFile   == null || !contentFileExists ? 0 : 1) +
                   (metadataFile == null ? 0 : 1));
        }

        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("contentFile", (contentFileExists ? contentFile : null))
                   .append("metadatafile", metadataFile)
                   .toString());
        }
    }
    
    
    public class VersionedContentAndMetadata
        extends ContentAndMetadata
        implements Comparable<VersionedContentAndMetadata>
    {
        private int version;


        public VersionedContentAndMetadata(final int version)
        {
            this.version = version;
        }
        
        public final int getVersion()
        {
            return(version);
        }
        
        @Override
        public String toString()
        {
            return(new ToStringBuilder(this)
                   .append("version", version)
                   .appendSuper("")
                   .toString());
        }

        public int compareTo(final VersionedContentAndMetadata other)
        {
            return(this.version < other.version ? -1 :
                   this.version == other.version ? 0 : 1);
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

            return(this.version == otherVCAM.version);
        }

        @Override
        public int hashCode()
        {
            return(version);
        }
    }

    
    public enum FileType
    {
        FILE,
        DIRECTORY,
        OTHER
    }
    
}
