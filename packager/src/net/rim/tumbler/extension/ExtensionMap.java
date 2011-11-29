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
package net.rim.tumbler.extension;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.file.FileManager;

public class ExtensionMap {

    // need to check in this collection first before creating a new ExtensionDescriptor
    private Map< String, ExtensionDescriptor > _masterList; // map from an entryClass key to the corresponding extension
                                                            // descriptor (of that same entryClass)

    public ExtensionMap( String platform, String version, String repositoryRoot ) {
        _masterList = new LinkedHashMap< String, ExtensionDescriptor >();

        File root = new File( repositoryRoot );

        // Note that it's possible that the ext folder doesn't even exist
        if( root.isDirectory() ) {
            File[] extFolders = root.getAbsoluteFile().listFiles();

            for( File extFolder : extFolders ) {
                String id = extFolder.getName();
                ExtensionDescriptor descriptor = new ExtensionDescriptor( id, "", extFolder.getAbsolutePath() );
                _masterList.put( id, descriptor );
                
                for( File f : extFolder.listFiles() ) {
                    if (f.isDirectory()) {
                        addFilesRecursively( descriptor, f, "", "" );
                    } else {
                        descriptor.addConfiguredPathname( f.getName() );
                    }
                }
            }
        }
    }

    // recursive helper method
    // directory.isDirectory() is assumed to be true
    private static void addFilesRecursively( ExtensionDescriptor descriptor, File directory, String relativePath, String pkg ) {
        for( String s : directory.list() ) {
            String relativeToExtDir = relativePath + File.separator + s;

            String relativeToPackage;
            if( pkg.length() > 0 ) {
                relativeToPackage = pkg + File.separator + s;
            } else {
                relativeToPackage = s;
            }

            File f = new File( directory, s );

            if( f.isDirectory() ) {
                addFilesRecursively( descriptor, f, relativeToExtDir, relativeToPackage );
            } else if( f.isFile() ) {
                descriptor.addConfiguredPathname( relativeToExtDir, relativeToPackage );
            }
        }
    }

    public void copyRequiredFiles( String outputFolder, String featureID ) throws IOException, PackageException {
        ExtensionDescriptor depDescriptor = _masterList.get( featureID );

        if( !depDescriptor.isCopied() ) {
            //
            // The prefix for javascript files. This can be
            // prepended
            // as-is to the configured pathname.
            //
            String javascriptPrefix = outputFolder + File.separator + "ext" + File.separator + featureID + File.separator;

            //
            // The partial prefix for actionscript files. This needs
            // the folder structure such that it matches
            // the package name.
            //
            String actionscriptPrefix = outputFolder + File.separator;

            for( ConfiguredPathname pathname : depDescriptor.getConfiguredPathnames() ) {
                if( pathname.getPathname().endsWith( ".js" ) ) {
                    //
                    // This is javascript and therefore has no
                    // package.
                    // Copy to javascriptPrefix +
                    // pathname.getPathname().
                    //
                    FileManager.copyFile( new File( depDescriptor.getRootFolder(), pathname.getPathname() ),
                            new File( javascriptPrefix + pathname.getPathname() ) );
                } else if( pathname.getRelativeToPackage() != null ) {
                    //
                    // This is something other than javascript and
                    // presumably has
                    // the notion of a package (e.g., ActionScript
                    // or Java).
                    // Copy to actionscriptPrefix +
                    // pathname.getRelativeToPackage().
                    //
                    FileManager.copyFile( new File( depDescriptor.getRootFolder(), pathname.getPathname() ),
                            new File( actionscriptPrefix + pathname.getRelativeToPackage() ) );
                } else {
                    // unexpected file type
                    // TODO: log it
                }
            }

            depDescriptor.markCopied();
        }
    }
}
