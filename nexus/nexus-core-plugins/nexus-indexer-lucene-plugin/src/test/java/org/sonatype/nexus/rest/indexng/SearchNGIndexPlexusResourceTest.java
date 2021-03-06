package org.sonatype.nexus.rest.indexng;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.queryParser.ParseException;
import org.apache.maven.index.ArtifactInfoFilter;
import org.apache.maven.index.SearchType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.sonatype.nexus.AbstractNexusTestCase;
import org.sonatype.nexus.index.Searcher;
import org.sonatype.plexus.rest.resource.PlexusResourceException;
import org.sonatype.plexus.rest.resource.error.ErrorMessage;
import org.sonatype.plexus.rest.resource.error.ErrorResponse;

/**
 * Test for SearchNGIndexPlexusResource
 */
public class SearchNGIndexPlexusResourceTest
    extends AbstractNexusTestCase
{

    @Before
    public void setUp()
        throws Exception
    {
    }

    @Test
    public void testGet()
        throws Exception
    {
        SearchNGIndexPlexusResource resource = new SearchNGIndexPlexusResource();
        Map<String, String> terms = new HashMap<String, String>( 4 );
        terms.put( "q", "!" );
        Searcher searcher = mock( Searcher.class );
        when( searcher.canHandle( Mockito.any( Map.class ) ) ).thenReturn( true );

        when( searcher.flatIteratorSearch( Mockito.any( Map.class ), anyString(), anyInt(), anyInt(), anyInt(), anyBoolean(),
                                           Mockito.any( SearchType.class ), Mockito.any( List.class ) ) )
            // emulate current indexer search behavior, illegal query results in IllegalArgEx with the ParseEx as cause
            .thenThrow( new IllegalArgumentException( new ParseException( "mock" ) ) );

        try
        {
            resource.searchByTerms( terms, "rid", 1, 1, false, false, true, Collections.<ArtifactInfoFilter>emptyList(),
                                    Arrays.asList( searcher ) );
        }
        catch ( PlexusResourceException e )
        {
            ErrorResponse resultObject = (ErrorResponse) e.getResultObject();
            assertThat( resultObject, notNullValue() );
            List<ErrorMessage> errors = resultObject.getErrors();
            assertThat( errors, hasSize( 1 ) );
            ErrorMessage errorMessage = errors.get( 0 );

            // ID needs to be stable for UI handling
            assertThat( errorMessage.getId(), equalTo( "search" ));
            assertThat( errorMessage.getMsg(), containsString( "mock" ) );
        }
    }
}
