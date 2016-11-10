package com.gitplex.plugin.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.UnArchiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.util.ArchiveEntryUtils;
import org.codehaus.plexus.components.io.fileselectors.IncludeExcludeFileSelector;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

import com.google.common.base.Preconditions;

/**
 * @goal create-product-sandbox
 * @requiresDependencyResolution compile+runtime
 */
public class CreateProductSandboxMojo extends AbstractMojo {
	
	/**
     * @parameter default-value="${project}"
     * @required
     * @readonly
	 */
	private MavenProject project;
	
    /**
     * The entry point to Aether, i.e. the component doing all the work.
     *
     * @component
     */
    private RepositorySystem repoSystem;

	/**
	 * @parameter default-value="${executables}"
	 */
	private String executables;

	/**
	 * @parameter default-value="${moduleClass}"
	 */
	private String moduleClass;

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

    /**
	 * @component
	 */
	private ArchiverManager archiverManager;
	
	public void execute() throws MojoExecutionException {
		if (moduleClass == null || executables != null)
			return;
		
		PluginUtils.checkResolvedArtifacts(project, true);
		
    	for (Artifact artifact: project.getArtifacts()) {
    		if (PluginUtils.isRuntimeArtifact(artifact) 
    				&& PluginUtils.containsFile(artifact.getFile(), PluginConstants.PRODUCT_PROPERTY_FILE)) {
    			File buildDir = new File(project.getBuild().getDirectory());
    			File sandboxDir = new File(buildDir, PluginConstants.SANDBOX);

    	    	File classpathFile = new File(sandboxDir, "boot/" + PluginConstants.SYSTEM_CLASSPATH);
    	    	if (!classpathFile.exists()) {
    	    		if (artifact.getFile().isFile()) {
    	    			UnArchiver unArchiver;
    	    			try {
							unArchiver = archiverManager.getUnArchiver(artifact.getFile());
						} catch (NoSuchArchiverException e) {
							throw new RuntimeException(e);
						}
    	    			unArchiver.setSourceFile(artifact.getFile());
    	    			unArchiver.setDestDirectory(buildDir);
	        	    	unArchiver.setOverwrite(true);
	        	    	IncludeExcludeFileSelector[] selectors = new IncludeExcludeFileSelector[] {new IncludeExcludeFileSelector()};
	        	    	selectors[0].setIncludes(new String[]{PluginConstants.SANDBOX + "/**"});
	        	    	unArchiver.setFileSelectors(selectors);
	        	    	unArchiver.extract();
    	    		} else {
    					File srcDir = new File(artifact.getFile().getParentFile(), PluginConstants.SANDBOX);
        				try {
    						FileUtils.copyDirectoryStructure(srcDir, sandboxDir);
    					} catch (IOException e) {
    						throw new RuntimeException(e);
    					}
    	    		}

    	    		Properties productProps = PluginUtils.loadProperties(
    	    				artifact.getFile(), PluginConstants.PRODUCT_PROPERTY_FILE);
    	    		Preconditions.checkNotNull(productProps);
    	    		
    				String executables = productProps.getProperty("executables");
    				if (executables != null && executables.trim().length() != 0) {
    					DirectoryScanner scanner = new DirectoryScanner();
    					scanner.setBasedir(sandboxDir);
    					List<String> includes = new ArrayList<String>();
    					for (String each: StringUtils.split(executables, ",")) {
    						if (each.trim().length() != 0)
    							includes.add(each.trim());
    					}
    					scanner.setIncludes(includes.toArray(new String[0]));
    					
    					scanner.scan();
    					
    					Logger logger = PluginUtils.toLogger(getLog(), "create-product-sandbox");
    					for (String each: scanner.getIncludedFiles())
    						ArchiveEntryUtils.chmod(new File(sandboxDir, each), 0755, logger);
    				}
        	    	PluginUtils.writeClasspath(classpathFile, project, repoSystem, repoSession, remoteRepos);
    			}
    			break;
    		}
    	}
    }
    
}
