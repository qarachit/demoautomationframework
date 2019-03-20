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

public class TestngDataProviderParallel extends DriverTestCase {
		private AddUserHelperPage adduser;
		PropertyReader propertyReader=new PropertyReader();
		String username=propertyReader.readApplicationFile("Username","//Src//com//myapp//Config//application.properties");	
		String email=propertyReader.readApplicationFile("email","//Src//com//myapp//Config//application.properties");		
		
		@BeforeMethod (alwaysRun=true)
		public void setup () throws Exception {
			try {
			setup("//Src//com//myapp//Config//application.properties");
        	adduser = new AddUserHelperPage(driver);
			}
			catch (Exception e)
			{
				System.out.println(e.getMessage());
			}
		}
		
		@DataProvider(name="getData", parallel = true)
		public Object[][] getData()
		{
			String projectpath=new PropertyReader().getPath();
			ReadDataFromExcelSheet sheet = new ReadDataFromExcelSheet(projectpath+"\\Src\\com\\myapp\\Data\\TestData.xlsx");
			
			Object[][] data = new Object[5][2];
			
			for (int i=0 ; i<sheet.getRowCount("TestData")-1; i++)
			{ 
				 String Username_excel = sheet.getCellData("TestData", "Username", i+2);
				 String email_excel = sheet.getCellData("TestData", "email", i+2);
				 data [i][0] = Username_excel;
				 data [i][1] = email_excel;
			}
			return data;
		};

		
		@Test (priority=1, groups = {"Functional"}, dataProvider="getData")
		public void addUserFromExcel (String Username_excel, String email_excel) throws Exception{	
			
			//String projectpath=new PropertyReader().getPath();
			//ReadDataFromExcelSheet sheet = new ReadDataFromExcelSheet(projectpath+"\\Src\\com\\myapp\\Data\\TestData.xlsx");
			
			//String Username_excel = sheet.getCellData("TestData", "Username", 2);
		    //String email_excel = sheet.getCellData("TestData", "email", 2);
			
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