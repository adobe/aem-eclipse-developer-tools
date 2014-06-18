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
import java.util.List;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.archetype.metadata.RequiredProperty;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.eclipse.jface.dialogs.IDialogPage;
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
import org.eclipse.swt.widgets.Label;
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

    Text javaPackage;

    private boolean javaPackageModified;

    private TableViewer propertiesViewer;

    Table propertiesTable;

    private List<RequiredProperty> properties;

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

        propertiesViewer.setColumnProperties(new String[] {
                KEY_PROPERTY,
                VALUE_PROPERTY });

        propertiesViewer.setCellEditors(new CellEditor[] {
                null /* cannot edit the name */,
                new TextCellEditor(propertiesTable, SWT.NONE) });
        propertiesViewer.setCellModifier(new ICellModifier() {
            public boolean canModify(Object element, String property) {
                return true;
            }

            public void modify(Object element, String property, Object value) {
                if (element instanceof TableItem) {
                    ((TableItem) element).setText(
                            getTextIndex(property),
                            String.valueOf(value));
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
            properties = (List<RequiredProperty>) archetypeManager
                    .getRequiredProperties(archetype,
                            remoteArchetypeRepository, null);

            Table table = propertiesViewer.getTable();
            table.setItemCount(properties.size());
            int i = 0;
            for (Iterator<RequiredProperty> it = properties.iterator(); it
                    .hasNext();) {
                RequiredProperty rp = it.next();
                TableItem item = table.getItem(i++);
                if (!rp.getKey().equals(item.getText())) {
                    // then create it - otherwise, reuse it
                    item.setText(0, rp.getKey());
                    item.setText(1, "");
                    item.setData(item);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not process archetype: "
                    + e.getMessage(), e);
        }

    }

    public void handleModifyText() {
        final String artifactId = wizardPage.getArtifactId();
        final String groupId = wizardPage.getGroupId();
        final String name = wizardPage.getParameterName();
        
        final String defaultJavaPackage;
        if (artifactId.length() == 0) {
            defaultJavaPackage = SimplerParametersWizardPage
                    .getDefaultJavaPackage(
                            groupId, "");
        } else {
            defaultJavaPackage = SimplerParametersWizardPage
                    .getDefaultJavaPackage(
                            groupId,
                            artifactId);
        }
        
        String defaultAppsName = name.toLowerCase();
        defaultAppsName = defaultAppsName.replaceAll("[^a-zA-Z]", "");
        
        Table table = propertiesViewer.getTable();
        table.setItemCount(properties.size());
        int i = 0;
        for (Iterator<RequiredProperty> it = properties.iterator(); it
                .hasNext();) {
            final RequiredProperty rp = it.next();
            final String text;
            if (rp.getKey().equals("artifactName")) {
                text = name;
            } else if (rp.getKey().equals("packageGroup")) {
                text = name+" Content Package";
            } else if (rp.getKey().equals("appsFolderName")) {
                text = defaultAppsName;
            } else if (rp.getKey().equals("contentFolderName")) {
                text = defaultAppsName;
            } else if (rp.getKey().equals("cssId")) {
                text = artifactId;
            } else if (rp.getKey().equals("componentGroupName")) {
                text = name+" Components";
            } else if (rp.getKey().equals("siteName")) {
                text = name+" Site";
            } else {
                continue;
            }
            // create or reuse:
            TableItem item = table.getItem(i++);
            item.setText(0, rp.getKey());
            item.setText(1, text);
            item.setData(item);
        }
        if (!javaPackageModified) {
            javaPackage.setText(defaultJavaPackage);
        }
    }
}