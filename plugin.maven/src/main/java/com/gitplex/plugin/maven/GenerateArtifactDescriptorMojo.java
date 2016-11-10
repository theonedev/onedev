package com.gitplex.plugin.maven;

import java.io.File;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * @goal generate-artifact-descriptor
 * @requiresDependencyResolution compile+runtime
 */
public class GenerateArtifactDescriptorMojo extends AbstractMojo {
	
	/**
     * @parameter default-value="${project}"
     * @required
     * @readonly
	 */
	private MavenProject project;
	
	/**
	 * @parameter default-value="${bootstrap}"
	 */
	private boolean bootstrap;

	public void execute() throws MojoExecutionException {
		PluginUtils.checkResolvedArtifacts(project, true);
		
    	File outputDir = new File(project.getBuild().getOutputDirectory());
    	
    	if (!outputDir.exists())
    		outputDir.mkdirs();
    	
    	File propsFile = new File(outputDir, PluginConstants.ARTIFACT_PROPERTY_FILE);
    	Properties props = new Properties();
    	props.put("id", project.getArtifact().getGroupId() + "." + project.getArtifact().getArtifactId());
    	props.put("version", project.getArtifact().getVersion());
    	
    	StringBuffer buffer = new StringBuffer();
    	for (Artifact artifact: project.getDependencyArtifacts()) {
    		if (PluginUtils.isRuntimeArtifact(artifact) 
    				&& PluginUtils.containsFile(artifact.getFile(), PluginConstants.ARTIFACT_PROPERTY_FILE)) {
	    		buffer.append(artifact.getGroupId()).append(".").append(artifact.getArtifactId())
	    			.append(":").append(artifact.getVersion()).append(";");
    		}
    	}
    	props.put("dependencies", buffer.toString());
    	
    	PluginUtils.writeProperties(propsFile, props);
    	
    	if (bootstrap)
    		PluginUtils.writeProperties(new File(outputDir, PluginConstants.BOOTSTRAP_PROPERTY_FILE), new Properties());
    }
    
}
