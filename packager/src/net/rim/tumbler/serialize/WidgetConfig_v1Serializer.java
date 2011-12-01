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
package net.rim.tumbler.serialize;

import java.util.Vector;

import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.json4j.JSONArray;
import net.rim.tumbler.json4j.JSONException;
import net.rim.tumbler.json4j.JSONObject;

/**
 * Generate JSON that contains information parsed from config.xml
 */
public class WidgetConfig_v1Serializer implements WidgetConfigSerializer {

    private StringBuffer _buffer;
    private JSONObject _configValues;
    private WidgetConfig _widgetConfig;
    private static final String[] KEYS_PROP_STRING = { 
        "version",
        "id",
        "name",
        "description",
        "content",
        "configXML",
        "backButtonBehaviour",
        "contentType",
        "contentCharset",
        "license",
        "licenseURL",
        "author",
        "authorEmail",
        "authorURL",
        "copyright",
        "loadingScreenColor",
        "backgroundImage",
        "foregroundImage",
        "backgroundSource",
        "foregroundSource"
    };
    private static final String[] KEYS_PROP_BOOLEAN = {
        "hasMultiAccess",
        "onFirstLaunch",
        "onLocalPageLoad",
        "onRemotePageLoad",
        "allowInvokeParams",
        "runOnStartUp",
        "debugEnabled"        
    };

    public WidgetConfig_v1Serializer( WidgetConfig widgetConfig ) {
        _buffer = new StringBuffer( "module.exports = " );
        _configValues = new JSONObject();
        _widgetConfig = widgetConfig;
    }
    
    private void serializeStringProperties() throws JSONException {
        String[] propValues = {
                _widgetConfig.getVersion(),
                _widgetConfig.getID(),
                _widgetConfig.getName(),
                _widgetConfig.getDescription(),
                _widgetConfig.getContent(),
                _widgetConfig.getConfigXML(),
                _widgetConfig.getBackButtonBehaviour(),
                _widgetConfig.getContentType(),
                _widgetConfig.getContentCharSet(),
                _widgetConfig.getLicense(),
                _widgetConfig.getLicenseURL(),
                _widgetConfig.getAuthor(),
                _widgetConfig.getAuthorEmail(),
                _widgetConfig.getAuthorURL(),
                _widgetConfig.getCopyright(),
                _widgetConfig.getLoadingScreenColour(),
                _widgetConfig.getBackgroundImage(),
                _widgetConfig.getForegroundImage(),
                _widgetConfig.getBackgroundSource(),
                _widgetConfig.getForegroundSource()
        };

        for( int i = 0; i < KEYS_PROP_STRING.length; i++ ) {
            if( propValues[ i ] != null ) {
                _configValues.put( KEYS_PROP_STRING[ i ], propValues[ i ] );
            }
        }
    }
    
    private void serializeBooleanProperties() throws JSONException {
        boolean[] propValues = {
                _widgetConfig.allowMultiAccess(),
                _widgetConfig.getFirstPageLoad(),
                _widgetConfig.getLocalPageLoad(),
                _widgetConfig.getRemotePageLoad(),
                _widgetConfig.allowInvokeParams(),
                _widgetConfig.isStartupEnabled(),
                _widgetConfig.isDebugEnabled()
        };

        for( int i = 0; i < KEYS_PROP_BOOLEAN.length; i++ ) {
            if( propValues[ i ] ) {
                _configValues.put( KEYS_PROP_BOOLEAN[ i ], propValues[ i ] );
            }
        }
    }

    private void serializeWhitelist() throws JSONException {
        // add access/features
        if( _widgetConfig.getAccessTable() != null && _widgetConfig.getAccessTable().size() > 0 ) {
            JSONArray accessList = new JSONArray();

            for( WidgetAccess key : _widgetConfig.getAccessTable().keySet() ) {
                JSONObject access = new JSONObject();
                JSONArray featureList = new JSONArray();

                if( key.getURI().toString().equals( "WidgetConfig.WIDGET_LOCAL_DOMAIN" ) ) {
                    access.put( "uri", "WIDGET_LOCAL" );
                } else {
                    access.put( "uri", key.getURI().toString() );
                }

                access.put( "allowSubDomain", key.allowSubDomain() );
                access.put( "features", featureList );

                Vector< ? > wfList = (Vector< ? >) _widgetConfig.getAccessTable().get( key );

                if( wfList.size() > 0 ) {
                    for( int j = 0; j < wfList.size(); j++ ) {
                        WidgetFeature wf = (WidgetFeature) wfList.get( j );
                        JSONObject feature = new JSONObject();

                        feature.put( "id", wf.getID() );
                        feature.put( "required", wf.isRequired() );
                        feature.put( "version", wf.getVersion() );

                        featureList.add( feature );
                    }
                }

                accessList.add( access );
            }

            _configValues.put( "accessList", accessList );
        }        
    }
    
    public byte[] serialize() throws ValidationException {
        try {
            serializeStringProperties();            
            serializeBooleanProperties();            
            serializeWhitelist();
            
            // add icons
            if( _widgetConfig.getIconSrc().size() > 0 ) {
                _configValues.put( "icon", _widgetConfig.getIconSrc().firstElement() );
                if( _widgetConfig.getHoverIconSrc().size() > 0 ) {
                    _configValues.put( "iconHover", _widgetConfig.getHoverIconSrc().firstElement() );
                }
            }

            // add custom headers
            if( _widgetConfig.getCustomHeaders().size() > 0 ) {
                JSONObject customHeaders = new JSONObject();
                for( String key : _widgetConfig.getCustomHeaders().keySet() ) {
                    customHeaders.put( key, _widgetConfig.getCustomHeaders().get( key ) );
                }
                _configValues.put( "customHeaders", customHeaders );
            }

            // set navigation mode
            if( _widgetConfig.getNavigationMode() ) {
                _configValues.put( "navigationMode", "focus" );
            }

            //Print out JSON data in a nice formatted way
            _buffer.append( _configValues.toString(4) );
            _buffer.append( ";\n" );
        } catch( JSONException e ) {
            throw new RuntimeException( e );
        }

        return _buffer.toString().getBytes();
    }
    
    public JSONObject getConfigJSONObject() {
        if( _configValues != null ) {
            return (JSONObject) _configValues.clone();
        }

        return null;
    }

    public static String[] getStringPropKeys() {
        return KEYS_PROP_STRING;
    }
    
    public static String[] getBooleanPropKeys() {
        return KEYS_PROP_BOOLEAN;
    }
}
