package com.library.Utils.listener;

import java.io.IOException;

import org.testng.ISuite;

import com.library.Utils.core.SuiteRunProperties;

public class AdaptiveDataCollectionUtil  {
	private static final String ATTR_TESTRUN = "adaptive.testsuite.props";
	public static SuiteRunProperties initialize(ISuite suite) {
		SuiteRunProperties props = getSuiteProps(suite);
		if (null == props) {
			props = new SuiteRunProperties(suite, null);
			try {
				props.save();
			} catch (IOException e) {
				e.printStackTrace();
			}
			suite.setAttribute(ATTR_TESTRUN, props);
		}
		return props;
	}

	public static SuiteRunProperties getSuiteProps(ISuite suite) {
		return (SuiteRunProperties) suite.getAttribute(ATTR_TESTRUN);
	}


	public static String getSuiteString(ISuite suite, String key) {
		return getSuiteString(suite, key, null);
	}

	public static String getSuiteString(ISuite suite, String key, String dflt) {
		String value = (String) suite.getAttribute(key);

		if (value == null) {
			SuiteRunProperties suiteProps = getSuiteProps(suite);
			value = suiteProps.getProperty(key);
			if (value != null) {
				suite.setAttribute("UpdateTestRails", value);
			}
		}

		return value == null ? dflt : value ;
	}
}
