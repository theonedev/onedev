package com.pmease.commons.wicket.assets.ace;

import com.pmease.commons.wicket.VersionlessJavaScriptResourceReference;

/**
 * Make sure to use versionless resource reference for all ACE relevant files to avoid 
 * inserting version string in url; otherwise ace.require will not able to load modules 
 * and themes. 
 */
@SuppressWarnings("serial")
public class AceResourceReference extends VersionlessJavaScriptResourceReference {

	public static final AceResourceReference INSTANCE = new AceResourceReference();
	
	private AceResourceReference() {
		super(AceResourceReference.class, "v20141220/noconflict/ace.js");
	}

}
