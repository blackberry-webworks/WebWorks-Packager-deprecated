/*
 * Copyright 2010-2011 Research In Motion Limited.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.rim.tumbler.config;

import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import net.rim.tumbler.exception.ValidationException;

public interface IWidgetConfig {
    public Boolean allowInvokeParams();

    public boolean allowMultiAccess();

    public Hashtable< WidgetAccess, Vector< WidgetFeature >> getAccessTable();

    public Integer getAggressiveCacheAge();

    public String getAppHomeScreenCategory();

    public String getAuthor();

    public String getAuthorEmail();

    public String getAuthorURL();

    public String getAutoOrientation();

    public String getBackButtonBehaviour();

    public String getBackgroundImage();

    public String getBackgroundSource();

    public String getConfigXML();

    public String getContent();

    public String getContentCharSet();

    public String getContentType();

    public String getCopyright();

    public Map< String, String > getCustomHeaders();

    public String getDescription();

    public Vector< String > getExtensionClasses();

    public boolean getFirstPageLoad();

    public String getForegroundImage();

    public String getForegroundSource();

    public Vector< String > getHoverIconSrc();

    public Vector< String > getIconSrc() throws ValidationException;

    public String getID();

    public String getLicense();

    public String getLicenseURL();

    public String getLoadingScreenColour();

    public boolean getLocalPageLoad();

    public Integer getMaxCacheItemSize();

    public Integer getMaxCacheSize();

    public String getName();

    public boolean getNavigationMode();

    public int getNumVersionParts();

    public String getOrientation();

    public String[] getPermissions();

    public boolean getRemotePageLoad();

    public String getTransitionDirection();

    public int getTransitionDuration();

    public String getTransitionType();

    public String[] getTransportOrder();

    public int getTransportTimeout();

    public String getVersion();

    public String getVersionParts( int beginIndex );

    public String getVersionParts( int beginIndex, int endIndex );
    
    public Boolean isCacheEnabled();
    
    public boolean isDebugEnabled();
    
    public Boolean isStartupEnabled();
}
