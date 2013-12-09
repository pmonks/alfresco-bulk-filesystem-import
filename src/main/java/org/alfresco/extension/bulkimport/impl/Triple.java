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

package org.alfresco.extension.bulkimport.impl;


/**
 * Utility class for containing three things that aren't like each other.
 * 
 * @author Peter Monks (peter.monks@alfresco.com)
 */
public final class Triple<T, U, V>
{
    /**
     * The first member of the triple.
     */
    private final T first;
    
    /**
     * The second member of the triple.
     */
    private final U second;
    
    /**
     * The third member of the triple.
     */
    private final V third;
    
    /**
     * Make a new one.
     * 
     * @param first  The first member.
     * @param second The second member.
     * @param third  The third member.
     */
    public Triple(final T first, final U second, final V third)
    {
        this.first  = first;
        this.second = second;
        this.third  = third;
    }
    
    /**
     * Get the first member of the tuple.
     * @return The first member.
     */
    public T getFirst()
    {
        return first;
    }
    
    /**
     * Get the second member of the tuple.
     * @return The second member.
     */
    public U getSecond()
    {
        return second;
    }
    
    /**
     * Get the third member of the tuple.
     * @return The third member.
     */
    public V getThird()
    {
        return third;
    }
    
    /**
     * Override of equals.
     * @param other The thing to compare to.
     * @return equality.
     */
    public boolean equals(final Object other)
    {
        if (this == other)
        {
            return true;
        }
        
        if (!(other instanceof Triple))
        {
            return false;
        }
        
        Triple<?, ?, ?> o = (Triple<?, ?, ?>)other;
        return (first.equals(o.getFirst()) &&
                second.equals(o.getSecond()) &&
                third.equals(o.getThird()));
    }
    
    /**
     * Override of hashCode.
     */
    public int hashCode()
    {
        return ((first  == null ? 0 : first.hashCode()) +
                (second == null ? 0 : second.hashCode()) +
                (third  == null ? 0 : third.hashCode()));
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "(" + first + ", " + second + ", " + third + ")";
    }
}
