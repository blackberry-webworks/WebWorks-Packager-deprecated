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
package net.rim.tumbler.bar;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import net.rim.tumbler.OSUtils;
import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.WidgetPackager.Target;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.processbuffer.ErrorBuffer;
import net.rim.tumbler.processbuffer.ExitBuffer;
import net.rim.tumbler.processbuffer.OutputBuffer;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class NativePackager {    
    private static final String NL = System.getProperty( "line.separator" );

    private BBWPProperties _bbwpProperties;
    private WidgetConfig _config;
    private List< String > _files;
    private Target _target;
    
    public NativePackager( BBWPProperties bbwpProperties, WidgetConfig config, List< String > files, Target target ) {
        _bbwpProperties = bbwpProperties;
        _config = config;
        _files = files;
        _target = target;
    }

    private void generateTabletXML() throws ParserConfigurationException, DOMException, ValidationException, TransformerException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();

        Element root = doc.createElement( "qnx" );
        doc.appendChild( root );

        Element id = doc.createElement( "id" );
        id.appendChild( doc.createTextNode( _config.getName().replace( " ", "" ) ) );
        root.appendChild( id );

        Element name = doc.createElement( "name" );
        name.appendChild( doc.createTextNode( _config.getName() ) );
        root.appendChild( name );

        Element versionNumber = doc.createElement( "versionNumber" );
        versionNumber.appendChild( doc.createTextNode( _config.getVersion() ) );
        root.appendChild( versionNumber );

        if( _config.getDescription() != null && !_config.getDescription().isEmpty() ) {
            Element description = doc.createElement( "description" );
            description.appendChild( doc.createTextNode( _config.getDescription() ) );
            root.appendChild( description );
        }

        Element author = doc.createElement( "author" );
        author.appendChild( doc.createTextNode( _config.getAuthor() ) );
        root.appendChild( author );

        if( _config.getIconSrc() != null && !_config.getIconSrc().isEmpty() ) {
            Element icon = doc.createElement( "icon" );
            Element image = doc.createElement( "image" );
            image.appendChild( doc.createTextNode( _config.getIconSrc().firstElement() ) );
            icon.appendChild( image );
            root.appendChild( icon );
        }

        Element asset = doc.createElement( "asset" );
        asset.setAttribute( "type", "qnx/elf" );
        asset.setAttribute( "entry", "true" );
        asset.appendChild( doc.createTextNode( WidgetPackager.WW_EXECUTABLE_NAME ) );
        root.appendChild( asset );

        Element initialWindow = doc.createElement( "initialWindow" );
        root.appendChild( initialWindow );

        Element systemChrome = doc.createElement( "systemChrome" );
        systemChrome.appendChild( doc.createTextNode( "none" ) );
        initialWindow.appendChild( systemChrome );

        Element transparent = doc.createElement( "transparent" );
        transparent.appendChild( doc.createTextNode( "true" ) );
        initialWindow.appendChild( transparent );

        Element env = doc.createElement( "env" );
        env.setAttribute( "var", "WEBKIT_NUMBER_OF_BACKINGSTORE_TILES" );
        env.setAttribute( "value", "12" );
        root.appendChild( env );

        Element permission = doc.createElement( "permission" );
        permission.setAttribute( "system", "true" );
        permission.appendChild( doc.createTextNode( "run_native" ) );
        root.appendChild( permission );

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty( OutputKeys.OMIT_XML_DECLARATION, "yes" );
        transformer.setOutputProperty( OutputKeys.INDENT, "yes" );

        DOMSource source = new DOMSource( doc );
        File blackberryTabletXML = new File( SessionManager.getInstance().getSourceFolder(), "blackberry-tablet.xml" );
        transformer.transform( source, new StreamResult( blackberryTabletXML ) );
    }
    
    private void generateOptionsFile() throws IOException {
        File options = new File( SessionManager.getInstance().getSourceFolder(), "options" );
        BufferedWriter writer = new BufferedWriter( new FileWriter( options ) );
        File outputDir = new File( SessionManager.getInstance().getOutputFolder() + File.separator + _target.name() );

        if( !outputDir.exists() ) {
            outputDir.mkdirs();
        }

        writer.write( "-package" + NL );
        writer.write( new File( outputDir, _config.getName().replace( " ", "" ).concat( ".bar" ) ).getAbsolutePath() + NL );
        writer.write( "-C" + NL );
        writer.write( options.getParentFile().getAbsolutePath() + NL );
        writer.write( "blackberry-tablet.xml" + NL );

        for( String file : _files ) {
            writer.write( file + NL );
        }
        
        writer.close();
    }

    private void execNativePackager() throws IOException, InterruptedException, PackageException {
        SessionManager session = SessionManager.getInstance();
        File cwd = new File( session.getSourceFolder() );
        String script = "blackberry-nativepackager";

        if( OSUtils.isWindows() ) {
            script += ".bat";
        }

        if( !cwd.exists() ) {
            cwd.mkdirs();
        }

        Process p = Runtime.getRuntime().exec(
                new String[] { new File( _bbwpProperties.getDependenciesDir() + "/tools/bin", script ).getAbsolutePath(),
                        "@options" }, null, cwd );

        OutputBuffer stdout = new OutputBuffer( p );
        ErrorBuffer stderr = new ErrorBuffer( p );
        ExitBuffer exitCode = new ExitBuffer( p );

        stdout.waitFor();
        stderr.waitFor();
        exitCode.waitFor();

        if( exitCode.getExitValue().intValue() != 0 ) {
            System.out.write( stderr.getStderr() );
            System.out.write( stdout.getStdout() );
            System.out.flush();
            throw new PackageException( "EXCEPTION_NATIVEPACKAGER" );
        }        
    }

    public void run() throws PackageException {
        try {
            generateTabletXML();
            generateOptionsFile();
            execNativePackager();
        } catch( DOMException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        } catch( ParserConfigurationException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        } catch( ValidationException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        } catch( TransformerException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        } catch( IOException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        } catch( InterruptedException e ) {
            throw new PackageException( e, "EXCEPTION_UNKNOWN_CREATE_BAR" );
        }
    }
}
