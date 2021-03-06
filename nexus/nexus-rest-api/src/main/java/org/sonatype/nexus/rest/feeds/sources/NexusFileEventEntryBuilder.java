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
package org.sonatype.nexus.rest.feeds.sources;

import org.codehaus.plexus.component.annotations.Component;
import org.sonatype.nexus.feeds.NexusArtifactEvent;

/**
 * Build feeds entry based on files
 * 
 * @author Juven Xu
 */
@Component( role = SyndEntryBuilder.class, hint = "file" )
public class NexusFileEventEntryBuilder
    extends AbstractNexusItemEventEntryBuilder
{

    @Override
    protected String buildTitle( NexusArtifactEvent event )
    {
        return buildFileName( event );
    }

    private String buildFileName( NexusArtifactEvent event )
    {
        return buildFilePath( event ).substring( buildFilePath( event ).lastIndexOf( "/" ) + 1 );
    }

    private String buildFilePath( NexusArtifactEvent event )
    {
        return event.getNexusItemInfo().getPath();
    }

    @Override
    protected String buildDescriptionMsgItem( NexusArtifactEvent event )
    {
        StringBuilder msg = new StringBuilder();

        msg.append( "The file '" );

        msg.append( buildFileName( event ) );

        msg.append( "' in repository '" );

        msg.append( getRepositoryName( event ) );

        msg.append( "' with path '" );

        msg.append( buildFilePath( event ) );

        msg.append( "'" );

        return msg.toString();
    }

}
