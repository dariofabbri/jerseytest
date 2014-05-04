package com.iconamanagement.rest;

import org.glassfish.jersey.server.ResourceConfig;

public class TheApplication extends ResourceConfig {

	public TheApplication() {
		packages(true, "com.iconamanagement.rest");
	}
}
