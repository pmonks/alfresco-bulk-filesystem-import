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

package org.alfresco.extension.bulkfilesystemimport.metadataloaders;


import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.alfresco.extension.bulkfilesystemimport.ImportableItem.ContentAndMetadata;
import org.alfresco.extension.bulkfilesystemimport.MetadataLoader;
import org.alfresco.extension.bulkfilesystemimport.impl.AbstractBulkFilesystemImporter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;


/**
 * Abstract MetadataLoader abstracts out the common features of loading metadata
 * from a <code>java.util.Map</code>, regardless of where it came from.
 *
 * @author Peter Monks (pmonks@alfresco.com)
 * @see MetadataLoader
 */
abstract class AbstractMapBasedMetadataLoader
    implements MetadataLoader
{
    private final static Log log = LogFactory.getLog(AbstractMapBasedMetadataLoader.class);
    
    private final static String PROPERTY_NAME_TYPE         = "type";
    private final static String PROPERTY_NAME_ASPECTS      = "aspects";
    private final static String PROPERTY_NAME_PARENT_ASSOC = "parentAssoc";

    private final static QName DATATYPE_CONTENT = QName.createQName("d:content");
    
    private final static String DEFAULT_MULTI_VALUED_SEPARATOR = ",";
    
    protected final NamespaceService  namespaceService;
    protected final DictionaryService dictionaryService; 
    protected final String            multiValuedSeparator;
    protected final String            metadataFileExtension;
    
    
    
    protected AbstractMapBasedMetadataLoader(final ServiceRegistry serviceRegistry, final String fileExtension)
    {
        this(serviceRegistry, DEFAULT_MULTI_VALUED_SEPARATOR, fileExtension);
    }
    
    
    protected AbstractMapBasedMetadataLoader(final ServiceRegistry serviceRegistry, final String multiValuedSeparator, final String fileExtension)
    {
        // PRECONDITIONS
        assert serviceRegistry      != null : "serviceRegistry must not be null";
        assert multiValuedSeparator != null : "multiValuedSeparator must not be null";
        
        // Body
        this.namespaceService      = serviceRegistry.getNamespaceService();
        this.dictionaryService     = serviceRegistry.getDictionaryService();
        this.multiValuedSeparator  = multiValuedSeparator;
        this.metadataFileExtension = fileExtension;
    }
    

    /**
     * @see org.alfresco.extension.bulkfilesystemimport.MetadataLoader#getMetadataFileExtension()
     */
    @Override
    public final String getMetadataFileExtension()
    {
        return(metadataFileExtension);
    }
    
    
    /**
     * Method that actually loads the properties from the file. 
     * @param metadataFile The file to load the properties from <i>(must not be null)</i>.
     * @return A new <code>Properties</code> object loaded from that file.
     */
    abstract protected Map<String,Serializable> loadMetadataFromFile(final File metadataFile);


    /**
     * @see org.alfresco.extension.bulkfilesystemimport.MetadataLoader#loadMetadata(org.alfresco.extension.bulkfilesystemimport.ImportableItem.ContentAndMetadata, org.alfresco.extension.bulkfilesystemimport.MetadataLoader.Metadata)
     */
    @Override
    public final void loadMetadata(final ContentAndMetadata contentAndMetadata, Metadata metadata)
    {
        if (contentAndMetadata.metadataFileExists())
        {
            final File metadataFile = contentAndMetadata.getMetadataFile();

            if (metadataFile.canRead())
            {
                Map<String,Serializable> metadataProperties = loadMetadataFromFile(metadataFile);
                
                if (metadataProperties != null)
                {
                    for (String key : metadataProperties.keySet())
                    {
                        if (PROPERTY_NAME_TYPE.equals(key))
                        {
                            String typeName = (String)metadataProperties.get(key);
                            QName  type     = QName.createQName(typeName, namespaceService);
                            
                            metadata.setType(type);
                        }
                        else if (PROPERTY_NAME_ASPECTS.equals(key))
                        {
                            String[] aspectNames = ((String)metadataProperties.get(key)).split(",");
                            
                            for (final String aspectName : aspectNames)
                            {
                                QName aspect = QName.createQName(aspectName.trim(), namespaceService);
                                metadata.addAspect(aspect);
                            }
                        }
                        else if (PROPERTY_NAME_PARENT_ASSOC.equals(key))
                        {
                            String parentAssocName = (String)metadataProperties.get(key);
                            QName  parentAssoc     = QName.createQName(parentAssocName, namespaceService);
                            
                            metadata.setParentAssoc(parentAssoc);
                        }
                        else  // Any other key => property
                        {
                            //####TODO: Issue #62: figure out how to handle properties of type cm:content - they need to be streamed in via a Writer
                        	QName              name               = QName.createQName(key, namespaceService);
                        	PropertyDefinition propertyDefinition = dictionaryService.getProperty(name);  // TODO: measure performance impact of this API call!!
                        	
                        	if (propertyDefinition != null)
                        	{
                            	if (propertyDefinition.isMultiValued())
                            	{
                                    // Multi-valued property
                            		ArrayList<Serializable> values = new ArrayList<Serializable>(Arrays.asList(((String)metadataProperties.get(key)).split(multiValuedSeparator)));
                            	    metadata.addProperty(name, values);
                            	}
                            	else
                            	{
                            	    // Single value property
                            		metadata.addProperty(name, metadataProperties.get(key));
                            	}
                        	}
                        	else
                        	{
                        	    if (log.isWarnEnabled()) log.warn("Property " + String.valueOf(name) + " doesn't exist in the Data Dictionary.  Ignoring it.");
                        	}
                        }
                    }
                }
            }
            else
            {
                if (log.isWarnEnabled()) log.warn("Metadata file '" + AbstractBulkFilesystemImporter.getFileName(metadataFile) + "' is not readable.");
            }
        }
    }

}
