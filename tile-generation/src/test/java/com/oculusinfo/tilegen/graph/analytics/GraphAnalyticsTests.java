/*
 * Copyright (c) 2014 Oculus Info Inc. 
 * http://www.oculusinfo.com/
 * 
 * Released under the MIT License.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:

 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.

 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oculusinfo.tilegen.graph.analytics;

//import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.oculusinfo.binning.util.Pair;
import com.oculusinfo.tilegen.graph.analytics.GraphAnalyticsRecord;
import com.oculusinfo.tilegen.graph.analytics.GraphCommunity;

public class GraphAnalyticsTests {

	// sample graph community record
	int _hierLevel = 0;
	long _id = 123L;
	Pair<Double, Double>_coords = new Pair<Double, Double>(1.2, 3.4);
	double _radius = 5.6;
	int _degree = 3;
	long _numNodes = 42L;
	String _metadata = "blah1\tblah2\tblah3";
	boolean _bIsPrimaryNode = false;
	long _parentID = 456L;
	Pair<Double, Double>_parentCoords = new Pair<Double, Double>(3.3, 4.4);
	double _parentRadius = 10.2;
	
	private GraphCommunity _sampleCommunity = new GraphCommunity(_hierLevel, 
																_id, 
																_coords,
																_radius,
																_degree,
																_numNodes,
																_metadata,
																_bIsPrimaryNode,
																_parentID,
																_parentCoords,
																_parentRadius);
	
	private GraphAnalyticsRecord _sampleRecord = new GraphAnalyticsRecord(1, Arrays.asList(_sampleCommunity));
	
	//---- Test that two records with the same data are equal
	@Test
	public void testRecordsEqual() {
		GraphAnalyticsRecord a = new GraphAnalyticsRecord(1, Arrays.asList(_sampleCommunity));
			
		Assert.assertEquals(_sampleRecord, a);
	}

	//---- Adding a community to an existing record
	@Test
    public void testCommunityToRecord () {
		GraphAnalyticsRecord a = new GraphAnalyticsRecord(1, Arrays.asList(_sampleCommunity));
		
		GraphCommunity community_b = new GraphCommunity(_hierLevel, 
														456L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														54,
														"blah4\tblah5",
														true,
														_parentID,
														_parentCoords,
														_parentRadius);

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(2, Arrays.asList(community_b, _sampleCommunity));		

		Assert.assertEquals(c, GraphAnalyticsRecord.addCommunityToRecord(a, community_b));
    }	
	
	
	//---- Adding two records
	@Test
    public void testRecordAggregation () {
		GraphAnalyticsRecord a = new GraphAnalyticsRecord(1, Arrays.asList(_sampleCommunity));
		
		GraphCommunity community_b = new GraphCommunity(_hierLevel, 
														456L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														54,
														"blah4\tblah5",
														true,
														_parentID,
														_parentCoords,
														_parentRadius);

		GraphAnalyticsRecord b = new GraphAnalyticsRecord(1, Arrays.asList(community_b));

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(2, Arrays.asList(community_b, _sampleCommunity));		

		Assert.assertEquals(c, GraphAnalyticsRecord.addRecords(a, b));
    }
	
	//---- Adding a 'too-small' community to a record already containing 10 communities	
	//TODO -- need to change this test if MAX_COMMUNITIES in GraphAnalyticsRecord class != 10
	@Test
    public void testRecordAggregationSmall () {
		GraphAnalyticsRecord a = new GraphAnalyticsRecord(10, Arrays.asList(_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity));
		
		GraphCommunity community_b = new GraphCommunity(_hierLevel, 
														456L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														2,
														"blain h4\tblah5",
														true,
														_parentID,
														_parentCoords,
														_parentRadius);

		GraphAnalyticsRecord b = new GraphAnalyticsRecord(1, Arrays.asList(community_b));

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(11, Arrays.asList(_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity));		

		Assert.assertEquals(c, GraphAnalyticsRecord.addRecords(a, b));
    }
	
	//---- Adding a 'very large' community to a record already containing 10 communities	
	//TODO -- need to change this test if MAX_COMMUNITIES in GraphAnalyticsRecord class != 10
	@Test
    public void testRecordAggregationLarge () {
		GraphAnalyticsRecord a = new GraphAnalyticsRecord(10, Arrays.asList(_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity));
		
		GraphCommunity community_b = new GraphCommunity(_hierLevel, 
														456L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														1000,
														"blain h4\tblah5",
														true,
														_parentID,
														_parentCoords,
														_parentRadius);

		GraphAnalyticsRecord b = new GraphAnalyticsRecord(1, Arrays.asList(community_b));

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(11, Arrays.asList(community_b,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity,
																			_sampleCommunity));		

		Assert.assertEquals(c, GraphAnalyticsRecord.addRecords(a, b));
    }

    //---- Check string conversion
    @Test
    public void testStringConversion () {
		GraphCommunity community_a = new GraphCommunity(_hierLevel, 
				123L, 
				new Pair<Double, Double>(1.2, 3.4),
				0.8,
				4,
				33,
				"abc\t\"\"\\\"\\\\\"\\\\\\\"\tdef",
				false,
				_parentID,
				_parentCoords,
				_parentRadius);
    		
    	GraphAnalyticsRecord a = new GraphAnalyticsRecord(1, Arrays.asList(community_a));

        String as = a.toString();
        GraphAnalyticsRecord b = GraphAnalyticsRecord.fromString(as);
        Assert.assertEquals(a, b);

    }	
		
	//---- Min of two records
    @Test
    public void testMin() {
    	GraphAnalyticsRecord a = new GraphAnalyticsRecord(2, Arrays.asList(_sampleCommunity));
		GraphCommunity community_b = new GraphCommunity(1, 
														567L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														54,
														"blah4\tblah5",
														true,
														567L,
														new Pair<Double, Double>(7.2, 0.1),
														10.1);
		GraphAnalyticsRecord b = new GraphAnalyticsRecord(1, Arrays.asList(community_b));
		
		GraphCommunity community_c = new GraphCommunity(0, 
				123L, 
				new Pair<Double, Double>(1.2, 3.4),
				3.4,
				3,
				42L,
				"",
				false,
				456L,
				new Pair<Double, Double>(3.3, 0.1),
				10.1);		

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(1, Arrays.asList(community_c));		
		
        Assert.assertEquals(c, GraphAnalyticsRecord.minOfRecords(a, b));
    }	
	
	//---- Max of two records
    @Test
    public void testMax() {
    	GraphAnalyticsRecord a = new GraphAnalyticsRecord(2, Arrays.asList(_sampleCommunity));
		GraphCommunity community_b = new GraphCommunity(1, 
														567L, 
														new Pair<Double, Double>(3.3, 4.4),
														3.4,
														4,
														54,
														"blah4\tblah5",
														true,
														567L,
														new Pair<Double, Double>(7.2, 0.1),
														10.1);
		GraphAnalyticsRecord b = new GraphAnalyticsRecord(1, Arrays.asList(community_b));
		
		GraphCommunity community_c = new GraphCommunity(1, 
				567L, 
				new Pair<Double, Double>(3.3, 4.4),
				5.6,
				4,
				54L,
				"",
				false,
				567L,
				new Pair<Double, Double>(7.2, 4.4),
				10.2);		

		GraphAnalyticsRecord c = new GraphAnalyticsRecord(2, Arrays.asList(community_c));		
		
        Assert.assertEquals(c, GraphAnalyticsRecord.maxOfRecords(a, b));
    }	
	
}
