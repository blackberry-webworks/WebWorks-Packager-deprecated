# Packager JUnit Test

1. Tests are written with JUnit 4
2. Some tests make use of jMock 2.5.1, you can get it from http://www.jmock.org/download.html, the JARS that are needed include:
	* hamcrest-core-1.1.jar
	* hamcrest-library-1.1.jar
	* jmock-2.5.1.jar
	* jmock-junit4-2.5.1.jar
	* cglib-nodep-2.1_3.jar
	* jmock-legacy-2.5.1.jar
	* objenesis-1.0.jar
3. In your Eclipse project, make sure the hamcrest*.jar are above the JUnit library, otherwise you might get SecurityException.