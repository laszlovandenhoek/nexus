/**
 * Sonatype Nexus (TM) Open Source Version.
 * Copyright (c) 2008 Sonatype, Inc. All rights reserved.
 * Includes the third-party code listed at http://nexus.sonatype.org/dev/attributions.html
 * This program is licensed to you under Version 3 only of the GNU General Public License as published by the Free Software Foundation.
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License Version 3 for more details.
 * You should have received a copy of the GNU General Public License Version 3 along with this program.
 * If not, see http://www.gnu.org/licenses/.
 * Sonatype Nexus (TM) Professional Version is available from Sonatype, Inc.
 * "Sonatype" and "Sonatype Nexus" are trademarks of Sonatype, Inc.
 */
package org.sonatype.nexus.proxy.maven.maven1;

import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.component.annotations.Requirement;
import org.sonatype.nexus.artifact.GavCalculator;
import org.sonatype.nexus.proxy.maven.AbstractMavenGroupRepository;
import org.sonatype.nexus.proxy.registry.ContentClass;
import org.sonatype.nexus.proxy.repository.GroupRepository;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurationValidator;
import org.sonatype.nexus.proxy.repository.RepositoryConfigurator;

@Component( role = GroupRepository.class, hint = "maven1", instantiationStrategy = "per-lookup", description = "Maven1 Repository Group" )
public class M1GroupRepository
    extends AbstractMavenGroupRepository
{
    @Requirement( hint = "maven1" )
    private ContentClass contentClass;

    @Requirement( hint = "maven1" )
    private GavCalculator gavCalculator;

    @Requirement
    private M1GroupRepositoryConfigurator m1GroupRepositoryConfigurator;

    private boolean mergeMetadata = true;

    public boolean isMergeMetadata()
    {
        return mergeMetadata;
    }

    public void setMergeMetadata( boolean mergeMetadata )
    {
        this.mergeMetadata = mergeMetadata;
    }

    public ContentClass getRepositoryContentClass()
    {
        return contentClass;
    }

    public GavCalculator getGavCalculator()
    {
        return gavCalculator;
    }

    @Override
    public RepositoryConfigurationValidator getRepositoryConfigurationValidator()
    {
        return null;
    }

    @Override
    public RepositoryConfigurator getRepositoryConfigurator()
    {
        return m1GroupRepositoryConfigurator;
    }
}
