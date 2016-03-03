/*
 *  Copyright 2016 Adobe Systems Incorporated
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

import org.apache.sling.ide.eclipse.core.DefaultSlingLaunchpadConfiguration;

public class DefaultCQLaunchpadConfiguration extends DefaultSlingLaunchpadConfiguration {

    public static final DefaultCQLaunchpadConfiguration INSTANCE = new DefaultCQLaunchpadConfiguration();

    @Override
    public int getPort() {
        return 4502;
    }
}
