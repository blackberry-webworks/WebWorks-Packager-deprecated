/**
 * 
 */
package net.rim.tumbler.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Utilities class to make reflection calls
 * @author jkeshavarzi
 *
 */
public class ReflectionUtils {
	public static void callPrivateMethod( Object myObject, String methodName, Class paramsTypes[], Object paramValues[] ){
    	Method method = null;
		
    	try {
			method = myObject.getClass().getDeclaredMethod(methodName, paramsTypes);
			method.setAccessible(true);
			method.invoke(myObject, paramValues);
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
	
	public static void setPrivateStaticField(Class myClass, String fieldName, Object value){
    	try {
    		Field field = myClass.getDeclaredField( fieldName );
        	field.setAccessible( true );
			field.set( null, value );
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }
	
	public static Object getPrivateField(Object myObject, String fieldName){
    	try {
    		Field field = myObject.getClass().getDeclaredField( fieldName );
    		field.setAccessible( true );
        	return field.get(myObject);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
		return null; 
    }
}
