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
package net.rim.tumbler;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.CommandLineException;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.extension.ExtensionMap;
import net.rim.tumbler.file.FileManager;
import net.rim.tumbler.log.LogType;
import net.rim.tumbler.log.Logger;
import net.rim.tumbler.serialize.WidgetConfigSerializer;
import net.rim.tumbler.serialize.WidgetConfig_v1Serializer;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;
import net.rim.tumbler.xml.ConfigXMLParser;
import net.rim.tumbler.xml.XMLParser;

public class WidgetPackager {

    public static final String PROPERTIES_FILE = "bbwp.properties";
    public static final String SIGNATURE_KEY_FILE = "sigtool.csk";

    private static final String AUTOGEN_FILE = "lib/config/user.js";

    private static final int NO_ERROR_RETURN_CODE = 0;
    private static final int PACKAGE_ERROR_RCODE = 1;
    private static final int VALIDATION_ERROR_RCODE = 2;
    private static final int RUNTIME_ERROR_RCODE = 3;
    private static final int UNEXPECTED_ERROR_RCODE = 4;
    private static final int COMMAND_LINE_EXCEPTION = 5;

    /**
     * Enables signing.
     */
    private static final boolean ENABLE_SIGNING = true;

    public static void main( String[] args ) {
        WidgetPackager wp = new WidgetPackager();
        wp.go( args );
    }

    public void go( String[] args ) {
        int returnCode = NO_ERROR_RETURN_CODE;

        try {
            CmdLineHandler cmd = new CmdLineHandler();
            if( !cmd.parse( args ) ) {
                // nothing to package
                System.exit( NO_ERROR_RETURN_CODE );
            }

            // create SessionManager
            SessionManager sessionManager = cmd.createSession();

            // create bbwp.properties
            Logger.logMessage( LogType.INFO, "PROGRESS_SESSION_BBWP_PROPERTIES" );
            String propertiesFile = sessionManager.getBBWPJarFolder() + WidgetPackager.PROPERTIES_FILE;
            BBWPProperties bbwpProperties = new BBWPProperties( propertiesFile, sessionManager.getSessionHome() );

            // validate widget archive
            Logger.logMessage( LogType.INFO, "PROGRESS_VALIDATING_WIDGET_ARCHIVE" );
            WidgetArchive wa = new WidgetArchive( sessionManager.getWidgetArchive() );
            wa.validate();

            // parse/validate config.xml
            Logger.logMessage( LogType.INFO, "PROGRESS_SESSION_CONFIGXML" );
            XMLParser xmlparser = new ConfigXMLParser();
            WidgetConfig config = xmlparser.parseXML( wa ); // raw data, without \

            // create/clean outputs/source
            // Logger.printInfoMessage("Widget packaging starts...");
            FileManager fileManager = new FileManager( bbwpProperties );
            Logger.logMessage( LogType.INFO, "PROGRESS_FILE_POPULATING_SOURCE" );
            fileManager.prepare();

            //
            // Copy the JS extensions.
            //
            Map< String, Vector< String >> entryClassTable = null;
            if( SessionManager.getInstance().isPlayBook() ) {
                entryClassTable = copyExtensions( bbwpProperties, config );
            }

            // create autogen file
            WidgetConfigSerializer wcs = new WidgetConfig_v1Serializer( config, entryClassTable );
            byte[] autogenFile = wcs.serialize();
            fileManager.writeToSource( autogenFile, AUTOGEN_FILE );

//            Logger.logMessage( LogType.INFO, "PROGRESS_COMPILING" );

            // TODO signing needs to be uncommented later
//            if( ENABLE_SIGNING && sessionManager.requireSigning() ) {
//                Logger.logMessage( LogType.INFO, "PROGRESS_SIGNING" );
//                if( SessionManager.getInstance().isPlayBook() ) {
//                    try {
//                        SigningSupport.signBar( bbwpProperties );
//                    } catch( Exception e ) {
//                        File barFile = new File( sessionManager.getOutputFilepath() );
//                        if( barFile.isFile() ) {
//                            barFile.delete();
//                        }
//                        throw e;
//                    }
//                } else {
//                    signCod( sessionManager );
//                }
//                Logger.logMessage( LogType.INFO, "PROGRESS_SIGNING_COMPLETE" );
//            }

            // clean/prep output folders
            fileManager.cleanOutput();            

            Logger.logMessage( LogType.INFO, "PROGRESS_GEN_OUTPUT" );
            
            // create output zip file
            fileManager.createOutputZip();

            // clean source (if necessary)
            if( !sessionManager.requireSource() ) {
                fileManager.cleanSource();
            }

            Logger.logMessage( LogType.INFO, "PROGRESS_COMPLETE" );
        } catch( CommandLineException cle ) {
            Logger.logMessage( LogType.ERROR, cle.getMessage(), cle.getInfo() );
            Logger.logMessage( LogType.NONE, CmdLineHandler.isPlayBook() ? "BBWP_PLAYBOOK_USAGE" : "BBWP_USAGE", getVersion() );
            returnCode = COMMAND_LINE_EXCEPTION;
        } catch( PackageException pe ) {
            Logger.logMessage( LogType.ERROR, pe.getMessage(), pe.getInfo() );
            returnCode = PACKAGE_ERROR_RCODE;
        } catch( ValidationException ve ) {
            Logger.logMessage( LogType.ERROR, ve.getMessage(), ve.getInfo() );
            returnCode = VALIDATION_ERROR_RCODE;
        } catch( RuntimeException re ) {
            Logger.logMessage( LogType.FATAL, re );
            returnCode = RUNTIME_ERROR_RCODE;
        } catch( Exception e ) {
            System.out.println( e );
            returnCode = UNEXPECTED_ERROR_RCODE;
        }

        System.exit( returnCode );
    }

