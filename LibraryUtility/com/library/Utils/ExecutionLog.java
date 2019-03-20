package com.library.Utils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import com.library.Utils.DriverTestCase;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.Reporter;



public class ExecutionLog extends DriverTestCase {	
	//static String txt="<html><body>"+txt+"</html></body>";
	public WebDriver driver=new DriverTestCase().getWebDriver();
	 public  static void Log(String text)
	 {
		 Random rg = new Random();
		 int n = rg.nextInt(100000);
	     Date date = new Date(System.currentTimeMillis());
         String dateString = "pass"+date.toString()+n;
		 System.out.println(dateString);
	
		 Reporter.log("<html><body><b>||"+text+" || <br></br>"+ new ExecutionLog().captureScreenshotOnPass(dateString)+"</b></html></body>");
		 //Reporter.log("<html><body><b>||"+text+"||<br></br></b></html></body>");
		 ExecutionLog executionLog = new ExecutionLog();	
		 String dateTime = executionLog.getDate();
		 String fileName = executionLog.getFileName();
		 try
		 {			 
		    // Create file 
		    FileWriter fstream = new FileWriter(System.getProperty("user.dir")+"\\ExecutionLog\\"+fileName+".txt",true);
		    
		    BufferedWriter out = new BufferedWriter(fstream);
		    text = dateTime +" [info]  "+ text;
		    out.write(text);
		    out.newLine();
		    
		    //Close the output stream
		    out.close();		    
		 }
		 catch (Exception e)
		 { System.err.println("Error: " + e.getMessage()); }
		
		
	 }
		
		//Capturing screenshot on Pass
		public String captureScreenshotOnPass(String screenshotName) 
		{
			try 
			{
							
				FileOutputStream out = new FileOutputStream("screenshots//" + screenshotName + ".jpg");
		        out.write(((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES));
		        out.close();
		        String path = getPath();
		        String  screen = "file://"+path+"/screenshots/"+screenshotName + ".jpg";
		       Reporter.log("<a href= '"+screen+  "'target='_blank' >" + screenshotName + "</a>");
		        return " ";
		     }
			 catch (Exception e) 
			 { 
				 System.out.println(e+" "+driver+" "+getPath());
				 System.err.println("Error: " + e.getMessage());
				 return "no image";
			 }
		 }
	 public static void LogExceptionMessage(Exception e) throws IOException
	 {
		 ExecutionLog executionLog = new ExecutionLog();
		 String dateTime = executionLog.getDate();
		 ExecutionLog.Log(dateTime +" [info]  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Error message >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");		 
		 String fileName = executionLog.getFileName();
		 PrintWriter pw;		
		 try 
		 {
			pw = new PrintWriter(new FileWriter(System.getProperty("user.dir")+"//ExecutionLog//"+fileName+".txt", true));
			e.printStackTrace(pw);
			pw.close();
		 } 
		 catch (FileNotFoundException e1)
		 { e1.printStackTrace(); }		
	 }
	 
	 public static void LogErrorMessage(Error e) throws IOException
	 {
		 ExecutionLog executionLog = new ExecutionLog();
		 String dateTime = executionLog.getDate();
		 ExecutionLog.Log(dateTime +" [info]  <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<< Error message >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>");		 
		 String fileName = executionLog.getFileName();
		 PrintWriter pw;
		 try 
		 {
			pw = new PrintWriter(new FileWriter(System.getProperty("user.dir")+"//ExecutionLog//"+fileName+".txt", true));
			e.printStackTrace(pw);
			pw.close();
		 } 
		 catch (FileNotFoundException e1)
		 { e1.printStackTrace(); }		
	 }
	 
	 public  String getFileName()
	 {
		 DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		 Calendar cal = Calendar.getInstance();		 
		 String fileName = "Report-"+dateFormat.format(cal.getTime());
		 return fileName;
	 }
	 	 
	 public String getDate()
	 {
		 DateFormat dateFormat = new SimpleDateFormat("MMM dd,yyyy HH:mm:ss");
		 Calendar cal = Calendar.getInstance();	
		 String dateTime =  dateFormat.format(cal.getTime());
		 return dateTime;
	 }
	 
	 public static void LogAddClass(String text)
	 {
		 ExecutionLog executionLog = new ExecutionLog();	
		 String dateTime = executionLog.getDate();
		 String fileName = executionLog.getFileName();
		 try
		 {			 
		    // Create file 
		    FileWriter fstream = new FileWriter(System.getProperty("user.dir")+"//ExecutionLog//"+fileName+".txt",true);
		    BufferedWriter out = new BufferedWriter(fstream);
		    text = dateTime +" [info]  "+" Execution Started of Test Class "+ text;
		    out.newLine();
		    out.write("*****************************************************************************");
		    out.newLine();
		    out.write(text);
		    out.newLine();
		    out.write("*****************************************************************************");
		    out.newLine();
		    //Close the output stream
		    out.close();		    
		 }
		 catch (Exception e)
		 { System.err.println("Error: " + e.getMessage()); }
	 }
	 
	 public static void LogEndClass(String text)
	 {
		 ExecutionLog executionLog = new ExecutionLog();	
		 String dateTime = executionLog.getDate();
		 String fileName = executionLog.getFileName();
		 try
		 {			 
		    // Create file 
		    FileWriter fstream = new FileWriter(System.getProperty("user.dir")+"//ExecutionLog//"+fileName+".txt",true);
		    BufferedWriter out = new BufferedWriter(fstream);
		    text = dateTime +" [info]  End Execution of Test Script "+ text;		    
		    out.write(text);
		    out.newLine();
		    out.write("*****************************************************************************");
		    out.newLine();
		    //Close the output stream
		    out.close();		    
		 }
		 catch (Exception e)
		 { System.err.println("Error: " + e.getMessage()); }
	 }
	 
	 public static void main(String[] str){	 
		 Log("Test execution");		 
	 }
	  
}
