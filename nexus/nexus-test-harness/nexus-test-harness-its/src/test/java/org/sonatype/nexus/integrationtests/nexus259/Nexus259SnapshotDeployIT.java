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
package org.sonatype.nexus.integrationtests.nexus259;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Date;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.maven.index.artifact.Gav;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.integrationtests.TestContainer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Deploys a snapshot artifact using a wagon and REST (both gav and pom) REST should fail 
 */
public class Nexus259SnapshotDeployIT
    extends AbstractNexusIntegrationTest
{

    private static final String TEST_SNAPSHOT_REPO = "nexus-test-harness-snapshot-repo";

    public Nexus259SnapshotDeployIT()
    {
        super( TEST_SNAPSHOT_REPO );
    }
    
    @BeforeClass
    public void setSecureTest(){
        TestContainer.getInstance().getTestContext().setSecureTest( true );
    }

    @Test
    public void deployUsingRest()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = getDeployUtils().deployUsingGavWithRest( uploadURL, TEST_SNAPSHOT_REPO, gav, fileToDeploy );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Snapshot repositories do not allow manual file upload: " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
            // download it
            downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( fileWasUploaded, "The file was uploaded and it should not have been." );

    }

    @Test
    public void deploywithPomUsingRest()
        throws Exception
    {

        Gav gav =
            new Gav( this.getTestId(), "uploadWithGav", "1.0.0-SNAPSHOT", null, "xml", 0,
                     new Date().getTime(), "Simple Test Artifact", false, null, false, null );

        // file to deploy
        File fileToDeploy = this.getTestFile( gav.getArtifactId() + "." + gav.getExtension() );

        File pomFile = this.getTestFile( "pom.xml" );

        // the Restlet Client does not support multipart forms: http://restlet.tigris.org/issues/show_bug.cgi?id=71

        // url to upload to
        String uploadURL = this.getBaseNexusUrl() + "service/local/artifact/maven/content";

        int status = getDeployUtils().deployUsingPomWithRest( uploadURL, TEST_SNAPSHOT_REPO, fileToDeploy, pomFile, null, null );

        if ( status != HttpStatus.SC_BAD_REQUEST )
        {
            Assert.fail( "Snapshot repositories do not allow manual file upload: " + status );
        }

        boolean fileWasUploaded = true;
        try
        {
            // download it
            downloadArtifact( gav, "./target/downloaded-jars" );
        }
        catch ( FileNotFoundException e )
        {
            fileWasUploaded = false;
        }

        Assert.assertFalse( fileWasUploaded, "The file was uploaded and it should not have been." );
    }
}
