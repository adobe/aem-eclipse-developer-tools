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
package com.adobe.granite.ide.eclipse.ui.internal;

import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.osgi.framework.Bundle;

import com.adobe.granite.ide.eclipse.ui.Activator;

/**
 * The <tt>SharedImages</tt> class contains references to images
 * 
 */
public final class SharedImages {
    
    public static final ImageDescriptor AEM_MEDIUM_LOGO = createImageDescriptor(Activator.getDefault().getBundle(),
            Path.fromPortableString("icons/mc_experiencemanager_transp_medium.png"));
    public static final ImageDescriptor NT_UNSTRUCTURED = ImageDescriptor.createFromFile(SharedImages.class, "unstructured.png");
    public static final ImageDescriptor AEM_ICON = createImageDescriptor(Activator.getDefault().getBundle(),
            Path.fromPortableString("icons/mc_experiencemanager_transp.png"));

    public static ImageDescriptor createImageDescriptor(Bundle bundle, IPath path) {
        URL url = FileLocator.find(bundle, path, null);
        if (url != null) {
            return ImageDescriptor.createFromURL(url);
        }
        return null;
    }

    private SharedImages() {
    }

}
