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

import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.sling.ide.eclipse.ui.wizards.np.AbstractNewMavenBasedSlingApplicationWizard;
import org.apache.sling.ide.eclipse.ui.wizards.np.ArchetypeParametersWizardPage;
import org.apache.sling.ide.eclipse.ui.wizards.np.ChooseArchetypeWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class SimplerParametersWizardPage extends ArchetypeParametersWizardPage {

    private static final String ARTIFACT_DEFAULT = "example";

    private static final String GROUP_DEFAULT = "org.myorg";

    private static final String NAME_DEFAULT = "Example";

    private Text groupId;

    private Text artifactId;

    private AdvancedSettingsComponent advancedSettings;

    private Text name;

    private final ChooseArchetypeWizardPage chooseArchetypePage;

    protected boolean groupIdChanged;

    protected boolean artifactIdChanged;

    protected boolean nameChanged;

    public SimplerParametersWizardPage(
            AbstractNewMavenBasedSlingApplicationWizard parent) {
        super(parent);
        chooseArchetypePage = parent.getChooseArchetypePage();
    }
    
    public Archetype getChosenArchetype() {
        return chooseArchetypePage.getSelectedArchetype();
    }

    public void createControl(Composite parent) {
        final Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        layout.numColumns = 2;
        layout.verticalSpacing = 9;
        Label label;
        GridData gd;

        label = new Label(container, SWT.NULL);
        label.setText("&Name:");
        name = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        name.setLayoutData(gd);
        name.setToolTipText("Enter a short human readable name of the project");
        name.setText(NAME_DEFAULT);
        name.setForeground(new Color(parent.getDisplay(), 100, 100, 100));
        name.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!nameChanged) {
                    nameChanged = true;
                    name.setForeground(container.getForeground());
                }
                dialogChanged();
                advancedSettings.handleModifyText();
            }
        });

        label = new Label(container, SWT.NULL);
        label.setText("&Group Id:");
        groupId = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        groupId.setLayoutData(gd);
        groupId.setText(GROUP_DEFAULT);
        groupId.setForeground(new Color(parent.getDisplay(), 100, 100, 100));
        groupId.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!groupIdChanged) {
                    groupIdChanged = true;
                    groupId.setForeground(container.getForeground());
                }
                dialogChanged();
                advancedSettings.handleModifyText();
            }
        });
        groupId.setToolTipText("Enter a package-like identifier, eg org.myorg");

        label = new Label(container, SWT.NULL);
        label.setText("&Artifact Id:");

        artifactId = new Text(container, SWT.BORDER | SWT.SINGLE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        artifactId.setLayoutData(gd);
        artifactId.setText(ARTIFACT_DEFAULT);
        artifactId.setForeground(new Color(parent.getDisplay(), 100, 100, 100));
        artifactId.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!artifactIdChanged) {
                    artifactIdChanged = true;
                    artifactId.setForeground(container.getForeground());
                }
                dialogChanged();
                advancedSettings.handleModifyText();
            }
        });
        artifactId.setToolTipText("Enter an identifier (without '.') of the project");

        Composite advanced = new Composite(container, SWT.NONE);
        gd = new GridData(GridData.FILL_BOTH);
        gd.horizontalSpan = 2;
        advanced.setLayoutData(gd);
        GridLayout layout2 = new GridLayout();
        advanced.setLayout(layout2);
        layout2.numColumns = 2;
        layout2.verticalSpacing = 9;
        
        gd = new GridData(SWT.FILL, SWT.TOP, true, true, 2, 1);
        gd.verticalIndent = 7;
        advancedSettings = new AdvancedSettingsComponent(advanced, null, true, this);
        advancedSettings.setLayoutData(gd);
        advancedSettings.initialize();
        setPageComplete(false);
        setControl(container);
    }
    
    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            advancedSettings.initialize();
        }
    }

    /**
     * Ensures that both text fields are set.
     */

    void dialogChanged() {
        if (!groupIdChanged || groupId.getText().length() == 0) {
            updateStatus("group Id must be specified");
            return;
        }
        if (!artifactIdChanged || artifactId.getText().length() == 0) {
            updateStatus("artifact Id must be specified");
            return;
        }
        if (advancedSettings.version.getText().length() == 0) {
            updateStatus("version must be specified");
            return;
        }
        if (advancedSettings.javaPackage.getText().length() == 0) {
            updateStatus("package must be specified");
            return;
        }
        int cnt = advancedSettings.propertiesTable.getItemCount();
        for (int i = 0; i < cnt; i++) {
            TableItem item = advancedSettings.propertiesTable.getItem(i);
            if (item.getText(1).length() == 0) {
                updateStatus(item.getText(0) + " must be specified");
                return;
            }
        }

        updateStatus(null);
    }

    private void updateStatus(String message) {
        setErrorMessage(message);
        setPageComplete(message == null);
    }

    public String getGroupId() {
        if (!groupIdChanged) {
            return "";
        }
        return groupId.getText();
    }

    public String getArtifactId() {
        if (!artifactIdChanged) {
            return "";
        }
        return artifactId.getText();
    }

    public String getParameterName() {
        if (!nameChanged) {
            return "";
        }
        return name.getText();
    }

    public String getVersion() {
        return advancedSettings.version.getText();
    }

    public String getJavaPackage() {
        return advancedSettings.javaPackage.getText();
    }

    public Properties getProperties() {
        int cnt = advancedSettings.propertiesTable.getItemCount();
        Properties p = new Properties();
        for (int i = 0; i < cnt; i++) {
            TableItem item = advancedSettings.propertiesTable.getItem(i);
            p.put(item.getText(0), item.getText(1));
        }
        return p;
    }

}