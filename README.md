Global Configuration Plugin
====================
This plugin allows you to set global config varaiables.

Included is a Portlet to manage the config parameters and viewtool to get the values from the config.  
The values are stored in your assets directory under aquentglobalconfig.json and are read read into a cache as they are used.

Installation
------------
* Navigate to the dotCMS Dynamic plugins page: "System" > "Dynamic Plugins"
* Click on "Upload plugin" and select the .jar file located in the "build/libs/" folder
* Navigate to "System" > "Roles and Tabs" and add the portlet to a tab that your admin users have access to.

Usage Example
-------------
The internal storage of the data uses [org.json.JSONobject](http://www.json.org/javadoc/org/json/JSONObject.html).

In Java Code:
```java
// Get a Util Instance
GlobalConfigUtil gcu =  GlobalConfigUtil.getInstance();

// Get a value of a property from a key
String key = "test";
String value = gcu.getValue(key);
// Do something with Value

// Get all the data in json format (org.json.JSONObject)
JSONObject data = gcu.getData();
for(String k : data.getNames(data)) {
	String v = data.getString(k);
	// Do something with the value v
} 

// Update a property
boolean update_status = gcu.updateProperty("original_key", "new_key", "new_value");

// Delete a property
boolean delete_status = gcu.deleteProperty("key");
```

In Velocity Code:
```
## Get a value from the config:
#set($key = "test")
#set($value = $gconfig.getProperty($key))
## Do Something with $value

## Get all the data in json format (org.json.JSONObject)
#set($data = $gconfig.getData())
#set($keys = $data.getNames($data))
#foreach($k in $keys) 
  #set($v = $data.getString($key))
  ## Do something with $v
#end
```

Building
--------
* Install Gradle (if not already installed)
* gradle jar 
