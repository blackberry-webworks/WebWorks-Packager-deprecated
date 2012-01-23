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

package net.rim.tumbler.extension;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.session.SessionManager;
import net.rim.tumbler.util.SessionMocker;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExtensionMapTest {
    private static final String PLATFORM = "BBX";
    private static final String TARGET = "default";
    private static final String EXT_REPO = "../packager.test/src/bbxwebworks/ext";

    private static boolean deleteDir( File dir ) {
        if( dir.isDirectory() ) {
            String[] children = dir.list();
            for( int i = 0; i < children.length; i++ ) {
                boolean success = deleteDir( new File( dir, children[ i ] ) );
                if( !success ) {
                    return false;
                }
            }
        }

        return dir.delete();
    }

    @BeforeClass    
    public static void mockSession() throws Exception {
        SessionMocker.mockSession( null );
    }    

    @Before
    public void cleanOutputDir() {
        File sourceFolder = new File( SessionManager.getInstance().getSourceFolder() );

        if( sourceFolder.exists() ) {
            deleteDir( new File( SessionManager.getInstance().getSourceFolder() ) );
        }

        sourceFolder.mkdirs();        
    }

    @Test
    public void testCopyRequiredFiles() throws IOException, PackageException {
        ExtensionMap map = new ExtensionMap( PLATFORM, TARGET, EXT_REPO );        
        map.copyRequiredFiles( SessionManager.getInstance().getSourceFolder(), "blackberry.invoke" );
        List< String > copiedFiles = map.getCopiedFiles();

        File outputDir = new File( SessionManager.getInstance().getSourceFolder() );
        Assert.assertTrue( outputDir.exists() );

        File extDir = new File( outputDir, "chrome/ext" );
        Assert.assertTrue( extDir.exists() );

        File invokeDir = new File( extDir, "blackberry.invoke" );
        Assert.assertTrue( invokeDir.exists() );

        File clientFile = new File( invokeDir, "client.js" );
        Assert.assertTrue( clientFile.exists() );
        Assert.assertTrue( copiedFiles.contains( clientFile.getAbsolutePath() ) );

        File indexFile = new File( invokeDir, "index.js" );
        Assert.assertTrue( indexFile.exists() );
        Assert.assertTrue( copiedFiles.contains( indexFile.getAbsolutePath() ) );
    }
    
    @Test
    public void testCopyRequiredFilesNonExistentExtension() throws IOException, PackageException {
        ExtensionMap map = new ExtensionMap( PLATFORM, TARGET, EXT_REPO );        
        map.copyRequiredFiles( SessionManager.getInstance().getSourceFolder(), "not.exists" );

        File outputDir = new File( SessionManager.getInstance().getSourceFolder() );
        Assert.assertTrue( outputDir.exists() );

        File extDir = new File( outputDir, "chrome/ext" );
        Assert.assertFalse( extDir.exists() );
    }
}
