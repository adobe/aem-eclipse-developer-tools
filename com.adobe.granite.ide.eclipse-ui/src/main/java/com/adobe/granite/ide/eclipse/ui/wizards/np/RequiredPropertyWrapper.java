package com.adobe.granite.ide.eclipse.ui.wizards.np;

import org.apache.maven.archetype.metadata.RequiredProperty;

public class RequiredPropertyWrapper extends RequiredProperty {
	
	private boolean modified = false;
	
	private String value;
	
	public RequiredPropertyWrapper(RequiredProperty property) {
		super();
		setKey(property.getKey());
		setDefaultValue(property.getDefaultValue());
		setValue(property.getDefaultValue());
	}
	
	public boolean isModified() {
		return modified;
	}
	
	public void setModified(boolean modified) {
		this.modified = modified;
	}
	
	public String getValue() {
		return value;
	}
	
	public void setValue(String value) {
		this.value = value;
	}
}
