package com.library.Utils;


import java.io.*;
import java.util.*;

public class PropertyReader
{	
	String path =  getPath();  	
    public String readApplicationFile(String key, String file){ 
    	String value = "";
        try{         	  
	          Properties prop = new Properties();
	          File f = new File(path +file );
	        
	          if(f.exists()){
		          prop.load(new FileInputStream(f));
		          value = prop.getProperty(key); 		        
          	}
	   }
        catch(Exception e){  
           System.out.println("Failed to read from application.properties file.");  
        }
        return value;
     } 
    
	public String getPath()
	{
		String path ="";		
		File file = new File("");
		String absolutePathOfFirstFile = file.getAbsolutePath();
		path = absolutePathOfFirstFile.replaceAll("\\\\+", "/");		
		return path;
	}
 
}