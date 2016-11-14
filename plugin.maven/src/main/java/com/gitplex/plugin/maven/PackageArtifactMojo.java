package com.gitplex.plugin.maven;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.Attributes;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.util.IOUtil;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.repository.RemoteRepository;

/**
 * @goal package-artifact
 * @requiresDependencyResolution compile+runtime
 */
public class PackageArtifactMojo extends AbstractMojo {
	
	/**
     * @parameter default-value="${project}"
     * @required
     * @readonly
	 */
	private MavenProject project;

    /**
     * @component
     */
    private ArchiverManager archiverManager;    

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
		PluginUtils.checkResolvedArtifacts(project, true);

		File jarFile = new File(
				project.getBuild().getDirectory(), 
				project.getBuild().getFinalName() + ".jar");

		getLog().info("Creating jar: " + jarFile.getAbsolutePath());
		
		File sandboxDir = new File(project.getBuild().getDirectory(), PluginConstants.SANDBOX);

		/*
		 * Below code is adapted from Maven's DefaultArchetypeManager to create an archetype style 
		 * archive to avoid the "Can not override..." warning when use this archive to generate 
		 * an archetype.
		 */
		JarOutputStream jos = null;
		try {
			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
			jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);
			jos.setLevel(9);

			List<File> files = new ArrayList<File>();
			PluginUtils.listFiles(new File(project.getBuild().getOutputDirectory()), files);

			boolean includeArchetypeResources = (project.getPlugin("org.apache.maven.plugins:maven-archetype-plugin")!=null);

			for (File file : files) { 
				PluginUtils.addFileToJar(jos, file, project.getBuild().getOutputDirectory().length(), includeArchetypeResources);
			}
			
			if (new File(project.getBuild().getOutputDirectory(), PluginConstants.PRODUCT_PROPERTY_FILE).exists()) {
				files.clear();
				PluginUtils.listFiles(sandboxDir, files);
				
				Set<String> excluded = new HashSet<String>();
				for (Artifact artifact: project.getArtifacts())
					excluded.add(PluginUtils.getArtifactKey(artifact));
				excluded.add(PluginConstants.SYSTEM_CLASSPATH);
				excluded.add(jarFile.getName());

				for (File file: files) {
					if (!excluded.contains(file.getName())) 
						PluginUtils.addFileToJar(jos, file, sandboxDir.getParent().length(), includeArchetypeResources);
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			IOUtil.close(jos);
		}
		
		project.getArtifact().setFile(jarFile);
		
		if (sandboxDir.exists()) {
			PluginUtils.populateArtifacts(project, sandboxDir, archiverManager, repoSystem, repoSession, remoteRepos);

			if (!new File(project.getBuild().getOutputDirectory(), PluginConstants.PRODUCT_PROPERTY_FILE).exists()) {
				Archiver archiver;
				try {
					archiver = archiverManager.getArchiver("zip");
				} catch (NoSuchArchiverException e) {
					throw new RuntimeException(e);
				}
		    	
				Collection<Artifact> bootstrapArtifacts = PluginUtils.getBootstrapArtifacts(
						project, repoSystem, repoSession, remoteRepos);
				
		    	Set<String> pluginTrails = new HashSet<String>();
		    	for (Artifact artifact: project.getArtifacts()) {    	
		    		if (PluginUtils.containsFile(artifact.getFile(), PluginConstants.PLUGIN_PROPERTY_FILE)) {
						String trail = artifact.getGroupId() + ":" + artifact.getArtifactId() + ":" 
								+ artifact.getType() + ":" 
								+ (artifact.hasClassifier()?artifact.getClassifier()+":":"") 
								+ artifact.getVersion();
						pluginTrails.add(trail);
		    		}
		    	}    	

		    	for (Artifact artifact: project.getArtifacts()) {
		    		if (PluginUtils.isRuntimeArtifact(artifact) && !bootstrapArtifacts.contains(artifact)) {
			    		boolean independent = true;
			    		for (String trail: artifact.getDependencyTrail()) {
			    			if (pluginTrails.contains(trail)) {
			    				independent = false;
			    				break;
			    			}
			    		}
			    		if (independent)
			    			archiver.addFile(artifact.getFile(), PluginUtils.getArtifactKey(artifact));
		    		}
		    	}
		    	
		    	archiver.addFile(project.getArtifact().getFile(), PluginUtils.getArtifactKey(project.getArtifact()));

				File distFile = new File(project.getBuild().getDirectory(), project.getBuild().getFinalName() + ".zip");
		    	archiver.setDestFile(distFile);
		    	
		    	try {
					archiver.createArchive();
				} catch (Exception e) {
					throw PluginUtils.unchecked(e);
				}		
			}			
		}
    }
}
