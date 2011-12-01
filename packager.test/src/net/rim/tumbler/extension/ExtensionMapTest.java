package net.rim.tumbler.extension;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import junit.framework.Assert;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExtensionMapTest {
    private static Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    private static SessionManager _session = _context.mock( SessionManager.class );    
    
    private static final String PLATFORM = "BBX";
    private static final String TARGET = "default";
    private static final String EXT_REPO = "../packager.test/src/bbxwebworks/ext";
    private static final String SOURCE_DIR = "source";

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
        // mock SessionManager which is used by FileManager
        _context.checking( new Expectations() {
            {
                allowing( _session ).getSourceFolder(); will( returnValue( SOURCE_DIR ) );
            }
        } );

        Class< ? > c = SessionManager.class;
        Field singleton = c.getDeclaredField( "_instance" );
        singleton.setAccessible( true );
        singleton.set( null, _session );
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

        File outputDir = new File( SessionManager.getInstance().getSourceFolder() );
        Assert.assertTrue( outputDir.exists() );

        File extDir = new File( outputDir, "ext" );
        Assert.assertTrue( extDir.exists() );

        File invokeDir = new File( extDir, "blackberry.invoke" );
        Assert.assertTrue( invokeDir.exists() );

        File clientFile = new File( invokeDir, "client.js" );
        Assert.assertTrue( clientFile.exists() );

        File indexFile = new File( invokeDir, "index.js" );
        Assert.assertTrue( indexFile.exists() );
    }
    
    @Test
    public void testCopyRequiredFilesNonExistentExtension() throws IOException, PackageException {
        ExtensionMap map = new ExtensionMap( PLATFORM, TARGET, EXT_REPO );        
        map.copyRequiredFiles( SessionManager.getInstance().getSourceFolder(), "not.exists" );

        File outputDir = new File( SessionManager.getInstance().getSourceFolder() );
        Assert.assertTrue( outputDir.exists() );

        File extDir = new File( outputDir, "ext" );
        Assert.assertFalse( extDir.exists() );
    }
}
