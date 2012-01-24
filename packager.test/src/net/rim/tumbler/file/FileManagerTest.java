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

package net.rim.tumbler.file;

import java.io.File;
import java.io.IOException;
import java.util.List;

import junit.framework.Assert;
import net.rim.tumbler.WidgetPackager;
import net.rim.tumbler.WidgetPackager.Target;
import net.rim.tumbler.config.WidgetConfig;
import net.rim.tumbler.session.BBWPProperties;
import net.rim.tumbler.util.SessionMocker;
import net.rim.tumbler.util.WidgetConfigMocker;

import org.junit.BeforeClass;
import org.junit.Test;

public class FileManagerTest {
    private static WidgetConfig _config;
    private static BBWPProperties _bbwpProperties;
    
    private static final String SOURCE_DIR = "source";

    @BeforeClass
    public static void runBeforeClass() throws Exception {
        SessionMocker.mockSession( "../packager.test/src/bbxwebworks/bin" );
        _config = WidgetConfigMocker.getInstance().getConfig();        
        _bbwpProperties = new BBWPProperties( "../packager.test/src/bbxwebworks/bin/bbwp.properties", SessionMocker.getSessionHome() );
    }

    @Test
    public void testPrepare() throws Exception {
        FileManager fileMgr = new FileManager( _config, _bbwpProperties );
        fileMgr.prepare();

        Assert.assertTrue( new File( SOURCE_DIR + "/chrome/lib" ).exists() );
        Assert.assertTrue( new File( SOURCE_DIR + "/icon.png" ).exists() );

        List< String > files = fileMgr.getFiles();
        Assert.assertNotNull( files );
        Assert.assertTrue( files.contains( new File( SOURCE_DIR + "/icon.png" ).getAbsolutePath() ) );
    }

    @Test
    public void testCopyWWExecutable() throws IOException {
        FileManager fileManager = new FileManager( _config, _bbwpProperties );
        fileManager.copyWWExecutable( Target.SIMULATOR );
        Assert.assertTrue( new File( SOURCE_DIR + "/" + WidgetPackager.WW_EXECUTABLE_NAME ).exists() );
    }
}
