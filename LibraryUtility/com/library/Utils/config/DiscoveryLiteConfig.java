package com.library.Utils.config;

import java.io.File;

class DiscoveryLiteConfig extends ProductConfig {

	/**
	 * 
	 * Create the DiscoveryLiteConfig by specifying its data directory
	 * 
	 */
	protected DiscoveryLiteConfig() {
		super(BRANCH_ROOT + ".DiscoveryLite.com.discoverylite.".replace('.', File.separatorChar));
	}
}
