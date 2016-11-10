package com.gitplex.plugin.maven;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * @goal generate-product-resources
 * @requiresDependencyResolution compile+runtime
 */
public class GenerateProductResourcesMojo extends AbstractMojo {
	
	/**
     * @parameter default-value="${project}"
     * @required
     * @readonly
	 */
	private MavenProject project;
	
	/**
	 * @parameter default-value="${executables}"
	 */
	private String executables;
	
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

    /**
     * The current repository/network configuration of Maven.
     *
     * @parameter default-value="${repositorySystemSession}"
     * @readonly
     */
    private RepositorySystemSession repoSession;

    /**
     * The project's remote repositories to use for the resolution of project dependencies.
     *
     * @parameter default-value="${project.remoteProjectRepositories}"
     * @readonly
     */
    private List<RemoteRepository> remoteRepos; 	

	public void execute() throws MojoExecutionException {
		if (executables == null)
			return;
		
		PluginUtils.checkResolvedArtifacts(project, true);
		
    	File bootDir = new File(project.getBuild().getDirectory(), PluginConstants.SANDBOX + "/boot");
    	if (!bootDir.exists())
    		bootDir.mkdirs();

    	Properties props = new Properties();
    	props.put("executables", executables);

    	PluginUtils.writeProperties(
    			new File(project.getBuild().getOutputDirectory(), PluginConstants.PRODUCT_PROPERTY_FILE), props);

    	Set<String> bootstrapKeys = new HashSet<String>();

    	for (Artifact artifact: PluginUtils.getBootstrapArtifacts(project, repoSystem, repoSession, remoteRepos))
    		bootstrapKeys.add(PluginUtils.getArtifactKey(artifact));
    	
    	PluginUtils.writeObject(new File(bootDir, PluginConstants.BOOTSTRAP_KEYS), bootstrapKeys);
    	
    	PluginUtils.writeClasspath(new File(bootDir, PluginConstants.SYSTEM_CLASSPATH), project, 
    			repoSystem, repoSession, remoteRepos);
    }

}