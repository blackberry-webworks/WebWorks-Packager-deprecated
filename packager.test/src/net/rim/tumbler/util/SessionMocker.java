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

package net.rim.tumbler.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;

import net.rim.tumbler.session.SessionManager;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

public class SessionMocker {
    private static Mockery _context = new JUnit4Mockery() {
        {
            setImposteriser( ClassImposteriser.INSTANCE );
        }
    };    
    private static final SessionManager _session = _context.mock( SessionManager.class );

    private static final String TLD = "$$ac$$ad$$ae$$aero$$af$$ag$$ai$$al$$am$$an$$ao$$aq$$ar$$arpa$$as$$asia$$at$$au$$aw$$ax$$az$$ba$$bb$$bd$$be$$bf$$bg$$bh$$bi$$biz$$bj$$bm$$bn$$bo$$br$$bs$$bt$$bv$$bw$$by$$bz$$ca$$cat$$cc$$cd$$cf$$cg$$ch$$ci$$ck$$cl$$cm$$cn$$co$$com$$coop$$cr$$cu$$cv$$cx$$cy$$cz$$de$$dj$$dk$$dm$$do$$dz$$ec$$edu$$ee$$eg$$er$$es$$et$$eu$$fi$$fj$$fk$$fm$$fo$$fr$$ga$$gb$$gd$$ge$$gf$$gg$$gh$$gi$$gl$$gm$$gn$$gov$$gp$$gq$$gr$$gs$$gt$$gu$$gw$$gy$$hk$$hm$$hn$$hr$$ht$$hu$$id$$ie$$il$$im$$in$$info$$int$$io$$iq$$ir$$is$$it$$je$$jm$$jo$$jobs$$jp$$ke$$kg$$kh$$ki$$km$$kn$$kp$$kr$$kw$$ky$$kz$$la$$lb$$lc$$li$$lk$$lr$$ls$$lt$$lu$$lv$$ly$$ma$$mc$$md$$me$$mg$$mh$$mil$$mk$$ml$$mm$$mn$$mo$$mobi$$mp$$mq$$mr$$ms$$mt$$mu$$museum$$mv$$mw$$mx$$my$$mz$$na$$name$$nc$$ne$$net$$nf$$ng$$ni$$nl$$no$$np$$nr$$nu$$nz$$om$$org$$pa$$pe$$pf$$pg$$ph$$pk$$pl$$pm$$pn$$pr$$pro$$ps$$pt$$pw$$py$$qa$$re$$ro$$rs$$ru$$rw$$sa$$sb$$sc$$sd$$se$$sg$$sh$$si$$sj$$sk$$sl$$sm$$sn$$so$$sr$$st$$su$$sv$$sy$$sz$$tc$$td$$tel$$tf$$tg$$th$$tj$$tk$$tl$$tm$$tn$$to$$tp$$tr$$travel$$tt$$tv$$tw$$tz$$ua$$ug$$uk$$us$$uy$$uz$$va$$vc$$ve$$vg$$vi$$vn$$vu$$wf$$ws$$xn--0zwm56d$$xn--11b5bs3a9aj6g$$xn--80akhbyknj4f$$xn--9t4b11yi5a$$xn--deba0ad$$xn--g6w251d$$xn--hgbk6aj7f53bba$$xn--hlcj6aya9esc7a$$xn--jxalpdlp$$xn--kgbechtv$$xn--zckzah$$ye$$yt$$yu$$za$$zm$$zw$$";
    private static final boolean DEBUG_ENABLED = false;
    private static final boolean VERBOSE = false;
    private static final String OUTPUT_DIR = "out";
    private static final String SOURCE_DIR = "source";    

    public static SessionManager mockSession( final String bbwpJarFolder ) throws Exception {
        _context.checking( new Expectations() {
            {
                allowing( _session ).getTLD(); will( returnValue( TLD ) );                
                allowing( _session ).debugMode(); will( returnValue( DEBUG_ENABLED ) );
                allowing( _session ).isVerbose(); will( returnValue( VERBOSE ) );
                allowing( _session ).getOutputFolder(); will( returnValue( OUTPUT_DIR ) );
                allowing( _session ).getSourceFolder(); will( returnValue( SOURCE_DIR ) );
                allowing( _session ).getBBWPJarFolder(); will( returnValue( bbwpJarFolder ) );
                allowing( _session ).getWidgetArchive(); will( returnValue( "../packager.test/src/bbxwebworks/apps/bbm.zip" ) );
                allowing( _session ).getSessionHome(); will( returnValue( getSessionHome() ) );
            }
        } );

        Class< ? > c = SessionManager.class;
        Field singleton = c.getDeclaredField( "_instance" );
        singleton.setAccessible( true );
        singleton.set( null, _session );
        
        return _session;
    }
    
    public static String getSessionHome() throws IOException, URISyntaxException {
        return new File( SessionMocker.class.getProtectionDomain().getCodeSource().getLocation().toURI() ).getCanonicalPath();
    }
}
