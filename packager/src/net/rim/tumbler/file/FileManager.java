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
package net.rim.tumbler.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.rim.tumbler.WidgetPackager.Target;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.extension.ExtensionMap;
import net.rim.tumbler.json4j.JSONArray;
import net.rim.tumbler.json4j.JSONException;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

public class FileManager {
    private WidgetConfig _config;
    private BBWPProperties _bbwpProperties;
    private Vector< String > _inputFiles;
    private File _sourceFolder;

    private static final String FILE_SEP = System.getProperty( "file.separator" );
    
    public FileManager( WidgetConfig config, BBWPProperties bbwpProperties ) {
        _config = config;
        _bbwpProperties = bbwpProperties;
        _inputFiles = new Vector< String >();
        _sourceFolder = Paths.SOURCE_DIR;
    }

    public void cleanSource() {        
        deleteDirectory( _sourceFolder );
        _sourceFolder.mkdirs();
    }

    public void copyWWExecutable( Target buildTarget ) throws IOException {
        String folderName = buildTarget.getExecutableFolder();

        if( folderName != null ) {
            File targetFile = new File( _sourceFolder, Paths.WW_EXECUTABLE_NAME );

            if( targetFile.exists() ) {
                targetFile.delete();
            }

            copyFile( new File( _bbwpProperties.getDependenciesDir() + FILE_SEP + folderName + FILE_SEP
                    + Paths.WW_EXECUTABLE_NAME ), targetFile );
            _inputFiles.add( targetFile.getAbsolutePath() );
        }
    }

    private void copyLib() throws IOException {
        TemplateWrapper templateWrapper = new TemplateWrapper( _bbwpProperties.getTemplateDir() );
        _inputFiles.addAll( templateWrapper.writeAllTemplates( Paths.LIB_DIR.getAbsolutePath() ) );
    }

    private void copyBootstrapFiles() throws IOException {
        TemplateWrapper templatewrapper = new TemplateWrapper( new File( _bbwpProperties.getDependenciesDir(),
                Paths.BOOTSTRAP_DIR_NAME ).getAbsolutePath() );
        _inputFiles.addAll( templatewrapper.writeAllTemplates( Paths.CHROME_DIR.getAbsolutePath() ) );
    }

    private void extractArchive() throws IOException {
        ZipFile zip = new ZipFile( new File( SessionManager.getInstance().getWidgetArchive() ).getAbsolutePath() );
        Enumeration< ? > en = zip.entries();

        while( en.hasMoreElements() ) {
            // create output file name
            ZipEntry ze = (ZipEntry) en.nextElement();
            if( ze.isDirectory() )
                continue;

            File zipEntryFile = new File( ze.getName() );

            // extract file
            InputStream is = new BufferedInputStream( zip.getInputStream( ze ) );
            File fi = new File( _sourceFolder, zipEntryFile.getPath() );
            if( !fi.getParentFile().isDirectory() || !fi.getParentFile().exists() )
                fi.getParentFile().mkdirs();
            OutputStream fos = new BufferedOutputStream( new FileOutputStream( fi ) );
            int bytesRead;
            while( ( bytesRead = is.read() ) != -1 )
                fos.write( bytesRead );
            fos.close();

            _inputFiles.add( fi.getAbsolutePath() );
        }
    }

    private Set< String > getWhitelistedFeatures() {
        Hashtable< WidgetAccess, Vector< WidgetFeature >> accessTable = _config.getAccessTable();
        Set< String > featureIDs = new HashSet< String >();
        // if the access table is empty, don't even bother since there's no features to search for
        if( accessTable != null && accessTable.size() > 0 ) {
            //
            // Extract the set of feature IDs from the access table.
            // We flatten the structure since we don't care about the
            // access node or whether it applies to local access; all
            // we want are the unique feature IDs.
            //
            for( Vector< WidgetFeature > accessTableValue : accessTable.values() ) {
                for( WidgetFeature widgetFeature : accessTableValue ) {
                    featureIDs.add( widgetFeature.getID() );
                }
            }
        }

        return featureIDs;
    }

    /**
     * Copies the correct set of extension source files from the extension repository into the project area so that they can be
     * compiled along with the framework, and returns a hashtable populated with javascript file names for use downstream. Each
     * key in the hashtable is the entry class name, and the value contains the relative pathnames of the corresponding javascript
     * files.
     */
    private void copyExtensions() throws IOException, PackageException {
        ExtensionMap extensionMap = new ExtensionMap( "BBX", "default", _bbwpProperties.getExtensionRepo( SessionManager
                .getInstance().getSessionHome() ) ); // location of the extension repository

        for( String featureID : getWhitelistedFeatures() ) {
            extensionMap.copyRequiredFiles( SessionManager.getInstance().getSourceFolder(), // destination for extensions
                    featureID );
        }

        _inputFiles.addAll( extensionMap.getCopiedFiles() );
    }

    public void prepare() throws Exception {
        cleanSource();
        
        copyBootstrapFiles();

        copyLib();        

        extractArchive();
        
        copyExtensions();
    }

