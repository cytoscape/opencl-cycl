package org.cytoscape.opencl.cycl;

import org.lwjgl.opencl.*;
import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.Scanner;

public class CyCLProgram 
{
	private Boolean finalized = false;
	
	private CLProgram program;
	private Hashtable<String, CyCLKernel> kernels = new Hashtable<String, CyCLKernel>();
	private HashMap<String, String> defines;
	
	public CyCLProgram(CyCLContext context, CyCLDevice device, URL resourcePath, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation) throws IOException
	{
    	InputStream programTextStream = resourcePath.openStream();
    	Scanner programTextScanner = new Scanner(programTextStream, "UTF-8");
    	String programText = programTextScanner.useDelimiter("\\Z").next();
    	programTextScanner.close();
        programTextStream.close();
        
        prepareAndBuildProgram(context, device, new String[] {programText}, kernelNames, defines, silentCompilation);	        	        
	}

	public CyCLProgram(CyCLContext context, CyCLDevice device, String source, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation) 
	{
        prepareAndBuildProgram(context, device, new String[] {source}, kernelNames, defines, silentCompilation);	        
	}
	
	public CyCLProgram(CyCLContext context, CyCLDevice device, String sources[], String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation) 
	{
        prepareAndBuildProgram(context, device, sources, kernelNames, defines, silentCompilation);	        
	}

	private void prepareAndBuildProgram(CyCLContext context, CyCLDevice device, String[] sources, String[] kernelNames, HashMap<String, String> defines, boolean silentCompilation)
	{
		try {
			this.defines = defines;
			StringBuilder buildOptions = new StringBuilder();
			if (defines != null)
			{
				for (Entry<String, String> entry : defines.entrySet()) {
					if(entry.getValue() == null)
					{
						buildOptions.append(" -D").append(entry.getKey());
					}
					else
					{
						buildOptions.append(" -D").append(entry.getKey()).append("=").append(entry.getValue());
					}
				}
			}

			IntBuffer errorBuffer = BufferUtils.createIntBuffer(1);
			program = CL10.clCreateProgramWithSource(context.getContext(), sources, errorBuffer);
			Util.checkCLError(errorBuffer.get(0));

			Util.checkCLError(CL10.clBuildProgram(program, device.getDevice(), buildOptions.toString(), null));

			for (String kernelName : kernelNames)
			{
				kernels.put(kernelName, new CyCLKernel(context, this, kernelName));
			}

		}
		catch (OpenCLException exc)
		{
			if (!silentCompilation && program != null) //TODO change to Cytoscape logging mechanism
				System.out.println(program.getBuildInfoString(device.getDevice(), CL10.CL_PROGRAM_BUILD_LOG));
			
			System.out.println("Could not create CL program");
			throw new CyCLException("Could not create CL program", exc);
		}
		catch (Exception exc) {
			throw new CyCLException("Could not create CL program", exc);
		}
	}
	
	public CLProgram getProgram()
	{
		return program;
	}
	
	public CyCLKernel getKernel(String name)
	{
		return kernels.get(name);
	}
	
	@Override
	protected void finalize() 
	{
		try
		{
			if(finalized)
				return;
			
			for(Entry<String, CyCLKernel> entry : kernels.entrySet())
				entry.getValue().finalize();
			kernels.clear();
			
			Util.checkCLError(CL10.clReleaseProgram(program));
			
			finalized = true;		
			super.finalize();
		}
		catch (Throwable exc)
		{
			System.out.println("Could not finalize CyCLProgram " + program + ": " + exc.getMessage());
			throw new CyCLException("Could not finalize CyCLProgram object.");
		}
	}
}
