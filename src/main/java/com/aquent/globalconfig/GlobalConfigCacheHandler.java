package com.aquent.globalconfig;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Type;
import java.util.HashMap;

import com.dotmarketing.business.CacheLocator;
import com.dotmarketing.business.DotCacheAdministrator;
import com.dotmarketing.business.DotCacheException;
import com.dotmarketing.util.Config;
import com.dotmarketing.util.Logger;
import com.dotmarketing.util.UtilMethods;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.liferay.util.FileUtil;

public enum GlobalConfigCacheHandler {
	// Makes this a singleton
	INSTANCE; 
	
	// A name for this cache pool
	public static final String CACHE_GROUP_NAME = "AquentGlobalConfigCache";
	
	// The filename for the properties file
	public static final String PLUGIN_FILE_NAME = "aquentglobalconfig.json";
	
	private String assetsDir = null;
	private HashMap<String,String> data = null;
	private Gson gson = new Gson();
	private Type dataType = new TypeToken<HashMap<String,String>>() {}.getType();
	
	
	/**
	 * Returns the value of a key in the global config or null if not found
	 * Checks the cache first if there is one there and then if not it loads it from memory
	 * If not in memory it goes to file
	 * 
	 * @param 	key		The key you want to pull from cache
	 * @return			The value of the key in the properties or null if not found
	 */
	public String get(String key) {
		if (!UtilMethods.isSet(key)) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		Object o = null;

		// First we try to get the item from the cache
		try {
			o = cache.get(key, CACHE_GROUP_NAME);
		} catch (DotCacheException e) {
			Logger.error(this.getClass(), String.format("DotCacheException for Group '%s', key '%s', message: %s", CACHE_GROUP_NAME, key, e.getMessage()), e);
		}
		
		if(o == null) {
			String v = loadProperty(key);
			put(key, v);
			return v;
		} else {
			return (String) o;
		}
	}
	
	/**
	 * Puts an item into the cache
	 * 
	 * @param 	key		The key value
	 * @param 	value	The value
	 */
	public void put(String key, String value) {
		if (!UtilMethods.isSet(key)) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		if (!UtilMethods.isSet(value)) {
			throw new IllegalArgumentException("value cannot be null.");
		}
		
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.put(key, value, CACHE_GROUP_NAME);
	}
	
	/**
	 * Removes an item from the cache
	 * 
	 * @param 	key		The key to remove
	 */
	public void remove(String key) {
		if (!UtilMethods.isSet(key)) {
			throw new IllegalArgumentException("key cannot be null.");
		}
		
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.remove(key, CACHE_GROUP_NAME);
		data = null; // want to reload from file if some reason we had to remove something from the cache
	}
	
	/**
	 * Flushing the cache
	 */
	public void removeAll() {
		DotCacheAdministrator cache = CacheLocator.getCacheAdministrator();
		cache.flushGroup(CACHE_GROUP_NAME);
		data = null; // want to reload from file again after a cache flush
	}
	
	/**
	 * Loads a property from memory or file
	 * 
	 * @param 	key		The property to get
	 * @return			The value of the property from memory or file
	 */
	private String loadProperty(String key) {
		HashMap<String,String> props = null;
		try {
			props = getData();
		} catch(Exception e) {
			Logger.error(this, "Unable to load the properties data", e);
			return null;
		}
		
		if(props != null) {
		    return props.get(key);
		} else {
		    return null;
		}
	}
	
	/**
	 * Loads the properties file into memory
	 * 
	 * @return		The data object
	 * @throws Exception
	 */
	public HashMap<String,String> getData(boolean fromFile) throws Exception {
	    if(data == null || fromFile) {
			String f = getAssetsDir() + File.separator + PLUGIN_FILE_NAME;
			File jsonFile = new File(f);
			String jsonString = "{}";
			if(jsonFile.exists()) {
				jsonString = Files.toString(jsonFile, Charsets.UTF_8);
			} else {
				// The file doesn't exist so let's create it
				jsonFile.createNewFile();
				FileOutputStream oFile = new FileOutputStream(jsonFile, false);
				oFile.write(jsonString.getBytes());
				oFile.flush();
				oFile.close();
			}
			
			data = gson.fromJson(jsonString, dataType);
		}
		return data;
	}
	
	/**
	 * Same as getData(String) where the fromFile is defaulted to false
	 * 
	 * @return		The data object
	 * @throws Exception
	 */
	public HashMap<String,String> getData() throws Exception {
		return getData(false);
	}
	
	
	/**
	 * Get the assets path.
	 * 
	 * @return	The assets path
	 * @throws Exception when the assets directory is not found
	 */
	private String getAssetsDir() throws Exception {
		if(!UtilMethods.isSet(assetsDir)) {
		    assetsDir = Config.getStringProperty("ASSET_REAL_PATH", 
		                      FileUtil.getRealPath(Config.getStringProperty("ASSET_PATH", null)));	
		}
		
		if(!UtilMethods.isSet(assetsDir)) {
		    throw new Exception("No Assets Directory Found");
		}
		
		return assetsDir;
	}
	
	/**
	 * Updates the data on file
	 * 
	 * @param 	newdata		A JSONObject for the new data
	 * @throws Exception
	 */
	public void saveData(HashMap<String,String> newdata) throws Exception {		
		String newDataString = gson.toJson(newdata, dataType);
		
		String f = getAssetsDir() + File.separator + PLUGIN_FILE_NAME;
		File jsonFile = new File(f);
		FileOutputStream oFile = new FileOutputStream(jsonFile, false);
		oFile.write(newDataString.getBytes());
		oFile.flush();
		oFile.close();
		
		removeAll();
	}
}