    public static Object[] getVersion() {
        return new Object[] { new WidgetPackager().getClass().getPackage().getImplementationVersion() };
    }

    /**
     * Copies the correct set of extension source files from the extension repository into the project area so that they can be
     * compiled along with the framework, and returns a hashtable populated with javascript file names for use downstream. Each
     * key in the hashtable is the entry class name, and the value contains the relative pathnames of the corresponding javascript
     * files.
     * 
     * @param bbwpProperties
     *            the current widget properties.
     * @param config
     *            the current widget configuration.
     * 
     * @return a newly-created, populated hashtable as described above.
     */
    private static Map< String, Vector< String >> copyExtensions( BBWPProperties bbwpProperties, WidgetConfig config )
            throws IOException, PackageException {
        Map< String, Vector< String >> result = new LinkedHashMap< String, Vector< String >>();

        //
        // We need to copy the correct set of extension source files from the
        // extension repository into the project area so that they can be
        // compiled along with the framework.
        // The correct set of extension source files is determined using
        // the features identified in config.xml and the supported feature
        // set of each library.xml.
        //
        Hashtable< WidgetAccess, Vector< WidgetFeature >> accessTable = config.getAccessTable();
        // if the access table is empty, don't even bother since there's no features to search for
        if( accessTable.size() > 0 ) {
            //
            // Go ahead and traverse the extension repository, looking for
            // library.xml files to parse. This is independent of config.xml, so far.
            //

            ExtensionMap extensionMap = new ExtensionMap( "BBX", "default", bbwpProperties.getExtensionRepo( SessionManager
                    .getInstance().getSessionHome() ) ); // location of the extension repository

            //
            // Extract the set of feature IDs from the access table.
            // We flatten the structure since we don't care about the
            // access node or whether it applies to local access; all
            // we want are the unique feature IDs.
            //
            Set< String > featureIDs = new HashSet< String >();
            for( Vector< WidgetFeature > accessTableValue : accessTable.values() ) {
                for( WidgetFeature widgetFeature : accessTableValue ) {
                    featureIDs.add( widgetFeature.getID() );
                }
            }

            //
            // For each feature ID in the set, we now perform the copying
            // based on the extension map that we constructed from the
            // library.xml files. The extension map will make sure we don't
            // copy the same set of files twice.
            //

            for( String featureID : featureIDs ) {
                //
                // The ExtensionMap is responsible for avoiding duplication.
                // In particular, extension sets are marked as such after
                // they have been copied to avoid duplication.
                //
                // This method is also responsible for distinguishing between
                // ActionScript and JavaScript source files. For this purpose
                // file name extensions may be used.
                //
                extensionMap.copyRequiredFiles( SessionManager.getInstance().getSourceFolder(), // destination for extensions
                        featureID );
            }

            //
            // Fill-in the javascript entry-class table. This is used elsewhere.
            //
            extensionMap.getCopiedFiles( ".js", result, "ext" + File.separator );
        }
        return result;
    }
}
