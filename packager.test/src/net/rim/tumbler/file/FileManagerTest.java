package net.rim.tumbler.file;

import java.io.File;
import java.lang.reflect.Field;

import junit.framework.Assert;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.BeforeClass;
import org.junit.Test;

public class FileManagerTest {
    private static Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    private static SessionManager _session = _context.mock( SessionManager.class );
    private static BBWPProperties _bbwpProperties;
    
    private static String OUTPUT_DIR = "out";
    private static String SOURCE_DIR = "source";

    private static void mockSession() throws Exception {
        // mock SessionManager which is used by FileManager
        _context.checking( new Expectations() {
            {
                allowing( _session ).getOutputFolder(); will( returnValue( OUTPUT_DIR ) );
                allowing( _session ).getSourceFolder(); will( returnValue( SOURCE_DIR ) );
                allowing( _session ).getBBWPJarFolder(); will( returnValue( "../packager.test/src/bbxwebworks/bin" ) );
                allowing( _session ).getWidgetArchive(); will( returnValue( "../packager.test/src/bbxwebworks/apps/bbm.zip" ) );
            }
        } );

        Class< ? > c = SessionManager.class;
        Field singleton = c.getDeclaredField( "_instance" );
        singleton.setAccessible( true );
        singleton.set( null, _session );
    }

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        mockSession();

        String sessionHome = new File( FileManagerTest.class.getProtectionDomain().getCodeSource().getLocation().toURI() )
                .getCanonicalPath();
        
        _bbwpProperties = new BBWPProperties( "../packager.test/src/bbxwebworks/bin/bbwp.properties", sessionHome );
    }

    @Test
    public void testPrepare() throws Exception {
        FileManager fileMgr = new FileManager( _bbwpProperties );
        fileMgr.prepare();
        
        Assert.assertTrue( new File( SOURCE_DIR + "/bin" ).exists() );
        Assert.assertTrue( new File( SOURCE_DIR + "/bin/bbx-framework" ).exists() );
        Assert.assertTrue( new File( SOURCE_DIR + "/dependencies" ).exists() );
        Assert.assertTrue( new File( SOURCE_DIR + "/lib" ).exists() );
        Assert.assertTrue( new File( SOURCE_DIR + "/lib/public/icon.png" ).exists() );
    }
}
