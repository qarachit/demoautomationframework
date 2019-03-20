package com.library.Utils.listener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.testng.IInvokedMethod;
import org.testng.IReporter;
import org.testng.IResultMap;
import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ISuiteResult;
import org.testng.ITestClass;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.xml.XmlSuite;

import com.library.Utils.core.SuiteRunProperties;
import com.library.Utils.testrail.QAAnnotations.TestCaseInfo;

/**
 * @author 
 */
public class AdaptiveReportListener implements IReporter, ISuiteListener {
	private static final int FIRST_LOWER = -1;
	private static final int FIRST_HIGHER = 1;
	private static final int EQUAL = 0;

	private static final String ATTR_CHRON_ORDER = "chronOrder";

	private static final int TEST_TABLE_COLS = 5;

	private static final boolean DEBUG = false;
	private static final String JIRA_ISSUE_URL = "https://adaptiveinsights.atlassian.net/browse/";

	private String reportTitle = "Execution Report";
	private String reportFileName = "selenium-automation-report.html";

	private List<IInvokedMethod> orderedInvokedMethods;

	private Css css = new Css();

	public class FailCategories {
		public int automationBug;
		public int failure;
		public int appFailure;
	}

	@Override
	public void onStart(ISuite suite) {
		AdaptiveDataCollectionUtil.initialize(suite);
	}

	@Override
	public void onFinish(ISuite suite) {
	}

