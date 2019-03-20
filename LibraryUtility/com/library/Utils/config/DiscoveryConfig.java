package com.library.Utils.config;

import java.io.File;

class DiscoveryConfig extends ProductConfig {

	/**
	 * 
	 * Create the DiscoveryConfig by specifying its data directory
	 * 
	 */
	protected DiscoveryConfig() {
		super(BRANCH_ROOT + ".Discovery.com.discovery.".replace('.', File.separatorChar));
	}
}
