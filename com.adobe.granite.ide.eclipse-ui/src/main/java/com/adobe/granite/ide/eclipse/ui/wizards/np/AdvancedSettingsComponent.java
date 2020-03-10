/*
 *  Copyright 2014 Adobe Systems Incorporated
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.adobe.granite.ide.eclipse.ui.wizards.np;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CellNavigationStrategy;
import org.eclipse.jface.viewers.ColumnViewerEditor;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationEvent;
import org.eclipse.jface.viewers.ColumnViewerEditorActivationStrategy;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerEditor;
import org.eclipse.jface.viewers.TableViewerFocusCellManager;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.m2e.core.internal.MavenPluginActivator;
import org.eclipse.m2e.core.internal.archetype.ArchetypeManager;
import org.eclipse.m2e.core.project.ProjectImportConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

public class AdvancedSettingsComponent extends ExpandableComposite {

    private static final String KEY_PROPERTY = "key";

    private static final String VALUE_PROPERTY = "value";

    private static final String DEFAULT_VALUE_PROPERTY = "defaultValue";
    
    public static final String GROUP_ID = "groupId";
    
    public static final String ARTIFACT_ID = "artifactId";
    
    public static final String APP_ID = "appId";
    
    public static final String APP_TITLE = "appTitle";

    Text javaPackage;

    private boolean javaPackageModified;

    private TableViewer propertiesViewer;

    Table propertiesTable;

    private Map<String, RequiredPropertyWrapper> properties = new LinkedHashMap<String, RequiredPropertyWrapper>();

    Text version;

    private final SimplerParametersWizardPage wizardPage;

    /**
     * Creates a new component.
     * 
     * @param wizardPage
     */
    public AdvancedSettingsComponent(final Composite parent,
            final ProjectImportConfiguration propectImportConfiguration,
            final boolean enableProjectNameTemplate,
            SimplerParametersWizardPage wizardPage) {
        super(parent, ExpandableComposite.COMPACT | ExpandableComposite.TWISTIE
                | ExpandableComposite.EXPANDED);
        this.wizardPage = wizardPage;
        setText("Advanced");
        final Composite advancedComposite = new Composite(this, SWT.NONE);
        setClient(advancedComposite);
        addExpansionListener(new ExpansionAdapter() {
            public void expansionStateChanged(ExpansionEvent e) {
                Shell shell = parent.getShell();
                Point minSize = shell.getMinimumSize();
                shell.setMinimumSize(shell.getSize().x, minSize.y);
                shell.pack();
                parent.layout();
                shell.setMinimumSize(minSize);
            }
        });
        GridLayout gridLayout = new GridLayout();
        gridLayout.marginLeft = 11;
        gridLayout.numColumns = 2;
        advancedComposite.setLayout(gridLayout);
        createAdvancedSection(advancedComposite);
    }

    public void createAdvancedSection(Composite container) {
        Label label;
        GridData gd;

        label = new Label(container, SWT.NULL);
        label.setText("&Version:");

        version = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        version.setLayoutData(gd);
        version.setText("0.0.1-SNAPSHOT");
        version.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                wizardPage.dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText("&Package:");

        javaPackage = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        javaPackage.setLayoutData(gd);
        javaPackageModified = false;
        javaPackage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                javaPackageModified = true;
            }
        });
        javaPackage.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                wizardPage.dialogChanged();
            }
        });

        label = new Label(container, SWT.NULL);
        gd = new GridData(SWT.LEFT, SWT.TOP, false, false);
        label.setLayoutData(gd);
        label.setText("&Parameters:");

        propertiesViewer = new TableViewer(container, SWT.BORDER
                | SWT.FULL_SELECTION);
        propertiesTable = propertiesViewer.getTable();
        propertiesTable.setLinesVisible(true);
        propertiesTable.setHeaderVisible(true);
        propertiesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
                true, 2, 2));

        CellNavigationStrategy strategy = new CellNavigationStrategy();
        TableViewerFocusCellManager focusCellMgr = new TableViewerFocusCellManager(
                propertiesViewer, new FocusCellOwnerDrawHighlighter(
                        propertiesViewer), strategy);

        ColumnViewerEditorActivationStrategy actSupport = new ColumnViewerEditorActivationStrategy(
                propertiesViewer) {

            @Override
            protected boolean isEditorActivationEvent(
                    ColumnViewerEditorActivationEvent event) {
                return event.eventType == ColumnViewerEditorActivationEvent.TRAVERSAL
                        || event.eventType == ColumnViewerEditorActivationEvent.MOUSE_DOUBLE_CLICK_SELECTION
                        || (event.eventType == ColumnViewerEditorActivationEvent.KEY_PRESSED && event.keyCode == SWT.CR)
                        || event.eventType == ColumnViewerEditorActivationEvent.PROGRAMMATIC;
            }
        };
        int features = ColumnViewerEditor.TABBING_HORIZONTAL
                | ColumnViewerEditor.TABBING_MOVE_TO_ROW_NEIGHBOR
                | ColumnViewerEditor.TABBING_VERTICAL
                | ColumnViewerEditor.KEYBOARD_ACTIVATION
                | ColumnViewerEditor.KEEP_EDITOR_ON_DOUBLE_CLICK;
        TableViewerEditor.create(propertiesViewer, focusCellMgr, actSupport,
                features);

        TableColumn propertiesTableNameColumn = new TableColumn(
                propertiesTable, SWT.NONE);
        propertiesTableNameColumn.setWidth(180);
        propertiesTableNameColumn.setText("Name");

        TableColumn propertiesTableValueColumn = new TableColumn(
                propertiesTable, SWT.NONE);
        propertiesTableValueColumn.setWidth(300);
        propertiesTableValueColumn.setText("Value");
        
        TableColumn propertiesTableDefaultValueColumn = new TableColumn(
                propertiesTable, SWT.NONE);
        propertiesTableDefaultValueColumn.setWidth(300);
        propertiesTableDefaultValueColumn.setText("Default");

        propertiesViewer.setColumnProperties(new String[] {
                KEY_PROPERTY,
                VALUE_PROPERTY,
                DEFAULT_VALUE_PROPERTY});

        propertiesViewer.setCellEditors(new CellEditor[] {
                null /* cannot edit the name */,
                new TextCellEditor(propertiesTable, SWT.NONE) });
        propertiesViewer.setCellModifier(new ICellModifier() {
            public boolean canModify(Object element, String property) {
                return true;
            }

            public void modify(Object element, String property, Object value) {
                if (element instanceof TableItem) {
                	TableItem item = (TableItem) element;
                    item.setText(
                            getTextIndex(property),
                            String.valueOf(value));
                    handleModifyText(item.getText(0), String.valueOf(value), true);
                    wizardPage.dialogChanged();
                }
            }

            public Object getValue(Object element, String property) {
                if (element instanceof TableItem) {
                    return ((TableItem) element)
                            .getText(getTextIndex(property));
                }
                return null;
            }
        });

    }

    protected int getTextIndex(String property) {
        if (KEY_PROPERTY.equals(property)) {
            return 0;
        } else {
            return 1;
        }
    }

    @SuppressWarnings("restriction")
    void initialize() {
        if (propertiesTable == null) {
            return;
        }

        Archetype archetype = wizardPage.getChosenArchetype();
        if (archetype == null) {
            return;
        }

        try {
            ArchetypeManager archetypeManager = MavenPluginActivator
                    .getDefault().getArchetypeManager();
            ArtifactRepository remoteArchetypeRepository = archetypeManager
                    .getArchetypeRepository(archetype);
            properties.clear();
            for (RequiredProperty prop : (List<RequiredProperty>) archetypeManager
                    .getRequiredProperties(archetype,
                            remoteArchetypeRepository, null)) {
            	properties.put(prop.getKey(), new RequiredPropertyWrapper(prop));
            }

            Table table = propertiesViewer.getTable();
            table.setItemCount(properties.size());
            int i = 0;
            for (Iterator<RequiredPropertyWrapper> it = properties.values().iterator(); it
                    .hasNext();) {
                RequiredPropertyWrapper rp = it.next();
                TableItem item = table.getItem(i++);
                if (!rp.getKey().equals(item.getText())) {
                    // then create it - otherwise, reuse it
                    item.setText(0, rp.getKey());
                    item.setText(1, rp.getDefaultValue() != null ? rp.getDefaultValue() : "");
                    item.setText(2, rp.getDefaultValue() != null ? rp.getDefaultValue() : "");
                    item.setData(item);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not process archetype: "
                    + e.getMessage(), e);
        }

    }

    @SuppressWarnings("restriction")
	public void handleModifyText(String propertyKey, String newValue, boolean updateMainControls) {
    	if (updateMainControls) {   		
	    	if (GROUP_ID.equals(propertyKey)) {
	   			wizardPage.setGroupId(newValue);
	    	}
	    	if (ARTIFACT_ID.equals(propertyKey)) {
	    		wizardPage.setArtifactId(newValue);
	    	}
	    }
    	if (GROUP_ID.equals(propertyKey) && !javaPackageModified) {
    		javaPackage.setText(SimplerParametersWizardPage.getDefaultJavaPackage(newValue, ""));
    	}

        Table table = propertiesViewer.getTable();
        int i = 0;
    	for (String key : properties.keySet()) {
    		RequiredPropertyWrapper property = properties.get(key);
    		if (propertyKey.equals(property.getKey())) {
    			property.setValue(newValue);
    		} else if (!property.isModified()) {
    			String defaultValue = property.getDefaultValue();
    			if (defaultValue != null && defaultValue.contains("${" + propertyKey + "}")) {
    				property.setValue(defaultValue.replace("${" + propertyKey + "}", newValue));
    				if (GROUP_ID.equals(property.getKey())) {
    		   			wizardPage.setGroupId(property.getValue());
    		    	}
    		    	if (ARTIFACT_ID.equals(property.getKey())) {
    		    		wizardPage.setArtifactId(property.getValue());
    		    	}
    			}
    		}
			TableItem item = table.getItem(i++);
            item.setText(0, property.getKey());
            if (property.getValue() != null) {
            	item.setText(1, property.getValue());
            }
            item.setText(2, property.getDefaultValue() != null ? property.getDefaultValue() : "");
            item.setData(item);
    	}

    }
}