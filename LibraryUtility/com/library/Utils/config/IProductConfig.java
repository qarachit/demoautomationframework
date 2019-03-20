package com.library.Utils.config;

import java.io.File;

/***
 * 
 * Interface that provides access to the Planning and Consolidation information
 * 
 * @author jyates
 *
 */
public interface IProductConfig {
	/**
	 * get the default USer Container
	 * 
	 * @return
	 * @throws Exception 
	 */
	public IUserContainer getUserContainer() throws Exception;
	
	/**
	 * 
	 * get the default Application Values container
	 * 
	 * @return
	 * @throws Exception 
	 */
	public INamedValueContainer getApplicationValues() throws Exception;

	/**
	 * 
	 * create a User Data container based on the file specified.
	 * 
	 * - the file can be .xml, .xslx, .properties
	 * - It must be in the Data directory of the Base dir.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public IUserContainer getUserContainer(String fileName) throws Exception;
	
	/**
	 * 
	 * create a Application container based on the file specified.
	 * 
	 * - the file can be .xml, .xslx, .properties
	 * - It must be in the Data directory of the base dir.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public INamedValueContainer getApplicationValuesContainer(String fileName) throws Exception;
	
	/**
	 * 
	 * get a File object for the specified file in the Product's Data directory
	 * 
	 * - the file can be .xml, .xslx, .properties
	 * - It must be in the Data directory of the base dir of the Product.
	 * 
	 * @param fileName
	 * @return
	 * @throws Exception
	 */
	public File getDataFile(String fileName) throws Exception;
}