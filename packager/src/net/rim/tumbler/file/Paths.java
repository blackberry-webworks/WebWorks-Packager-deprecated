/*
 * Copyright 2012 Research In Motion Limited.
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

import net.rim.tumbler.session.SessionManager;

public class Paths {
    // source folder paths - temporary directory that resembles the BAR folder structure
    public static final File SOURCE_DIR = new File( SessionManager.getInstance().getSourceFolder() );
    public static final String CHROME_DIR_NAME = "chrome";
    public static final File CHROME_DIR = new File( SOURCE_DIR, CHROME_DIR_NAME );
    public static final File LIB_DIR = new File( CHROME_DIR, "lib" );
    public static final File EXT_DIR = new File( CHROME_DIR, "ext" );
    public static final File CONFIG_DIR = new File( LIB_DIR, "config" );
    public static final File USER_JS_FILE = new File( CONFIG_DIR, "user.js" );
    public static final File MODULES_JS_FILE = new File( CHROME_DIR, "frameworkModules.js" );
    
    // dependencies - these are subfolders under the dependencies folder
    public static final String BOOTSTRAP_DIR_NAME = "bootstrap";
    public static final String DEVICE_WWE_DIR_NAME = "device-wwe";
    public static final String SIMULATOR_WWE_DIR_NAME = "simulator-wwe";
    public static final String WW_EXECUTABLE_NAME = "wwe";
    public static final String NATIVE_PACKAGER_RELATIVE_PATH = "/tools/bin";
}
