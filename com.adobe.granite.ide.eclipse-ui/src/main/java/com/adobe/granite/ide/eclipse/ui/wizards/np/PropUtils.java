package com.adobe.granite.ide.eclipse.ui.wizards.np;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropUtils {
    
    private static final Pattern pattern = Pattern.compile("\\$\\{(\\w+?)\\}");

	/**
	 * Update all properties based on default value, replacing placeholders with recursively processed values
	 * 
	 * @param properties
	 */
	public static void updateProperties(Map<String, RequiredPropertyWrapper> properties) {
    	for (String key : properties.keySet()) {
    		getUpdateProperty(key, properties, 0);
    	}
	}
	
	/**
	 * Update a property, looking up and updating placeholders too, recursively
	 * 
	 * @param key Property key
	 * @param properties Properties map
	 * @param calls Recursion limit to avoid stack overflow
	 * 
	 * @return Updated value for the property
	 */
	@SuppressWarnings("restriction")
	private static String getUpdateProperty(String key, Map<String, RequiredPropertyWrapper> properties, int calls) {
		// Prevent stack overflows
		if (calls > 50) {
			return "";
		}
		RequiredPropertyWrapper property = properties.get(key);
		if (property == null) {
			return "";
		}
		// Skip manually modified properties
		if (!property.isModified()) {
			String defaultValue = property.getDefaultValue();
			if (defaultValue != null) {
				Matcher matcher = pattern.matcher(defaultValue);
				String newValue = defaultValue;
				// For each matched placeholder, replace it with respective property, updating it before
				while (matcher.find()) {
					if (matcher.groupCount() > 0) {
						String match = matcher.group(1);
						newValue = newValue.replace("${" + match + "}", getUpdateProperty(match, properties, ++calls));
					}
				}
				property.setValue(newValue);
			} else {
				property.setValue("");
			}
		}
		return property.getValue();
	}

}
