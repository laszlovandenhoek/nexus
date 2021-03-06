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
package org.sonatype.nexus.ahc;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.configuration.application.ApplicationConfiguration;
import org.sonatype.nexus.proxy.repository.ClientSSLRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.NtlmRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.ProxyRepository;
import org.sonatype.nexus.proxy.repository.RemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.repository.RemoteProxySettings;
import org.sonatype.nexus.proxy.repository.UsernamePasswordRemoteAuthenticationSettings;
import org.sonatype.nexus.proxy.storage.remote.RemoteStorageContext;
import org.sonatype.nexus.proxy.utils.UserAgentBuilder;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.ProxyServer;
import com.ning.http.client.Realm;

@Component( role = AhcProvider.class )
public class DefaultAhcProvider
    implements AhcProvider
{
    @Requirement
    private ApplicationConfiguration applicationConfiguration;

    @Requirement
    private UserAgentBuilder userAgentBuilder;

    private volatile AsyncHttpClient sharedClient;

    private volatile boolean shutdown = false;

    public synchronized void reset()
    {
        sharedClient = null;
    }

    public synchronized void close()
    {
        try
        {
            if ( sharedClient != null )
            {
                sharedClient.close();
            }
        }
        finally
        {
            shutdown = true;
        }
    }

    @Override
    public synchronized AsyncHttpClient getAsyncHttpClient()
    {
        if ( shutdown )
        {
            throw new IllegalStateException( "AHC provider was shut down, not serving client up anymore." );
        }

        if ( sharedClient == null )
        {
            // TODO: nexus wide singleton or new instance per invocation?
            final Builder configBuilder =
                getAsyncHttpClientConfigBuilder( applicationConfiguration.getGlobalRemoteStorageContext() );

            configBuilder.setUserAgent( userAgentBuilder.formatGenericUserAgentString() );

            sharedClient = new AsyncHttpClient( configBuilder.build() );
        }

        return sharedClient;
    }

    @Override
    public Builder getAsyncHttpClientConfigBuilder( final ProxyRepository repository, final RemoteStorageContext ctx )
    {
        if ( shutdown )
        {
            throw new IllegalStateException( "AHC provider was shut down, not serving client up anymore." );
        }

        final Builder result = getAsyncHttpClientConfigBuilder( ctx );

        result.setUserAgent( userAgentBuilder.formatRemoteRepositoryStorageUserAgentString( repository, ctx ) );

        // enable redirects for RRS use
        result.setFollowRedirects( true );

        // limiting (with httpClient defaults) to prevent bashing of remote repositories
        result.setMaximumConnectionsPerHost( 20 );
        result.setMaximumConnectionsTotal( 20 );

        // proxy-logic will handle retries
        result.setMaxRequestRetry( 0 );

        return result;
    }

    // ==

    protected Builder getAsyncHttpClientConfigBuilder( final RemoteStorageContext ctx )
    {
        final AsyncHttpClientConfig.Builder result = new AsyncHttpClientConfig.Builder();

        // timeout
        final int timeout = ctx.getRemoteConnectionSettings().getConnectionTimeout();
        result.setConnectionTimeoutInMs( timeout );
        result.setRequestTimeoutInMs( timeout );

        // handle compression
        result.setCompressionEnabled( true );

        // remote auth
        RemoteAuthenticationSettings ras = ctx.getRemoteAuthenticationSettings();

        if ( ras != null )
        {
            Realm realm = null;

            if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
            {
                // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                // TODO - implement this
            }
            else if ( ras instanceof NtlmRemoteAuthenticationSettings )
            {
                NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                realm =
                    new Realm.RealmBuilder().setPrincipal( nras.getUsername() ).setPassword( nras.getPassword() ).setNtlmDomain(
                        nras.getNtlmDomain() ).setNtlmHost( nras.getNtlmHost() ).build();
            }
            else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
            {
                UsernamePasswordRemoteAuthenticationSettings uras = (UsernamePasswordRemoteAuthenticationSettings) ras;

                realm =
                    new Realm.RealmBuilder().setPrincipal( uras.getUsername() ).setPassword( uras.getPassword() ).setUsePreemptiveAuth(
                        true ).build();
            }

            if ( realm != null )
            {
                result.setRealm( realm );
            }
        }

        // proxy
        RemoteProxySettings rps = ctx.getRemoteProxySettings();

        if ( rps.isEnabled() )
        {
            ProxyServer proxy = null;

            if ( rps.getProxyAuthentication() != null )
            {
                ras = rps.getProxyAuthentication();

                if ( ras instanceof ClientSSLRemoteAuthenticationSettings )
                {
                    // ClientSSLRemoteAuthenticationSettings cras = (ClientSSLRemoteAuthenticationSettings) ras;

                    // TODO - implement this
                }
                else if ( ras instanceof NtlmRemoteAuthenticationSettings )
                {
                    NtlmRemoteAuthenticationSettings nras = (NtlmRemoteAuthenticationSettings) ras;

                    proxy = new ProxyServer( rps.getHostname(), rps.getPort(), nras.getUsername(), nras.getPassword() );

                    proxy.setNtlmDomain( nras.getNtlmDomain() );
                }
                else if ( ras instanceof UsernamePasswordRemoteAuthenticationSettings )
                {
                    UsernamePasswordRemoteAuthenticationSettings uras =
                        (UsernamePasswordRemoteAuthenticationSettings) ras;

                    proxy = new ProxyServer( rps.getHostname(), rps.getPort(), uras.getUsername(), uras.getPassword() );
                }
            }
            else
            {
                proxy = new ProxyServer( rps.getHostname(), rps.getPort() );
            }

            // to avoid NPEs while incomplete
            if ( proxy != null )
            {
                // check if we have non-proxy hosts
                if ( rps.getNonProxyHosts() != null && !rps.getNonProxyHosts().isEmpty() )
                {
                    // TODO: someone "invented" non-proxy hosts as a list of regexps! So, question is, how to
                    // transport that "smart move" to AHC?
                    // AHC supports "*" as wildcard
                    for ( String nonProxyHost : rps.getNonProxyHosts() )
                    {
                        proxy.addNonProxyHost( nonProxyHost );
                    }
                }

                result.setProxyServer( proxy );
            }
        }

        return result;
    }
}
