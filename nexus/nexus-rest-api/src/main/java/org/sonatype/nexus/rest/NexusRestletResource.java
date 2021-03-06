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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import org.codehaus.plexus.swizzle.IssueSubmissionException;
import org.restlet.Context;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.data.Status;
import org.restlet.resource.Representation;
import org.restlet.resource.ResourceException;
import org.restlet.resource.Variant;
import org.sonatype.nexus.error.reporting.ErrorReportRequest;
import org.sonatype.nexus.error.reporting.ErrorReportingManager;
import org.sonatype.plexus.rest.resource.PlexusResource;
import org.sonatype.plexus.rest.resource.RestletResource;

public class NexusRestletResource
    extends RestletResource {

    public NexusRestletResource( Context context, Request request, Response response, PlexusResource delegate ) {
        super( context, request, response, delegate );
    }

    @Override
    public Representation represent( Variant variant )
        throws ResourceException {
        try {
            return super.represent( variant );
        } catch ( ResourceException e ) {
            // NEXUS-4238, NEXUS-4290
            // if it's server error based on HTTP Code, but NOT when Nexus throws a known 503 
            // (see org.sonatype.nexus.rest.AbstractResourceStoreContentPlexusResource.handleException(Request, Response, Exception))
            final Status status = e.getStatus();
            if ( status == null ) {
                handleError( e );
            } else {
                final int code = status.getCode();
                if ( Status.isServerError( code ) && Status.SERVER_ERROR_SERVICE_UNAVAILABLE.getCode() != code ) {
                    handleError( e );
                }
            }

            throw e;
        } catch ( RuntimeException e ) {
            handleError( e );

            throw e;
        }
    }

    @Override
    public void acceptRepresentation( Representation representation )
        throws ResourceException {
        try {
            super.acceptRepresentation( representation );
        } catch ( ResourceException e ) {
            if ( Status.isServerError( e.getStatus().getCode() ) ) {
                handleError( e );
            }

            throw e;
        } catch ( RuntimeException e ) {
            handleError( e );

            throw e;
        }
    }

    @Override
    public void storeRepresentation( Representation representation )
        throws ResourceException {
        try {
            super.storeRepresentation( representation );
        } catch ( ResourceException e ) {
            if ( Status.isServerError( e.getStatus().getCode() ) ) {
                handleError( e );
            }

            throw e;
        } catch ( RuntimeException e ) {
            handleError( e );

            throw e;
        }
    }

    @Override
    public void removeRepresentations()
        throws ResourceException {
        try {
            super.removeRepresentations();
        } catch ( ResourceException e ) {
            if ( Status.isServerError( e.getStatus().getCode() ) ) {
                handleError( e );
            }
            throw e;
        } catch ( RuntimeException e ) {
            handleError( e );

            throw e;
        }
    }

    protected void handleError( Throwable throwable ) {
        Context c = getContext();
        ConcurrentMap<String, Object> attrs = c.getAttributes();
        ErrorReportingManager manager =
            ( ErrorReportingManager ) attrs.get( ErrorReportingManager.class.getName() );

        if ( manager != null ) {
            ErrorReportRequest request = new ErrorReportRequest();

            request.getContext().putAll( getContext().getAttributes() );

            request.setThrowable( throwable );

            try {
                manager.handleError( request );
            } catch ( IssueSubmissionException e ) {
                getLogger().log( Level.SEVERE, "Unable to submit error report to jira", e );
            } catch ( IOException e ) {
                getLogger().log( Level.SEVERE, "Unable to submit error report to jira", e );
            } catch ( GeneralSecurityException e ) {
                getLogger().log( Level.SEVERE, "Unable to submit error report to jira", e );
            }
        }
    }
}
