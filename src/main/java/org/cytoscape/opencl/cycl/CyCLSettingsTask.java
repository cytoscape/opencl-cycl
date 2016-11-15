package org.cytoscape.opencl.cycl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.property.CyProperty;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ProvidesTitle;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;


public class CyCLSettingsTask extends AbstractTask implements TunableValidator 
{	
	@ProvidesTitle
	public String getTitle() 
	{
		return "OpenCL Settings";
	}
		
	private static final List<String> KEYS = Arrays.asList(CyCLSettings.PREFERREDNAME, CyCLSettings.PREVENT_FULL_OCCUPATION);
	

	@Tunable(description="Preferred Device:")
	public ListSingleSelection<String> preferredNameList;

	@Tunable(description="Prevent Full Device Occupation:",tooltip="Useful when computing on a GPU that runs the computers display.\n"
			+ " Instructs apps to not occupy the device completely, letting OS rendering to take place and preventing the OS from freezing.")
	public boolean preventFullOccupation;
	
	private final Map<String, String> oldSettings;
	private final Properties properties;

	public CyCLSettingsTask(CyProperty<Properties> properties) 
	{
		oldSettings = new HashMap<String, String>();
		this.properties = properties.getProperties();
				
	    List<String> deviceNames = new ArrayList<>();
		List<CyCLDevice> devices = CyCL.getDevices();
		for (CyCLDevice device : devices)
			deviceNames.add(device.name);
		
		preferredNameList = new ListSingleSelection<String>(deviceNames);
		
		try 
		{
            final String preferredName = this.properties.getProperty(CyCLSettings.PREFERREDNAME);
            if (deviceNames.contains(preferredName))
            	preferredNameList.setSelectedValue(preferredName);
            else
            	preferredNameList.setSelectedValue(deviceNames.get(0));
		} 
		catch (IllegalArgumentException e) 
		{
			preferredNameList.setSelectedValue(deviceNames.get(0));
		}

		String preventOccupationString = this.properties.getProperty(CyCLSettings.PREVENT_FULL_OCCUPATION);
		if(preventOccupationString == null)
		{
			preventFullOccupation = false; //the default is false
		}
		else 
		{
			preventFullOccupation = Boolean.parseBoolean(preventOccupationString);			
		}

        assignSystemProperties();
	}

    public void assignSystemProperties() 
    {
        String newPreferred = properties.getProperty(CyCLSettings.PREFERREDNAME);
        if (newPreferred == null || newPreferred.length() == 0)
        	return;
        
        CyCL.makePreferred(newPreferred);
    }
	
	@Override
	public ValidationState getValidationState(final Appendable errMsg) 
	{	
		storeSettings();

		revertSettings();
		
		return ValidationState.OK;
	}

	@Override
	public void run(TaskMonitor taskMonitor) 
	{
		taskMonitor.setProgress(0.0);
		
		storeSettings();
		oldSettings.clear();
		
		taskMonitor.setProgress(1.0);
	}

	void storeSettings() 
	{
		oldSettings.clear();
		for (String key : KEYS) 
		{
			if (properties.getProperty(key) != null)
				oldSettings.put(key, properties.getProperty(key));
			properties.remove(key);
		}

		properties.setProperty(CyCLSettings.PREFERREDNAME, preferredNameList.getSelectedValue());
		properties.setProperty(CyCLSettings.PREVENT_FULL_OCCUPATION, Boolean.toString(preventFullOccupation));
        
        assignSystemProperties();
	}

	void revertSettings() 
	{
		for (String key : KEYS) 
		{
			properties.remove(key);
			
			if (oldSettings.containsKey(key))
				properties.setProperty(key, oldSettings.get(key));
		}
		oldSettings.clear();
        
        assignSystemProperties();
	}
}
