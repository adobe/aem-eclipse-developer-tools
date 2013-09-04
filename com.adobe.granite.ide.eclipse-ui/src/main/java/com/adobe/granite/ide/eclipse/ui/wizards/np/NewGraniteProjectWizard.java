/*************************************************************************
 *
 * ADOBE CONFIDENTIAL
 * __________________
 *
 *  Copyright 2013 Adobe Systems Incorporated
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Adobe Systems Incorporated and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Adobe Systems Incorporated and its
 * suppliers and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 **************************************************************************/
package com.adobe.granite.ide.eclipse.ui.wizards.np;

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.model.Model;
import org.apache.maven.model.Parent;
import org.apache.sling.ide.eclipse.core.EmbeddedArchetypeInstaller;
import org.apache.sling.ide.eclipse.core.ISlingLaunchpadServer;
import org.apache.sling.ide.eclipse.ui.wizards.MavenHelper;
import org.apache.sling.ide.eclipse.ui.wizards.np.AbstractNewSlingApplicationWizard;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.m2e.core.MavenPlugin;
import org.eclipse.wst.server.core.IServer;

import com.adobe.granite.ide.eclipse.ui.Activator;
import com.adobe.granite.ide.eclipse.ui.internal.SharedImages;

public class NewGraniteProjectWizard extends AbstractNewSlingApplicationWizard {

	public NewGraniteProjectWizard() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public ImageDescriptor getLogo() {
		return SharedImages.GRANITE_MEDIUM_LOGO;
	}
	
	@Override
	public String doGetWindowTitle() {
		return "Create new Adobe Granite application";
	}
	
	@Override
	public void installArchetypes() {
	    EmbeddedArchetypeInstaller archetypeInstaller = new EmbeddedArchetypeInstaller(
	    		"com.adobe.granite.archetypes", "sample-project-archetype", "slingclipse-embedded");
	    try {
	    	URL jarUrl = Activator.getDefault().getBundle().getResource(
	    			"target/sample-project-archetype/sample-project-archetype-5-SNAPSHOT.jar");
			archetypeInstaller.addResource("jar", jarUrl);
			URL pomUrl = Activator.getDefault().getBundle().getResource(
					"target/sample-project-archetype/sample-project-archetype-5-SNAPSHOT.pom");
			archetypeInstaller.addResource("pom", pomUrl);
			
			archetypeInstaller.installArchetype();
		} catch (IOException e) {
			// TODO proper logging
			e.printStackTrace();
		}
	    
	}

	@Override
	public boolean acceptsArchetype(Archetype archetype2) {
		//TODO: could further restrict it to only accept the slingclipse-embedded one
		// but then it would be better to remove the archetype selection wizard page entirely
		return (archetype2.getGroupId().startsWith("com.adobe.granite.archetypes") );
	}
	
	private IProject getParentProject(List<IProject> projects) {
		for (Iterator<IProject> it = projects.iterator(); it.hasNext();) {
			IProject project = it.next();
			Model mavenModel = MavenHelper.getMavenModel(project);
			if (mavenModel==null) {
				continue;
			}
			if ("pom".equals(mavenModel.getPackaging()) && "parent".equals(mavenModel.getArtifactId())) {
				return project;
			}
		}
		return null;
	}

	private String calculateRelativePath(IProject from, IProject to) {
		IPath fromPath = from.getRawLocation();
		IPath toPath = to.getRawLocation();
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
		existingPom.delete(true, false, null);
		MavenPlugin.getMavenModelManager().createMavenModel(p.getFile("pom.xml"), model);
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
			existingPom.delete(true, false, null);
			MavenPlugin.getMavenModelManager().createMavenModel(parentProject.getFile("pom.xml"), model);
		}

		super.finishConfiguration(projects, server, monitor);
	}
	
}
