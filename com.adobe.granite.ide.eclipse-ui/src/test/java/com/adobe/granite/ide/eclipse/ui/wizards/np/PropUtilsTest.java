package com.adobe.granite.ide.eclipse.ui.wizards.np;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.maven.archetype.metadata.RequiredProperty;

import org.apache.maven.shared.utils.StringUtils;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
class PropUtilsTest {

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
    void updateProperties() {
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
    		},
    		new String[] {
    				"empty", "", "", ""
    		},
    		new String[] {
    				"useempty", "", "${empty}", ""
    		},
    		new String[] {
    				"null", null, null, null
    		},
    		new String[] {
    				"usenull", "", "${null}", ""
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
    		System.out.println(pad(property.getKey()) + " | " + 
    				pad(property.getValue()) + " | " + 
    				pad(property.getDefaultValue()) + " | " +
    				pad(property.isModified()));
    	}
    	System.out.println("\n");
    }
	
	private String pad(Object obj) {
		return StringUtils.rightPad(String.valueOf(obj), 30);
	}
}
