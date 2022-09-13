package org.cytoscape.opencl.cycl;

import org.junit.Test;
import org.lwjgl.opencl.CL;

public class CLTest
{

	@Test
	public void testBestDevice() throws Exception
	{		
		CyCLDevice device = null;
		
		CL.destroy();
		CL.create();

    System.out.println("Getting device");
		device = CyCLDevice.getAll("").get(0);

    System.out.println("Performing the benchmark");
		//Now a benchmark with offsets
		double time = device.performBenchmark(true);
    System.out.println("...done in "+time);

		// System.out.println(device.name);

		CL.destroy();
	}
	
}
