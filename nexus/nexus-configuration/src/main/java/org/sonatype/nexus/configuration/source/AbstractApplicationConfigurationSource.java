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
package org.sonatype.nexus.configuration.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import org.codehaus.plexus.component.annotations.Requirement;
import org.codehaus.plexus.interpolation.InterpolatorFilterReader;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
import org.sonatype.configuration.ConfigurationException;
import org.sonatype.nexus.configuration.model.Configuration;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Reader;
import org.sonatype.nexus.configuration.model.io.xpp3.NexusConfigurationXpp3Writer;
import org.sonatype.nexus.util.ApplicationInterpolatorProvider;

/**
 * Abstract class that encapsulates Modello model loading and saving with interpolation.
 * 
 * @author cstamas
 */
public abstract class AbstractApplicationConfigurationSource
    extends AbstractConfigurationSource
    implements ApplicationConfigurationSource
{
    /**
     * The application interpolation provider.
     */
    @Requirement
    private ApplicationInterpolatorProvider interpolatorProvider;

    /** The configuration. */
    private Configuration configuration;

    public Configuration getConfiguration()
    {
        return configuration;
    }

    public void setConfiguration( Configuration configuration )
    {
        this.configuration = configuration;
    }

    /**
     * Called by subclasses when loaded configuration is rejected for some reason.
     */
    protected void rejectConfiguration( String message, Throwable e )
    {
        this.configuration = null;

        if ( message != null )
        {
            getLogger().warn( message, e );
        }
    }

    /**
     * Load configuration.
     * 
     * @param file the file
     * @return the configuration
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void loadConfiguration( InputStream is )
        throws IOException,
            ConfigurationException
    {
        setConfigurationUpgraded( false );

        Reader fr = null;

        try
        {
            NexusConfigurationXpp3Reader reader = new NexusConfigurationXpp3Reader();

            fr = new InputStreamReader( is );

            InterpolatorFilterReader ip = new InterpolatorFilterReader( fr, interpolatorProvider.getInterpolator() );

            // read again with interpolation
            configuration = reader.read( ip );
        }
        catch ( XmlPullParserException e )
        {
            configuration = null;

            throw new ConfigurationException( "Nexus configuration file was not loaded, it has the wrong structure.", e );
            }
        finally
        {
            if ( fr != null )
            {
                fr.close();
            }
        }

        // check the model version if loaded
        if ( configuration != null && !Configuration.MODEL_VERSION.equals( configuration.getVersion() ) )
        {
            rejectConfiguration( "Nexus configuration file was loaded but discarded, it has the wrong version number.", null );
            
            throw new ConfigurationException( "Nexus configuration file was loaded but discarded, it has the wrong version number." );
        }

        if ( getConfiguration() != null )
        {
            getLogger().info( "Configuration loaded succesfully." );
        }
    }

    /**
     * Save configuration.
     * 
     * @param file the file
     * @throws IOException Signals that an I/O exception has occurred.
     */
    protected void saveConfiguration( OutputStream os, Configuration configuration )
        throws IOException
    {
        Writer fw = null;
        try
        {
            fw = new OutputStreamWriter( os );

            NexusConfigurationXpp3Writer writer = new NexusConfigurationXpp3Writer();

            writer.write( fw, configuration );
        }
        finally
        {
            if ( fw != null )
            {
                fw.flush();

                fw.close();
            }
        }
    }

    /**
     * Returns the default source of ConfigurationSource. May be null.
     */
    public ApplicationConfigurationSource getDefaultsSource()
    {
        return null;
    }

}
