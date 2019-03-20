package com.library.Utils.config;

import java.io.File;

class IntegrationConfig extends ProductConfig {
	/**
	 * 
	 * Specify the Integration config by pointing to the approprieat directory.
	 * 
	 */
	protected IntegrationConfig() {
		super(BRANCH_ROOT + ".Integration.com.adaptive.integration.".replace('.', File.separatorChar));
	}
}
