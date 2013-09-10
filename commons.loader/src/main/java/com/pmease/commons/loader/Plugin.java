package com.pmease.commons.loader;

import java.util.Set;

import com.pmease.commons.bootstrap.Lifecycle;
import com.pmease.commons.util.dependency.Dependency;

public interface Plugin extends Dependency, Lifecycle {
	
	String getId();

	String getName();
	
	String getVendor();
	
	String getVersion();

	String getDescription();

	Set<String> getDependencyIds();

	/**
	 * This function will be called before starting other plugins depending on this plugin.
	 */
	public void start();
	
	/**
	 * This function will be called after other plugins depending on this plugin have been started.
	 */
	public void postStart();
	
	/**
	 * This function will be called before stopping other plugins depending on this plugin.
	 */
	public void preStop();
	
	/**
	 * This function will be called after other plugins depending on this plugin have been stopped.
	 */
	public void stop();

}
