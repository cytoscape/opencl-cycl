package org.cytoscape.opencl.cycl;

import static org.junit.Assert.*;

import org.lwjgl.opencl.*;
import org.junit.Test;

public class CLTest
{

	@Test
	public void testBestDevice()
	{		
		CyCLDevice device = null;
		try
		{
			CL.create();
			//Also performs a benchmark without offsets
			device = CyCLDevice.getAll("").get(0);
			
			//Now a benchmark with offsets
			device.performBenchmark(true);
		} 
		catch (Exception e) 
		{
			CL.destroy();
			fail("Could not obtain device."); 
		}
		
		//System.out.println(device.name);
		
		CL.destroy();
	}
	
}
