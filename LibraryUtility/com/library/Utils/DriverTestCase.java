package com.library.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import com.library.Utils.config.*;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.annotations.AfterTest;
import org.testng.Reporter;

import com.library.Utils.PropertyReader;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;


public class DriverTestCase {

	enum WebBrowser 
	{ Firefox, IE, Chrome, Remote }
	
	public  static WebDriver driver;
	int i=0;
	public PropertyReader propertyReader;
	Random rg = new Random();
	public int n = rg.nextInt(100000);

	
	public void setup(String filepath) throws Exception{
		propertyReader = new PropertyReader();	
		String driverType,url;
	 	driverType=propertyReader.readApplicationFile("BROWSER",filepath);
		url=propertyReader.readApplicationFile("URL",filepath);

		//Check if desired browser is Firefox
		if (WebBrowser.Firefox.toString().equals(driverType)) 
		{ 
			try {
			String path = getPath();
			File file = new File(path+"//driver//geckodriver.exe");
			System.setProperty("webdriver.firefox.marionette",file.getAbsolutePath());
			driver = new FirefoxDriver();
			} catch (Exception e){
				System.out.println(e.getMessage());
			}
		} 		
		
		//Check if desired browser is Internet Explorer
		else if (WebBrowser.IE.toString().equals(driverType)) 
		{ 
			//Set property for IEDriverServer
			String path = getPath();
			File file = new File(path+"//driver//IEDriverServer.exe");
			System.setProperty("webdriver.ie.driver", file.getAbsolutePath());
			
			//Accept all SSL Certificates
			DesiredCapabilities capabilities = new DesiredCapabilities();
			capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
			
			//Create driver object
			driver = new InternetExplorerDriver(); 
		}
		
		//Check if desired browser is Chrome
		else if (WebBrowser.Chrome.toString().equals(driverType)) 
		{   
			String path = getPath();
		   
		   System.setProperty("webdriver.chrome.driver",path+"//driver//chromedriver.exe");
		   DesiredCapabilities capabilities = DesiredCapabilities.chrome();
		   ChromeOptions options = new ChromeOptions();
		   options.addArguments("test-type");
		   capabilities.setCapability("chrome.binary",path+"//driver//chromedriver.exe");
		   capabilities.setCapability(ChromeOptions.CAPABILITY, options);
		   driver = new ChromeDriver( options);
		}
		//If browser type is not matched, consider it as Firefox
		else if (WebBrowser.Remote.toString().equals(driverType))
		{   
			/*
			String browser =Configuration.readConfig("browser");
			String version = Configuration.readConfig("version");
			String os = Configuration.readConfig("os");
			String res = Configuration.readConfig("resolution");
			String username = Configuration.readConfig("LambdaTest_UserName");
			String accesskey = Configuration.readConfig("LambdaTest_AppKey");
			*/
			
			String browser=propertyReader.readApplicationFile("browser",filepath);
			String version=propertyReader.readApplicationFile("version",filepath);
			String os=propertyReader.readApplicationFile("os",filepath);
			String res=propertyReader.readApplicationFile("resolution",filepath);
			String username=propertyReader.readApplicationFile("LambdaTest_UserName",filepath);
			String accesskey=propertyReader.readApplicationFile("LambdaTest_AppKey",filepath);
			
			DesiredCapabilities capability = new DesiredCapabilities();
			capability.setCapability(CapabilityType.BROWSER_NAME, browser);
			capability.setCapability(CapabilityType.VERSION, version);
			capability.setCapability(CapabilityType.PLATFORM, os);
			capability.setCapability("build", "Vipin's First Test");
			capability.setCapability("name", "Running Single JAVA Test");
			capability.setCapability("screen_resolution", res);
			capability.setCapability("network", true);
			capability.setCapability("video", true);
			capability.setCapability("console", true);
			capability.setCapability("visual", true);

			String gridURL = "http://" + username + ":" + accesskey + "@hub.lambdatest.com/wd/hub";
			//String gridURL = "https://" + username + ":" + accesskey + "@ondemand.saucelabs.com:443/wd/hub";
		    Exception ei = null;
			try {
				driver = new RemoteWebDriver(new URL(gridURL), capability);
			} catch (Exception e) {
				ei=e;
				System.out.println("driver error");
				System.out.println(e.getMessage());
			}
		}
		else 
		{
			System.out.println("no error");
		}
		
		//Maximize window
		driver.manage().window().maximize();
		
		//Delete cookies
		driver.manage().deleteAllCookies();		
		driver.navigate().to(url);
			
	}	


	
	
	@AfterTest
	public void afterMainMethod() {		
		
		//driver.quit();
	}
	
	public WebDriver getWebDriver(){
		      
		    	return driver;
	}
	
	public int getRandomNumber(){
		   return n;
     }
	

	//Get absolute path
	public String getPath()
	{
		String path ="";		
		File file = new File("");
		String absolutePathOfFirstFile = file.getAbsolutePath();
		path = absolutePathOfFirstFile.replaceAll("\\\\+", "/");		
		return path;
	}
	

	
	//Capturing screenshot on failure 
	public void captureScreenshot(String fileName) 
	{
		try 
		{
			String screenshotName = this.getFileName(fileName);
	        FileOutputStream out = new FileOutputStream("screenshots//" + screenshotName + ".jpg");
	        out.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
	        out.close();
	        String path = getPath();
	        String  screen = "file://"+path+"/screenshots/"+screenshotName + ".jpg";
	        Reporter.log("<a href= '"+screen+  "'target='_blank' >" + screenshotName + "</a>");
	     }
		 catch (Exception e) 
		 { }
	 }
	
	//Creating file name
	public  String getFileName(String file)
	{
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 Calendar cal = Calendar.getInstance();		 
		 String fileName = file+dateFormat.format(cal.getTime());
		 return fileName;
	 }

}
