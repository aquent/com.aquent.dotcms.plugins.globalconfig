package com.aquent.globalconfig;

import org.apache.felix.http.api.ExtHttpService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;

import com.dotmarketing.filters.CMSFilter;
import com.dotmarketing.osgi.GenericBundleActivator;
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

}
