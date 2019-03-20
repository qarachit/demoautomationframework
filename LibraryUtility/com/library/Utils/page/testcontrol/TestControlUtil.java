package com.library.Utils.page.testcontrol;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertTrue;

import java.util.Collections;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.WrapsDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.JavascriptExecutor;

public class TestControlUtil {
	public static final long DEFAULT_WAIT_MILLISECONDS = 250L;
	public static final long DEFAULT_WAIT_SECONDS = 5;
	private static boolean dbug = true;

	// this works for IE, Firefox and Chrome.  If it doesn't for what you are trying to
	// do, you need to use the method that expects the WebDriver.
	public static WebDriver getWebDriver(WebElement element) {
		return ((WrapsDriver)element).getWrappedDriver();
	}
	
	public static boolean click(SearchContext context, By by) {
		return click(context, by, DEFAULT_WAIT_SECONDS);
	}

	public static String getIDFromXpath(WebDriver driver, String xpathString) {
        WebElement we= driver.findElement(By.xpath(xpathString));
        String ID=we.getAttribute("id");
		return ID;
	}
	
	public static boolean click(SearchContext context, By by, long seconds) {
		WebDriver driver = (context instanceof WebElement) ? getWebDriver((WebElement)context) : (WebDriver)context;
		
		WebElement clickElement = waitForClickableElement(driver, (WebElement) by, seconds);
		if (clickElement == null) {
			return false;
		}
		click(getWebDriver(clickElement), clickElement, 0);
		
		return true;
	}

	public static void click(WebElement element) {
		click(getWebDriver(element), element, DEFAULT_WAIT_SECONDS);
	}
	
	public static void click(WebDriver driver, WebElement element) {
		click(driver, element, DEFAULT_WAIT_SECONDS);
	}
	
	public static void click(WebElement element, long seconds) {
		WebDriver driver = getWebDriver(element);
		
		click(driver, element, seconds);
	}
	
	public static void click(WebDriver driver, WebElement element, long pause) {
		scrollIntoView(driver, element);
		TestControlUtil.waitForClickableElement(driver, element, pause);
		element.click();
	}

	public static void clearAndSendKeys(WebElement element, String string) {
		clearAndSendKeys(getWebDriver(element), element, string, DEFAULT_WAIT_MILLISECONDS);
	}
	
	public static void clearAndSendKeys(WebDriver driver, WebElement element, String string) {
		clearAndSendKeys(driver, element, string, DEFAULT_WAIT_MILLISECONDS);
	}

	public static void clearAndSendKeys(WebElement element, String string, long pause) {
		clearAndSendKeys(getWebDriver(element), element, string, pause);
	}
	
	public static void clearAndSendKeys(WebDriver driver, WebElement element, String string, long milliseconds) {

		click(driver, element);
		
		//@Autor:Sourav, Replaced 'element.clear()'
//		element.sendKeys(Keys.CONTROL,"a");
//		element.sendKeys(Keys.DELETE);
		
		//element.clear();  it removes the element from 'dom' in few cases and then the next line method sendkeys shows the stateStale exception
		// I don't think it does.  And this alternative does not work on Mac.  Restoring and sending Sourav a note to understand the reason for the
		// change.

		element.clear();
		element.sendKeys(string);
		
		sleep(milliseconds);
	}
	
	public static void moveTo(WebElement element) {
		moveTo(getWebDriver(element), element, DEFAULT_WAIT_MILLISECONDS);
	}

	public static void moveTo(WebDriver driver, WebElement element) {
		moveTo(driver, element, DEFAULT_WAIT_MILLISECONDS);
	}
	
	public static void moveTo(WebElement element, long milliseconds) {
		moveTo(getWebDriver(element), element, milliseconds);
	}
	
	public static void moveTo(WebDriver driver, WebElement element, long milliseconds) {
		Actions actions = new Actions(driver);
		
		actions.moveToElement(element).perform();
		
		sleep(milliseconds);
	}

