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

import java.net.URI;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.sling.ide.eclipse.core.ISlingLaunchpadServer;
import org.apache.sling.ide.eclipse.core.internal.ProjectHelper;
import org.apache.sling.ide.eclipse.m2e.EmbeddedArchetypeInstaller;
import org.apache.sling.ide.eclipse.ui.wizards.np.AbstractNewMavenBasedSlingApplicationWizard;
import org.apache.sling.ide.eclipse.ui.wizards.np.ArchetypeParametersWizardPage;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.m2e.core.project.MavenUpdateRequest;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

import com.adobe.granite.ide.eclipse.ui.Activator;

public class NewGraniteProjectWizard extends AbstractNewMavenBasedSlingApplicationWizard {

	@Override
	public String doGetWindowTitle() {
		return "Create new Adobe AEM application";
	}
	
	@Override
	public void installArchetypes() {
	    EmbeddedArchetypeInstaller archetypeInstaller = new EmbeddedArchetypeInstaller(
	    		"com.adobe.granite.archetypes", "sample-project-archetype", "7");
	    try {
	    	URL jarUrl = Activator.getDefault().getBundle().getResource(
	    			"target/sample-project-archetype/sample-project-archetype-7.jar");
			archetypeInstaller.addResource("jar", jarUrl);
			URL pomUrl = Activator.getDefault().getBundle().getResource(
					"target/sample-project-archetype/sample-project-archetype-7.pom");
			archetypeInstaller.addResource("pom", pomUrl);
			
			archetypeInstaller.installArchetype();
		} catch (Exception e) {
			// TODO proper logging
			e.printStackTrace();
		}
	    
	}
	
	@Override
	protected ArchetypeParametersWizardPage createArchetypeParametersWizardPage() {
        return new SimplerParametersWizardPage(this);
	}

	@Override
	public boolean acceptsArchetype(Archetype archetype2) {
		//TODO: could further restrict it to only accept the slingclipse-embedded one
		// but then it would be better to remove the archetype selection wizard page entirely
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

	protected void updateProjectConfigurations(List<IProject> projects, final boolean forceDependencyUpdate, IProgressMonitor monitor) throws CoreException {
        for (Iterator<IProject> it = projects.iterator(); it.hasNext();) {
            final IProject project = it.next();
            monitor.beginTask("Refreshing "+project.getName(), 2);
            project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
            monitor.beginTask("Updating "+project.getName(), 2);
            MavenPlugin.getProjectConfigurationManager().updateProjectConfiguration(new MavenUpdateRequest(project, /*mavenConfiguration.isOffline()*/false, forceDependencyUpdate), monitor);
            monitor.beginTask("Cleaning "+project.getName(), 2);
            project.build(IncrementalProjectBuilder.CLEAN_BUILD, monitor);
        }
    }
	
	@Override
	protected void finishConfiguration(List<IProject> projects, IServer server,
			IProgressMonitor monitor) throws CoreException {
		IProject parentProject = getParentProject(projects);
		if (parentProject!=null) {
			// set granite.host and granite.port
			IFile existingPom = parentProject.getFile("pom.xml");
			Model model = MavenPlugin.getMavenModelManager().readMavenModel(existingPom);
			Properties props = model.getProperties();
			props.put("granite.host", server.getHost());
			props.put("granite.port", String.valueOf(server.getAttribute(ISlingLaunchpadServer.PROP_PORT, 4502)));
			// cannot delete existingPom directly, as that might be locked (eg on windows)
			final IFile tmpfile = parentProject.getFile("_newpom_.xml");
			MavenPlugin.getMavenModelManager().createMavenModel(tmpfile, model);
			// then copying that content over to the pom.xml
			existingPom.setContents(tmpfile.getContents(), true,  true, new NullProgressMonitor());
			// and deleting the temp pom
			tmpfile.delete(true,  false, new NullProgressMonitor());
		}

		updateProjectConfigurations(projects, true, monitor);

		super.finishConfiguration(projects, server, monitor);
	}

	@Override
	public boolean performFinish() {
	    //TODO: Disabling this check for now - remove completely at later stage if 
	    // problems with initial-newproject-on-windows is fixed
//	    if (!assertPublicRepoConfigured()) {
//	        return false;
//	    }
	    return super.performFinish();
	}

    private boolean assertPublicRepoConfigured() {
        try {
            List<ArtifactRepository> repos = MavenPlugin.getMaven().getPluginArtifactRepositories();
            for (Iterator<ArtifactRepository> it = repos.iterator(); it.hasNext();) {
                ArtifactRepository artifactRepository = it.next();
                if (isRepoAdobeCom(artifactRepository)) {
                    return true;
                }
            }
            if (!MessageDialog.openQuestion(getShell(), "Could not find repo.adobe.com in settings.xml",
                    "Could not find repo.adobe.com or *.adobe.com as a configured repository. Please note that you need direct or indirect access to an adobe.com repository.\n\n"+
                            "For details on how to setup repo.adobe.com please visit http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html\n\n"+
                            "Would you still like to continue?")) {
                reportError(new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 
                        "Could not find repo.adobe.com as a configured repository (double-check settings.xml as per http://helpx.adobe.com/experience-manager/kb/SetUpTheAdobeMavenRepository.html)")));
                return false;
            } else {
                return true;
            }
        } catch (CoreException e) {
            reportError(e);
            return false;
        }
        
    }

    private boolean isRepoAdobeCom(ArtifactRepository artifactRepository) {
        if (artifactRepository==null) {
            return false;
        }
        try{
            URI uri = new URI(artifactRepository.getUrl());
            if (uri.getHost().equals("repo.adobe.com")) {
                return true;
            }
            // GRANITE-6406 
            //  accepting any *.adobe.com as a valid repo - besides 
            //  explicitly accepting repo.adobe.com
            //  to remain flexible..
            if (uri.getHost().endsWith(".adobe.com")) {
                return true;
            }
            return false;
        } catch(Exception e) {
            return false;
        }
    }
	
}
