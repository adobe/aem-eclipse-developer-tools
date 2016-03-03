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
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.sling.ide.eclipse.core.ISlingLaunchpadServer;
import org.apache.sling.ide.eclipse.core.internal.ProjectHelper;
import org.apache.sling.ide.eclipse.ui.wizards.np.AbstractNewMavenBasedSlingApplicationWizard;
import org.apache.sling.ide.eclipse.ui.wizards.np.ArchetypeParametersWizardPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

public class NewGraniteProjectWizard extends AbstractNewMavenBasedSlingApplicationWizard {

	@Override
	public String doGetWindowTitle() {
		return "Create new Adobe AEM application";
	}
	
	@Override
    public void installArchetypes() {
        // nothing to do
    }

    @Override
	protected ArchetypeParametersWizardPage createArchetypeParametersWizardPage() {
        return new SimplerParametersWizardPage(this);
	}

	@Override
	public boolean acceptsArchetype(Archetype archetype2) {
		return (archetype2.getGroupId().startsWith("com.adobe.granite.archetypes") );
	}
	
	private IProject getParentProject(List<IProject> projects) {
		for (Iterator<IProject> it = projects.iterator(); it.hasNext();) {
			final IProject project = it.next();
			final String packaging = ProjectHelper.getMavenProperty(project, "packaging");
			final String artifactId = ProjectHelper.getMavenProperty(project, "artifactId");
			if (artifactId!=null && artifactId.endsWith("parent") && packaging!=null && packaging.equals("pom")) {
				return project;
			}
		}
		return null;
	}

	private String calculateRelativePath(IProject from, IProject to) {
		IPath fromPath = from.getRawLocation();
		IPath toPath = to.getRawLocation();
		toPath.setDevice(null);
		fromPath.setDevice(null);
		int ssc = fromPath.matchingFirstSegments(toPath);
		fromPath = fromPath.removeFirstSegments(ssc);
		toPath = toPath.removeFirstSegments(ssc);
		StringBuffer relPath = new StringBuffer();
		for(int i=0; i<fromPath.segmentCount(); i++) {
			if (relPath.length()!=0) {
				relPath.append("/");
			}
			relPath.append("..");
		}
		if (relPath.length()!=0) {
			relPath.append("/");
		}
		relPath.append(toPath.toString());
		return relPath.toString();
	}
	
	private void fixParentProject(IProject p, IProject parentProject)
			throws CoreException {
		IFile existingPom = p.getFile("pom.xml");
		Model model = MavenPlugin.getMavenModelManager().readMavenModel(existingPom);
		Model parent = MavenPlugin.getMavenModelManager().readMavenModel(parentProject.getFile("pom.xml"));
		//Parent oldParent = model.getParent();
		Parent newParent = new Parent();
		newParent.setGroupId(parent.getGroupId());
		newParent.setArtifactId(parent.getArtifactId());
		newParent.setRelativePath(calculateRelativePath(p, parentProject));
		newParent.setVersion(parent.getVersion());
		model.setParent(newParent);
		// outright deletion doesn't work on windows as the process has a ref to the file itself
		// so creating a temp '_newpom_.xml'
		final IFile newPom = p.getFile("_newpom_.xml");
		MavenPlugin.getMavenModelManager().createMavenModel(newPom, model);
		// then copying that content over to the pom.xml
		existingPom.setContents(newPom.getContents(), true,  true, new NullProgressMonitor());
		// and deleting the temp pom
		newPom.delete(true,  false, new NullProgressMonitor());
		
	}
	
	@Override
	protected boolean shouldDeploy(IModule module) {
	    if (module.getProject().getName().contains("it.tests")) {
	        return false;
	    }
	    return super.shouldDeploy(module);
	}

	@Override
	protected void configureContentProject(IProject aContentProject,
			List<IProject> projects, IProgressMonitor monitor) 
			throws CoreException{
		IProject parentProject = getParentProject(projects);
		if (parentProject!=null) {
			fixParentProject(aContentProject, parentProject);
		}
		super.configureContentProject(aContentProject, projects, monitor);
	}
	
	@Override
	protected void configureBundleProject(IProject aBundleProject,
			List<IProject> projects, IProgressMonitor monitor)
			throws CoreException {
		IProject parentProject = getParentProject(projects);
		if (parentProject!=null) {
			fixParentProject(aBundleProject, parentProject);
		}
		super.configureBundleProject(aBundleProject, projects, monitor);
	}
}
