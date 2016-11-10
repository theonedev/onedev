package com.gitplex.plugin.maven;

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * @goal copy-antlr-tokens
 * @requiresDependencyResolution compile+runtime
 */
public class CopyAntlrTokensMojo extends AbstractMojo {
	
	/**
     * @parameter default-value="${project}"
     * @required
     * @readonly
	 */
	private MavenProject project;
	
	public void execute() throws MojoExecutionException, MojoFailureException {
		PluginUtils.checkResolvedArtifacts(project, true);
		
    	File outputDir = new File(project.getBuild().getOutputDirectory());
    	
    	if (!outputDir.exists())
    		outputDir.mkdirs();
    	
    	File antlr4Dir = new File(project.getBasedir(), "target/generated-sources/antlr4");
    	if (antlr4Dir.exists()) {
        	for (File file: antlr4Dir.listFiles()) {
        		if (file.getName().endsWith(".tokens")) {
        			try {
    					FileUtils.copyFileToDirectory(file, outputDir);
    				} catch (IOException e) {
    					throw new RuntimeException(e);
    				}
        		}
        	}
    	}
    }
    
}
