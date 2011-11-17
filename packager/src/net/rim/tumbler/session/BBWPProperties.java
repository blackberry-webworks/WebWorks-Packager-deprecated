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
package net.rim.tumbler.session;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import net.rim.tumbler.exception.ValidationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

public class BBWPProperties {
    private static final String NODE_WCP = "wcp";
    private static final String NODE_TEMPLATE = "wcp_template";
    private static final String NODE_ADDITIONAL = "additional";
    private static final String NODE_COPYRIGHT = "developer_cn_signature";
    private static final String NODE_EXTENSION_REPO = "extension_repository";
    private static final String NODE_DEBUG_TOKEN = "debug_token";
    private static final String NODE_DEPENDENCIES = "dependencies";

    private String _templateDir;
    private List< String > _imports;
    private String _additional;
    private String _bbwpProperties;
    private String _sessionHome;
    private String _copyright;
    private String _extensionRepo;
    private String _debugToken;
    private String _dependenciesDir;

    public BBWPProperties( String bbwpProperties, String sessionHome ) throws Exception {
        // parse bbwp.properties
        _bbwpProperties = bbwpProperties;
        _sessionHome = sessionHome;
        parsePropertiesFile();

        // quick validation of property file info
        validate();
    }

    public String getCopyright() {
        return _copyright;
    }

    public String getTemplateDir() {
        return _templateDir;
    }

    public List< String > getImports() {
        return _imports;
    }

    public String getAdditional() {
        return _additional;
    }

    public String getExtensionRepo( String base ) {
        File dir = new File( _extensionRepo );

        return dir.isAbsolute() ? _extensionRepo : ( base + File.separator + _extensionRepo );
    }

    /**
     * Returns the pathname of the debug token file, as a string, as specified in the properties file. If the
     * <code>&lt;debug_token&gt;</code> element is missing or empty, this function returns an empty string.
     * 
     * @return the pathname of the debug token file, or an empty string if the <code>&lt;debug_token&gt;</code> element is missing
     *         or empty.
     */
    public String getDebugToken() {
        return _debugToken;
    }
    
    public String getDependenciesDir() {
        return _dependenciesDir;
    }

    private void validate() throws Exception {
        // Check template and archive
        if( !( new File( _templateDir ) ).exists() ) {
            throw new ValidationException( "EXCEPTION_TEMPLATES_NOT_FOUND" );
        }
    }

    private void parsePropertiesFile() throws Exception {
        FileInputStream fisProperties = null;
        Document docProperties = null;

        fisProperties = new FileInputStream( _bbwpProperties );
        byte[] data = new byte[ (int) ( new File( _bbwpProperties ) ).length() ];
        fisProperties.read( data );
        docProperties = createPropertiesDocument( data );
        getProperties( docProperties );

        return;
    }

    private Document createPropertiesDocument( byte[] input ) throws Exception {
        ByteArrayInputStream ba = new ByteArrayInputStream( input );
        DOMParser dp = new DOMParser();

        dp.setFeature( "http://xml.org/sax/features/validation", false );
        dp.setFeature( "http://xml.org/sax/features/external-parameter-entities", false );
        dp.setFeature( "http://xml.org/sax/features/namespaces", false );
        dp.setFeature( "http://apache.org/xml/features/nonvalidating/load-external-dtd", false );

        dp.parse( new InputSource( ba ) );
        return dp.getDocument();
    }

    private void getProperties( Document dom ) throws Exception {
        _copyright = "";
        _templateDir = "";
        _imports = null;
        _additional = "";

        _extensionRepo = "ext";
        _debugToken = "";
        _dependenciesDir = "";

        if( dom == null )
            return;

        Node root = (Node) dom.getElementsByTagName( NODE_WCP ).item( 0 );
        NodeList list = root.getChildNodes();

        for( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );
            String nodename = node.getNodeName();
            if( nodename != null ) {
                if( nodename.equals( NODE_TEMPLATE ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _templateDir = childlist.item( j ).getNodeValue();

                            if( new File( _templateDir ).isAbsolute() ) {
                                _templateDir = getAbsolutePath( _templateDir );
                            } else {
                                _templateDir = _sessionHome + File.separator + _templateDir;
                            }
                        }
                    }
                } else if( nodename.equals( NODE_ADDITIONAL ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _additional = childlist.item( j ).getNodeValue();
                        }
                    }
                } else if( nodename.equals( NODE_COPYRIGHT ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _copyright = childlist.item( j ).getNodeValue();
                        }
                    }
                } else if( nodename.equals( NODE_EXTENSION_REPO ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _extensionRepo = childlist.item( j ).getNodeValue();
                        }
                    }
                } else if( nodename.equals( NODE_DEBUG_TOKEN ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _debugToken = childlist.item( j ).getNodeValue();
                        }
                    }
                } else if( nodename.equals( NODE_DEPENDENCIES ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _dependenciesDir = childlist.item( j ).getNodeValue();
                        }
                    }
                }
            }
        }
    }

    private String getAbsolutePath( String filePath ) {
        try {
            return ( new File( filePath ) ).getCanonicalFile().getAbsolutePath();
        } catch( Exception e ) {
            return ( new File( filePath ) ).getAbsolutePath();
        }
    }
}