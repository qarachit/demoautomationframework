package com.library.Utils.config;

/**
 * 
 * A Named Value container with a method to get login info.
 * 
 * @author jyates
 *
 */
public interface IUserContainer extends INamedValueContainer {
	public UserLoginInfo getUserLoginInfo(String key) throws Exception;
}
