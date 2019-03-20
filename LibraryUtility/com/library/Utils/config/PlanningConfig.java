package com.library.Utils.config;

import java.io.File;

class PlanningConfig extends ProductConfig {

	/**
	 * 
	 * Create the Planning config by pointing to the appropriate directory and
	 * (for now) overriding the default user filename.
	 * 
	 */
	protected PlanningConfig() {
		super(BRANCH_ROOT + ".PlanningAndConsolidation.com.adaptiveinsight.".replace('.', File.separatorChar));
		defaultUsersFile = "PlanningAndConsolidationUsers";
	}

}
