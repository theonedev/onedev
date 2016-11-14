package com.gitplex.plugin.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.archiver.Archiver;
import org.codehaus.plexus.archiver.manager.ArchiverManager;
import org.codehaus.plexus.archiver.manager.NoSuchArchiverException;
import org.codehaus.plexus.archiver.util.DefaultArchivedFileSet;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.IOUtil;
import org.codehaus.plexus.util.StringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;

public class PluginUtils {
	
	public static boolean containsFile(File file, String path) {
		if (file.isFile() && file.getName().endsWith(".jar")) {
			JarFile jar = null;
			try {
				jar = new JarFile(file);
				return jar.getJarEntry(path) != null; 
			} catch (Exception e) {
				throw unchecked(e);
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
					}
				}
			}
		} else if (file.isDirectory() && new File(file, path).exists()) {
			return true;
		} else { 
			return false;
		}
	}
	
	public static Properties loadProperties(File file, String path) {
		if (file.isFile() && file.getName().endsWith(".jar")) {
			JarFile jar = null;
			try {
				jar = new JarFile(file);
				JarEntry entry = jar.getJarEntry(path);
				if (entry != null) {
					InputStream is = null;
					try {
						is = jar.getInputStream(entry);
						Properties props = new Properties();
						props.load(is);
						return props;
					} catch (IOException e) {
						throw new RuntimeException(e);
					} finally {
						if (is != null) {
							is.close();
						}
					}
				} else {
					return null;
				}
			} catch (Exception e) {
				throw unchecked(e);
			} finally {
				if (jar != null) {
					try {
						jar.close();
					} catch (IOException e) {
					}
				}
			}
		} else if (file.isDirectory() && new File(file, path).exists()) {
			Properties props = new Properties();
			InputStream is = null;
			try {
				is = new FileInputStream(new File(file, path));
				props.load(is);
				return props;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} finally {
				if (is != null) {
					try {
						is.close();
					} catch (IOException e) {
					}
				}
			}
		} else {
			return null;
		}
	}

	public static boolean isRuntimeArtifact(Artifact artifact) {
		return artifact.getScope().equals(Artifact.SCOPE_COMPILE) || artifact.getScope().equals(Artifact.SCOPE_RUNTIME) 
				|| artifact.getScope().equals(Artifact.SCOPE_SYSTEM);
	}
	
	public static void writeClasspath(File file, MavenProject project, RepositorySystem repoSystem, 
			RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) throws MojoExecutionException {
		
    	Map<String, File> classpath = new HashMap<String, File>();
    	
    	for (Artifact artifact: project.getArtifacts()) { 
    		if (isRuntimeArtifact(artifact)) 
    			classpath.put(getArtifactKey(artifact), artifact.getFile());
    	}
    	
    	for (Artifact artifact: getBootstrapArtifacts(project, repoSystem, repoSession, remoteRepos))
    		classpath.put(getArtifactKey(artifact), artifact.getFile());

    	classpath.put(getArtifactKey(project.getArtifact()), new File(project.getBuild().getOutputDirectory()));
    	
    	writeObject(file, classpath);
	}
	
	public static Collection<Artifact> getBootstrapArtifacts(MavenProject project, RepositorySystem repoSystem, 
			RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {
		Collection<Artifact> bootArtifacts = new HashSet<Artifact>();
		for (Artifact each: project.getArtifacts()) {
			if (containsFile(each.getFile(), PluginConstants.BOOTSTRAP_PROPERTY_FILE)) {
				bootArtifacts.add(each);
				org.eclipse.aether.artifact.Artifact aetherArtifact = new org.eclipse.aether.artifact.DefaultArtifact(
						each.getGroupId(), each.getArtifactId(), each.getClassifier(), each.getType(), 
						each.getVersion());
                CollectRequest collectRequest = new CollectRequest(new Dependency(aetherArtifact, null), 
                		new ArrayList<Dependency>(), remoteRepos);
                DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, null);
                try {
					for (ArtifactResult result: repoSystem.resolveDependencies(repoSession, dependencyRequest).getArtifactResults()) {
						Dependency dependency = result.getRequest().getDependencyNode().getDependency();
						if (dependency.getArtifact()  != null && dependency.getArtifact().getFile() != null) {
							aetherArtifact = dependency.getArtifact();
							Artifact artifact = new DefaultArtifact(aetherArtifact.getGroupId(), aetherArtifact.getArtifactId(), 
									aetherArtifact.getVersion(), dependency.getScope(), 
									aetherArtifact.getExtension(), aetherArtifact.getClassifier(), null);
							artifact.setFile(aetherArtifact.getFile());
							if (isRuntimeArtifact(artifact))
								bootArtifacts.add(artifact);
						}
					}
				} catch (Exception e) {
					throw PluginUtils.unchecked(e);
				}
			}
		}
		return bootArtifacts;
	}
	
	@SuppressWarnings("unchecked")
	public static void populateArtifacts(MavenProject project, File sandboxDir, ArchiverManager archiverManager, 
			RepositorySystem repoSystem, RepositorySystemSession repoSession, List<RemoteRepository> remoteRepos) {
		
		if (project.getArtifact().getFile() == null)
			throw new RuntimeException("Project artifact not generated yet."); 

		// copy necessary library files to sandbox so that sandbox can 
		// be executed from command line
		File bootDir = new File(sandboxDir, "boot");
		File libDir = new File(sandboxDir, "lib");

		if (!bootDir.exists())
			bootDir.mkdirs();
		if (!libDir.exists())
			libDir.mkdirs();
		
		Set<String> bootstrapKeys = (Set<String>) readObject(
				new File(bootDir, PluginConstants.BOOTSTRAP_KEYS));

		Set<Artifact> artifacts = new HashSet<Artifact>();
		for (Artifact artifact: project.getArtifacts()) {
			if (isRuntimeArtifact(artifact))
				artifacts.add(artifact);
		}
		for (Artifact artifact: getBootstrapArtifacts(project, repoSystem, repoSession, remoteRepos))
			artifacts.add(artifact);
		artifacts.add(project.getArtifact());
		
    	for (Artifact artifact: artifacts) {
    		String artifactKey = getArtifactKey(artifact);

			File destFile;
			if (bootstrapKeys.contains(artifactKey))
				destFile = new File(bootDir, artifactKey);
			else
				destFile = new File(libDir, artifactKey);
			copyArtifact(artifact.getFile(), destFile, archiverManager);
    	}
	}
	
	private static void copyArtifact(File srcFile, File destFile, ArchiverManager archiverManager) {
		if (containsFile(srcFile, PluginConstants.PRODUCT_PROPERTY_FILE)) {
	    	Archiver archiver;
			try {
				archiver = archiverManager.getArchiver("jar");
			} catch (NoSuchArchiverException e) {
				throw new RuntimeException(e);
			}
			archiver.setDestFile(destFile);
			DefaultArchivedFileSet fileSet = new DefaultArchivedFileSet();
			fileSet.setArchive(srcFile);
			fileSet.setExcludes(new String[]{"sandbox/**", "archetype-resources/**"});
			archiver.addArchivedFileSet(fileSet);
			
			try {
				archiver.createArchive();
			} catch (Exception e) {
				throw unchecked(e);
			}
		} else {
			try {
				FileUtils.copyFile(srcFile, destFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	public static Logger toLogger(final Log log, final String name) {
		return new Logger() {

			@Override
			public void debug(String message) {
				log.debug(message);
			}

			@Override
			public void debug(String message, Throwable throwable) {
				log.debug(message, throwable);
			}

			@Override
			public boolean isDebugEnabled() {
				return log.isDebugEnabled();
			}

			@Override
			public void info(String message) {
				log.info(message);
			}

			@Override
			public void info(String message, Throwable throwable) {
				log.info(message, throwable);
			}

			@Override
			public boolean isInfoEnabled() {
				return log.isInfoEnabled();
			}

			@Override
			public void warn(String message) {
				log.warn(message);
			}

			@Override
			public void warn(String message, Throwable throwable) {
				log.warn(message, throwable);
			}

			@Override
			public boolean isWarnEnabled() {
				return log.isWarnEnabled();
			}

			@Override
			public void error(String message) {
				log.error(message);
			}

			@Override
			public void error(String message, Throwable throwable) {
				log.error(message, throwable);
			}

			@Override
			public boolean isErrorEnabled() {
				return log.isErrorEnabled();
			}

			@Override
			public void fatalError(String message) {
				log.error(message);
			}

			@Override
			public void fatalError(String message, Throwable throwable) {
				log.error(message, throwable);
			}

			@Override
			public boolean isFatalErrorEnabled() {
				return log.isErrorEnabled();
			}

			@Override
			public int getThreshold() {
				return 0;
			}

			@Override
			public void setThreshold(int threshold) {
			}

			@Override
			public Logger getChildLogger(String name) {
				return this;
			}

			@Override
			public String getName() {
				return name;
			}
			
		};
	}
	
	public static void writeProperties(File file, Properties props) {
		if (!file.getParentFile().exists())
			file.getParentFile().mkdirs();
    	OutputStream os = null;
    	try {
    		os = new FileOutputStream(file);
    		props.store(os, null);
    	} catch (Exception e) {
    		throw unchecked(e);
		} finally {
			if (os != null) {
				try {
					os.close();
				} catch (IOException e) {
				}
			}
    	}
	}
	
	public static Properties loadProperties(File file) {
		Properties props = new Properties();
		
		InputStream is = null;
		try {
			is = new FileInputStream(file);
			props.load(is);
			return props;
		} catch (Exception e) {
			throw unchecked(e);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static void writeObject(File file, Object obj) {
    	ObjectOutputStream oos = null;
    	try {
    		oos = new ObjectOutputStream(new FileOutputStream(file));
    		oos.writeObject(obj);
    	} catch (Exception e) {
    		throw unchecked(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
				}
			}
    	}
	}
	
	public static Object readObject(File file) {
    	ObjectInputStream ois = null;
    	try {
    		ois = new ObjectInputStream(new FileInputStream(file));
    		return ois.readObject();
    	} catch (Exception e) {
			throw unchecked(e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
				}
			}
    	}
	}
	
	public static RuntimeException unchecked(Throwable e) {
		if (e instanceof RuntimeException)
			throw (RuntimeException)e;
		else
			throw new RuntimeException(e);
	}

	public static String getArtifactKey(Artifact artifact) {
		return artifact.getGroupId() + "." + artifact.getArtifactId() + "-" + artifact.getVersion() 
				+ (artifact.hasClassifier()? "-" + artifact.getClassifier():"") + "." + artifact.getType();
	}
	
	public static Collection<String> splitAndTrim(String str, String separator) {
		Collection<String> fields = new ArrayList<String>();
		for (String each: StringUtils.split(str, separator)) {
			if (each != null && each.trim().length() != 0)
				fields.add(each.trim());
		}
		return fields;
	}
	
	public static void checkResolvedArtifacts(MavenProject project, boolean onlyRuntime) {
		for (Artifact artifact: project.getArtifacts()) {
			if ((onlyRuntime && isRuntimeArtifact(artifact) || !onlyRuntime) && artifact.getFile() == null) {
				throw new RuntimeException("Failed to resolve artifact '" + artifact 
						+ "', please check if it has been relocated.");
			}
		}
		for (Artifact artifact: project.getDependencyArtifacts()) {
			if ((onlyRuntime && isRuntimeArtifact(artifact) || !onlyRuntime) && artifact.getFile() == null) {
				throw new RuntimeException("Failed to resolve artifact '" + artifact 
						+ "', please check if it has been relocated.");
			}
		}
	}

	public static void addFileToJar(JarOutputStream jos, File file, int offset, boolean includeArchetypeResources) {
		String entryName = file.getAbsolutePath().substring(offset + 1);
		
		if (File.separatorChar != '/')
			entryName = entryName.replace('\\', '/');
		
		/*
		 * When a produt or a plugin is created via archetype "archetype.product" or "archetype.plugin",
		 * it will contain an archetype targeted at creating extensions to the product or plugin. To 
		 * avoid the issue that the directory "archetype-resources/" representing the second archetype 
		 * is trimmed when getting resources out of the archetype jar file 
		 * by maven "DefaultArchetypeArtifactManager.getFilesetArchetypeResources" at time of generating 
		 * the product or plugin, we rename it to "archetype-resource/"; otherwise, m2e will report 
		 * weird errors when generating product/plugin using the archetype. Below code rename it back when 
		 * package the archetype to confirm to maven archetype specification.  
		 */
		
		if (entryName.startsWith(PluginConstants.ARCHETYPE_RESOURCE))
			entryName = PluginConstants.ARCHETYPE_RESOURCES + entryName.substring(PluginConstants.ARCHETYPE_RESOURCE.length());
		
		if (file.isDirectory())
			entryName += "/";

		if ((entryName.startsWith(PluginConstants.ARCHETYPE_RESOURCES) || entryName.equals("META-INF/maven/archetype-metadata.xml")) && !includeArchetypeResources)
			return;
		
		try {
			jos.putNextEntry(new JarEntry(entryName));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		if (file.isFile()) {
			try {
				FileInputStream is = null;
				try {
					is = new FileInputStream(file);
					IOUtil.copy(is, jos);
				} finally {
					IOUtil.close(is);
				}

				jos.flush();
				jos.closeEntry();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public static void listFiles(File dir, List<File> fileList) {
		for (File file : dir.listFiles()) {
			fileList.add(file);
			if (file.isDirectory())
				listFiles(file, fileList);
		}
	}
}