    public void writeToSource( byte[] fileToWrite, String file ) throws Exception {
        try {
            File f = new File( file );
            if( !new File( file ).isAbsolute() ) {
                f = new File( _sourceFolder, file );
            }

            if( !f.exists() ) {
                f.getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream( f );
            fos.write( fileToWrite );
            fos.close();
            _inputFiles.add( f.getAbsolutePath() );
        } catch( Exception e ) {
            throw new PackageException( e, file );
        }
    }

    public byte[] generateFrameworkModulesJSFile() {
        FilenameFilter libFilter = new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                return new File( dir, name ).isDirectory() || name.toLowerCase().endsWith( ".js" );
            }
        };
        FilenameFilter extFilter = new FilenameFilter() {
            @Override
            public boolean accept( File dir, String name ) {
                if( new File( dir, name ).isDirectory() ) {
                    return true;
                } else if( getWhitelistedFeatures().contains( dir.getName() ) ) {
                    return name.toLowerCase().endsWith( ".js" );
                }

                return false;
            }
        };
        List< File > files = new ArrayList< File >();
        List< String > relativePaths = new ArrayList< String >();
        files.addAll( listFiles( Paths.LIB_DIR, libFilter ) );
        files.addAll( listFiles( Paths.EXT_DIR, extFilter ) );

        for( File f : files ) {
            relativePaths.add( Paths.CHROME_DIR.toURI().relativize( f.toURI() ).getPath() );
        }

        try {
            StringBuffer buffer = new StringBuffer( "var frameworkModules = " );
            buffer.append( new JSONArray( relativePaths.toArray() ).toString( 4 ) );
            buffer.append( ";\n" );
            return buffer.toString().getBytes();
        } catch( JSONException e ) {
            throw new RuntimeException( e );
        }
    }

    private List< File > listFiles( File dir, FilenameFilter filter ) {
        List< File > files = new ArrayList< File >();

        if( dir.exists() ) {
            if( dir.isDirectory() ) {
                File[] children = dir.listFiles( filter );
                for( File child : children ) {
                    if( child.isDirectory() ) {
                        files.addAll( listFiles( child, filter ) );
                    } else {
                        files.add( child );
                    }
                }
            }
        }

        return files;
    }

    // Copy a file
    public static void copyFile( File in, File out ) throws IOException {
        // Create parent directories
        if( out.getAbsolutePath().lastIndexOf( File.separator ) > 0 ) {
            String parentDirectory = out.getAbsolutePath().substring( 0, out.getAbsolutePath().lastIndexOf( File.separator ) );
            new File( parentDirectory ).mkdirs();
        }

        FileChannel inChannel = new FileInputStream( in ).getChannel();
        FileChannel outChannel = new FileOutputStream( out ).getChannel();
        try {
            // windows is limited to 64mb chunks
            long size = inChannel.size();
            long position = 0;
            while( position < size )
                position += inChannel.transferTo( position, 67076096, outChannel );
        } finally {
            if( inChannel != null )
                inChannel.close();
            if( outChannel != null )
                outChannel.close();
        }
    }

    // delete a dir
    private boolean deleteDirectory( File dir ) {
        // remove files first
        if( dir.exists() && dir.isDirectory() ) {
            String[] children = dir.list();
            for( String child : children ) {
                if( !deleteDirectory( new File( dir, child ) ) )
                    return false;
            }
        }
        if( dir.exists() ) {
            // then remove the directory
            return dir.delete();
        }
        return false;
    }
    
    public List< String > getFiles() {
        return _inputFiles;
    }

    /**
     * Returns either <code>msWindows</code> or <code>macOsx</code> based on the host platform. Supports <code>null</code> for
     * either or both inputs.
     */
    public static String selectOnPlatform( String msWindows, String macOsx ) {
        String os = System.getProperty( "os.name" ).toLowerCase();
        return os.indexOf( "win" ) >= 0 ? msWindows : macOsx;
    }

    /**
     * Returns a copy of <code>path</code> with trailing separator characters removed. For example:
     * 
     * <pre>
     *     removeTrailingSeparators("/foo/bar/") returns "/foo/bar"
     * </pre>
     * 
     * Here, a separator character is <code>'/'</code> or <code>'\\'</code>. Also, here, a separator character is considered
     * trailing only if there exists a non-separator character somewhere before it in the string. For example:
     * 
     * <pre>
     *     removeTrailingSeparators("/") returns "/"
     *     removeTrailingSeparators("//") returns "//"
     *     removeTrailingSeparators("//foo") returns "//foo"
     *     removeTrailingSeparators("//foo/") returns "//foo"
     * </pre>
     * 
     * @param path
     *            the input string possibly ending in one or more separator characters.
     * 
     * @return a copy of <code>path</code> with trailing separator characters removed.
     */
    public static String removeTrailingSeparators( String path ) {
        boolean nonSeparatorFound = false;
        int len = path.length();
        int i;

        for( i = len - 1; i >= 0; i-- ) {
            if( path.charAt( i ) != '/' && path.charAt( i ) != '\\' ) {
                nonSeparatorFound = true;
                break;
            }
        }

        return path.substring( 0, ( nonSeparatorFound ? i + 1 : len ) );
    }
}
