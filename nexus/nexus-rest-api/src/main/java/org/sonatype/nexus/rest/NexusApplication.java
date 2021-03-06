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
package org.sonatype.nexus.rest;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Application;
import org.restlet.Directory;
import org.restlet.Router;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.nexus.plugins.rest.NexusResourceBundle;
import org.sonatype.nexus.plugins.rest.StaticResource;
import org.sonatype.nexus.proxy.events.NexusStartedEvent;
import org.sonatype.nexus.proxy.events.NexusStoppedEvent;
import org.sonatype.plexus.appevents.ApplicationEventMulticaster;
import org.sonatype.plexus.appevents.Event;
import org.sonatype.plexus.appevents.EventListener;
import org.sonatype.plexus.rest.PlexusRestletApplicationBridge;
import org.sonatype.plexus.rest.RetargetableRestlet;
import org.sonatype.plexus.rest.resource.ManagedPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.security.web.ProtectedPathManager;

import com.thoughtworks.xstream.XStream;

/**
 * Nexus REST Application. This will ultimately replace the two applications we have now, and provide us plugin UI
 * extension capability.
 * 
 * @author cstamas
 */
@Component( role = Application.class, hint = "nexus" )
public class NexusApplication
    extends PlexusRestletApplicationBridge
    implements EventListener
{
    @Requirement
    private ApplicationEventMulticaster applicationEventMulticaster;

    @Requirement
    private ProtectedPathManager protectedPathManager;

    @Requirement( hint = "indexTemplate" )
    private ManagedPlexusResource indexTemplateResource;

    @Requirement( hint = "IndexRedirectingPlexusResource" )
    private ManagedPlexusResource indexRedirectingResource;

    @Requirement( hint = "content" )
    private ManagedPlexusResource contentResource;

    @Requirement( hint = "StatusPlexusResource" )
    private ManagedPlexusResource statusPlexusResource;

    @Requirement( hint = "CommandPlexusResource" )
    private ManagedPlexusResource commandPlexusResource;

    @Requirement( role = NexusResourceBundle.class )
    private List<NexusResourceBundle> nexusResourceBundles;

    @Requirement( role = NexusApplicationCustomizer.class )
    private List<NexusApplicationCustomizer> customizers;

    @Requirement( role = ErrorReportingManager.class )
    private ErrorReportingManager errorManager;

    /**
     * Listener.
     */
    public void onEvent( Event<?> evt )
    {
        if ( NexusStartedEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( true );
        }
        else if ( NexusStoppedEvent.class.isAssignableFrom( evt.getClass() ) )
        {
            recreateRoot( false );
        }
    }

    /**
     * Adding this as config change listener.
     */
    @Override
    protected void doConfigure()
    {
        // NEXUS-2883: turning off Range support for now
        getRangeService().setEnabled( false );

        // adding ourselves as listener
        applicationEventMulticaster.addEventListener( this );
    }

    /**
     * Configuring xstream with our aliases.
     */
    @Override
    public XStream doConfigureXstream( XStream xstream )
    {
        return org.sonatype.nexus.rest.model.XStreamConfigurator.configureXStream( xstream );
    }

    @Override
    protected Router initializeRouter( Router root, boolean isStarted )
    {
        // ========
        // SERVICE

        // service router
        Router applicationRouter = new Router( getContext() );

        // attaching filter to a root on given URI
        attach( root, false, "/service/" + AbstractNexusPlexusResource.NEXUS_INSTANCE_LOCAL, applicationRouter );

        // return the swapped router
        return applicationRouter;
    }

    @Override
    protected void afterCreateRoot( RetargetableRestlet root )
    {
        // customizers
        for ( NexusApplicationCustomizer customizer : customizers )
        {
            customizer.customize( this, root );
        }
    }

    /**
     * "Decorating" the root with our resources.
     * 
     * @TODO Move this to PlexusResources, except Status (see isStarted usage below!)
     */
    @Override
    protected void doCreateRoot( Router root, boolean isStarted )
    {
        if ( !isStarted )
        {
            return;
        }
        
        // Add error manager to context
        getContext().getAttributes().put( ErrorReportingManager.class.getName(), errorManager );

        // SERVICE (two always connected, unrelated to isStarted)

        attach( getApplicationRouter(), false, statusPlexusResource );

        attach( getApplicationRouter(), false, commandPlexusResource );

        // ==========
        // INDEX.HTML and WAR contents
        // To redirect "uncaught" requests to indexTemplateResource
        attach( root, true, "", new NexusPlexusResourceFinder( getContext(), indexRedirectingResource ) );
        attach( root, true, "/", new NexusPlexusResourceFinder( getContext(), indexRedirectingResource ) );

        // the indexTemplateResource
        attach( root, false, indexTemplateResource );

        // publish the WAR contents
        Directory rootDir = new NexusDirectory( getContext(), "war:///" );
        rootDir.setListingAllowed( false );
        rootDir.setNegotiateContent( false );
        attach( root, false, "/", rootDir );

        // ================
        // STATIC RESOURCES

        if ( nexusResourceBundles.size() > 0 )
        {
            for ( NexusResourceBundle bundle : nexusResourceBundles )
            {
                List<StaticResource> resources = bundle.getContributedResouces();

                if ( resources != null )
                {
                    for ( StaticResource resource : resources )
                    {
                        attach( root, false, resource.getPath(), new StaticResourceFinder( getContext(), resource ) );
                    }
                }
            }
        }

        // =======
        // CONTENT

        // prepare for browser diversity :)
        BrowserSensingFilter bsf = new BrowserSensingFilter( getContext() );

        // mounting it
        attach( root, false, "/content", bsf );

        bsf.setNext( new NexusPlexusResourceFinder( getContext(), contentResource ) );

        // protecting the content service manually
        this.protectedPathManager.addProtectedResource( "/content"
                    + contentResource.getResourceProtection().getPathPattern(), contentResource.getResourceProtection().getFilterExpression() );

        // protecting service resources with "wall" permission
        this.protectedPathManager.addProtectedResource( "/service/**",
                                                        "authcBasic,perms[nexus:permToCatchAllUnprotecteds]" );    }

    @Override
    protected void handlePlexusResourceSecurity( PlexusResource resource )
    {
        PathProtectionDescriptor descriptor = resource.getResourceProtection();

        if ( descriptor == null )
        {
            return;
        }

        this.protectedPathManager.addProtectedResource( "/service/*"
                                                        + descriptor.getPathPattern(), descriptor.getFilterExpression() );
    }

    @Override
    protected void attach( Router router, boolean strict, PlexusResource resource )
    {
        attach( router, strict, resource.getResourceUri(), new NexusPlexusResourceFinder( getContext(), resource ) );

        handlePlexusResourceSecurity( resource );
    }
}
