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
package org.sonatype.nexus.integrationtests.nexus4268;

import java.io.IOException;

import org.restlet.data.Method;
import org.restlet.data.Response;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.RequestFacade;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * This test tests NEXUS-4268 and Nexus' capability to route properly repository types contributed by plugins, hence
 * their role and also implementation comes from core's child classloader (the plugin classloader). For this purpose,
 * nexus-it-helper-plugin got {@link org.sonatype.nexus.plugins.repository.SimpleRepository} repository type, and this
 * IT in it's resources {@code test-config} delivers a configuration that contains this new repository type with id
 * "simple" defined. We test it's reachability over {@code /content/repositories/simple} but also
 * {@code /contenet/simply/simple} since the new repository type defines "simply" as path prefix (the
 * {@code repositories} path prefix is reserved for ALL repositories (by design).
 * 
 * @author cstamas
 */
public class Nexus4268NewPluginContributedRepositoryTypeRoutingIT
    extends AbstractNexusIntegrationTest
{
    @Test
    public void testRepositoriesPath()
        throws IOException
    {
        // note the ending slash! We query the repo root, and slash is there to
        // avoid redirect
        Response response = null;

        try
        {
            final String servicePath = "content/repositories/simple/";

            response = RequestFacade.sendMessage( servicePath, Method.GET );

            Assert.assertEquals( response.getStatus().getCode(), 200, "Repository should be accessible over "
                + servicePath );
        }
        finally
        {
            RequestFacade.releaseResponse( response );
        }
    }

    @Test
    public void testPathPrefixPath()
        throws IOException
    {
        // note the ending slash! We query the repo root, and slash is there to
        // avoid redirect
        Response response = null;

        try
        {
            final String servicePath = "content/simply/simple/";

            response = RequestFacade.sendMessage( servicePath, Method.GET );

            Assert.assertEquals( response.getStatus().getCode(), 200, "Repository should be accessible over "
                + servicePath );
        }
        finally
        {
            RequestFacade.releaseResponse( response );
        }
    }
}
