/*
 * Copyright 2011 Research In Motion Limited.
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
package net.rim.tumbler.util;

import java.util.Hashtable;
import java.util.Vector;

import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;

public class WidgetConfigMocker {
    private static WidgetConfigMocker _instance;
    private static WidgetConfig _config;
    
    private WidgetConfigMocker() throws Exception {
        prepareConfig();
    }
    
    public static WidgetConfigMocker getInstance() throws Exception {
        if (_instance == null) {
            _instance = new WidgetConfigMocker();
        }
        
        return _instance;
    }
    
    private static Hashtable< WidgetAccess, Vector< WidgetFeature >> getTestAccessTable() throws Exception {
        Hashtable< WidgetAccess, Vector< WidgetFeature >> accessTable = new Hashtable< WidgetAccess, Vector< WidgetFeature >>();
        WidgetAccess localAccess = new WidgetAccess( "WidgetConfig.WIDGET_LOCAL_DOMAIN", true );
        Vector< WidgetFeature > localFeatures = new Vector< WidgetFeature >();
        localFeatures.add( new WidgetFeature( "blackberry.system", true, "1.0.0", null ) );
        accessTable.put( localAccess, localFeatures );
        return accessTable;
    }

    private static void prepareConfig() throws Exception {
        _config = new WidgetConfig();
        _config.setName( "Test" );
        _config.setDescription( "This is a description." );
        _config.setAuthor( "John Doe" );
        _config.setVersion( "1.0.0" );
        _config.addIcon( "icon.png" );
        _config.setAccessTable( getTestAccessTable() );
    }
    
    public WidgetConfig getConfig() {
        return _config;
    }
}
