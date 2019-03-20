package com.myapp.Tests;

import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.library.Utils.DriverTestCase;
import com.library.Utils.ExecutionLog;
import com.library.Utils.PropertyReader;
import com.library.Utils.testrail.TestRailHandler;
import com.myapp.Data.ReadDataFromExcelSheet;
import com.myapp.page.AddUserHelperPage;

public class AddUser extends DriverTestCase {
		private AddUserHelperPage adduser;
		PropertyReader propertyReader=new PropertyReader();
		String username=propertyReader.readApplicationFile("Username","//Src//com//myapp//Config//application.properties");	
		String email=propertyReader.readApplicationFile("email","//Src//com//myapp//Config//application.properties");		
		
		@BeforeMethod (alwaysRun=true)
		public void setup () throws Exception {
			try {
			setup("//Src//com//myapp//Config//application.properties");
        	adduser       = new AddUserHelperPage(driver);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		
		@Test (priority=1, groups = {"Acceptance"})
		public void testSendEmail() throws Exception {
			
			try{
			TestRailHandler trh = new TestRailHandler("robovipin@gmail.com", "123-asd", "https://viping.testrail.io");
			Robot jv = new Robot();
			
			driver.get("https://accounts.google.com/signin/v2/identifier?continue=https%3A%2F%2Fmail.google.com%2Fmail%2F&service=mail&sacu=1&rip=1&flowName=GlifWebSignIn&flowEntry=ServiceLogin");
			WebElement userElement = driver.findElement(By.id("identifierId"));
			//userElement.sendKeys("robovipin");
			userElement.click();

			//driver.findElement(By.id("identifierNext")).click();
			WebElement nextbutton = driver.findElement(By.id("identifierNext"));
			Thread.sleep(10000);
			jv.keyPress(KeyEvent.VK_A);
			Thread.sleep(10000);
			//WebElement Image = driver.findElement(By.xpath("//img[@border='0']"));
		    //Used points class to get x and y coordinates of element.
			
		    org.openqa.selenium.Point classname = nextbutton.getLocation();
		    int xcordi = classname.getX();
		    System.out.println("Element's Position from left side"+xcordi +" pixels.");
		    int ycordi = classname.getY();
		    System.out.println("Element's Position from top"+ycordi +" pixels.");
		    Thread.sleep(10000);
		    
		    jv.mouseMove(xcordi, ycordi);
		    Thread.sleep(10000);
		    jv.mousePress(InputEvent.BUTTON1_DOWN_MASK);
		    Thread.sleep(10000);
			
			}
			catch (Exception e){
				System.out.println(e.toString());
			}
		}
		
		//@TestCaseInfo(testCaseID = "1", title = "djhdfk", description = "")
		@Test  (priority=1, groups = {"Acceptance"})
		public void testAddUser() throws Exception {	
			TestRailHandler trh = new TestRailHandler("robovipin@gmail.com", "123-asd", "https://viping.testrail.io");
         try{
        	 
			ExecutionLog.LogAddClass(this.getClass().getName()+" >> "+new Exception().getStackTrace()[0].getMethodName());
			
			System.out.println("\n*****Start Execution*****\n");
			
			//Enter User name into the given input field		
			adduser.enterUserName(username);
			ExecutionLog.Log("Name is entered!");
			
			//Enter User email into the given input field
			adduser.enterUserEmail(email);
			ExecutionLog.Log("Email is entered!");
			
			//click save button
			adduser.clickSaveButton();
			ExecutionLog.Log("Button is clicked!");
			
			//check if user is added
			adduser.checkUser();
			ExecutionLog.Log("User is created successfully");
			
			//trh.updateResultToTestRail(1, "1", "1");
         }	catch(Error e) 
 		   {
 			captureScreenshot("testAddUser");	
 			ExecutionLog.LogErrorMessage(e);
 			//trh.updateResultToTestRail(5, "1", "1");
 			throw e;
 			
 		    } 
 		   catch(Exception e) 
 		    {
 			captureScreenshot("Add User is failed!!");
 			ExecutionLog.LogExceptionMessage(e);
 			//trh.updateResultToTestRail(5, "1", "1");
 			throw e;
 		    }	
		
		}
		
		@Test (priority=2, groups = {"Regression"})
		public void checkEmail () throws Exception{	
			adduser.enterUserName(username);
			adduser.enterUserEmail(email);
			adduser.clickSaveButton();
			adduser.verifyEmailValidation();
		}
		
		
		@DataProvider(parallel=true)
		public Object[][] getData()
		{
			String projectpath=new PropertyReader().getPath();
			ReadDataFromExcelSheet sheet = new ReadDataFromExcelSheet(projectpath+"\\Src\\com\\myapp\\Data\\TestData.xlsx");
			
			Object[][] data = new Object[5][2];
			
			for (int i=0 ; i<sheet.getRowCount("TestData")-1; i++)
			{ 
				 String Username_excel = sheet.getCellData("TestData", "Username", i+2);
				 String email_excel = sheet.getCellData("TestData", "email", i+2);
				 //String browser_excel = sheet.getCellData("TestData", "browsers", i+2);
				 data [i][0] = Username_excel;
				 data [i][1] = email_excel;
			}
			return data;
		};

		
		@Test (priority=3, groups = {"Functional"}, dataProvider="getData")
		public void addUserFromExcel (String Username_excel, String email_excel) throws Exception{	
			adduser.enterUserName(Username_excel);
			adduser.enterUserEmail(email_excel);
			adduser.clickSaveButton();
			adduser.verifyEmailValidation();
		}

		@AfterMethod (alwaysRun=true)
		public void closebrowser()
		{
			driver.quit();
		}
	
}

