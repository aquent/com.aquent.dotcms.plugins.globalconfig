package com.aquent.globalconfig;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PropertyResourceBundle;
import java.util.Set;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.dotmarketing.business.APILocator;
import com.dotmarketing.exception.DotDataException;
import com.dotmarketing.filters.CMSFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
import com.dotmarketing.portlets.languagesmanager.model.Language;
import com.dotmarketing.util.Logger;

public class GlobalConfigActivator extends GenericBundleActivator {

	private GlobalConfigServlet gcs;
	
	private ServiceTracker<ExtHttpService, ExtHttpService> tracker;
	
    @Override
    public void start ( BundleContext bundleContext ) throws Exception {

        initializeServices( bundleContext );
        
        //Registering the ViewTool service
        registerViewToolService( bundleContext, new GlobalConfigViewtoolInfo() );
        
        // Start the Servlets
    	registerServlets( bundleContext );
        
        // Register the portlets
        registerPortlets( bundleContext, new String[] { "conf/portlet.xml", "conf/liferay-portlet.xml"} );
        
        // Register language variables (portlet name)
     	registerLanguageVariables( bundleContext );
     	
    }
    
    @Override
    public void stop ( BundleContext bundleContext ) throws Exception {
    	// Unregister the view tools
    	unregisterViewToolServices();
    	
    	// Clear the cache
    	GlobalConfigCacheHandler.INSTANCE.removeAll();
    	
    	// Stop the Servlets
    	tracker.close();
    	
    	unregisterServices( bundleContext );
    	
    }
    
    private void registerServlets( BundleContext ctx) {
		tracker = new ServiceTracker<ExtHttpService, ExtHttpService>(ctx, ExtHttpService.class, null) {
			@Override public ExtHttpService addingService(ServiceReference<ExtHttpService> reference) {
				ExtHttpService extHttpService = super.addingService(reference);
				
				gcs = new GlobalConfigServlet();
				
				try {
					extHttpService.registerServlet("/globalConfigServlet", gcs, null, null);
				} catch (Exception e) {
					throw new RuntimeException("Failed to register servlets", e);
				}
				
				// Add the Servlets to the Exclude list
		    	CMSFilter.addExclude("/app/globalConfigServlet");
		    	
				Logger.info(this, "Registered servlets");

				return extHttpService;
			}
			@Override public void removedService(ServiceReference<ExtHttpService> reference, ExtHttpService extHttpService) {
				// Remove serlvet Excludes from the list
		    	CMSFilter.removeExclude("/app/globalConfigServlet");
		    	
				extHttpService.unregisterServlet(gcs);
				
				super.removedService(reference, extHttpService);
			}
		};
		tracker.open();
    }
    
	private void registerLanguageVariables(Map<String, String> languageVariables, Language language) {
		Map<String, String> emptyMap = new HashMap<String, String>();
		Set<String> emptySet = new HashSet<String>();
		try {

			Logger.info(this, "Registering " + languageVariables.keySet().size() + " language variable(s)");
			APILocator.getLanguageAPI().saveLanguageKeys(language, languageVariables, emptyMap, emptySet);

		} catch (DotDataException e) {
			Logger.warn(this, "Unable to register language variables", e);
		}
	}
	
	private void registerLanguageVariables(BundleContext context) {
		try {

			// Read all the language variables from the properties file
			URL resourceURL = context.getBundle().getResource("conf/Language-ext.properties");
			PropertyResourceBundle resourceBundle = new PropertyResourceBundle(resourceURL.openStream());
			
			// Put the properties in a map
			Map<String, String> languageVariables = new HashMap<String, String>();
			for(String key: resourceBundle.keySet()) {
				languageVariables.put(key, resourceBundle.getString(key));
			}
			
			// Register the variables in locale en_US
			registerLanguageVariables(languageVariables, APILocator.getLanguageAPI().getLanguage("en", "US"));
			
		} catch (IOException e) {
			Logger.warn(this, "Exception while registering language variables", e);
		}
	}

}
