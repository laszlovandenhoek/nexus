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
package org.sonatype.nexus.log.internal;

import java.io.IOException;
import java.io.InputStream;


import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.log.LogConfigurationParticipant;

/**
 * Contributes "logback-events" to logback configuration.
 * 
 * @author adreghiciu@gmail.com
 */
@Component( role = LogConfigurationParticipant.class, hint="logback-events" )
public class LogbackNexusEventSystemLogConfigurationParticipant
    implements LogConfigurationParticipant
{
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getName()
    {
        return "logback-events.xml";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public InputStream getConfiguration()
    {
        try
        {
            return this.getClass().getResource( "/META-INF/log/logback-events.xml" ).openStream();
        }
        catch ( IOException e )
        {
            throw new IllegalStateException( "Could not access logback-events.xml", e );
        }
    }

}
