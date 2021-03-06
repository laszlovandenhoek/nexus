/**
 * Copyright (c) 2008-2011 Sonatype, Inc. All rights reserved.
 *
 * This program is licensed to you under the Apache License Version 2.0,
 * and you may not use this file except in compliance with the Apache License Version 2.0.
 * You may obtain a copy of the Apache License Version 2.0 at http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the Apache License Version 2.0 is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Apache License Version 2.0 for the specific language governing permissions and limitations there under.
 */
package org.sonatype.nexus.plugins.migration;

import java.io.File;
import java.io.IOException;

import org.restlet.data.Status;
import org.sonatype.nexus.integrationtests.AbstractPrivilegeTest;
import org.sonatype.nexus.plugin.migration.artifactory.dto.MigrationSummaryDTO;
import org.sonatype.nexus.plugins.migration.util.ImportMessageUtil;

public abstract class AbstractMigrationPrivilegeTest
    extends AbstractPrivilegeTest
{
    
    protected final String ARTIFACTORY_MIGRATOR = "artifactory-migrate";

    abstract protected File getBackupFile();

    protected Status doMigration()
        throws Exception
    {
        MigrationSummaryDTO migrationSummary = ImportMessageUtil.importBackup( getBackupFile() );

        return ImportMessageUtil.commitImport( migrationSummary ).getStatus();
    }
    
    @Override
    protected void copyConfigFiles()
        throws IOException
    {
        super.copyConfigFiles();
        copyConfigFile( "logback-migration.xml", getTestProperties(), WORK_CONF_DIR );
    }

}
