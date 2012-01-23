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

import net.rim.tumbler.bar.NativePackager;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.exception.CommandLineException;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
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
    public static final String WW_EXECUTABLE_FILE = "wwe";

    private static final String AUTOGEN_FILE = "chrome/lib/config/user.js";
    private static final String PATHS_FILE = "chrome/paths.js";

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
            FileManager fileManager = new FileManager( config, bbwpProperties );
            Logger.logMessage( LogType.INFO, "PROGRESS_FILE_POPULATING_SOURCE" );
            fileManager.prepare();

            // create autogen file
            WidgetConfigSerializer wcs = new WidgetConfig_v1Serializer( config );
            byte[] autogenFile = wcs.serialize();
            fileManager.writeToSource( autogenFile, AUTOGEN_FILE );
            
            fileManager.writeToSource( fileManager.generatePathsJSFile(), PATHS_FILE );

            Logger.logMessage( LogType.INFO, "PROGRESS_COMPILING" );

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

            Logger.logMessage( LogType.INFO, "PROGRESS_GEN_OUTPUT" );

            // create output bar file
            Logger.logMessage( LogType.INFO, "PROGRESS_PACKAGING" );
            new NativePackager( config, fileManager.getFiles() ).run();
            Logger.logMessage( LogType.INFO, "PACKAGING_COMPLETE" );

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
}
