package com.library.Utils.testrail;

public class PushToTestRail {
	
	public static void main(String[] args) throws Exception
	{	
		TestRailHandler testRailHandler = new TestRailHandler("jhuang@adaptiveinsights.com","Welcome1234!!","https://adaptiveqa.testrail.net/");

		//testRailHandler.pushTestInfoToTestRailByTestClass("com.discoverylite.tests.Sample_RegressionTests_TestRail");

		//testRailHandler.getProjectIDByProjectName("test");
		
		testRailHandler.getResultsForCase("69","16726");
		//testRailHandler.getTestCaseIDs("Discovery and Integration","Discovery Lite", "Test Cases");
	}
}
