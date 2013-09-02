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
package com.adobe.granite.ide.eclipse.ui.internal;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * The <tt>SharedImages</tt> class contains references to images
 * 
 */
public final class SharedImages {
    
    public static final ImageDescriptor GRANITE_MEDIUM_LOGO = ImageDescriptor.createFromFile(SharedImages.class, "granitemedium.png");
    public static final ImageDescriptor NT_UNSTRUCTURED = ImageDescriptor.createFromFile(SharedImages.class, "unstructured.png");

    private SharedImages() {
    }

}
