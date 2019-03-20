package com.myapp.Locator;


import java.io.File;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;

public class LocatorReader {

	private Document doc;

	public LocatorReader(String xmlName) {		
		SAXReader reader = new SAXReader();
		try {
			String path =getPath()+"//Src//com//myapp//Locator//";
			doc = reader.read(path+xmlName);
		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	  public static String getPath()
		{
			String path ="";		
			File file = new File("");
			String absolutePathOfFirstFile = file.getAbsolutePath();
			path = absolutePathOfFirstFile.replaceAll("\\\\+", "/");		
			return path;
		}
	
	public String getLocator(String locator){
		return doc.selectSingleNode("//" + locator.replace('.', '/')).getText();
		
	}
}
