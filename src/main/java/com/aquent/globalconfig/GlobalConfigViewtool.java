package com.aquent.globalconfig;

import org.apache.velocity.tools.view.tools.ViewTool;
import org.json.JSONObject;

import com.dotmarketing.util.Logger;

public class GlobalConfigViewtool implements ViewTool {
	
	@Override
	public void init(Object initData) {
		Logger.info(this, "GlobalConfig Viewtool Initialized");
	}
	
	/**
	 * Get a value from the config
	 * 
	 * @param 	key		The key for the value you want
	 * @return			The value for the passed in key, or null if not found
	 */
	public String getProperty(String key) {
		return GlobalConfigCacheHandler.INSTANCE.get(key);
	}
	
	/**
	 * Gets The Data object in memory
	 * 
	 * @return  The Data object containing all the keys and values
	 */
	public JSONObject getData() {
		try {
			return GlobalConfigCacheHandler.INSTANCE.getData(false);
		} catch(Exception e) {
			Logger.error(this, "Unable to get the data from the properties file", e);
			return null;
		}
	}
	
	/**
	 * Caution:  Do not use on a live page without some caching involved
	 * This re-reads the data into memory from the config file and then returns the data object
	 * 
	 * @return	The Data object containing all the keys and values
	 */
	public JSONObject getDataFromFile() {
		try {
			return GlobalConfigCacheHandler.INSTANCE.getData(true);
		} catch(Exception e) {
			Logger.error(this, "Unable to get the data from the properties file", e);
			return null;
		}
	}

}
