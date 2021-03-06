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
import java.util.StringTokenizer;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import com.sun.org.apache.xerces.internal.parsers.DOMParser;

import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;

public class BBWPProperties {
    private static final String NODE_WCP = "wcp";
    private static final String NODE_JAVA = "java";
    private static final String NODE_RAPC = "rapc";
    private static final String NODE_JAR = "jar";
    private static final String NODE_TEMPLATE = "wcp_template";
    private static final String NODE_ADDITIONAL = "additional";
    private static final String NODE_COPYRIGHT = "developer_cn_signature";
    private static final String NODE_AIR_TEMPLATE = "air_template";
    private static final String NODE_TABLET_SDK = "tablet_sdk";
    private static final String NODE_EXTENSION_REPO = "extension_repository";
    private static final String NODE_DEBUG_TOKEN = "debug_token";

    private String _rapc;
    private String _javac;
    private String _templateDir;
    private List< String > _imports;
    private String _additional;
    private String _bbwpProperties;
    private String _sessionHome;
    private String _javaHome;
    private String _copyright;
    private String _airTemplate;
    private String _tabletSDK;
    private String _extensionRepo;
    private String _debugToken;

    public BBWPProperties( String bbwpProperties, String sessionHome ) throws Exception {
        // parse bbwp.properties
        _bbwpProperties = bbwpProperties;
        _sessionHome = sessionHome;
        parsePropertiesFile();

        // quick validation of property file info
        validate();
    }

    public String getRapc() {
        return _rapc;
    }

    public String getJavac() {
        return _javac;
    }

    public String getJavaHome() {
        return _javaHome;
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

    public String getAirTemplate() {
        return _airTemplate;
    }

    public String getTabletSDK() {
        return _tabletSDK;
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

    private void validate() throws Exception {
        // Check template and archive
        if( !( new File( _templateDir ) ).exists() ) {
            throw new ValidationException( "EXCEPTION_TEMPLATES_NOT_FOUND" );
        }

        /*if( !( new File( _airTemplate ) ).exists() ) {
            throw new ValidationException( "EXCEPTION_TEMPLATES_NOT_FOUND" );
        }*/
        if( !SessionManager.getInstance().isPlayBook() ) {
            if( _rapc.length() == 0 ) {
                throw new ValidationException( "EXCEPTION_RAPC_NOT_FOUND" );
            } else {
                if( !_rapc.equals( "rapc.exe" ) && !_rapc.equals( "rapc" ) ) {
                    if( !( new File( _rapc ) ).exists() ) {
                        throw new ValidationException( "EXCEPTION_RAPC_NOT_FOUND" );
                    }
                }
            }

            // Check javac path
            if( _javac == null || _javac.length() == 0 ) {
                // For tooling, they will set _javac to empty if they find javac.exe in "Path" environment variable
                // Rapc doesn't depend on this value to locate javac.exe, either
                // throw new ValidationException("EXCEPTION_JAVAC_NOT_FOUND");
            } else {
                if( !_javac.equals( "javac.exe" ) && !_javac.equals( "javac" ) ) {
                    String javac = _javac;

                    if( javac.startsWith( "\"" ) && javac.endsWith( "\"" ) ) {
                        javac = javac.substring( 1, javac.length() - 1 );
                    }

                    if( !( new File( javac ) ).exists() ) {
                        throw new ValidationException( "EXCEPTION_JAVAC_NOT_FOUND" );
                    }
                }
            }
        }
        // Tablet SDK path should be absolute and existing
        /*if( ( new File( _tabletSDK ).isAbsolute() ) && ( new File( _tabletSDK ) ).exists() ) {
        } else {
            throw new ValidationException( "EXCEPTION_TABLET_SDK_NOT_FOUND" );
        }*/
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
        _rapc = "";
        _javac = "";
        _javaHome = "";
        _copyright = "";
        _templateDir = "";
        _imports = null;
        _additional = "";

        _airTemplate = "";
        _tabletSDK = "";

        _extensionRepo = "ext";
        _debugToken = "";

        if( dom == null )
            return;

        Node root = (Node) dom.getElementsByTagName( NODE_WCP ).item( 0 );
        NodeList list = root.getChildNodes();

        for( int i = 0; i < list.getLength(); i++ ) {
            Node node = list.item( i );
            String nodename = node.getNodeName();
            if( nodename != null ) {
                if( nodename.equals( NODE_RAPC ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _rapc = childlist.item( j ).getNodeValue();
                            if( !_rapc.equals( "rapc.exe" ) && !_rapc.equals( "rapc" ) ) {
                                if( new File( _rapc ).isAbsolute() ) {
                                    _rapc = getAbsolutePath( _rapc );
                                } else {
                                    _rapc = _sessionHome + File.separator + _rapc;
                                }
                            }
                        }
                    }
                } else if( nodename.equals( NODE_JAVA ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _javaHome = childlist.item( j ).getNodeValue();

                            if( !_javaHome.isEmpty() ) {
                                _javac = "\"" + _javaHome + File.separator + "bin" + File.separator + "javac.exe" + "\"";
                            } else {
                                _javac = getAbsolutePath( "javac.exe" );
                            }
                        }
                    }
                } else if( nodename.equals( NODE_TEMPLATE ) ) {
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
                } else if( nodename.equals( NODE_JAR ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _imports = parseClasspath( childlist.item( j ).getNodeValue() );
                        }
                    }
                } else if( nodename.equals( NODE_ADDITIONAL ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _additional = childlist.item( j ).getNodeValue();
                        }
                    }
                } /*else if( nodename.equals( NODE_AIR_TEMPLATE ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _airTemplate = childlist.item( j ).getNodeValue();

                            if( new File( _airTemplate ).isAbsolute() ) {
                                _airTemplate = getAbsolutePath( _airTemplate );
                            } else {
                                _airTemplate = _sessionHome + File.separator + _airTemplate;
                            }
                        }
                    }
                }*/ else if( nodename.equals( NODE_COPYRIGHT ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _copyright = childlist.item( j ).getNodeValue();
                        }
                    }
                }/* else if( nodename.equals( NODE_TABLET_SDK ) ) {
                    NodeList childlist = node.getChildNodes();
                    for( int j = 0; j < childlist.getLength(); j++ ) {
                        if( childlist.item( j ).getNodeType() == Node.TEXT_NODE ) {
                            _tabletSDK = childlist.item( j ).getNodeValue();
                        }
                    }
                }*/ else if( nodename.equals( NODE_EXTENSION_REPO ) ) {
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

    private List< String > parseClasspath( String cp ) throws PackageException {
        List< String > result = new ArrayList< String >();
        StringTokenizer st = new StringTokenizer( cp, ";" );
        while( st.hasMoreTokens() ) {
            String lib = st.nextToken().trim();

            if( new File( lib ).isAbsolute() ) {
                lib = getAbsolutePath( lib );
            } else {
                lib = _sessionHome + File.separator + lib;
            }

            if( new File( lib ).exists() ) {
                result.add( lib );
            } else {
                throw new PackageException( "EXCEPTION_LIBRARY_NOT_FOUND", lib );
            }
        }
        return result;
    }
}