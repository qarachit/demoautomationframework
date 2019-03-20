package com.library.Utils.listener;

import static com.library.Utils.listener.AdaptiveDataCollectionUtil.getSuiteString;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;

import com.library.Utils.core.SuiteRunProperties;
import com.library.Utils.testrail.TestRailHandler;
import com.library.Utils.testrail.QAAnnotations.TestCaseInfo;

public class AdaptiveTestRailListener 
implements ITestListener, ISuiteListener {
	private static final String PROP_TESTRAIL_USER = "define_a_prop_for_user";
	private static final String PROP_TESTRAIL_PASSWORD = "define_a_prop_for_password";
	private static final String PROP_TESTRAIL_URL = "define_a_prop_for_url";

	private static final String PROP_PROJECTNAME = "env.TestrailProjectName";
	private static final String PROP_TESTRUNNAME = "env.TestrailRunName";
	private static final String PROP_RUNID = "env.RunId";
	private static final String PROP_UPDATETESTRAIL = "env.UpdateTestRailResults";
	private static final String PROP_DEBUG = "env.TestRailDebug";

	private static final String DEFAULT_TESTRAIL_USER = "vgupta@360logica.com";
	private static final String DEFAULT_TESTRAIL_PASSWORD = "123-asd";
	private static final String DEFAULT_TESTRAIL_URL = "https://demovipin.testrail.net";

	private static final int TEST_RAIL_STATUS_SUCCESS = 1;
	private static final int TEST_RAIL_STATUS_SKIP = 2;
	private static final int TEST_RAIL_STATUS_FAILURE = 5;
	
	private TestRailHandler th = null;
	private String runid;
	private boolean debug;


	public boolean isTestRailToBeUpdated(ISuite suite) {

		Boolean isTestRailToBeUpdated = (Boolean) suite.getAttribute("UpdateTestRails");

		if (isTestRailToBeUpdated == null) {
			SuiteRunProperties suiteProps = AdaptiveDataCollectionUtil.getSuiteProps(suite);
			String s = suiteProps.getProperty(PROP_UPDATETESTRAIL);
			isTestRailToBeUpdated = (s != null) && ("true".equals(s) || "True".equals(s));
			suite.setAttribute("UpdateTestRails", isTestRailToBeUpdated);
		}
		
		return isTestRailToBeUpdated;
	}
	
	@Override
	public void onStart(ISuite suite) {
		AdaptiveDataCollectionUtil.initialize(suite);
		
		debug = "true".equals(getSuiteString(suite, PROP_DEBUG, "false"));
	
		// debug = true;
		if (debug || isTestRailToBeUpdated(suite)) {
			try {
				String user = getSuiteString(suite, PROP_TESTRAIL_USER, DEFAULT_TESTRAIL_USER);
				String password = getSuiteString(suite, PROP_TESTRAIL_PASSWORD, DEFAULT_TESTRAIL_PASSWORD);
				String url = getSuiteString(suite, PROP_TESTRAIL_URL, DEFAULT_TESTRAIL_URL);
				
				String projectName = getSuiteString(suite, PROP_PROJECTNAME);
				String testRunName = getSuiteString(suite, PROP_TESTRUNNAME);
				runid = getSuiteString(suite, PROP_RUNID);
				
				th = new TestRailHandler(user, password, url);
				
 				//runid = getRunId(projectName, testRunName);
				
				System.err.println(String.format("TestRail listener initialied: %s(%s) - %s", user, password, url));
				System.err.println(String.format("Project: %s Run: %s(%s)", projectName, testRunName, runid));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Long getRunId(String projectName, String testRunName) throws Exception {
	 if (debug) {
		int debugRunId = 42;
		System.err.println(String.format("DEBUG: getRunId(%s, %s) returning %d",
				 projectName, testRunName, debugRunId));
		return new Long(debugRunId);
	 }
	 return th.getRunIdByTestRunName(projectName, testRunName);
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		updateResultInTestRail(result,TEST_RAIL_STATUS_SUCCESS);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		updateResultInTestRail(result, TEST_RAIL_STATUS_SKIP);
	}


	@Override
	public void onTestFailure(ITestResult result) {
		System.out.println("Failed.............................");
		updateResultInTestRail(result, TEST_RAIL_STATUS_FAILURE);
	}
	

	public void updateResultInTestRail(ITestResult result, int testRailStatus) {
		if (th != null) {
			try {
				ITestNGMethod method = result.getMethod();
				TestCaseInfo info = TestRailUtil.getTestCaseInfoAnnotation(method.getRealClass(), method.getMethodName());
				if (info != null) {
					System.out.println(info);
					String caseID = cleanup(info.testCaseID());
					System.out.println(caseID);
					updateResultToTestRail(caseID, testRailStatus);
				} else {
					updateResultToTestRail(
							result.getTestClass().getName(), 
							result.getMethod().getMethodName(),
							testRailStatus);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String cleanup(String testCaseID) {
		if (testCaseID == null) return null; // should never happen.  required in annotation.
		
		if (testCaseID.isEmpty()) {
			System.err.println("Empty testCaseID encountered");
			return "";
		}
		
		StringBuffer b = new StringBuffer(testCaseID.length());
		for (int i = 0; i < testCaseID.length(); i++) {
			char c = testCaseID.charAt(i);
			if (Character.isDigit(c)) {
				b.append(c);
			}
		}
		
		if (b.length() != testCaseID.length()) {
			System.err.println("'" + testCaseID + "' converted to '" + b.toString() + "'");
		}
		
		return b.toString();
	}

	private void updateResultToTestRail(String name, String methodName, int testRailStatus) {
		if (debug) {
			System.err.println(String.format("updateResultToTestRail(%s, %s, %s) - by class/method name",
					name, methodName, testRailStatus)
			);
			return;
		}
		//th.updateResultToTestRail(testRailStatus, caseID, runid);
	}

	public void updateResultToTestRail(String caseID, int testRailStatus) throws Exception {
		if (debug) {
			System.err.println(String.format("updateResultToTestRail(%s, %s, %s)",
					testRailStatus, caseID, runid)
			);
			return;
		}
		th.updateResultToTestRail(testRailStatus, caseID, runid);
	}

	@Override
	public void onTestStart(ITestResult arg0) {
	}
	
	@Override
	public void onFinish(ITestContext arg0) {
	}

	@Override
	public void onStart(ITestContext arg0) {
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult arg0) {
	}

	@Override
	public void onFinish(ISuite suite) {
	}

}
