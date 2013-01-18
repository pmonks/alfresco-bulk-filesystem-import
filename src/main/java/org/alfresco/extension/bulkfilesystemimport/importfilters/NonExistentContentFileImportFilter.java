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

package org.alfresco.extension.bulkfilesystemimport.importfilters;

import org.alfresco.extension.bulkfilesystemimport.ImportableItem;
import org.alfresco.extension.bulkfilesystemimport.ImportFilter;


/**
 * This class is an <code>ImportFilter</code> that filters out importable items whose content file doesn't exist. 
 *
 * @author Peter Monks (pmonks@alfresco.com)
 */
public class NonExistentContentFileImportFilter
    implements ImportFilter
{
    /**
     * @see org.alfresco.extension.bulkfilesystemimport.ImportFilter#shouldFilter(org.alfresco.extension.bulkfilesystemimport.ImportableItem)
     */
    public boolean shouldFilter(final ImportableItem importableItem)
    {
        return(!importableItem.getHeadRevision().contentFileExists());
    }

}
