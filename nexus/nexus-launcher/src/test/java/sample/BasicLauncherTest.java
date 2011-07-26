package sample;

import org.sonatype.nexus.bundle.launcher.NexusBundleLauncher;
import javax.inject.Inject;
import javax.inject.Named;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import org.slf4j.Logger;
import org.sonatype.nexus.bundle.NexusBundleConfiguration;
import org.sonatype.nexus.bundle.launcher.ManagedNexusBundle;
import org.sonatype.test.ConfigurableInjectedTest;

/**
 *
 * @author plynch
 */
public class BasicLauncherTest extends ConfigurableInjectedTest{

    @Inject
    private Logger logger;

    @Inject
    private NexusBundleLauncher nexusBundleLauncher;

    @Before
    public void before() {
        assertThat(logger, notNullValue());
        logger.debug(testName.getMethodName());
        assertThat(nexusBundleLauncher, notNullValue());
    }

    @After
    public void after() {
    }

    @Test
    public void testBundleService(){
        String nexusOSSArtifactCoords = "org.sonatype.nexus:nexus-oss-webapp:tar.gz:bundle:1.9.2";
        NexusBundleConfiguration config = new NexusBundleConfiguration.Builder(nexusOSSArtifactCoords, "mybundle").build();
        //ManagedNexusBundle bundle = nexusBundleLauncher.start(config);
        //nexusBundleLauncher.stop(bundle);

    }
}
