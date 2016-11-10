package com.gitplex.plugin.maven;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @goal check-project-dependencies
 * @requiresDependencyResolution compile+runtime
 */
public class CheckProjectDependenciesMojo extends AbstractMojo {

	/**
	 * @parameter default-value="${project}"
	 * @required
	 * @readonly
	 */
	private MavenProject project;

	/**
	 * @parameter default-value="${aggregation}"
	 */
	private boolean aggregation;

	@Override
	public void execute() throws MojoExecutionException, MojoFailureException {
		PluginUtils.checkResolvedArtifacts(project, true);
		
		Map<String, String> projectVersions = new HashMap<String, String>();

		File workspaceDir = project.getBasedir().getParentFile();
		List<File> projectDirs = new ArrayList<>();
		projectDirs.add(workspaceDir);
		for (File file: workspaceDir.listFiles())
			projectDirs.add(file);
		for (File projectDir: projectDirs) {
			if (!projectDir.equals(project.getBasedir())) {
				File propsFile = new File(projectDir, "target/classes/"
						+ PluginConstants.ARTIFACT_PROPERTY_FILE);
				if (propsFile.exists()) {
					Properties props = PluginUtils.loadProperties(propsFile);
					projectVersions.put(props.getProperty("id"), props.getProperty("version"));
				} else {
					File pomFile = new File(projectDir, "pom.xml");
					if (pomFile.exists()) {
						try {
							DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
							Document document = builder.parse(pomFile);
							NodeList nodeList = document.getElementsByTagName("groupId");
							String groupId = null;
							if (nodeList.getLength() != 0) {
								groupId = nodeList.item(0).getTextContent().trim();
							} 
							
							nodeList = document.getElementsByTagName("artifactId");
							if (nodeList.getLength() == 0) {
								throw new MojoFailureException("Can not determine artifactId in POM '" + 
										pomFile.getAbsolutePath() + "'.");
							}
							String artifactId = nodeList.item(0).getTextContent().trim();
							
							nodeList = document.getElementsByTagName("version");
							String version = null;
							if (nodeList.getLength() != 0)
								version = nodeList.item(0).getTextContent().trim();
							
							if (groupId == null) {
								nodeList = document.getElementsByTagName("parent");
								if (nodeList.getLength() == 0) {
									throw new MojoFailureException("Unable to determine groupId in POM '" + 
											pomFile.getAbsolutePath() + "'.");
								}
								Element parentElement = (Element) nodeList.item(0);
								
								nodeList = parentElement.getElementsByTagName("groupId");
								if (nodeList.getLength() == 0) {
									throw new MojoFailureException("Unable to determine groupId in POM '" + 
											pomFile.getAbsolutePath() + "'.");
								}
								groupId = nodeList.item(0).getTextContent().trim();
							}

							if (version == null) {
								nodeList = document.getElementsByTagName("parent");
								if (nodeList.getLength() == 0) {
									throw new MojoFailureException("Unable to determine version in POM '" + 
											pomFile.getAbsolutePath() + "'.");
								}
								Element parentElement = (Element) nodeList.item(0);
								
								nodeList = parentElement.getElementsByTagName("version");
								if (nodeList.getLength() == 0) {
									throw new MojoFailureException("Unable to determine version in POM '" + 
											pomFile.getAbsolutePath() + "'.");
								}
								version = nodeList.item(0).getTextContent().trim();
							}
							
							projectVersions.put(groupId + "." + artifactId, version);
						} catch (ParserConfigurationException e) {
							throw new MojoFailureException("Error parsing POM file '" + pomFile.getAbsolutePath() + "'.", e);
						} catch (SAXException e) {
							throw new MojoFailureException("Error parsing POM file '" + pomFile.getAbsolutePath() + "'.", e);
						} catch (IOException e) {
							throw new MojoFailureException("Error reading POM file '" + pomFile.getAbsolutePath() + "'.", e);
						}
					}
				}
			}
		}

		List<String> errors = new ArrayList<String>();

		for (Artifact artifact : project.getDependencyArtifacts()) {
			String projectKey = artifact.getGroupId() + "." + artifact.getArtifactId();
			if (!artifact.getScope().equals(Artifact.SCOPE_TEST)) {
				String projectVersion = projectVersions.get(projectKey);
				if (projectVersion != null) {
					if (!projectVersion.equals(artifact.getVersion())) {
						errors.add("Dependency to project '" + projectKey + "' should use version '" 
								+ projectVersion + "'");
					}
				}
			}
		}
		
		MavenProject parentProject = project.getParent();
		if (parentProject != null) {
			String projectKey = parentProject.getGroupId() + "." + parentProject.getArtifactId();
			String projectVersion = projectVersions.get(projectKey);
			if (projectVersion != null) {
				if (!projectVersion.equals(parentProject.getVersion())) {
					errors.add("Parent project '" + projectKey + "' should use version '" 
							+ projectVersion + "'");
				}
			}
		}

		if (aggregation) {
			Set<String> dependencyKeys = new HashSet<String>();
			for (Artifact artifact : project.getArtifacts()) {
				if (!artifact.getScope().equals(Artifact.SCOPE_TEST))
					dependencyKeys.add(artifact.getGroupId() + "." + artifact.getArtifactId());
			}

			Set<String> pluginKeys = new HashSet<String>();
			for (File projectDir : workspaceDir.listFiles()) {
				if (!projectDir.equals(project.getBasedir())) {
					File propsFile = new File(projectDir, "target/classes/"
							+ PluginConstants.PLUGIN_PROPERTY_FILE);
					if (propsFile.exists()) {
						Properties props = PluginUtils.loadProperties(propsFile);
						pluginKeys.add(props.getProperty("id"));
					}
				}
			}

			for (String pluginKey: pluginKeys) {
				if (!dependencyKeys.contains(pluginKey))
					errors.add("Missed dependency to project '" + pluginKey + "'.");
			}

			Map<String, Set<String>> artifactVersions = new HashMap<String, Set<String>>();
			for (Artifact artifact: project.getArtifacts()) {
				if (PluginUtils.isRuntimeArtifact(artifact)) {
					Properties props = PluginUtils.loadProperties(artifact.getFile(), PluginConstants.ARTIFACT_PROPERTY_FILE);
					if (props != null) {
						String dependencies = props.getProperty("dependencies");
						if (dependencies != null) {
							for (String dependency: PluginUtils.splitAndTrim(dependencies, ";")) {
								String[] fields = StringUtils.split(dependency.trim(), ":");
								Set<String> versions = artifactVersions.get(fields[0]);
								if (versions == null) {
									versions = new HashSet<String>();
									artifactVersions.put(fields[0], versions);
								}
								versions.add(fields[1]);
							}
						}
						String key = artifact.getGroupId() + "." + artifact.getArtifactId();
						Set<String> versions = artifactVersions.get(key);
						if (versions == null) {
							versions = new HashSet<String>();
							artifactVersions.put(key, versions);
						}
						versions.add(artifact.getVersion());
					}
				}
			}
			
			for (Map.Entry<String, Set<String>> entry: artifactVersions.entrySet()) {
				if (entry.getValue().size() > 1)
					errors.add("Version onflicts found for dependency '" + entry.getKey() + "'.");
			}
		}

		if (!errors.isEmpty()) {
			StringBuffer errorMessage = new StringBuffer();
			for (String each : errors)
				errorMessage.append(each).append(" ");
			throw new MojoFailureException(errorMessage.toString());
		}

	}

}
