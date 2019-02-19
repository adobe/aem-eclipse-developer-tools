package com.adobe.granite.ide.eclipse.ui.wizards.np;

public enum Property {

    APPS_FOLDER_NAME("appsFolderName"),
    ARTIFACT_ID("artifactId"),
    GROUP_ID("groupId"),
    ARTIFACT_NAME("artifactName"),
    PACKAGE_GROUP("packageGroup"),
    CONTENT_FOLDER_NAME("contentFolderName"),
    CONF_FOLDER_NAME("confFolderName"),
    CSS_ID("cssId"),
    COMPONENT_GROUP_NAME("componentGroupName"),
    SITE_NAME("siteName"),
    OPTION_INCLUDE_EXAMPLES("optionIncludeExamples");

    private String key;


    Property(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }

    public static Property fromString(String key) {
        for (Property property: Property.values()) {
            if (property.key.equals(key)) {
                return property;
            }
        }
        return null;
    }
}
