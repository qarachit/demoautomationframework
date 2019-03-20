package com.library.Utils;

public enum WebBrowser {
	FIREFOX("Firefox"),
	IE("IE"),
	CHROME("Chrome");	
		
	private String alias;
		
	private WebBrowser(String alias) {
		this.alias=alias;
	}
		
	public String getAlias() {
		return this.alias;
	}
}
