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
package org.sonatype.security.ldap.dao.password;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.test.PlexusTestCaseSupport;
import org.sonatype.security.ldap.dao.password.hash.MD5Crypt;

public class MD5CryptPasswordEncoderTest
    extends PlexusTestCaseSupport
{

    @Test
    public void testEncryptAndVerify()
        throws Exception
    {
        PasswordEncoder encoder = lookup( PasswordEncoder.class, "crypt" );

        String crypted = encoder.encodePassword( "test", null );

        // System.out.println( "Crypted password: \'" + crypted + "\'" );

        int lastIdx = crypted.lastIndexOf( '$' );
        int firstIdx = crypted.indexOf( '$' );

        String salt = crypted.substring( firstIdx + "$1$".length(), lastIdx );

        String check = "{CRYPT}" + MD5Crypt.unixMD5( "test", salt );

        // System.out.println( "Check value: \'" + check + "\'" );

        Assert.assertEquals( check, crypted );

        Assert.assertTrue( encoder.isPasswordValid( crypted, "test", null ) );
    }

}
