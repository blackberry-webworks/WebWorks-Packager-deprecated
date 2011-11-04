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
package net.rim.tumbler.serialize;

import java.lang.reflect.Field;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import junit.framework.Assert;
import net.rim.tumbler.config.WidgetAccess;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.config.WidgetFeature;
import net.rim.tumbler.json4j.JSONArray;
import net.rim.tumbler.json4j.JSONException;
import net.rim.tumbler.json4j.JSONObject;
import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * JUnit for WidgetConfig_v1Serializer.
 * 
 * Given values parsed from XML, test if the serializer writes correct values to the JSON object
 */
public class SerializerTest {
    private static Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };

    private WidgetConfig _widgetConfig;
    private WidgetConfig_v1Serializer _serializer;
    private static SessionManager _session = _context.mock( SessionManager.class );;
    
    private static final String TLD = "$$ac$$ad$$ae$$aero$$af$$ag$$ai$$al$$am$$an$$ao$$aq$$ar$$arpa$$as$$asia$$at$$au$$aw$$ax$$az$$ba$$bb$$bd$$be$$bf$$bg$$bh$$bi$$biz$$bj$$bm$$bn$$bo$$br$$bs$$bt$$bv$$bw$$by$$bz$$ca$$cat$$cc$$cd$$cf$$cg$$ch$$ci$$ck$$cl$$cm$$cn$$co$$com$$coop$$cr$$cu$$cv$$cx$$cy$$cz$$de$$dj$$dk$$dm$$do$$dz$$ec$$edu$$ee$$eg$$er$$es$$et$$eu$$fi$$fj$$fk$$fm$$fo$$fr$$ga$$gb$$gd$$ge$$gf$$gg$$gh$$gi$$gl$$gm$$gn$$gov$$gp$$gq$$gr$$gs$$gt$$gu$$gw$$gy$$hk$$hm$$hn$$hr$$ht$$hu$$id$$ie$$il$$im$$in$$info$$int$$io$$iq$$ir$$is$$it$$je$$jm$$jo$$jobs$$jp$$ke$$kg$$kh$$ki$$km$$kn$$kp$$kr$$kw$$ky$$kz$$la$$lb$$lc$$li$$lk$$lr$$ls$$lt$$lu$$lv$$ly$$ma$$mc$$md$$me$$mg$$mh$$mil$$mk$$ml$$mm$$mn$$mo$$mobi$$mp$$mq$$mr$$ms$$mt$$mu$$museum$$mv$$mw$$mx$$my$$mz$$na$$name$$nc$$ne$$net$$nf$$ng$$ni$$nl$$no$$np$$nr$$nu$$nz$$om$$org$$pa$$pe$$pf$$pg$$ph$$pk$$pl$$pm$$pn$$pr$$pro$$ps$$pt$$pw$$py$$qa$$re$$ro$$rs$$ru$$rw$$sa$$sb$$sc$$sd$$se$$sg$$sh$$si$$sj$$sk$$sl$$sm$$sn$$so$$sr$$st$$su$$sv$$sy$$sz$$tc$$td$$tel$$tf$$tg$$th$$tj$$tk$$tl$$tm$$tn$$to$$tp$$tr$$travel$$tt$$tv$$tw$$tz$$ua$$ug$$uk$$us$$uy$$uz$$va$$vc$$ve$$vg$$vi$$vn$$vu$$wf$$ws$$xn--0zwm56d$$xn--11b5bs3a9aj6g$$xn--80akhbyknj4f$$xn--9t4b11yi5a$$xn--deba0ad$$xn--g6w251d$$xn--hgbk6aj7f53bba$$xn--hlcj6aya9esc7a$$xn--jxalpdlp$$xn--kgbechtv$$xn--zckzah$$ye$$yt$$yu$$za$$zm$$zw$$";
    private static final boolean DEBUG_ENABLED = false;
    private static final boolean VERBOSE = false;    
    
    private static Hashtable< WidgetAccess, Vector< WidgetFeature >> getTestAccessTable() throws Exception {
        Hashtable< WidgetAccess, Vector< WidgetFeature >> accessTable = new Hashtable< WidgetAccess, Vector< WidgetFeature >>();

        WidgetAccess localAccess = new WidgetAccess( "WidgetConfig.WIDGET_LOCAL_DOMAIN", true );
        Vector< WidgetFeature > localFeatures = new Vector< WidgetFeature >();

        localFeatures.add( new WidgetFeature( "blackberry.system", true, "1.0.0", null ) );
        localFeatures.add( new WidgetFeature( "blackberry.app", true, "1.0.0.0", null ) );

        accessTable.put( localAccess, localFeatures );

        WidgetAccess rimAccess = new WidgetAccess( "http://test-xp.rim.net", false );
        Vector< WidgetFeature > rimFeatures = new Vector< WidgetFeature >();

        rimFeatures.add( new WidgetFeature( "blackberry.ui.dialog", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.io.file", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.media.microphone", true, "1.0.0", null ) );
        rimFeatures.add( new WidgetFeature( "blackberry.system", true, "1.0.0", null ) );

        accessTable.put( rimAccess, rimFeatures );

        return accessTable;
    }
    
    private static void mockSession() throws Exception {
        // mock SessionManager which is used by WidgetConfig and WidgetAccess
        _context.checking( new Expectations() {
            {
                allowing( _session ).getTLD(); will( returnValue( TLD ) );
                allowing( _session ).debugMode(); will( returnValue( DEBUG_ENABLED ) );
                allowing( _session ).isVerbose(); will( returnValue( VERBOSE ) );
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
    }

    @Test
    public void testSerializeString() throws Exception {
        final String[] values = {
                "1.0.0.0",                      // version
                "MyApp",                        // id
                "My App",                       // name
                "This is a very powerful app!", // description
                "default.html",                 // content
                "config.xml",                   // config XML
                "exit",                         // back button behavior
                "",                             // content type
                "utf-8",                        // content charset
                "Apache 2.0",                   // license
                "http://www.apache.org",        // license URL
                "John O' Conner",               // author
                "john@helloworld.com",          // author email
                "http://john.helloworld.com",   // author url
                "Copyright (c) 2011 John O'Connor", // copyright
                "#000000",                      // loading screen color
                "images/backgroundImg.jpg",     // background image
                "images/foregroundImg.jpg",     // foreground image
                "background.html",              // background source
                "foreground.html"               // foreground source
        };
        int i = 0;
        
        _widgetConfig = new WidgetConfig();
        _widgetConfig.setVersion( values[i++] );
        _widgetConfig.setID( values[i++] );
        _widgetConfig.setName( values[i++] );
        _widgetConfig.setDescription( values[i++] );
        _widgetConfig.setContent( values[i++] );
        _widgetConfig.setConfigXML( values[i++] );
        _widgetConfig.setBackButtonBehaviour( values[i++] );
        _widgetConfig.setContentType( values[i++] );
        _widgetConfig.setContentCharSet( values[i++] );
        _widgetConfig.setLicense( values[i++] );
        _widgetConfig.setLicenseURL( values[i++] );
        _widgetConfig.setAuthor( values[i++] );        
        _widgetConfig.setAuthorEmail( values[i++] );
        _widgetConfig.setAuthorURL( values[i++] );
        _widgetConfig.setCopyright( values[i++] );        
        _widgetConfig.setLoadingScreenColour( values[i++] );
        _widgetConfig.setBackgroundImage( values[i++] );
        _widgetConfig.setForegroundImage( values[i++] );
        _widgetConfig.setBackgroundSource( values[i++] );        
        _widgetConfig.setForegroundSource( values[i++] );        

        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertNotNull( configJSON );

        for( i = 0; i < values.length; i++ ) {
            Assert.assertEquals( values[ i ],
                    configJSON.getString( WidgetConfig_v1Serializer.getStringPropKeys()[ i ] ) );
        }
    }
    
    @Test
    public void testSerializeBoolean() throws Exception {
        final boolean[] values = {
                true,   // allow multiple access
                true,   // on first launch
                true,   // on local page load
                true,   // on remote page load
                true,   // allow invoke params
                true,   // run on start-up
                DEBUG_ENABLED                
        };
        int i = 0;
        
        _widgetConfig = new WidgetConfig();
        _widgetConfig.setMultiAccess( values[i++] );
        _widgetConfig.setFirstPageLoad( values[i++] );
        _widgetConfig.setLocalPageLoad( values[i++] );
        _widgetConfig.setRemotePageLoad( values[i++] );
        _widgetConfig.setAllowInvokeParams( new Boolean( values[i++] ) );
        _widgetConfig.setStartup( new Boolean( values[i++] ) );
        
        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertNotNull( configJSON );

        for( i = 0; i < values.length; i++ ) {
            if( values[ i ] ) {
                Assert.assertEquals( values[ i ],
                        configJSON.getBoolean( WidgetConfig_v1Serializer.getBooleanPropKeys()[ i ] ) );
            }
        }
    }
    
    @Test
    public void testSerializeIcons() throws Exception {
        _widgetConfig = new WidgetConfig();
        _widgetConfig.addIcon( "images/icon.png" );

        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();

        Assert.assertEquals( "images/icon.png", configJSON.getString( "icon" ) );
    }
    
    @Test
    public void testSerializeHeaders() throws Exception {
        _widgetConfig = new WidgetConfig();
        _widgetConfig.addHeader( "webworks", "rim/webworks" );
        _widgetConfig.addHeader( "RIM-Widget", "rim/widget" );        

        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        JSONObject headers = configJSON.getJSONObject( "customHeaders" );
        Assert.assertNotNull( headers );
        
        Set< String > headerKeys = headers.keySet();

        for( String key : headerKeys ) {
            if( key.equals( "webworks" ) ) {
                Assert.assertEquals( "rim/webworks", headers.getString( key ) );
            } else if( key.equals( "RIM-Widget" ) ) {
                Assert.assertEquals( "rim/widget", headers.getString( key ) );
            } else {
                Assert.fail( "customHeaders contains unknown header: " + key );
            }
        }
    }
    
    @Test
    public void testSerializeNavMode() throws Exception {
        _widgetConfig = new WidgetConfig();
        _widgetConfig.setNavigationMode( true );

        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertEquals( "focus", configJSON.getString( "navigationMode" ) );
    }
    
    @Test
    public void testSerializeWhitelist() throws Exception {
        _widgetConfig = new WidgetConfig();
        _widgetConfig.setAccessTable( getTestAccessTable() );
        
        _serializer = new WidgetConfig_v1Serializer( _widgetConfig, null );
        _serializer.serialize();

        JSONObject configJSON = _serializer.getConfigJSONObject();
        Assert.assertNotNull( configJSON );

        JSONArray accessList = configJSON.getJSONArray( "accessList" );
        Assert.assertNotNull( accessList );

        int accessListSize = accessList.size();
        Assert.assertEquals( 2, accessListSize );

        Iterator< JSONObject > it = accessList.iterator();
        while( it.hasNext() ) {
            JSONObject access = it.next();
            String uri = access.getString( "uri" );
            JSONArray features = access.getJSONArray( "features" );
            boolean allowSubDomain = access.getBoolean( "allowSubDomain" );
            Iterator< JSONObject > featureIt = features.iterator();

            Assert.assertNotNull( features );

            if( uri.equals( "WIDGET_LOCAL" ) ) {
                Assert.assertEquals( 2, features.size() );
                Assert.assertEquals( true, allowSubDomain );

                while( featureIt.hasNext() ) {
                    JSONObject feature = featureIt.next();
                    String id = feature.getString( "id" );

                    if( id.equals( "blackberry.system" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.app" ) ) {
                        assertFeature( feature, id, true, "1.0.0.0" );
                    } else {
                        Assert.fail( "feautres contains unknown feature: " + id );
                    }
                }
            } else if( uri.equals( "http://test-xp.rim.net" ) ) {
                Assert.assertEquals( 4, features.size() );
                Assert.assertEquals( false, allowSubDomain );

                while( featureIt.hasNext() ) {
                    JSONObject feature = featureIt.next();
                    String id = feature.getString( "id" );

                    if( id.equals( "blackberry.system" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.ui.dialog" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.io.file" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else if( id.equals( "blackberry.media.microphone" ) ) {
                        assertFeature( feature, id, true, "1.0.0" );
                    } else {
                        Assert.fail( "feautres contains unknown feature: " + id );
                    }
                }
            } else {
                Assert.fail( "accessList contains unknown uri: " + uri );
            }
        }
    }
    
    private static void assertFeature( JSONObject feature, String id, boolean required, String version ) throws JSONException {
        Assert.assertEquals( required, feature.getBoolean( "required" ) );
        Assert.assertEquals( version, feature.getString( "version" ) );
        Assert.assertEquals( id, feature.getString( "id" ) );
    }

    @After
    public void tearDown() throws Exception {
        _widgetConfig = null;
        _serializer = null;
    }
}
