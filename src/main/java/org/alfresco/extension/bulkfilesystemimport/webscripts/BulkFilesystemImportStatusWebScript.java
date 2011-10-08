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

package org.alfresco.extension.bulkfilesystemimport.webscripts;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import org.alfresco.extension.bulkfilesystemimport.BulkFilesystemImporter;


/**
 * Web Script class that provides status information on the bulk filesystem import process.
 *
 * @author Peter Monks (peter.monks@alfresco.com)
 */
public class BulkFilesystemImportStatusWebScript
    extends DeclarativeWebScript
{
    private final static Log log = LogFactory.getLog(BulkFilesystemImportStatusWebScript.class);
    
    
    // Output parameters (for Freemarker)
    private final static String RESULT_IMPORT_STATUS = "importStatus";
    
    // Attributes
    private final BulkFilesystemImporter importer;
    
    
    public BulkFilesystemImportStatusWebScript(final BulkFilesystemImporter importer)
    {
        // PRECONDITIONS
        assert importer != null : "importer must not be null.";
        
        //BODY
        this.importer = importer;
    }
    

    /**
     * @see org.alfresco.web.scripts.DeclarativeWebScript#executeImpl(org.alfresco.web.scripts.WebScriptRequest, org.alfresco.web.scripts.Status, org.alfresco.web.scripts.Cache)
     */
    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest request,
                                              final Status           status,
                                              final Cache            cache)
    {
        Map<String, Object> result = new HashMap<String, Object>();
        
        cache.setNeverCache(true);
        
        result.put(RESULT_IMPORT_STATUS, importer.getStatus());
        
        return(result);
    }
    
}
