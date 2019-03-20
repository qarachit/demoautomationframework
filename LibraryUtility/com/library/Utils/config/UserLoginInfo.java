package com.library.Utils.config;

/**
 * 
 * 
 * A class to encapsulate the user, login and (maybe) company.
 * 
 * @author jyates
 *
 */
public class UserLoginInfo {
	public String login = null;
	public String password = null;
	public String company = null;
	public UserLoginInfo(String aLogin, String aPassword, String aCompany) {
		login = aLogin;
		password = aPassword;
		company = aCompany;
	}
	public String toString() {
		return String.format("UserLoginInfo: %s (%s) in %s", login, password, company);
	}
}
