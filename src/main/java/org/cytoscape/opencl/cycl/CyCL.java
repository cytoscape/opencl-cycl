package org.cytoscape.opencl.cycl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.lwjgl.opencl.CL;


/***
 * Central OpenCL service that is initialized on startup and stores all available devices.
 * 
 * @author Dimitry Tegunov
 *
 */
public class CyCL 
{
	public static Object initSync = new Object();
	public static Object sync = new Object();
	private static List<CyCLDevice> devices = new ArrayList<>();
	private static boolean isInitialized = false;
	
	public CyCL()
	{
	}
	
	public static List<CyCLDevice> getDevices()
	{
		if (devices == null)
			devices = new ArrayList<>();
		
		return devices;
	}
	
	/**
	 * Loads all necessary native libraries, initializes LWJGL and populates the device list.
	 * Should be called only once on startup.
	 * 
	 * @param applicationConfig Instance of Cytoscape's application configuration service
	 * @param propertyService Instance of Cytoscape's property service for cyPropertyName=cytoscape3.props
	 * @return True if initialized correctly; false otherwise
	 */
	public static boolean initialize(CyApplicationConfiguration applicationConfig, CyProperty<Properties> propertyService)
	{		
		synchronized (initSync)
		{
			if (isInitialized)
				return true;
					
			File configDir = applicationConfig.getConfigurationDirectoryLocation();
			String dummyPath = configDir.getAbsolutePath() + File.separator + "disable-opencl.dummy";
			
			File dummy = new File(dummyPath);
			if (dummy.exists())
			{
				System.out.println("OpenCL was not initialized because it crashed on the previous attempt.");
				System.out.println("If you think it works now, remove disable-opencl.dummy manually from Cytoscape's configuration directory.");
				System.out.println("For more information on how to troubleshoot OpenCL, please refer to http://manual.cytoscape.org/en/stable/Cytoscape_and_OpenCL_GPU.html.");
			}
			else
			{						
				try
				{
					dummy.createNewFile();
					
					CL.create();
				
					// Populate device list
					Properties globalProps = propertyService.getProperties();
					String preferredDevice = globalProps.getProperty("opencl.device.preferred");
					
					if (preferredDevice == null)
						preferredDevice = "";
					
					devices = CyCLDevice.getAll(preferredDevice);

					if (!dummy.delete())
					{
						System.out.println("Could not delete OpenCL dummy file despite OpenCL being OK.");
						System.out.println("You should try to remove disable-opencl.dummy manually from Cytoscape's configuration directory.");
					}
					
					if (devices == null || devices.size() == 0)
						return false;
				}
				catch (Throwable e)
				{
					e.printStackTrace();
					return false;
				}
			}
			
			isInitialized = true;
			
			return true;
		}
	}
	
	public static void makePreferred(String name)
	{
		synchronized (initSync)
		{
			if (devices == null)
				return;
			
			CyCLDevice newPreferred = null;
			for (CyCLDevice device : devices)
				if (device.name.equals(name))
				{
					newPreferred = device;
					break;
				}
			
			if (newPreferred != null)
			{
				devices.remove(newPreferred);
				devices.add(0, newPreferred);
			}
		}
	}
}
