/**
 * Copyright (c) 2008-2011 Sonatype, Inc.
 * All rights reserved. Includes the third-party code listed at http://www.sonatype.com/products/nexus/attributions.
 *
 * This program is free software: you can redistribute it and/or modify it only under the terms of the GNU Affero General
 * Public License Version 3 as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License Version 3
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License Version 3 along with this program.  If not, see
 * http://www.gnu.org/licenses.
 *
 * Sonatype Nexus (TM) Open Source Version is available from Sonatype, Inc. Sonatype and Sonatype Nexus are trademarks of
 * Sonatype, Inc. Apache Maven is a trademark of the Apache Foundation. M2Eclipse is a trademark of the Eclipse Foundation.
 * All other trademarks are the property of their respective owners.
 */
package org.sonatype.nexus.proxy.item;

import java.io.IOException;
import java.io.InputStream;

/**
 * An abstract wrapper for ContentLocator. It implements all methods, but is declared abstract. Subclass it to add some
 * spice.
 * 
 * @author cstamas
 */
public abstract class AbstractWrappingContentLocator
    implements ContentLocator
{
    private final ContentLocator contentLocator;

    public AbstractWrappingContentLocator( final ContentLocator contentLocator )
    {
        this.contentLocator = contentLocator;
    }

    protected ContentLocator getTarget()
    {
        return contentLocator;
    }

    @Override
    public InputStream getContent()
        throws IOException
    {
        return getTarget().getContent();
    }

    @Override
    public String getMimeType()
    {
        return getTarget().getMimeType();
    }

    @Override
    public boolean isReusable()
    {
        return getTarget().isReusable();
    }
}