	public static void sleep(long milliseconds) {
		try {
			Thread.sleep(milliseconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public static WebElement getAndAssertSingleElement(SearchContext context, By by) {
		List<WebElement> elements = context.findElements(by);
		assertTrue("No elements found", !elements.isEmpty());
		assertEquals("More than one element found", 1, elements.size());
		return elements.get(0);
	}
	
	public static void scrollIntoView(WebElement element) {
		scrollIntoView(getWebDriver(element), element);
	}
	
	public static void scrollIntoView(WebDriver driver, WebElement element, boolean force) {
		if(force || !element.isDisplayed()){
			((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
		}
		sleep(500); // let UI react
	}
	
	public static void scrollIntoView(WebDriver driver, WebElement element) {
		scrollIntoView(driver, element, false);
	}
	
	public static WebElement waitForClickableElement(SearchContext context, WebElement element, long seconds) {
		return (context instanceof WebElement) ?
				waitForClickableElement(getWebDriver((WebElement)context), element, seconds) :
				waitForClickableElement(                                    (WebDriver)context, element, seconds);
	}

	/*
	public static WebElement waitForClickableElement(WebDriver driver, WebElement element, long seconds) {
		if (element != null) {
			if (!element.isDisplayed() || !element.isEnabled()) {
				try {
					element =
							(new WebDriverWait(driver, seconds))
							.until(ExpectedConditions.elementToBeClickable(element));
				} catch (TimeoutException e) {

				}
				if ((element == null) && dbug) {
					System.out.println(String.format("Element (%s) not clickable after %d seconds", element, seconds));
				}
			}
		}
		return element;
	}

	public static WebElement waitForClickableElement(WebDriver driver, WebElement contextElement, By by,
			long seconds) {
		WebElement element = null;
		try {
			element =
				(new WebDriverWait(driver, seconds))
				.until(ExpectedConditions.presenceOfNestedElementLocatedBy(contextElement, by));
			if (element != null) {
				element = (new WebDriverWait(driver, seconds))
				.until(ExpectedConditions.elementToBeClickable(element));
			}
		} catch (TimeoutException e) {
			
		}

		if ((element == null) && dbug) 
			System.out.println(String.format("Nested clickable element (%s) not found after %d seconds", by.toString(), seconds));
		return element;
	}
	
	public static WebElement waitForClickableElement(WebDriver driver, By by, long seconds) {
		
		WebElement element = null;
		try {
			element = (new WebDriverWait(driver, seconds))
				.until(ExpectedConditions.elementToBeClickable(by));
		} catch (TimeoutException e) {
			
		}
		if ((element == null) && dbug) 
			System.out.println(String.format("Clickable element (%s) not found after %d seconds", by.toString(), seconds));
		return element;
	}


//	private static void waitUnitClickable(WebDriver driver, WebElement element, int seconds) {
//		(new WebDriverWait(driver, seconds))
//		.until(ExpectedConditions.elementToBeClickable(element));
//	}


	public static List<WebElement> waitForRelativeElements(WebElement webElement, By by, long seconds) {
		WebDriver driver = getWebDriver(webElement);
		
		List<WebElement> elements = Collections.<WebElement> emptyList();
		try {
			WebElement element = (new WebDriverWait(driver, seconds))
				.ignoring(TimeoutException.class)
				.until(ExpectedConditions.presenceOfNestedElementLocatedBy(webElement, by));
			if (element != null) {
				elements = webElement.findElements(by);
			}
		} catch (TimeoutException e) {
			
		}
		if (elements.isEmpty() && dbug) 
			System.out.println(String.format("Relative elements (%s) not found after %d seconds", by.toString(), seconds));
		return elements;
	}
	
	
	public static WebElement waitForElement(SearchContext context, By by, long milliseconds) {
		WebDriver driver;
		if (context instanceof WebElement) {
			driver = getWebDriver((WebElement)context);
		} else {
			driver = (WebDriver) context;
		}
		
		long seconds = milliseconds < 1000L ? 1 : milliseconds/1000L;
		WebElement element = null;
		try {
			element = (new WebDriverWait(driver, seconds))
				.ignoring(TimeoutException.class)
				.until(ExpectedConditions.presenceOfElementLocated(by));
		} catch (TimeoutException e) {
			
		}

		if ((element == null) && dbug) 
			System.out.println(String.format("Element (%s) not found after %d seconds", by.toString(), seconds));
		return element;
	}

	public static List<WebElement> waitForElements(WebDriver driver, By by, long seconds) {

		List<WebElement> elements = Collections.<WebElement> emptyList();
		try {
			elements = (new WebDriverWait(driver, seconds))
				.ignoring(TimeoutException.class)
				.until(ExpectedConditions.presenceOfAllElementsLocatedBy(by));
		} catch (TimeoutException e) {
			
		}

		if (elements.isEmpty() && dbug) 
			System.out.println(String.format("Elements (%s) not found after %d seconds", by.toString(), seconds));
		return elements;
	}

	public static boolean waitUntilGone(WebDriver driver, By by, long seconds) {
		boolean res = false;
		try {
			res = (new WebDriverWait(driver, seconds))
				.ignoring(TimeoutException.class)
				.until(ExpectedConditions.invisibilityOfElementLocated(by));
		} catch (TimeoutException e) {
			
		}
		return res;
	}

	public static void waitUntilShownAndGone(WebDriver driver, By by, int maxAppearSeconds, int maxDisplaySeconds) {
		TestControlUtil.waitForElement(driver, by, maxAppearSeconds*1000L);
		TestControlUtil.waitUntilGone(driver,  by, maxDisplaySeconds);
	}

	public static void scrollToTopOfPage(WebDriver driver) {
		scrollIntoView(driver.findElement(By.xpath("//body/div[1]")));
	}

	/**
	 * 
	 * We want to avoid sleeps.  They are time inefficient and you should look for some change in the DOM to 
	 * wait for.  But if you cannot sort that out quickly, use this method to wait so that we can document and
	 * find its use.
	 * 
	 * @param milliseconds - how many milliseconds to wait.
	 * @param why - a (ideally unique to this call) description of why the sleep was needed.
	 */
	public static void temporarySleep(long milliseconds, String why) {
		System.err.println(String.format("Temporary sleep for %d milliseconds: %s", milliseconds, why));
		sleep(milliseconds);
	}
	/*

	public static void waitUntilVisible(WebDriver driver, WebElement element, long maxWaitSeconds) {
			try {
				(new WebDriverWait(driver, maxWaitSeconds))
					.ignoring(TimeoutException.class)
					.until(ExpectedConditions.visibilityOf(element));
			} catch (TimeoutException e) {
				
			}
		}
*/
	/*
	public static void scrollToTop(SearchContext context, By by, int seconds) {
		WebElement element = waitForElement(context, by, seconds*1000);
		scrollTo(getWebDriver(element), element, 0);
	}
	*/
	public static void scrollTo(WebDriver driver, WebElement element, int pixels) {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollTop = arguments[1];", element, pixels);
	}

}
