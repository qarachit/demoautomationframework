package com.myapp.page;

import org.openqa.selenium.WebDriver;
import org.testng.Assert;

import com.library.Utils.DriverHelper;
import com.library.Utils.PropertyReader;
import com.myapp.Locator.LocatorReader;

public class AddUserHelperPage extends DriverHelper{

	private LocatorReader locatorReader;
	private PropertyReader propertyReader;	
	
	public AddUserHelperPage(WebDriver driver) {
		super(driver);	
		locatorReader = new LocatorReader("adduser.xml");
		 propertyReader=new PropertyReader();
	}
	
	//Enter user's name
	public void enterUserName(String data ) throws InterruptedException{
		String loc= locatorReader.getLocator("adduser.name");
		sendKeys(loc,data);
			
	}
	
	//Enter user's email
		public void enterUserEmail(String data ) throws InterruptedException{
			String loc= locatorReader.getLocator("adduser.email");
			sendKeys(loc,data);
				
		}
		
	//click Save button
	public void clickSaveButton() throws InterruptedException{
		String loc= locatorReader.getLocator("adduser.savebutton");
		clickOn(loc);
		
	}
	
	//verify if there is validation
	public void verifyEmailValidation() throws InterruptedException{
		Thread.sleep(4000);
		String loc= locatorReader.getLocator("adduser.emailcheck");
		Assert.assertTrue(isElementPresent(loc));	
	}
		
	//verify if user is added
		public void checkUser() throws InterruptedException{
			Thread.sleep(4000);
			String loc= locatorReader.getLocator("adduser.usercheck");
			Assert.assertTrue(isElementPresent(loc));	
		}
}
