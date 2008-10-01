package org.sonatype.nexus.integrationtests.nexus639;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.sonatype.nexus.integrationtests.AbstractNexusIntegrationTest;
import org.sonatype.nexus.rest.model.ScheduledServiceListResource;
import org.sonatype.nexus.rest.model.ScheduledServicePropertyResource;
import org.sonatype.nexus.tasks.PurgeTimeline;
import org.sonatype.nexus.test.utils.FeedUtil;
import org.sonatype.nexus.test.utils.TaskScheduleUtil;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class Nexus639PurgeTaskTest
    extends AbstractNexusIntegrationTest
{
    
    @Test
    public void doPurgeTaskTest() throws Exception
    {
        // an artifact was deployed already, so test the deploy feed has something.
        
        SyndFeed feed = FeedUtil.getFeed( "recentlyDeployed" );
        List<SyndEntry> entries = feed.getEntries();
        
        Assert.assertTrue("Expected artifacts in the recentlyDeployed feed.", entries.size() > 0 );
        
        // run the purge task for everything
        ScheduledServicePropertyResource repo = new ScheduledServicePropertyResource();
        repo.setId( "purgeOlderThan" );
        repo.setValue( "0" );
        ScheduledServiceListResource task = TaskScheduleUtil.runTask( "purge", PurgeTimeline.HINT, repo );
        
        Assert.assertNotNull( task );
        Assert.assertEquals( "SUBMITTED", task.getStatus() );
        
        // validate the feeds contain nothing.
        
        feed = FeedUtil.getFeed( "recentlyDeployed" );
        entries = feed.getEntries();
        
        Assert.assertTrue("Expected ZERO artifacts in the recentlyDeployed feed.", entries.size() == 0 );
    }

}
