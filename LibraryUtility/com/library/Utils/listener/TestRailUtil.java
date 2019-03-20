package com.library.Utils.listener;

import java.lang.reflect.Method;

import com.library.Utils.testrail.QAAnnotations.TestCaseInfo;

public class TestRailUtil {
	
	public static TestCaseInfo getTestCaseInfoAnnotation(Class<? extends Object> clz, String methodName) {
		Method method = null;
		try {
			method = clz.getMethod(methodName);
		} catch (NoSuchMethodException e) {
			/* most likely, this method takes parameters.  Scan through all the methods and
			 * assume there is only one with that name (no overloads) and return that one.
			 */
			
			Method[] methods = clz.getMethods();
			int i = methods.length - 1;
			while (i > 0) {
				if (methodName.equals(methods[i].getName())) {
					method = methods[i];
					break;
				}
				i--;
			}
		}
		
		if (method != null) {
			return method.getAnnotation(TestCaseInfo.class);
		}
		
		return null;
	}
}
