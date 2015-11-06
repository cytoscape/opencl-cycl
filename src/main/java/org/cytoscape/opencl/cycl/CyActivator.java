package org.cytoscape.opencl.cycl;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.util.Properties;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator 
{
	@Override
	public void start(BundleContext context) throws Exception 
	{
		
		//System.out.println("Preferred device = " + preferredDevice);
		
		CyApplicationConfiguration applicationConfig = getService(context, CyApplicationConfiguration.class);		
		CyProperty<Properties> cyPropertyServiceRef = getService(context, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
			
		CyCL.initialize(applicationConfig, cyPropertyServiceRef);
		
		CyCL service = new CyCL();
		
		//System.out.println("Top device after init = " + CyCL.getDevices().get(0).name);
		
		Properties properties = new Properties();
		registerService(context, service, CyCL.class, properties);
		
		if (CyCL.getDevices().size() > 0)
		{
			CyCLSettingsTaskFactory settingsTaskFactory = new CyCLSettingsTaskFactory(cyPropertyServiceRef);
			
			Properties settingsTaskFactoryProps = new Properties();
			settingsTaskFactoryProps.setProperty(PREFERRED_MENU, "Edit.Preferences");
			settingsTaskFactoryProps.setProperty(MENU_GRAVITY, "5.0");
			settingsTaskFactoryProps.setProperty(TITLE, "OpenCL Settings...");
			registerService(context, settingsTaskFactory, TaskFactory.class, settingsTaskFactoryProps);
		}
	}
}