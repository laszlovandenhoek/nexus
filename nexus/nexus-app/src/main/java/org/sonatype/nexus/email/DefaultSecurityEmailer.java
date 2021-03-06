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
package org.sonatype.nexus.email;

import java.util.List;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.micromailer.Address;
import org.sonatype.micromailer.MailRequest;
import org.sonatype.security.email.SecurityEmailer;

/**
 * The default emailer that is "stolen" by Security. Look at the NexusEmailer for the real thing.
 * 
 * @author cstamas
 */
@Component( role = SecurityEmailer.class )
public class DefaultSecurityEmailer
    implements SecurityEmailer
{
    @Requirement
    private NexusEmailer nexusEmailer;

    public void sendNewUserCreated( String email, String userid, String password )
    {
        StringBuilder body = new StringBuilder();
        body.append( "User Account " );
        body.append( userid );
        body.append( " has been created.  Another email will be sent shortly containing your password." );

        MailRequest request = nexusEmailer.getDefaultMailRequest( "Nexus: New user account created.", body.toString() );

        request.getToAddresses().add( new Address( email ) );

        nexusEmailer.sendMail( request );

        body = new StringBuilder();
        body.append( "Your new password is " );
        body.append( password );

        request = nexusEmailer.getDefaultMailRequest( "Nexus: New user account created.", body.toString() );

        request.getToAddresses().add( new Address( email ) );

        nexusEmailer.sendMail( request );
    }

    public void sendForgotUsername( String email, List<String> userIds )
    {
        StringBuilder body = new StringBuilder();

        body.append( "Your email is associated with the following Nexus User Id(s):\n " );
        for ( String userId : userIds )
        {
            body.append( "\n - \"" );
            body.append( userId );
            body.append( "\"" );
        }

        MailRequest request = nexusEmailer.getDefaultMailRequest( "Nexus: User account notification.", body.toString() );

        request.getToAddresses().add( new Address( email ) );

        nexusEmailer.sendMail( request );
    }

    public void sendResetPassword( String email, String password )
    {
        StringBuilder body = new StringBuilder();
        body.append( "Your password has been reset.  Your new password is: " );
        body.append( password );

        MailRequest request = nexusEmailer.getDefaultMailRequest( "Nexus: User account notification.", body.toString() );

        request.getToAddresses().add( new Address( email ) );

        nexusEmailer.sendMail( request );
    }
}
