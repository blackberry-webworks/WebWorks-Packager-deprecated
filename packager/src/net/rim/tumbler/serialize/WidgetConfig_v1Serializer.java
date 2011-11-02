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

import java.util.Map;
import java.util.Vector;

import net.rim.tumbler.config.IWidgetConfig;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.json4j.JSONArray;
import net.rim.tumbler.json4j.JSONException;
import net.rim.tumbler.json4j.JSONObject;
import net.rim.tumbler.json4j.JSONString;

/**
 * Generate JSON that contains information parsed from config.xml
 */
public class WidgetConfig_v1Serializer implements WidgetConfigSerializer {

    private StringBuffer _buffer;
    private JSONObject _configValues;
    private IWidgetConfig _widgetConfig;

    public WidgetConfig_v1Serializer( IWidgetConfig widgetConfig, Map< String, Vector< String >> entryClassTable ) {
        _buffer = new StringBuffer();
        _buffer.append( "var _self;\n\n" );
        _buffer.append( "_self = " );

        _configValues = new JSONObject();

        try {
            if( widgetConfig.getVersion() != null ) {
                _configValues.put( "version", widgetConfig.getVersion() );
            }
            
            if( widgetConfig.getID() != null ) {
                _configValues.put( "id", widgetConfig.getID() );
            }
            
            if( widgetConfig.getName() != null ) {
                _configValues.put( "name", widgetConfig.getName() );
            }
            
            if( widgetConfig.getDescription() != null ) {
                _configValues.put( "description", widgetConfig.getDescription() );
            }
            
            if( widgetConfig.getContent() != null ) {
                _configValues.put( "content", widgetConfig.getContent() );
            }
            
            if( widgetConfig.getConfigXML() != null ) {
                _configValues.put( "configXML", widgetConfig.getConfigXML() );
            }
            
            if( widgetConfig.getBackButtonBehaviour() != null ) {
                _configValues.put( "backButtonBehaviour", widgetConfig.getBackButtonBehaviour() );
            }
            
            if( widgetConfig.getContentType() != null ) {
                _configValues.put( "contentType", widgetConfig.getContentType() );
            }
            
            if( widgetConfig.getContentCharSet() != null ) {
                _configValues.put( "contentCharset", widgetConfig.getContentCharSet() );
            }
            
            if( widgetConfig.getLicense() != null ) {
                _configValues.put( "license", widgetConfig.getLicense() );
            }
            
            if( widgetConfig.getLicenseURL() != null ) {
                _configValues.put( "licenseURL", widgetConfig.getLicenseURL() );
            }
            
            if( widgetConfig.getAuthor() != null ) {
                _configValues.put( "author", widgetConfig.getAuthor() );
            }
            
            if( widgetConfig.getCopyright() != null ) {
                _configValues.put( "copyright", widgetConfig.getCopyright() );
            }
            
            if( widgetConfig.getAuthorEmail() != null ) {
                _configValues.put( "authorEmail", widgetConfig.getAuthorEmail() );
            }
            
            if( widgetConfig.getLoadingScreenColour() != null ) {
                _configValues.put( "loadingScreenColor", widgetConfig.getLoadingScreenColour() );
            }
            
            if( widgetConfig.getBackgroundImage() != null ) {
                _configValues.put( "backgroundImage", widgetConfig.getBackgroundImage() );
            }
            
            if( widgetConfig.getForegroundImage() != null ) {
                _configValues.put( "foregroundImage", widgetConfig.getForegroundImage() );
            }
            
            if( widgetConfig.getAuthorURL() != null ) {
                _configValues.put( "authorURL", widgetConfig.getAuthorURL() );
            }
            
            if( widgetConfig.getBackgroundSource() != null ) {
                _configValues.put( "backgroundSource", widgetConfig.getBackgroundSource() );
            }
            
            if( widgetConfig.getForegroundSource() != null ) {
                _configValues.put( "foregroundSource", widgetConfig.getForegroundSource() );
            }
        } catch( JSONException e ) {
            throw new RuntimeException( e );
        }

        _widgetConfig = widgetConfig;
    }

    public byte[] serialize() throws ValidationException {
        try {
            // * present
            if( _widgetConfig.allowMultiAccess() ) {
                _configValues.put( "hasMultiAccess", _widgetConfig.allowMultiAccess() );
            }

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

            // add LoadingScreen configuration
            _configValues.put( "onFirstLaunch", _widgetConfig.getFirstPageLoad() );
            _configValues.put( "onRemotePageLoad", _widgetConfig.getRemotePageLoad() );
            _configValues.put( "onLocalPageLoad", _widgetConfig.getLocalPageLoad() );

            // add TransitionEffect configuration
            if( _widgetConfig.getTransitionType() != null ) {
                _configValues.put( "transitionType", new JSExpression( _widgetConfig.getTransitionType() ) );

                if( _widgetConfig.getTransitionDuration() >= 0 ) {
                    _configValues.put( "transitionDuration", _widgetConfig.getTransitionDuration() );
                }

                if( _widgetConfig.getTransitionDirection() != null ) {
                    _configValues.put( "transitionDirection", new JSExpression( _widgetConfig.getTransitionDirection() ) );
                }
            }

            // add cache options
            if( _widgetConfig.isCacheEnabled() != null ) {
                _configValues.put( "disableAllCache", true );
            }

            if( _widgetConfig.getAggressiveCacheAge() != null ) {
                // Enable aggressive caching if applicable
                _configValues.put( "aggressiveCacheAge", _widgetConfig.getAggressiveCacheAge() );
            }

            if( _widgetConfig.getMaxCacheSize() != null ) {
                _configValues.put( "maxCacheSizeTotal", _widgetConfig.getMaxCacheSize() );
            }

            if( _widgetConfig.getMaxCacheItemSize() != null ) {
                _configValues.put( "maxCacheSizeItem", _widgetConfig.getMaxCacheItemSize() );
            }

            // Debug issue fix ?
            if( _widgetConfig.isDebugEnabled() ) {
                _configValues.put( "debugEnabled", _widgetConfig.isDebugEnabled() );
            }

            // Auto-Startup options
            if( _widgetConfig.allowInvokeParams() ) {
                _configValues.put( "allowInvokeParams", _widgetConfig.allowInvokeParams() );
            }

            if( _widgetConfig.isStartupEnabled() ) {
                _configValues.put( "runOnStartUp", _widgetConfig.isStartupEnabled() );
            }

            // add access/features
            if( _widgetConfig.getAccessTable().size() > 0 ) {
                JSONArray accessList = new JSONArray();

                for( WidgetAccess key : _widgetConfig.getAccessTable().keySet() ) {
                    JSONObject access = new JSONObject();
                    JSONArray featureList = new JSONArray();

                    if( key.getURI().toString().equals( "WidgetConfig.WIDGET_LOCAL_DOMAIN" ) ) {
                        access.put( "uri", new JSExpression( "ConfigConstants.WIDGET_LOCAL_DOMAIN" ) );
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

            // TODO don't know what extensions will look like, just put in an
            // empty array for now
            _configValues.put( "widgetExtensions", new JSONArray() );

            _buffer.append( _configValues.toString() );
            _buffer.append( ";\n\n" );
            _buffer.append( "module.exports = _self;" );
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
    
    /**
     * Output JS expression (e.g. ConfigConstants.WIDGET_LOCAL_DOMAIN) without quotes 
     */
    private static class JSExpression implements JSONString {
        private String _str;

        public JSExpression( String str ) {
            _str = str;
        }

        public String toString() {
            return _str;
        }

        @Override
        public String toJSONString() {
            return toString();
        }
    }
}
