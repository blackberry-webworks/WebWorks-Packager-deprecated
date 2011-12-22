package net.rim.tumbler.bar;

import java.io.File;
import java.lang.reflect.Field;

import net.rim.tumbler.OSUtils;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.exception.ValidationException;
import net.rim.tumbler.file.FileManager;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NativePackagerTest {
    private static Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    private static final SessionManager _session = _context.mock( SessionManager.class );
    private static BBWPProperties _bbwpProperties;
    private static WidgetConfig _config;

    private static final boolean DEBUG_ENABLED = false;
    private static final boolean VERBOSE = false;
    private static final String OUTPUT_DIR = "out";
    private static final String SOURCE_DIR = "source";
    private static String toolsDir = OSUtils.isMac() ? "/Users/build/testrun/qnxtools copy/bin" : "C:\\testrun\\qnxtools copy";

    private static void mockSession() throws Exception {
        // mock SessionManager which is used by NativePackager
        _context.checking( new Expectations() {
            {
                allowing( _session ).debugMode(); will( returnValue( DEBUG_ENABLED ) );
                allowing( _session ).isVerbose(); will( returnValue( VERBOSE ) );
                allowing( _session ).getOutputFolder(); will( returnValue( OUTPUT_DIR ) );
                allowing( _session ).getSourceFolder(); will( returnValue( SOURCE_DIR ) );
                // mock it to somewhere that contains space to make sure it works with space
                allowing( _session ).getBBWPJarFolder(); will( returnValue( toolsDir ) );
                allowing( _session ).getWidgetArchive(); will( returnValue( "../packager.test/src/bbxwebworks/apps/bbm.zip" ) );
            }
        } );

        Class< ? > c = SessionManager.class;
        Field singleton = c.getDeclaredField( "_instance" );
        singleton.setAccessible( true );
        singleton.set( null, _session );
    }

    private static void prepareConfig() throws ValidationException {
        _config = new WidgetConfig();
        _config.setName( "Test" );
        _config.setDescription( "This is a description." );
        _config.setAuthor( "John Doe" );
        _config.setVersion( "1.0.0" );
        _config.addIcon( "icon.png" );
    }

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        mockSession();

        prepareConfig();

        String sessionHome = new File( NativePackager.class.getProtectionDomain().getCodeSource().getLocation().toURI() )
                .getCanonicalPath();
        _bbwpProperties = new BBWPProperties( "../packager.test/src/bbxwebworks/bin/bbwp.properties", sessionHome );
    }

    @Test
    public void testRun() throws Exception {
        try {
            FileManager fileMgr = new FileManager( _bbwpProperties );
            fileMgr.prepare();
            new NativePackager( _config, fileMgr.getFiles() ).run();
        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Exception caught in NativePackager.run()" );
        }
    }
}
