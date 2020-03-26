package com.adobe.granite.ide.eclipse.ui.wizards.np;

import static org.junit.Assert.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.shared.utils.StringUtils;
import org.junit.Test;

@SuppressWarnings("restriction")
public class PropUtilsTest {

	private Map<String, RequiredPropertyWrapper> getProperties(String[][] props) {
		Map<String, RequiredPropertyWrapper> properties = new LinkedHashMap<String, RequiredPropertyWrapper>();
		for (String[] prop : props) {
			RequiredProperty property = new RequiredProperty();
			property.setKey(prop[0]);
			property.setDefaultValue(prop[2]);
			RequiredPropertyWrapper propertyWrapper = new RequiredPropertyWrapper(property);
			propertyWrapper.setValue(prop[1]);
			propertyWrapper.setModified("true".equalsIgnoreCase(prop[3]));
			properties.put(property.getKey(), propertyWrapper);
		}
		return properties;
	}
	
    @Test
    public void testUpdateProperties() {
    	Map<String, RequiredPropertyWrapper> properties = getProperties(new String[][] {
    		new String[] {
    				"unmodified", "", "DEFAULT_UNMODIFIED", ""
    		},
    		new String[] {
    				"computed", "", "DEFAULT_COMPUTED.${unmodified}", ""
    		},
    		new String[] {
    				"multiple", "", "DEFAULT_MULTIPLE.${computed}-${hasvalue}", ""
    		},
    		new String[] {
    				"hasvalue", "HASVALUE", "DEFAULT_HASVALUE", ""
    		},
    		new String[] {
    				"modified", "MODIFIED", "DEFAULT_MODIFIED.${hasvalue}", "true"
    		},
    		new String[] {
    				"stackoverflow", "", "${stackoverflow}", ""
    		}
    	});
    	
    	printProperties(properties);
    	PropUtils.updateProperties(properties);
    	printProperties(properties);
    	
    	assertEquals("DEFAULT_MULTIPLE.DEFAULT_COMPUTED.DEFAULT_UNMODIFIED-DEFAULT_HASVALUE", properties.get("multiple").getValue());
    	assertEquals("MODIFIED", properties.get("modified").getValue());
    	assertEquals("", properties.get("stackoverflow").getValue());
    }
    
	private void printProperties(Map<String, RequiredPropertyWrapper> properties) {
    	for (String key : properties.keySet()) {
    		RequiredPropertyWrapper property = properties.get(key);
    		System.out.println(StringUtils.rightPad(property.getKey(), 30) + " | " + 
    				StringUtils.rightPad(property.getValue(), 30) + " | " + 
    				StringUtils.rightPad(property.getDefaultValue(), 30) + " | " +
    				StringUtils.rightPad(String.valueOf(property.isModified()), 30));
    	}
    	System.out.println("\n");
    }
}
