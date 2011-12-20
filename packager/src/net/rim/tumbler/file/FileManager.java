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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

public class FileManager {
    private BBWPProperties _bbwpProperties;
    private Vector< String > _inputFiles;

    private static final String FILE_SEP = System.getProperty( "file.separator" );

    public FileManager( BBWPProperties bbwpProperties ) {
        _bbwpProperties = bbwpProperties;
        _inputFiles = new Vector< String >();
    }

    public void cleanOutput() {
        File zipFile = getOutputFolderZipFile();

        if( zipFile.exists() ) {
            zipFile.delete();
        }
    }

    public void cleanSource() {
        File sourceDir = new File( SessionManager.getInstance().getSourceFolder() );        
        deleteDirectory( sourceDir );
        sourceDir.mkdirs();
    }

    private void copyBootstrapScript() throws IOException {
        File target = new File( SessionManager.getInstance().getSourceFolder(), "/bin/bbx-framework" );
        copyFile( new File( SessionManager.getInstance().getBBWPJarFolder() + "/bbx-framework" ), target );
        _inputFiles.add( target.getAbsolutePath() );
    }

    private void copyWWExecutable() throws IOException {
        File target = new File( SessionManager.getInstance().getSourceFolder(), WidgetPackager.WW_EXECUTABLE_FILE );
        copyFile( new File( SessionManager.getInstance().getBBWPJarFolder() + FILE_SEP + WidgetPackager.WW_EXECUTABLE_FILE ),
                target );
        _inputFiles.add( target.getAbsolutePath() );
    }

    private void copyLib() throws IOException {
        TemplateWrapper templateWrapper = new TemplateWrapper( _bbwpProperties.getTemplateDir() );
        _inputFiles.addAll( templateWrapper.writeAllTemplates( SessionManager.getInstance().getSourceFolder() + "/lib" ) );
    }

    private void copyDependencies() throws IOException {
        TemplateWrapper wrapper = new TemplateWrapper( _bbwpProperties.getDependenciesDir() );
        _inputFiles.addAll( wrapper.writeAllTemplates( SessionManager.getInstance().getSourceFolder() + "/dependencies" ) );
    }

    private void extractArchive() throws IOException {
        ZipFile zip = new ZipFile( new File( SessionManager.getInstance().getWidgetArchive() ).getAbsolutePath() );
        Enumeration< ? > en = zip.entries();
        String sourceFolder = SessionManager.getInstance().getSourceFolder();

        while( en.hasMoreElements() ) {
            // create output file name
            ZipEntry ze = (ZipEntry) en.nextElement();
            if( ze.isDirectory() )
                continue;

            File zipEntryFile = new File( ze.getName() );
            String fname = sourceFolder + File.separator + zipEntryFile.getPath();

            // extract file
            InputStream is = new BufferedInputStream( zip.getInputStream( ze ) );
            File fi = new File( fname );
            if( !fi.getParentFile().isDirectory() || !fi.getParentFile().exists() )
                fi.getParentFile().mkdirs();
            OutputStream fos = new BufferedOutputStream( new FileOutputStream( fname ) );
            int bytesRead;
            while( ( bytesRead = is.read() ) != -1 )
                fos.write( bytesRead );
            fos.close();
            
            _inputFiles.add( fname );
        }
    }

    public void createOutputZip() throws IOException {
        File outputDir = new File( SessionManager.getInstance().getOutputFolder() );
        File sourceDir = new File( SessionManager.getInstance().getSourceFolder() );
        File zip = getOutputFolderZipFile();
        
        if( !outputDir.exists() ) {
            outputDir.mkdirs();
        }
        
        ZipOutputStream out = new ZipOutputStream( new FileOutputStream( zip ) );

        createZipEntry( zip, sourceDir, "", sourceDir, out );
        
        out.close();
    }

    private static File getOutputFolderZipFile() {
        String zipFileName = new File( SessionManager.getInstance().getWidgetArchive() ).getName();
        return new File( SessionManager.getInstance().getOutputFolder() + FILE_SEP + zipFileName );
    }

    private static void createZipEntry( File zip, File sourceDir, String parentDir, File toZip, ZipOutputStream out )
            throws IOException {
        if( toZip.isDirectory() ) {
            File[] files = toZip.listFiles();

            for( File f : files ) {
                createZipEntry( zip, sourceDir, sourceDir.equals( toZip ) ? "" : parentDir + FILE_SEP + toZip.getName(), f, out );
            }
        } else {
            if( !zip.equals( toZip ) ) {
                byte[] buf = new byte[ 1024 ];
                FileInputStream in = new FileInputStream( toZip );
                int len;

                out.putNextEntry( new ZipEntry( parentDir + FILE_SEP + toZip.getName() ) );

                while( ( len = in.read( buf ) ) > 0 ) {
                    out.write( buf, 0, len );
                }

                out.closeEntry();
                in.close();
            }
        }
    }

    public void prepare() throws Exception {
        deleteDirectory( new File( SessionManager.getInstance().getOutputFolder() ) );

        cleanSource();

        copyBootstrapScript();

        copyWWExecutable();

        copyLib();

        copyDependencies();

        extractArchive();
    }

    public void writeToSource( byte[] fileToWrite, String relativeFile ) throws Exception {
        try {
            String s = SessionManager.getInstance().getSourceFolder() + FILE_SEP + relativeFile;
            if( !new File( s ).exists() ) {
                new File( s ).getParentFile().mkdirs();
            }
            FileOutputStream fos = new FileOutputStream( s );
            fos.write( fileToWrite );
            fos.close();
        } catch( Exception e ) {
            throw new PackageException( e, relativeFile );
        }
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
