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
package net.rim.tumbler;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.exception.PackageException;
import net.rim.tumbler.session.SessionManager;
import net.rim.tumbler.util.ReflectionUtils;
import net.rim.tumbler.xml.ConfigXMLParser;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JUnit4Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * JUnit tests for the ConfigXMLParser (UNFINISHED). This test uses the test-app.zip file located under /src.
 * @author jkeshavarzi
 */
public class ConfigXMLParserTest {
	private final String TLD = "$$ac$$ad$$ae$$aero$$af$$ag$$ai$$al$$am$$an$$ao$$aq$$ar$$arpa$$as$$asia$$at$$au$$aw$$ax$$az$$ba$$bb$$bd$$be$$bf$$bg$$bh$$bi$$biz$$bj$$bm$$bn$$bo$$br$$bs$$bt$$bv$$bw$$by$$bz$$ca$$cat$$cc$$cd$$cf$$cg$$ch$$ci$$ck$$cl$$cm$$cn$$co$$com$$coop$$cr$$cu$$cv$$cx$$cy$$cz$$de$$dj$$dk$$dm$$do$$dz$$ec$$edu$$ee$$eg$$er$$es$$et$$eu$$fi$$fj$$fk$$fm$$fo$$fr$$ga$$gb$$gd$$ge$$gf$$gg$$gh$$gi$$gl$$gm$$gn$$gov$$gp$$gq$$gr$$gs$$gt$$gu$$gw$$gy$$hk$$hm$$hn$$hr$$ht$$hu$$id$$ie$$il$$im$$in$$info$$int$$io$$iq$$ir$$is$$it$$je$$jm$$jo$$jobs$$jp$$ke$$kg$$kh$$ki$$km$$kn$$kp$$kr$$kw$$ky$$kz$$la$$lb$$lc$$li$$lk$$lr$$ls$$lt$$lu$$lv$$ly$$ma$$mc$$md$$me$$mg$$mh$$mil$$mk$$ml$$mm$$mn$$mo$$mobi$$mp$$mq$$mr$$ms$$mt$$mu$$museum$$mv$$mw$$mx$$my$$mz$$na$$name$$nc$$ne$$net$$nf$$ng$$ni$$nl$$no$$np$$nr$$nu$$nz$$om$$org$$pa$$pe$$pf$$pg$$ph$$pk$$pl$$pm$$pn$$pr$$pro$$ps$$pt$$pw$$py$$qa$$re$$ro$$rs$$ru$$rw$$sa$$sb$$sc$$sd$$se$$sg$$sh$$si$$sj$$sk$$sl$$sm$$sn$$so$$sr$$st$$su$$sv$$sy$$sz$$tc$$td$$tel$$tf$$tg$$th$$tj$$tk$$tl$$tm$$tn$$to$$tp$$tr$$travel$$tt$$tv$$tw$$tz$$ua$$ug$$uk$$us$$uy$$uz$$va$$vc$$ve$$vg$$vi$$vn$$vu$$wf$$ws$$xn--0zwm56d$$xn--11b5bs3a9aj6g$$xn--80akhbyknj4f$$xn--9t4b11yi5a$$xn--deba0ad$$xn--g6w251d$$xn--hgbk6aj7f53bba$$xn--hlcj6aya9esc7a$$xn--jxalpdlp$$xn--kgbechtv$$xn--zckzah$$ye$$yt$$yu$$za$$zm$$zw$$";
	private final String ARCHIVE_LOCATION = "src/test-app.zip";
	
	private WidgetArchive _widgetArchive;
	private WidgetConfig _config;
	private Mockery _context;
	private Node _widgetNode;
	private ConfigXMLParser _configParser;
	
	@Before
	public void prepare() throws Exception {
		//----------------------SessionManager Mocking----------------------------------------------
		_context = new JUnit4Mockery() {{setImposteriser( ClassImposteriser.INSTANCE );}};
		
		// mock SessionManager which is used by WidgetAccess for getting top-level domains
        final SessionManager session = _context.mock( SessionManager.class );
        _context.checking( new Expectations() {
            {
            	allowing( session ).debugMode(); will( returnValue( false ) );
            	allowing( session ).isVerbose(); will( returnValue( false ) );
            	allowing( session ).getTLD(); will( returnValue( TLD ) );
            }
        } );
        ReflectionUtils.setPrivateStaticField(SessionManager.class, "_instance", session);
        //-------------------------------------------------------------------------------------------
		        
		//Create WidgetArchive that points to testing zip
		_widgetArchive = new WidgetArchive(ARCHIVE_LOCATION);
    	_widgetArchive.validate();
		
    	//Instantiate ConfigXMLParser
		_configParser = new ConfigXMLParser();
    	_config = (WidgetConfig) ReflectionUtils.getPrivateField(_configParser, "_widgetConfig");
    	
    	//------------Parse XML-----------------------------------------------------------------
		try {
            // create DOM
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();
            //   builder.setErrorHandler( new MyErrorHandler() );
            Document doc = builder.parse( new ByteArrayInputStream( _widgetArchive.getConfigXML() ) );
            doc.getDocumentElement().normalize();
            
            // widget
            _widgetNode = (Node) doc.getElementsByTagName( "widget" ).item( 0 );

        } catch( SAXException saxEx ) {
            throw new PackageException( "EXCEPTION_CONFIGXML_BADXML", saxEx );
        }
    }
	
	@Test
	public void testProcessWidgetNode(){
		ReflectionUtils.callPrivateMethod(_configParser, "processWidgetNode", new Class[]{Node.class}, new Object[]{_widgetNode});
		Assert.assertEquals("2.0.0.0", _config.getVersion());
		Assert.assertEquals(null, _config.getID());
		Assert.assertEquals(0, _config.getCustomHeaders().size());
	}
	
	@Test
	public void testProcessContentNode(){
		Node contentNode = getChildNode(_widgetNode, "content");
		ReflectionUtils.callPrivateMethod(_configParser, "processContentNode", new Class[]{Node.class}, new Object[]{contentNode});
		Assert.assertEquals("index.html", _config.getContent());
		Assert.assertEquals("index.html", _config.getForegroundSource());
	}
	
	private Node getChildNode(Node node, String name){
		NodeList children = node.getChildNodes();
		Node child;
		
		for (int i = 0; i < children.getLength(); i++){
			child = children.item(i);
			
			if (child.getNodeName().equals(name)){
				return child;
			}
		}
		
		return null;
	}

    @After
    public void tearDown() throws Exception {
    	_widgetArchive = null;
    	_config = null;
    	_context = null;
    	_widgetNode = null;
    	_configParser = null;
    }
}
