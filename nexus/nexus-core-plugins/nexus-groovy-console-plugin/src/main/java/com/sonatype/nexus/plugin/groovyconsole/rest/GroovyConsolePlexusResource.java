package com.sonatype.nexus.plugin.groovyconsole.rest;

import java.io.IOException;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.rest.AbstractNexusPlexusResource;
import org.sonatype.plexus.rest.resource.PathProtectionDescriptor;
import org.sonatype.plexus.rest.resource.PlexusResource;

import com.sonatype.nexus.plugin.groovyconsole.GroovyScriptManager;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptDTO;
import com.sonatype.nexus.plugin.groovyconsole.rest.dto.GroovyScriptResponseDTO;
import com.thoughtworks.xstream.XStream;

@Component( role = PlexusResource.class, hint = "GroovyConsole" )
public class GroovyConsolePlexusResource
    extends AbstractNexusPlexusResource
{

    @Requirement
    private GroovyScriptManager manager;

    @Override
    public Object getPayloadInstance()
    {
        return new GroovyScriptResponseDTO();
    }

    @Override
    public void configureXStream( XStream xstream )
    {
        super.configureXStream( xstream );

        xstream.processAnnotations( GroovyScriptDTO.class );
        xstream.processAnnotations( GroovyScriptResponseDTO.class );
    }

    public GroovyConsolePlexusResource()
    {
        setReadable( true );
        setModifiable( true );
    }

    @Override
    public PathProtectionDescriptor getResourceProtection()
    {
        return new PathProtectionDescriptor( "/groovy_console", "authcBasic,perms[groovy:console]" );
    }

    @Override
    public String getResourceUri()
    {
        return "/groovy_console";
    }

    @Override
    public Object get( Context context, Request request, Response response, Variant variant )
        throws ResourceException
    {
        GroovyScriptResponseDTO r = new GroovyScriptResponseDTO();
        r.setData( manager.getScripts() );

        return r;
    }

    @Override
    public Object post( Context context, Request request, Response response, Object payload )
        throws ResourceException
    {
        GroovyScriptDTO script = (GroovyScriptDTO) payload;
        try
        {
            manager.save( script );
        }
        catch ( IOException e )
        {
            throw new ResourceException( Status.SERVER_ERROR_INTERNAL, e );
        }

        return Status.SUCCESS_CREATED;
    }
}
