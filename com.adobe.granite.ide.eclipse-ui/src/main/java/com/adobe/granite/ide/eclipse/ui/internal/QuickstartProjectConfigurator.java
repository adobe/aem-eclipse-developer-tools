package com.adobe.granite.ide.eclipse.ui.internal;

import org.apache.sling.ide.eclipse.core.ConfigurationHelper;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.m2e.core.project.configurator.AbstractProjectConfigurator;
import org.eclipse.m2e.core.project.configurator.ProjectConfigurationRequest;

public class QuickstartProjectConfigurator extends AbstractProjectConfigurator {

	@Override
	public void configure(ProjectConfigurationRequest request, IProgressMonitor monitor) throws CoreException {
		ConfigurationHelper.convertToLaunchpadProject(request.getProject(), Path.fromPortableString("src/main/provisioning"));
	}
}
