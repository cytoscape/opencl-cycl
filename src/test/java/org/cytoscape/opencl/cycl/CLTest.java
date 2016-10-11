package org.cytoscape.opencl.cycl;

import org.junit.Test;
import org.lwjgl.LWJGLUtil;
import org.lwjgl.opencl.CL;

public class CLTest
{

	@Test
	public void testBestDevice() throws Exception
	{		
		CyCLDevice device = null;
		
		
		CL.create();
		//Also performs a benchmark without offsets
		device = CyCLDevice.getAll("").get(0);
		
		//Now a benchmark with offsets
		device.performBenchmark(true);
		
		//System.out.println(device.name);
		
		CL.destroy();
	}
	
}
