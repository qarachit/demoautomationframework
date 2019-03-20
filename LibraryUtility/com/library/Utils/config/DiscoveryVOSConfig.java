package com.library.Utils.config;

import java.io.File;

class DiscoveryVOSConfig extends ProductConfig {

	/**
	 * 
	 * Create the DiscoveryVOSConfig by specifying its data directory
	 * 
	 */
	protected DiscoveryVOSConfig() {
		super(BRANCH_ROOT + ".DiscoveryVOS.com.discoveryVOS.".replace('.', File.separatorChar));
	}
}