	/** Creates summary of the run */
	@Override
	public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites, String outdir) {
		try (PrintStream writer = createWriter(outdir)) {
			PrintStream w = DEBUG ? System.err : writer;
			startHtml(w, suites);
			for (ISuite suite : suites) {
				markInvokedMethods(suite);
				generateTitle(w, reportTitle, suite);
				generateCounts(w, suite);
				generateSummary(w, suite);
				generateDetail(w, suite);
			}
			endHtml(w);
		} catch (IOException e) {
			System.err.println("Unable to create output file");
			e.printStackTrace();
		}
	}

	/***
	 * 
	 * Set an chronological order attribute on the invoked methods.
	 * 
	 * @param suite
	 */
	private void markInvokedMethods(ISuite suite) {
		orderedInvokedMethods = new ArrayList<IInvokedMethod>(suite.getAllInvokedMethods());
		Collections.sort(orderedInvokedMethods, InvokedMethodsComparator.getSorter());
		int i = 0;
		for (IInvokedMethod im : orderedInvokedMethods) {
			im.getTestResult().setAttribute(ATTR_CHRON_ORDER, i++);
		}
	}

	/***
	 * 
	 * Write an index if there is more than one suite (usually isn't).
	 * 
	 * @param writer
	 * @param suites
	 */
	private void generateSuitesIndex(PrintStream writer, List<ISuite> suites) {
		StringBuilder builder = new StringBuilder();
		if (suites.size() == 1) {
			builder.append("<!-- Only one suite: ").append(suites.get(0).getName()).append(" -->");
		} else {

			builder.append("<ul class=\"").append(Css.CLASS_SUITE_LIST).append("\">\n");
			for (ISuite suite : suites) {
				builder.append("\n\t<li><a href=\"#").append(linkToSuite(suite)).append("\">").append(suite.getName())
				.append("</li>\n");
			}
			builder.append("</ul>");
		}
		writer.println(builder.toString());
	}

	private String linkToSuite(ISuite suite) {
		return "s_" + linkable(suite.getName());
	}

	private String linkable(String name) {
		return name;
	}

	protected PrintStream createWriter(String outdir) throws IOException {
		new File(outdir).mkdirs();
		return new PrintStream(new FileOutputStream(new File(outdir, reportFileName)));
	}

	/**
	 * Creates a table showing the highlights of each test method with links to
	 * the method details
	 * 
	 * @param writer
	 *            TODO
	 */
	protected void generateSummary(PrintStream writer, ISuite suite) {
		startResultSummaryTable(writer, Css.CLASS_TEST_SUMMARY);
		for (ISuiteResult result : suite.getResults().values()) {
			ITestContext testContext = result.getTestContext();
			resultSummary(writer, testContext, Css.CLASS_FAILED, true, testContext.getFailedConfigurations());
			resultSummary(writer, testContext, Css.CLASS_FAILED, false, testContext.getFailedTests());
			resultSummary(writer, testContext, Css.CLASS_SKIPPED, true, testContext.getSkippedConfigurations());
			resultSummary(writer, testContext, Css.CLASS_SKIPPED, false, testContext.getSkippedTests());
			resultSummary(writer, testContext, Css.CLASS_PASSED, false, testContext.getPassedTests());
		}
		writer.println("</table>");
	}

	/**
	 * Creates a section showing known results for each method
	 * 
	 * @param writer
	 *            TODO
	 */
	protected void generateDetail(PrintStream writer, ISuite suite) {
		writer.println(String.format("\n<div class=\"%s\">", Css.CLASS_DETAIL));

		for (ISuiteResult suiteResult : suite.getResults().values()) {
			ITestContext testContext = suiteResult.getTestContext();
			writer.println("<h1>" + testContext.getName() + "</h1>");
			resultDetail(writer, Css.CLASS_FAILED, true, testContext.getFailedConfigurations());
			resultDetail(writer, Css.CLASS_FAILED, false, testContext.getFailedTests());
			resultDetail(writer, Css.CLASS_SKIPPED, true, testContext.getSkippedConfigurations());
			resultDetail(writer, Css.CLASS_SKIPPED, false, testContext.getSkippedTests());
			resultDetail(writer, Css.CLASS_PASSED, false, testContext.getPassedTests());

		}
		writer.println("</div>");
	}

	/**
	 * @param testContext
	 *            TODO
	 * @param config
	 *            TODO
	 * @param tests
	 */
	private void resultSummary(PrintStream writer, ITestContext testContext, String style, boolean config,
			IResultMap tests) {

		String testname = testContext.getName();
		if (tests.getAllResults().size() > 0) {
			StringBuffer buff = new StringBuffer();
			String currentClassName = "";
			int resultCount = 0;
			int classCount = 0;

			List<ITestResult> sortedResults = new ArrayList<ITestResult>(tests.getAllResults());
			Collections.sort(sortedResults, new ResultSorter());

			for (ITestResult testResult : sortedResults) {
				ITestNGMethod method = testResult.getMethod();
				ITestClass testClass = method.getTestClass();
				String className = testClass.getName();
				if (classCount == 0) {
					titleRow(writer, testname, style, config);
				}
				long end = Long.MIN_VALUE;
				long start = Long.MAX_VALUE;
				long startMS = 0;
				String firstLine = "";

				if (!className.equalsIgnoreCase(currentClassName)) {
					classCount++;
					// new class, need write out lastClass if there was one.
					if (resultCount > 0) {
						outputRow(writer, currentClassName, style, classCount, resultCount, buff);
					}
					resultCount = 0;
					buff.setLength(0);
					currentClassName = className;
				}

				if (testResult.getEndMillis() > end) {
					end = testResult.getEndMillis() / 1000;
				}
				if (testResult.getStartMillis() < start) {
					startMS = testResult.getStartMillis();
					start = startMS / 1000;
				}

				Throwable exception = testResult.getThrowable();

				if (null != exception) {
					try (Scanner scanner = new Scanner(getStackTrace(exception))) {
						firstLine = scanner.nextLine();
					}
				}

				DateFormat formatter = new SimpleDateFormat("hh:mm:ss a");
				Calendar calendar = Calendar.getInstance();
				calendar.setTimeInMillis(startMS);

				if (resultCount > 0) {
					buff.append("\n<tr class=\"").append(style).append(((resultCount % 2) == 0) ? " even" : " odd")
					.append("\">");
				}
				String description = method.getDescription();
				String testInstanceName = testResult.getTestName();

				// method name/description column start - mark if AUTOBUG fixed
				// (maybe)

				TestCaseInfo info = null;
				String bug = null;
				boolean isAutomation = false;
				try {
					info = getTestCaseInfo(method);
					if (info != null) {
						isAutomation = info.isAutomationBug();
						bug = info.bug();
						if ((bug != null) && bug.isEmpty()) {
							bug = null;
						}
					}
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}

				buff.append("\n\t<td");
				if (isAutomation) {
					buff.append(" title=\"Marked as automation bug\");");
				}

				if (isAutomation || (bug != null)) {
					buff.append(" class=\"").append(isAutomation ? Css.CLASS_AUTOBUG : Css.CLASS_BUG).append('\"');
				}

				buff.append(">\n");

				// method name link with optional description
				buff.append("<a href=\"#m_").append(method.getMethodName()).append("\">").append(qualifiedName(method))
				.append(' ');

				if (bug != null) {
					buff.append(" (<a target=\"_blank\" href=\"").append(JIRA_ISSUE_URL).append(bug)
					.append("\">").append(bug).append("</a>)");
				}

				if (description != null && !description.isEmpty()) {
					buff.append('(').append(description).append(')');
				}

				buff.append("</a>");

				// test instance name
				if (testInstanceName != null) {
					buff.append("<br>(").append(testInstanceName + ")");
				}

				buff.append("</td>");

				// first line column
				buff.append(
						String.format("\n\t<td class=\"%s\" style=text-align:left;padding-right:2em>", Css.CLASS_NUMI))
				.append(firstLine).append("<br/></td>");

				// time run column
				buff.append("\n\t<td style=text-align:right>").append(formatter.format(calendar.getTime()))
				.append("</td>");

				// elapsed time column
				buff.append("\n\t<td class=\"").append(Css.CLASS_NUMI).append("\">").append(timeConversion(end - start))
				.append("</td>");

				// end of row
				buff.append("\n</tr>");
				resultCount++;
			}
			if (resultCount > 0) {
				outputRow(writer, currentClassName, style, classCount, resultCount, buff);
			}
		}
	}

	public String getStackTrace(Throwable exception) {
		@SuppressWarnings("deprecation")
		String str = Utils.stackTrace(exception, true)[0];
		int i = str.indexOf("at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method");
		int j = -1;
		int k = 500;
		if (i > -1) {
			j = str.indexOf("at org.testng.internal.MethodInvocationHelper.invokeMethod");
			if (j > -1) {
				k = j - i;
			}
		}
		return (k > 325) ? str : str.substring(0, i);
	}

	private void outputRow(PrintStream writer, String className, String style, int classCount, int resultCount,
			StringBuffer buff) {
		StringBuffer rowStrt = new StringBuffer();

		// tr element
		rowStrt.append("\n<tr").append(" class=\"").append(style).append(((resultCount % 2) == 0) ? " even" : " odd")
		.append("\">");

		// first column
		rowStrt.append("\n\t<td");
		if (resultCount > 1) {
			rowStrt.append(" rowspan=").append(resultCount);
		}
		rowStrt.append('>').append(className).append("</td>");

		writer.println(rowStrt.append(buff));
	}

	private TestCaseInfo getTestCaseInfo(ITestNGMethod method) throws NoSuchMethodException {
		TestCaseInfo info = TestRailUtil.getTestCaseInfoAnnotation(method.getConstructorOrMethod().getDeclaringClass(), method.getMethodName());
		return info;
	}

	private String timeConversion(long seconds) {

		final int MINUTES_IN_AN_HOUR = 60;
		final int SECONDS_IN_A_MINUTE = 60;

		int minutes = (int) (seconds / SECONDS_IN_A_MINUTE);
		seconds -= minutes * SECONDS_IN_A_MINUTE;

		int hours = minutes / MINUTES_IN_AN_HOUR;
		minutes -= hours * MINUTES_IN_AN_HOUR;

		return prefixZeroToDigit(hours) + ":" + prefixZeroToDigit(minutes) + ":" + prefixZeroToDigit((int) seconds);
	}

	private String prefixZeroToDigit(int num) {
		int number = num;
		if (number <= 9) {
			String sNumber = "0" + number;
			return sNumber;
		} else
			return "" + number;

	}

	/** Starts and defines columns result summary table */
	private void startResultSummaryTable(PrintStream writer, String style) {
		tableStart(writer, style, "summary");
		writer.println("\n<tr>\n\t<th>Test Class</th>"
				+ "\n\t<th>Test Cases/Method Name</th>\n\t<th>Exception Information</th>\n\t<th>Start Time<br/>(hh:mm:ss)</th>\n\t<th>Execution Time<br/>(hh:mm:ss)</th>\n</tr>");
	}

	private String qualifiedName(ITestNGMethod method) {
		StringBuilder addon = new StringBuilder();
		String[] groups = method.getGroups();
		int length = groups.length;
		if (length > 0 && !"basic".equalsIgnoreCase(groups[0])) {
			addon.append("(");
			for (int i = 0; i < length; i++) {
				if (i > 0) {
					addon.append(", ");
				}
				addon.append(groups[i]);
			}
			addon.append(")");
		}

		return "<b>" + method.getMethodName() + "</b> " + addon;
	}

	private void resultDetail(PrintStream writer, String style, boolean config, IResultMap tests) {
		writer.println(String.format("<div class=\"%s\">", style));
		ArrayList<ITestResult> results = new ArrayList<ITestResult>(tests.getAllResults());
		Collections.sort(results, ResultSorter.getSorter());
		for (ITestResult result : results) {
			ITestNGMethod method = result.getMethod();
			String methodName = method.getMethodName();
			writer.println(String.format("\n<h2 id=\"%s\">%s: %s%s</h2>", linkable("m_", methodName),
					method.getTestClass().getName(), methodName, config ? " (configuration)" : ""));
			generateResult(writer, result);
			writer.println(String.format("<p class=\"%s\"><a href=#summary>back to summary</a></p>", Css.CLASS_TOTOP));
		}
		writer.println("\n</div>");
	}

	private void generateResult(PrintStream writer, ITestResult result) {
		tableStart(writer, Css.CLASS_DETAIL, null);

		Object[] parameters = result.getParameters();

		int numParams = (parameters == null) ? 0 : parameters.length;
		if (numParams > 0) {
			writer.print(String.format("\n<tr class=\"%s\">", Css.CLASS_PARAM));
			for (int x = 1; x <= parameters.length; x++) {
				writer.print("\n\t<th>Param." + x + "</th>");
			}
			writer.print(String.format("\n</tr>\n<tr class=\"%s\">", Css.CLASS_PARAM));
			for (Object p : parameters) {
				writer.print("\n\t<th>" + Utils.escapeHtml(Utils.toString(p, null)) + "</th>");
			}
			writer.println("\n</tr>");
		}

		Throwable exception = result.getThrowable();

		if (exception != null) {
			writer.println(String.format("<tr class=\"%s\">", Css.CLASS_STACKTRACE));
			writer.println(String.format("\t<td colspan=\"%d\">", numParams > 1 ? numParams : 1));
			writer.println(String.format("<h3>%s</h3>",
					(result.getStatus() == ITestResult.SUCCESS) ? "Expected Exception" : "Failure"));
			generateExceptionReport(writer, exception);
			writer.println("\t</td>\n</tr>");
		}

		writer.println("</table>");

		List<String> msgs = Reporter.getOutput(result);

		if (msgs.size() > 0) {
			tableStart(writer, Css.CLASS_LOG, null);
			writer.println(String.format("<tr class=\"%s\">", Css.CLASS_LOG));
			writer.println("\t<td><h3>Log from Test</h3>");
			writer.println(msgs.stream().collect(Collectors.joining("\n")));
			writer.println("\t</td>\n</tr>\n</table>");
		}
	}

	protected void generateExceptionReport(PrintStream writer, Throwable exception) {
		writer.print(String.format("\n<div class=\"%s\">", Css.CLASS_STACKTRACE));
		writer.print(getStackTrace(exception));
		writer.println("</div>");
	}

	public void generateCounts(PrintStream writer, ISuite suite) {
		tableStart(writer, Css.CLASS_TEST_COUNTS, null);
		generateSuiteSummaryTableHeader(writer);
		int qty_tests = 0;
		int qty_passed = 0;
		int qty_skipped = 0;
		int qty_autoFailed  = 0;
		int qty_newlyFailed = 0;
		int qty_appFailed = 0;
		int qty_totalFails = 0;
		int qty_totalTests = 0;
		int row = 0;

		long time_start = Long.MAX_VALUE;

		long time_end = Long.MIN_VALUE;
		Map<String, ISuiteResult> tests = suite.getResults();
		
//		ISuiteResult test = tests.values().iterator().next(); // for testing multi-test scenario
//		for (int i = 0; i < 2; i++) {
			
		for (ISuiteResult test : tests.values()) {
			qty_tests += 1;
			
			ITestContext testContext = test.getTestContext();

			int tPassed = testContext.getPassedTests().size();
			qty_passed += tPassed;

			int tSkipped = testContext.getSkippedTests().size();
			qty_skipped += tSkipped;

			FailCategories failCats = categorizeFails(testContext.getFailedTests());
			
			int tNewlyFailed = failCats.failure;
			qty_newlyFailed += tNewlyFailed;
			
			int tAutoFailed = failCats.automationBug;
			qty_autoFailed += tAutoFailed;
			
			int tAppFailed = failCats.appFailure;
			qty_appFailed += tAppFailed;

			int totalFails = tSkipped + tNewlyFailed + tAppFailed + tAutoFailed;
			qty_totalFails += totalFails;
			
			int totalTests = tPassed + totalFails;
			qty_totalTests += totalTests;
			
			int percentPassed = totalTests > 0 ? (tPassed*100)/totalTests : 0;
			
			SimpleDateFormat countFormat = new SimpleDateFormat("hh:mm:ss a");
			time_start = Math.min(testContext.getStartDate().getTime(), time_start);
			time_end = Math.max(testContext.getEndDate().getTime(), time_end);
			
			startCountRow(writer, row++, testContext.getName());
			countCell(writer, tPassed, Integer.MAX_VALUE, Css.CLASS_PASSED);
			countCell(writer, tSkipped, 0, Css.CLASS_SKIPPED);
			countCell(writer, tNewlyFailed, 0, Css.CLASS_FAILED);
			countCell(writer, tAppFailed, 0, Css.CLASS_APPBUG);
			countCell(writer, tAutoFailed, 0, Css.CLASS_AUTOBUG);
			countCell(writer, totalFails, Integer.MAX_VALUE, Css.CLASS_PASSED);
			
			countCell(writer, totalTests, Integer.MAX_VALUE, "");
			
			summaryCell(writer, String.format("%d%%",percentPassed), true, "");

			// Write OS and Browser
			/*
			 * summaryCell(browserType, true,""); writer.println("</td>");
			 */

			summaryCell(writer, countFormat.format(testContext.getStartDate()), true, "");
			summaryCell(writer, countFormat.format(testContext.getEndDate()), true, "");
			summaryCell(writer,
					timeConversion((testContext.getEndDate().getTime() - testContext.getStartDate().getTime()) / 1000),
					true, "");
			summaryCell(writer, testContext.getIncludedGroups());
			summaryCell(writer, testContext.getExcludedGroups());
			writer.println("\n</tr>");
		}

		if (qty_tests > 1) {
			writer.println(String.format("\n<tr class=\"%s\"><td>Total</td>", Css.CLASS_TOTAL));
			
			countCell(writer, qty_passed, Integer.MAX_VALUE, Css.CLASS_PASSED);
			countCell(writer, qty_skipped, 0, Css.CLASS_SKIPPED);
			countCell(writer, qty_newlyFailed, 0, Css.CLASS_FAILED);
			countCell(writer, qty_appFailed, 0, Css.CLASS_APPBUG);
			countCell(writer, qty_autoFailed, 0, Css.CLASS_AUTOBUG);
			countCell(writer, qty_totalFails, Integer.MAX_VALUE, Css.CLASS_PASSED);
			
			countCell(writer, qty_totalTests, Integer.MAX_VALUE, "");

			int percentPassed = qty_totalTests > 0 ? (qty_passed*100)/qty_totalTests : 0;
			summaryCell(writer, String.format("%d%%",percentPassed), true, "");
		}
		writer.println("</table>");
	}

	public void generateSuiteSummaryTableHeader(PrintStream writer) {
		writer.print("<tr>");
		tableTwoRowColumnStart(writer, "Test Area/Feature Name");
		tableTwoRowColumnStart(writer, "# Passed");
		writer.print("<th colspan=\"5\"># Failed</th>\n");
		// tableColumnStart("Browser");
		tableTwoRowColumnStart(writer, "Total<br/>tests");
		tableTwoRowColumnStart(writer, "Percent<br/>passed");
		tableTwoRowColumnStart(writer, "Start<br/>Time<br/>(hh:mm:ss)");
		tableTwoRowColumnStart(writer, "End<br/>Time<br/>(hh:mm:ss)");
		tableTwoRowColumnStart(writer, "Total<br/>Time<br/>(hh:mm:ss)");
		tableTwoRowColumnStart(writer, "Included<br/>Groups");
		tableTwoRowColumnStart(writer, "Excluded<br/>Groups");

		writer.println("\n</tr>\n<tr>");

		tableColumnStart(writer, "Conf.<br/>problem");
		tableColumnStart(writer, "Newly<br/>Failed");
		tableColumnStart(writer, "App<br/>problem");
		tableColumnStart(writer, "Auto.<br/>problem");
		tableColumnStart(writer, "Total<br/>failed");
		
		writer.println("\n</tr>");

	}

	private FailCategories categorizeFails(IResultMap resultMap) {
		FailCategories res = new FailCategories();
		for (ITestNGMethod m : resultMap.getAllMethods()) {
			try {
				TestCaseInfo info = getTestCaseInfo(m);
				if ((null != info) && (info.bug() != null) && !info.bug().isEmpty()) {
					if (info.isAutomationBug()) {
						res.automationBug++;
					} else {
						res.appFailure++;
					}
				} else {
					res.failure++;
				}
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		return res;
	}

	private void summaryCell(PrintStream writer, String[] val) {
		StringBuffer b = new StringBuffer();
		for (String v : val) {
			b.append(v + " ");
		}
		summaryCell(writer, b.toString(), true, "");
	}

	private void summaryCell(PrintStream writer, String v, boolean isgood, String clss) {
		StringBuilder sb = new StringBuilder("\t<td class=\"");
		sb.append(Css.CLASS_NUMI).append(' ');
		if ((clss != null) && !clss.isEmpty()) {
			sb.append(clss).append(' ');
		}
		sb.append(isgood ? Css.CLASS_EXPECTED : Css.CLASS_UNEXPECTED).append("\">").append(v).append("</td>");
		writer.println(sb.toString());
	}

	private void startCountRow(PrintStream writer, int rowNum, String label) {
		StringBuilder sb = new StringBuilder("\n<tr class=\"");
		sb.append(((rowNum % 2) == 0) ? " even" : " odd").append("\">");
		sb.append("\n\t<td><a href=\"#").append(linkable("t_", label)).append("\">").append(label).append("</a></td>");
		writer.println(sb.toString());
	}

	private String linkable(String prefix, String name) {
		return prefix + linkable(name);
	}

	private void countCell(PrintStream writer, int v, int maxexpected, String clss) {
		summaryCell(writer, String.valueOf(v), v <= maxexpected, clss);
	}

	private void tableStart(PrintStream writer, String cssclass, String id) {
		writer.println("<table" + (cssclass != null ? " class=\"" + cssclass + "\"" : " style=padding-bottom:2em")
				+ (id != null ? " id=" + id + "" : "") + ">");
	}

	private void tableColumnStart(PrintStream writer, String label) {
		writer.print("\n\t<th>" + label + "</th>");
	}
	
	private void tableTwoRowColumnStart(PrintStream writer, String label) {
		writer.print("\n\t<th rowspan=\"2\">" + label + "</th>");		
	}

	private void titleRow(PrintStream writer, String testname, String style, boolean config) {
		StringBuilder builder = new StringBuilder("\n<tr id=\"");
		builder.append(testname).append('-').append(style).append("\" class=\"").append(Css.CLASS_TEST_SUMMARY)
		.append("\"/>");
		builder.append("\n\t<th colspan=").append(TEST_TABLE_COLS).append(">");
		builder.append("<a name=\"").append(linkable("t_", testname)).append("\"/>");
		builder.append(testname).append(" &#8212; ").append(style);
		if (config) {
			builder.append(" (configuration)");
		}
		builder.append("</th>\n</tr>");
		writer.println(builder.toString());
	}

	protected void generateTitle(PrintStream writer, String title, ISuite suite) {
		String suiteName = suite.getName();
		SuiteRunProperties props = getSuiteProps(suite);

		String browser = props.getProperty("browser.browserName", "N/A") + " ("
				+ props.getProperty("system.os.name", "N/A") + '/' + props.getProperty("system.os.version", "?") + ')';
		String planning = props.getProperty("testng.planning.buildnumber", "N/A");
		String discovery = props.getProperty("testng.discovery.buildnumber", "N/A");

		writer.println(String.format("\n<div class=\"%s\">", Css.CLASS_SUITE_TITLE));

		writer.println(String.format("<h2>%s %s - on %s - %s </h2>", suiteName, title, browser, getDateAsString()));

		writer.println(String.format("<h4> Planning Build Number  : %s Discovery Build Number  : %s</h4>", planning,
				discovery));
		
		String branch = props.getProperty("env.ReleaseBranch", "");
		String release = props.getProperty("env.ReleaseName", "");
		
		if (!branch.isEmpty()) {
			writer.println(String.format("<h4> Release  : %s Branch  : %s</h4>", release, branch));
		}

		writer.println("</div>");
	}

	private SuiteRunProperties getSuiteProps(ISuite suite) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * Method to get Date as String
	 */
	private String getDateAsString() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss a");
		Date date = new Date();
		return dateFormat.format(date);
	}

	/** Starts HTML stream */
	protected void startHtml(PrintStream writer, List<ISuite> suites) {
		StringBuilder sb = new StringBuilder();

		String title = generatePageTitle(suites);

		sb.append("<!DOCTYPE html PUBLIC \n");// W3C//DTD XHTML 1.1//EN"
		// "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">\n");
		// sb.append("<html xmlns="http:/"//www.w3.org/1999/xhtml">\n");
		sb.append("<head>\n");
		sb.append("<title>").append(title).append(" (TestNG Report)</title>\n");
		sb.append("<style type=\"text/css\">\n");
		sb.append(css.getStylesheet());
		sb.append("\n</style>\n");
		sb.append("</head>\n");
		sb.append("<body>\n");
		writer.println(sb.toString());

		generateSuitesIndex(writer, suites);
	}

	private String generatePageTitle(List<ISuite> suites) {
		String title = suites.size() == 0 ? "" : suites.get(0).getName();
		return title;
	}

	/** Finishes HTML stream */
	protected void endHtml(PrintStream out) {
		out.println("<center> TestNG Report </center>");
		out.println("</body></html>");
	}

	private static class Css {

		public static final String COLOR_ERROR = "#FF5555";
		public static final String COLOR_AUTOBUG = "#FFDAB9";
		public static final String COLOR_WARN = "#FFFFBB";
		public static final String COLOR_HEADER = "#FFE4C4";
		public static final String COLOR_PASSED = "lightgreen";
		public static final String COLOR_PASSED_BUG = "pink";
		public static final String COLOR_APPBUG = COLOR_ERROR;

		public static final String COLOR_TEST_SUMMARY = "#FFF5D5";

		public static final String CLASS_PASSED = "passed";
		public static final String CLASS_SKIPPED = "skipped";
		public static final String CLASS_FAILED = "failed";
		public static final String CLASS_AUTOBUG = "autobug";
		public static final String CLASS_UNEXPECTED = "unexpected";
		public static final String CLASS_EXPECTED = "expected";
		public static final String CLASS_SUITE = "suite";
		public static final String CLASS_SUITE_LIST = "suitelist";
		public static final String CLASS_TEST_COUNTS = "testCounts";
		public static final String CLASS_TEST_SUMMARY = "testsummary";
		public static final String CLASS_SUITE_TITLE = "suitetitle";
		public static final String CLASS_DETAIL = "detail";
		public static final String CLASS_RESULT = "result";
		public static final String CLASS_NUMI = "numi";
		public static final String CLASS_TOTAL = "total";
		public static final String CLASS_STACKTRACE = "stacktrace";
		public static final String CLASS_PARAM = "param";
		public static final String CLASS_TOTOP = "totop";
		public static final String CLASS_LOG = "log";
		public static final String CLASS_BUG = "bug";
		public static final String CLASS_APPBUG = "appbug";

		public static final int MAX_FILE_SIZE = 2048 * 2;

		@SuppressWarnings("serial")
		private static HashMap<String, String> mappings = new HashMap<String, String>() {
			{
				put("COLOR_WARN", COLOR_WARN);
				put("COLOR_ERROR", COLOR_ERROR);
				put("COLOR_AUTOBUG", COLOR_AUTOBUG);
				put("COLOR_HEADER", COLOR_HEADER);
				put("COLOR_TEST_SUMMARY", COLOR_TEST_SUMMARY);
				put("COLOR_PASSED", Css.COLOR_PASSED);
				put("COLOR_PASSED_BUG", Css.COLOR_PASSED_BUG);
				put("COLOR_APPBUG", Css.COLOR_APPBUG);

				put("CLASS_PASSED", CLASS_PASSED);
				put("CLASS_SKIPPED", CLASS_SKIPPED);
				put("CLASS_FAILED", CLASS_FAILED);
				put("CLASS_AUTOBUG", CLASS_AUTOBUG);
				put("CLASS_UNEXPECTED", CLASS_UNEXPECTED);
				put("CLASS_EXPECTED", CLASS_EXPECTED);
				put("CLASS_SUITE", CLASS_SUITE);
				put("CLASS_SUITE_LIST", CLASS_SUITE_LIST);
				put("CLASS_TEST_COUNTS", CLASS_TEST_COUNTS);
				put("CLASS_TEST_SUMMARY", CLASS_TEST_SUMMARY);
				put("CLASS_SUITE_TITLE", CLASS_SUITE_TITLE);
				put("CLASS_DETAIL", CLASS_DETAIL);
				put("CLASS_RESULT", CLASS_RESULT);
				put("CLASS_NUMI", CLASS_NUMI);
				put("CLASS_TOTAL", CLASS_TOTAL);
				put("CLASS_STACKTRACE", CLASS_STACKTRACE);
				put("CLASS_PARAM", CLASS_PARAM);
				put("CLASS_TOTOP", CLASS_TOTOP);
				put("CLASS_LOG", CLASS_LOG);
				put("CLASS_BUG", CLASS_BUG);
			}
		};

		private static Pattern p = Pattern.compile("(%\\w+%)", Pattern.DOTALL);

		/**
		 * This will read the reeport.css file from the classpath (usually, the
		 * same directory as this file) and replace any of the strings
		 * %SOMTHING% with the value from the map above.
		 * 
		 * @return
		 */
		public String getStylesheet() {
			String template = "";
			try (InputStream is = getClass().getResourceAsStream("report.css")) {
				if (is != null) {
					byte[] buffer = new byte[Css.MAX_FILE_SIZE];
					int bytesRead = is.read(buffer);
					if (bytesRead < Css.MAX_FILE_SIZE) {

						template = new String(buffer, 0, bytesRead);
						for (String key : mappings.keySet()) {
							template = template.replaceAll('%' + key + '%', mappings.get(key));
						}

						Matcher m = p.matcher(template);

						if (m.find()) {
							System.err.println("Template not exhausted:  '" + m.group(1) + "' not converted.");
						}
					} else {
						System.err.println("report.css is too big");
					}
				} else {
					System.err.println("Cannot open 'report.css'");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return template;
		}
	}

	public static class ResultSorter implements Comparator<ITestResult> {
		private ResultSorter() {
		}

		private static Comparator<ITestResult> instance = null;

		@Override
		public int compare(ITestResult o1, ITestResult o2) {
			// System.err.println("Compare " + o1.getAttribute(CHRON_ORDER_ATTR)
			// + " with " + o2.getAttribute(CHRON_ORDER_ATTR) );
			Integer io1 = (Integer) o1.getAttribute(ATTR_CHRON_ORDER);
			Integer io2 = (Integer) o2.getAttribute(ATTR_CHRON_ORDER);
			if (io1 == null) {
				return io2 == null ? EQUAL : FIRST_LOWER;
			} else {
				if (io2 == null) {
					return FIRST_HIGHER;
				}
			}
			return Integer.compare((Integer) o1.getAttribute(ATTR_CHRON_ORDER),
					(Integer) o2.getAttribute(ATTR_CHRON_ORDER));
		}

		public static Comparator<ITestResult> getSorter() {
			if (instance != null) {
				instance = new ResultSorter();
			}
			return instance;
		}
	}

	private static class InvokedMethodsComparator implements Comparator<IInvokedMethod> {
		private InvokedMethodsComparator() {
		}

		private static Comparator<IInvokedMethod> instance = null;

		@Override
		public int compare(IInvokedMethod o1, IInvokedMethod o2) {
			if (o1 == null) {
				return o2 == null ? EQUAL : FIRST_LOWER;
			} else {
				if (o2 == null) {
					return FIRST_HIGHER;
				}
			}
			return Long.compare(o1.getDate(), o2.getDate());
		}

		public static Comparator<IInvokedMethod> getSorter() {
			if (instance == null) {
				instance = new InvokedMethodsComparator();
			}
			return instance;
		}
	}

}