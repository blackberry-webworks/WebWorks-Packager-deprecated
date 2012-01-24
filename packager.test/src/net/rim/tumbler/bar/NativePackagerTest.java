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

package net.rim.tumbler.bar;

import net.rim.tumbler.OSUtils;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.file.FileManager;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.util.SessionMocker;
import net.rim.tumbler.util.WidgetConfigMocker;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class NativePackagerTest {
    private static BBWPProperties _bbwpProperties;
    private static WidgetConfig _config;
    private static String toolsDir = OSUtils.isMac() ? "../target/dependency/bin" : "C:\\testrun\\qnxtools copy\\bin";

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        SessionMocker.mockSession( toolsDir );
        _config = WidgetConfigMocker.getInstance().getConfig();
        _bbwpProperties = new BBWPProperties( "../packager.test/src/bbxwebworks/bin/bbwp.properties", SessionMocker.getSessionHome() );
    }

    @Test
    public void testRun() throws Exception {
        try {
            FileManager fileMgr = new FileManager( _config, _bbwpProperties );
            fileMgr.prepare();
            new NativePackager( _config, fileMgr.getFiles() ).run();
        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Exception caught in NativePackager.run()" );
        }
    }
}
