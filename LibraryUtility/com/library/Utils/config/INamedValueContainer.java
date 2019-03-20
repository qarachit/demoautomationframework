package com.library.Utils.config;
/**
 * 
 * An object that you can get a value from a key
 * 
 * @author jyates
 *
 */
public interface INamedValueContainer {
	/**
	 * 
	 * Returns the value from this container associated with key
	 * 
	 * @param key
	 * @return a value
	 * @throws Exception
	 */
	public String getValue(String key) throws Exception;
}
