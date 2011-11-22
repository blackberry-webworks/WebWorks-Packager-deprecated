package net.rim.tumbler.extension;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

import net.rim.tumbler.exception.PackageException;

public class ExtensionMapTest {
    private static final String PLATFORM = "BBX";
    private static final String TARGET = "default";
    private static final String EXT_REPO = "../packager.test/src/bbxwebworks/ext";
    private static final String OUTPUT_DIR = "out";

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

    private static File getFile( File dir, String name ) {
        if( dir != null && dir.exists() ) {
            for( File file : dir.listFiles() ) {
                if( file.getName().equals( name ) ) {
                    return file;
                }
            }
        }

        return null;
    }

    @Before
    public void cleanOutputDir() {
        deleteDir( new File( OUTPUT_DIR ) );
    }

    @Test
    public void testCopyRequiredFiles() throws IOException, PackageException {
        ExtensionMap map = new ExtensionMap( PLATFORM, TARGET, EXT_REPO );
        map.copyRequiredFiles( OUTPUT_DIR, "blackberry.invoke" );

        File outputDir = new File( OUTPUT_DIR );
        Assert.assertTrue( outputDir.exists() );

        File extDir = getFile( outputDir, "ext" );
        Assert.assertTrue( extDir.exists() );

        File invokeDir = getFile( extDir, "blackberry_invoke_Invoke" );
        Assert.assertTrue( invokeDir.exists() );

        File clientFile = getFile( invokeDir, "client.js" );
        Assert.assertTrue( clientFile.exists() );

        File serverFile = getFile( invokeDir, "server.js" );
        Assert.assertTrue( serverFile.exists() );
    }

    @Test
    public void testGetCopiedFiles() throws IOException, PackageException {
        Map< String, Vector< String >> result = new LinkedHashMap< String, Vector< String >>();
        ExtensionMap map = new ExtensionMap( PLATFORM, TARGET, EXT_REPO );
        map.copyRequiredFiles( OUTPUT_DIR, "blackberry.system" );

        map.getCopiedFiles( ".js", result, "" );

        String key = "blackberry.system.System";
        Assert.assertTrue( result.containsKey( key ) );

        Vector<String> value = result.get( key );
        Assert.assertFalse( value.isEmpty() );

        Assert.assertTrue( value.contains( "blackberry_system_System" + File.separator + "client.js" ) );
        Assert.assertTrue( value.contains( "blackberry_system_System" + File.separator + "server.js" ) );
    }
}
