package com.library.Utils;

public enum OperatingSystem {
	WINDOWS_XP("XP"),
	WINDOWS_VISTA("VISTA"),
	WINDOWS_7("WINDOWS"),
	LINUX("Linux");
	
	private String alias;
	
	private OperatingSystem(String alias) {
		this.alias=alias;
	}
	
	public String getAlias() {
		return this.alias;
	}
}
