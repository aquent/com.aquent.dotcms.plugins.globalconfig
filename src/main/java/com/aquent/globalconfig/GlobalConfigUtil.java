package com.aquent.globalconfig;

import org.json.JSONObject;

import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;

public class GlobalConfigUtil {
	public final static GlobalConfigUtil INSTANCE = new GlobalConfigUtil();
	
	private GlobalConfigUtil() {}
	
	public static GlobalConfigUtil getInstance() {
		return INSTANCE;
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
	
	/**
	 * Updates a property in the data
	 * 
	 * @param 	orig_key	The original key
	 * @param 	key			The new key
	 * @param 	value		The value
	 * @return 				true if successfully changed, false otherwise
	 */
	public boolean updateProperty(String orig_key, String key, String value) {
		synchronized("agc_data") {
			// Remove the key from the data
			JSONObject data = getDataFromFile();
			if(UtilMethods.isSet(orig_key) && data.has(orig_key)) data.remove(orig_key);
			if(UtilMethods.isSet(key) && data.has(key)) data.remove(key);
			
			try {
				// Add the key to the data
				data.put(key, value);
				// Save the data
				GlobalConfigCacheHandler.INSTANCE.saveData(data);
			} catch(Exception e) {
				Logger.error(this, "Unable to add the new property: "+key+"="+value);
				return false;
			}
		}
		
		return true;
	}

	/**
	 * Deletes a property from the data
	 * 
	 * @param 	key		The key to remove
	 * @return			true if successfully removed, false otherwise
	 */
	public boolean deleteProperty(String key) {
		synchronized("agc_data") {
			// Remove the key from the data
			JSONObject data = getDataFromFile();
			data.remove(key);
			
			try {
				// Save the data
				GlobalConfigCacheHandler.INSTANCE.saveData(data);
			} catch(Exception e) {
				Logger.error(this, "Unable to delete the property: "+key);
				return false;
			}
		}
		
		return true;
	}
}
