package com.library.Utils;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;
import org.testng.Assert;


public abstract class  DriverHelper extends DriverTestCase {
	
	private WebDriver driver;
	//private Selenium selenium;

	public DriverHelper(WebDriver webdriver) {
		driver = webdriver;		
		//selenium = new WebDriverBackedSelenium(driver, "http://ch.qa.guidance.com/");
	}
	public DriverHelper() {
			
		//selenium = new WebDriverBackedSelenium(driver, "http://ch.qa.guidance.com/");
	}
	

	/*public Selenium getSelenium(){
		return selenium;
	}*/
	
	public void Log(String logMsg){
		System.out.println(logMsg);
	}
	
	public WebDriver getWebDriver() 
	{
		   System.out.println(driver+"------"+i++);
	    	return this.driver;
	}

	//Handle locator type
	public By ByLocator(String locator) 
	{
		By result = null;

		if (locator.startsWith("//")) 
		{ result = By.xpath(locator); }
		else if (locator.startsWith("css=")) 
		{ result = By.cssSelector(locator.replace("css=", "")); } 
		else if (locator.startsWith("name=")) 
		{ result = By.name(locator.replace("name=", ""));
		} else if (locator.startsWith("link=")) 
		{ result = By.linkText(locator.replace("link=", "")); } 
		else 
		{ result = By.id(locator); }
		return result;
	}

	//Assert element present
	public Boolean isElementPresent(String locator) 
	{
		Boolean result = false;
		try 
		{
			
			getWebDriver().findElement(ByLocator(locator));
			result = true;
		} 
		catch (Exception ex) { }
		return result;
	}

    public void dragAndDrop(String loc_SourceElement, String loc_TargetElement)
    {
     Actions act = new Actions(getWebDriver());
     WebElement SourceElement = getWebDriver().findElement(ByLocator(loc_SourceElement)); 
     WebElement TargetElement = getWebDriver().findElement(ByLocator(loc_TargetElement));
     act.clickAndHold(SourceElement).moveToElement(TargetElement).pause(2000).release(TargetElement).build().perform();
    }
	public void WaitForElementPresent(String locator, int timeout) {

		for (int i = 0; i < timeout; i++) {
			if (isElementPresent(locator)) {
				break;
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void WaitForElementEnabled(String locator, int timeout) {

		for (int i = 0; i < timeout; i++) {
			if (isElementPresent(locator)) {
				if (getWebDriver().findElement(ByLocator(locator)).isEnabled()) {
					break;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void WaitForElementNotEnabled(String locator, int timeout) {

		for (int i = 0; i < timeout; i++) {
			if (isElementPresent(locator)) {
				if (!getWebDriver().findElement(ByLocator(locator)).isEnabled()) {
					break;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void WaitForElementVisible(String locator, int timeout) {

		for (int i = 0; i < timeout; i++) {
			if (isElementPresent(locator)) {
				if (getWebDriver().findElement(ByLocator(locator)).isDisplayed()) {
					break;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public void WaitForElementNotVisible(String locator, int timeout) {

		for (int i = 0; i < timeout; i++) {
			if (isElementPresent(locator)) {
				if (!getWebDriver().findElement(ByLocator(locator)).isDisplayed()) {
					break;
				}
			}

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void mouseOver(String locator){		
		this.WaitForElementPresent(locator, 20);		
		// find Assignments menu
		WebElement el = getWebDriver().findElement(ByLocator(locator));
		
		//build and perform the mouseOver with Advanced User Interactions API
		Actions builder = new Actions(getWebDriver());    
		builder.moveToElement(el).build().perform();
	}
	  public void dragAndDrop(String loc_SourceElement, String loc_TargetElement, String DashbaordTab)
	    {
	     Actions act = new Actions(getWebDriver());
	     WebElement SourceElement = getWebDriver().findElement(ByLocator(loc_SourceElement)); 
	     WebElement TargetElement = getWebDriver().findElement(ByLocator(loc_TargetElement));
	     WebElement SupportElement = getWebDriver().findElement(ByLocator(DashbaordTab));
	     act.clickAndHold(SourceElement).moveToElement(SupportElement).pause(2000).moveToElement(TargetElement).pause(2000).release(TargetElement).build().perform();
	    }
	public void clickOn(String locator)
	{		
		this.WaitForElementPresent(locator, 20);
		Assert.assertTrue(isElementPresent(locator));
		WebElement el = getWebDriver().findElement(ByLocator(locator));			
		el.click();
	}
	
	public void doubleClick(String locator)
	{		
		this.WaitForElementPresent(locator, 20);
		Assert.assertTrue(isElementPresent(locator));
		WebElement el = getWebDriver().findElement(ByLocator(locator));	
		Actions action = new Actions(driver);
		action.doubleClick(el).perform();
	}
	public void rightClick(String locator)
	{		
	
		WebElement el = getWebDriver().findElement(ByLocator(locator));	
		Actions action = new Actions(driver);
		action.contextClick(el).build().perform();
	}
	public void sendKeys(String locator, String value){
		
		this.WaitForElementPresent(locator, 20);
		Assert.assertTrue(isElementPresent(locator));
		WebElement el = getWebDriver().findElement(ByLocator(locator));
		el.clear();
		el.sendKeys(value);
	}
	
	public void selectFrame(String locator){
		
		this.WaitForElementPresent(locator, 20);
		Assert.assertTrue(isElementPresent(locator));
		getWebDriver().switchTo().frame(locator);
		
	}

	public void selectDropDown(String locator, String targetValue){ 
		Assert.assertTrue(isElementPresent(locator));
		this.WaitForElementPresent(locator, 20);
		new Select(getWebDriver().findElement(ByLocator(locator))).selectByVisibleText(targetValue);
		
    }
	
	public void selectDropDownByValue(String locator, String value){ 
		Assert.assertTrue(isElementPresent(locator));
		this.WaitForElementPresent(locator, 20);
		new Select(getWebDriver().findElement(ByLocator(locator))).selectByValue(value);
		
    }
	public boolean isTextPresent(String locator, String str){		
		String message = getWebDriver().findElement(ByLocator(locator)).getText();			
		if(message.contains(str)){return true;}
		else {	return false; }
	}
	
	public String getText(String locator){
		WaitForElementPresent(locator, 20);
		String text = getWebDriver().findElement(ByLocator(locator)).getText();	
		return text;
	}
}
