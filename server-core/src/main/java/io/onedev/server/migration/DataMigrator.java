package io.onedev.server.migration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;

import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.dom4j.Element;
import org.dom4j.Node;

import com.google.common.base.Preconditions;

import io.onedev.commons.launcher.bootstrap.Bootstrap;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;

@Singleton
@SuppressWarnings("unused")
public class DataMigrator {
	
	private void migrate1(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element branchRefElement = element.element("branchRef");
					if (branchRefElement != null)
						branchRefElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}	
	}

	private void migrate2(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Depots.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element gateKeeperElement = element.element("gateKeeper");
					gateKeeperElement.detach();
					element.addElement("gateKeepers");
				}
				dom.writeToFile(file, false);
			}
		}	
	}

	private void migrate3(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
			for (Element element: dom.getRootElement().elements()) {
				String name = element.getName();
				name = StringUtils.replace(name, "com.pmease.commons", "com.gitplex.commons");
				name = StringUtils.replace(name, "com.pmease.gitplex", "com.gitplex.server");
				element.setName(name);
			}
			if (file.getName().startsWith("Configs.xml")) {
				for (Element element: dom.getRootElement().elements()) {
					Element settingElement = element.element("setting");
					if (settingElement != null) {
						String clazz = settingElement.attributeValue("class");
						settingElement.addAttribute("class", StringUtils.replace(clazz, "com.pmease.gitplex", "com.gitplex.server"));
						Element gitConfigElement = settingElement.element("gitConfig");
						if (gitConfigElement != null) {
							clazz = gitConfigElement.attributeValue("class");
							gitConfigElement.addAttribute("class", StringUtils.replace(clazz, "com.pmease.gitplex", "com.gitplex.server"));
						}
					}
				}
			}
			dom.writeToFile(file, false);
		}	
	}
	
	private void migrate4(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Accounts.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element avatarUploadDateElement = element.element("avatarUploadDate");
					if (avatarUploadDateElement != null)
						avatarUploadDateElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}	
	}
	
	private void migrate5(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element settingElement = element.element("setting");
						if (settingElement != null)
							settingElement.addElement("enableSSL").setText("false");
					}
				}
				dom.writeToFile(file, false);
			}
		}	
	}
	
	private void migrate6(File dataDir, Stack<Integer> versions) {
	}
	
	private void migrate7(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				content = StringUtils.replace(content, 
						"com.gitplex.commons.hibernate.migration.VersionTable", 
						"com.gitplex.server.model.ModelVersion");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.entity.support.IntegrationPolicy", 
						"com.gitplex.server.model.support.IntegrationPolicy");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.entity.PullRequest_-IntegrationStrategy", 
						"com.gitplex.server.model.PullRequest_-IntegrationStrategy");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.entity.", "com.gitplex.server.model.");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.setting.SpecifiedGit", "com.gitplex.server.git.config.SpecifiedGit");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.setting.SystemGit", "com.gitplex.server.git.config.SystemGit");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.setting.", "com.gitplex.server.model.support.setting.");
				content = StringUtils.replace(content, 
						"com.gitplex.server.core.gatekeeper.", "com.gitplex.server.gatekeeper.");
				FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
				
				if (file.getName().equals("VersionTables.xml")) {
					FileUtils.moveFile(file, new File(file.getParentFile(), "ModelVersions.xml"));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}	
	}
	
	private void migrateIntegrationStrategy8(Element integrationStrategyElement) {
		if (integrationStrategyElement != null) {
			integrationStrategyElement.setName("mergeStrategy");
			switch (integrationStrategyElement.getText()) {
			case "MERGE_ALWAYS":
				integrationStrategyElement.setText("ALWAYS_MERGE");
				break;
			case "MERGE_WITH_SQUASH":
				integrationStrategyElement.setText("SQUASH_MERGE");
				break;
			case "REBASE_SOURCE_ONTO_TARGET":
				integrationStrategyElement.setText("REBASE_MERGE");
				break;
			case "REBASE_TARGET_ONTO_SOURCE":
				integrationStrategyElement.setText("MERGE_IF_NECESSARY");
				break;
			}
		}
	}
	
	private void migrate8(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SYSTEM")) {
						Element settingElement = element.element("setting");
						settingElement.addElement("curlConfig")
								.addAttribute("class", "com.gitplex.server.git.config.SystemCurl");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Accounts.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("reviewEffort").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Depots.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("gateKeepers").detach();
					element.element("integrationPolicies").detach();
					element.addElement("branchProtections");
					element.addElement("tagProtections");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element assigneeElement = element.element("assignee");
					if (assigneeElement != null)
						assigneeElement.detach();
					migrateIntegrationStrategy8(element.element("integrationStrategy"));
					Element lastIntegrationPreviewElement = element.element("lastIntegrationPreview");
					if (lastIntegrationPreviewElement != null) {
						lastIntegrationPreviewElement.setName("lastMergePreview");
						Element integratedElement = lastIntegrationPreviewElement.element("integrated");
						if (integratedElement != null)
							integratedElement.setName("merged");
						migrateIntegrationStrategy8(lastIntegrationPreviewElement.element("integrationStrategy"));
					}
					Element closeInfoElement = element.element("closeInfo");
					if (closeInfoElement != null) {
						Element closeStatusElement = closeInfoElement.element("closeStatus");
						if (closeStatusElement.getText().equals("INTEGRATED"))
							closeStatusElement.setText("MERGED");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestReviews.xml") 
					|| file.getName().startsWith("PullRequestReviewInvitations.xml")
					|| file.getName().startsWith("PullRequestStatusChanges.xml")
					|| file.getName().startsWith("PullRequestTasks.xml")
					|| file.getName().startsWith("PullRequestVerifications.xml")
					|| file.getName().startsWith("CodeComments.xml")
					|| file.getName().startsWith("CodeCommentRelations.xml")
					|| file.getName().startsWith("CodeCommentReplys.xml") 
					|| file.getName().startsWith("CodeCommentStatusChanges.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("PullRequestUpdates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element mergeCommitHashElement = element.element("mergeCommitHash");
					mergeCommitHashElement.setName("mergeBaseCommitHash");
				}				
				dom.writeToFile(file, false);
			}
		}	
	}
	
	private void migrate9(File dataDir, Stack<Integer> versions) {
		try {
			Map<String, String> accountIdToName = new HashMap<>();
			Set<String> userIds = new HashSet<>();
			for (File file: dataDir.listFiles()) {
				if (file.getName().startsWith("Accounts.xml")) {
					File renamedFile = new File(dataDir, file.getName().replace("Accounts.xml", "Users.xml"));
					FileUtils.moveFile(file, renamedFile);
					String content = FileUtils.readFileToString(renamedFile, StandardCharsets.UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Account", 
							"com.gitplex.server.model.User");
					VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
					for (Element element: dom.getRootElement().elements()) {
						accountIdToName.put(element.elementText("id"), element.elementText("name"));
						if (element.elementTextTrim("organization").equals("true")) {
							element.detach();
						} else {
							userIds.add(element.elementText("id"));
							element.element("organization").detach();
							element.element("defaultPrivilege").detach();
							element.element("noSpaceName").detach();
							if (element.element("noSpaceFullName") != null)
								element.element("noSpaceFullName").detach();
						}
					}
					dom.writeToFile(renamedFile, false);
				}
			}
			
			long lastUserAuthorizationId = 0;
			VersionedXmlDoc userAuthorizationsDom = new VersionedXmlDoc();
			Element userAuthorizationListElement = userAuthorizationsDom.addElement("list");
			
			for (File file: dataDir.listFiles()) {
				if (file.getName().startsWith("Depots.xml")) {
					File renamedFile = new File(dataDir, file.getName().replace("Depots.xml", "Projects.xml"));
					FileUtils.moveFile(file, renamedFile);
					String content = FileUtils.readFileToString(renamedFile, StandardCharsets.UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Depot", 
							"com.gitplex.server.model.Project");
					VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
					for (Element element: dom.getRootElement().elements()) {
						String accountId = element.elementText("account");
						element.element("account").detach();
						String depotName = element.elementText("name");
						element.element("name").setText(accountIdToName.get(accountId) + "." + depotName);
						if (element.element("defaultPrivilege") != null	)
							element.element("defaultPrivilege").detach();
						
						String adminId;
						if (userIds.contains(accountId)) {
							adminId = accountId;
						} else {
							adminId = "1";
						}
						Element userAuthorizationElement = 
								userAuthorizationListElement.addElement("com.gitplex.server.model.UserAuthorization");
						userAuthorizationElement.addAttribute("revision", "0.0");
						userAuthorizationElement.addElement("id").setText(String.valueOf(++lastUserAuthorizationId));
						userAuthorizationElement.addElement("user").setText(adminId);
						userAuthorizationElement.addElement("project").setText(element.elementText("id"));
						userAuthorizationElement.addElement("privilege").setText("ADMIN");
					}
					
					dom.writeToFile(renamedFile, false);
				} else if (file.getName().startsWith("BranchWatchs.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element: dom.getRootElement().elements()) {
						if (!userIds.contains(element.elementText("user"))) {
							element.detach();
						} else {
							element.element("depot").setName("project");
						}
					}
					dom.writeToFile(file, false);
				} else if (file.getName().startsWith("Teams.xml") 
						|| file.getName().startsWith("TeamMemberships.xml")
						|| file.getName().startsWith("TeamAuthorizations.xml")
						|| file.getName().startsWith("OrganizationMemberships.xml")
						|| file.getName().startsWith("UserAuthorizations.xml")
						|| file.getName().startsWith("PullRequest")
						|| file.getName().startsWith("Review")
						|| file.getName().startsWith("ReviewInvitation")) {
					FileUtils.deleteFile(file);
				} else if (file.getName().startsWith("Configs.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element: dom.getRootElement().elements()) {
						if (element.elementText("key").equals("SYSTEM")) {
							String storagePath = element.element("setting").elementText("storagePath");
							File storageDir = new File(storagePath);
							File repositoriesDir = new File(storageDir, "repositories");
							if (repositoriesDir.exists()) {
								File projectsDir = new File(storageDir, "projects");
								FileUtils.moveDirectory(repositoriesDir, projectsDir);
								for (File projectDir: projectsDir.listFiles()) {
									File infoDir = new File(projectDir, "info");
									if (infoDir.exists())
										FileUtils.deleteDir(infoDir);
								}
							}
						} else if (element.elementText("key").equals("SECURITY")) {
							element.element("setting").addElement("enableAnonymousAccess").setText("false");
						}
					}		
					dom.writeToFile(file, false);
				}
			}	
			userAuthorizationsDom.writeToFile(new File(dataDir, "UserAuthorizations.xml"), false);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	private void migrate10(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml") || file.getName().startsWith("CodeCommentReplys.xml") 
					|| file.getName().startsWith("CodeCommentStatusChanges.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						Element exprElement = branchProtectionElement.element("reviewAppointmentExpr");
						if (exprElement != null)
							exprElement.setName("reviewRequirementSpec");
						for (Element fileProtectionElement: branchProtectionElement.element("fileProtections").elements()) {
							exprElement = fileProtectionElement.element("reviewAppointmentExpr");
							if (exprElement != null)
								exprElement.setName("reviewRequirementSpec");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
		VersionedXmlDoc dom = VersionedXmlDoc.fromFile(new File(dataDir, "Configs.xml"));
		for (Element element: dom.getRootElement().elements()) {
			if (element.elementText("key").equals("SYSTEM")) {
				String storagePath = element.element("setting").elementText("storagePath");
				File codeCommentsFromWeiFeng = new File(storagePath, "CodeComments.xml");
				if (codeCommentsFromWeiFeng.exists()) {
					dom = VersionedXmlDoc.fromFile(codeCommentsFromWeiFeng);
					for (Element commentElement: dom.getRootElement().elements()) {
						commentElement.setName("com.gitplex.server.model.CodeComment");
						commentElement.element("depot").setName("project");
						commentElement.element("resolved").detach();
						commentElement.element("commentPos").setName("markPos");
					}
					dom.writeToFile(new File(dataDir, "CodeComments.xml"), false);
				}
			}
		}		
	}
	
	private void migrate11(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				long maxId = 0;
				for (Element element: dom.getRootElement().elements()) {
					Long id = Long.parseLong(element.elementTextTrim("id"));
					if (maxId < id)
						maxId = id;
				}
				Element licenseConfigElement = dom.getRootElement().addElement("com.gitplex.server.model.Config");
				licenseConfigElement.addElement("id").setText(String.valueOf(maxId+1));
				licenseConfigElement.addElement("key").setText("LICENSE");
				dom.writeToFile(file, false);
			} 
		}
	}
	
	private void migrate12(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: projectElement.element("branchProtections").elements()) {
						branchProtectionElement.addElement("enabled").setText("true");
					}
					for (Element tagProtectionElement: projectElement.element("tagProtections").elements()) {
						tagProtectionElement.addElement("enabled").setText("true");
					}
				}
				dom.writeToFile(file, false);
			} 
		}
	}
	
	private void migrate13(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				content = StringUtils.replace(content, "gitplex", "turbodev");
				content = StringUtils.replace(content, "GitPlex", "TurboDev");
				FileUtils.writeFile(file, content, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void migrate14(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: projectElement.element("branchProtections").elements()) {
						Element submitterElement = branchProtectionElement.addElement("submitter");
						submitterElement.addAttribute("class", "com.turbodev.server.model.support.submitter.Anyone");
						branchProtectionElement.addElement("noCreation").setText("true");
					}
					for (Element tagProtectionElement: projectElement.element("tagProtections").elements()) {
						tagProtectionElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} 
		}
	}
	
	private void migrate15(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				content = StringUtils.replace(content, "com.turbodev", "io.onedev");
				content = StringUtils.replace(content, "com/turbodev", "io/onedev");
				content = StringUtils.replace(content, "turbodev.com", "onedev.io");
				content = StringUtils.replace(content, "turbodev", "onedev");
				content = StringUtils.replace(content, "TurboDev", "OneDev");
				FileUtils.writeFile(file, content, StandardCharsets.UTF_8.name());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}
	
	private void migrateMergeStrategy16(Element mergeStrategyElement) {
		if (mergeStrategyElement != null) {
			mergeStrategyElement.setName("mergeStrategy");
			switch (mergeStrategyElement.getText()) {
			case "ALWAYS_MERGE":
				mergeStrategyElement.setText("CREATE_MERGE_COMMIT");
				break;
			case "MERGE_IF_NECESSARY":
				mergeStrategyElement.setText("CREATE_MERGE_COMMIT_IF_NECESSARY");
				break;
			case "SQUASH_MERGE":
				mergeStrategyElement.setText("SQUASH_SOURCE_BRANCH_COMMITS");
				break;
			case "REBASE_MERGE":
				mergeStrategyElement.setText("REBASE_SOURCE_BRANCH_COMMITS");
				break;
			}
		}
	}
	
	/*
	 * Migrate from 1.0 to 2.0
	 */
	private void migrate16(File dataDir, Stack<Integer> versions) {
		Map<String, Integer> codeCommentReplyCounts = new HashMap<>();
		Map<String, String> userNames = new HashMap<>();
		Map<String, Set<String>> requestCodeComments = new HashMap<>();
		Map<String, Integer> requestCommentCounts = new HashMap<>();
		Set<String> openRequests = new HashSet<>();
		Map<String, String> reviewRequirements = new HashMap<>(); 
		 
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element fullNameElement = element.element("fullName");
					if (fullNameElement != null)
						userNames.put(element.elementTextTrim("id"), fullNameElement.getText());
					else
						userNames.put(element.elementTextTrim("id"), element.elementText("name"));
				}				
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("comment");
					Integer replyCount = codeCommentReplyCounts.get(commentId);
					if (replyCount == null)
						replyCount = 0;
					replyCount++;
					codeCommentReplyCounts.put(commentId, replyCount);
				}				
			} else if (file.getName().startsWith("CodeCommentRelations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("comment");
					String requestId = element.elementTextTrim("request");
					Set<String> codeComments = requestCodeComments.get(requestId);
					if (codeComments == null) {
						codeComments = new HashSet<>();
						requestCodeComments.put(requestId, codeComments);
					}
					codeComments.add(commentId);
				}				
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("request");
					Integer commentCount = requestCommentCounts.get(commentId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					requestCommentCounts.put(commentId, commentCount);
				}				
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.element("closeInfo") == null) {
						openRequests.add(element.elementTextTrim("id"));
					}
				}				
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String projectId = element.elementTextTrim("id");
					StringBuilder builder = new StringBuilder();
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						Element reviewRequirementSpecElement = branchProtectionElement.element("reviewRequirementSpec");
						if (reviewRequirementSpecElement != null) 
							builder.append(reviewRequirementSpecElement.getText()).append(";");
						
						for (Element fileProtectionElement: branchProtectionElement.element("fileProtections").elements()) {
							reviewRequirementSpecElement = fileProtectionElement.element("reviewRequirementSpec");
							builder.append(reviewRequirementSpecElement.getText()).append(";");
						}
					}
					reviewRequirements.put(projectId, builder.toString());
				}				
			}
		}
		
		for (Map.Entry<String, Set<String>> entry: requestCodeComments.entrySet()) {
			Integer commentCount = requestCommentCounts.get(entry.getKey());
			if (commentCount == null)
				commentCount = 0;
			for (String commentId: entry.getValue()) {
				commentCount++;
				Integer replyCount = codeCommentReplyCounts.get(commentId);
				if (replyCount != null)
					commentCount += replyCount;
			}
			requestCommentCounts.put(entry.getKey(), commentCount);
		}
		
		VersionedXmlDoc requestReviewsDOM = new VersionedXmlDoc();
		Element requestReviewListElement = requestReviewsDOM.addElement("list");
		
		VersionedXmlDoc configurationsDOM = new VersionedXmlDoc();
		Element configurationListElement = configurationsDOM.addElement("list");
		Map<String, Map<String, Long>> projectConfigurations = new HashMap<>();
		long configurationCount = 0;
		
		int reviewCount = 0;
		
		VersionedXmlDoc requestBuildsDOM = new VersionedXmlDoc();
		Element requestBuildListElement = requestBuildsDOM.addElement("list");
		int requestBuildCount = 0;
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("BranchWatches.xml") 
					|| file.getName().startsWith("PullRequestReferences.xml")
					|| file.getName().startsWith("PullRequestStatusChanges.xml")
					|| file.getName().startsWith("PullRequestTasks.xml")
					|| file.getName().startsWith("ReviewInvitations.xml")
					|| file.getName().startsWith("Reviews.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("UserAuthorizations.xml") || file.getName().startsWith("GroupAuthorizations.xml")) {
				try {
					String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
					content = StringUtils.replace(content, "ADMIN", "ADMINISTRATION");
					content = StringUtils.replace(content, "WRITE", "CODE_WRITE");
					content = StringUtils.replace(content, "READ", "CODE_READ");
					FileUtils.writeFile(file, content, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Integer replyCount = codeCommentReplyCounts.get(element.elementTextTrim("id"));
					if (replyCount == null)
						replyCount = 0;
					element.addElement("replyCount").setText(String.valueOf(replyCount));
					
					Element dateElement = element.element("date");
					dateElement.setName("createDate");
					Element updateDateElement = element.addElement("updateDate");
					updateDateElement.addAttribute("class", "sql-timestamp");
					Element lastEventElement = element.element("lastEvent");
					if (lastEventElement != null) {
						updateDateElement.setText(lastEventElement.elementText("date"));
						lastEventElement.detach();
					} else {
						updateDateElement.setText(dateElement.getText());
					}
				}				
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrateMergeStrategy16(element.element("mergeStrategy"));
					Element lastMergePreviewElement = element.element("lastMergePreview");
					if (lastMergePreviewElement != null) {
						migrateMergeStrategy16(lastMergePreviewElement.element("mergeStrategy"));
					}
					
					Integer commentCount = requestCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.addElement("commentCount").setText(String.valueOf(commentCount));
					
					Element lastCodeCommentEventDateElement = element.element("lastCodeCommentEventDate");
					if (lastCodeCommentEventDateElement != null)
						lastCodeCommentEventDateElement.setName("lastCodeCommentActivityDate");
					
					Element closeInfoElement = element.element("closeInfo");
					if (closeInfoElement != null) {
						Element closedByElement = closeInfoElement.element("closedBy");
						if (closedByElement != null)
							closedByElement.setName("user");
						Element closedByNameElement = closeInfoElement.element("closedByName");
						if (closedByNameElement != null)
							closedByNameElement.setName("userName");
						closeInfoElement.element("closeDate").setName("date");
						closeInfoElement.element("closeStatus").setName("status");
					}
					Element submitDateElement = element.element("submitDate");
					Element updateDateElement = element.addElement("updateDate");
					updateDateElement.addAttribute("class", "sql-timestamp");
					Element lastEventElement = element.element("lastEvent");
					if (lastEventElement != null) {
						updateDateElement.setText(lastEventElement.elementText("date"));
						lastEventElement.detach();
					} else {
						updateDateElement.setText(submitDateElement.getText());
					}
				}				
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Configs.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = StringUtils.replace(content, "io.onedev.server.security.authenticator.", 
						"io.onedev.server.model.support.authenticator.");
				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element: dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.Setting");
					Element settingElement = element.element("setting");
					if (settingElement != null) {
						settingElement.setName("value");
						if (element.elementTextTrim("key").equals("AUTHENTICATOR")) {
							Element authenticatorElement = settingElement.elementIterator().next();
							settingElement.addAttribute("class", authenticatorElement.getName());
							for (Element fieldElement: authenticatorElement.elements()) {
								if (!fieldElement.getName().equals("defaultGroupNames")) {
									fieldElement.detach();
									settingElement.add(fieldElement);
								}
							}
							authenticatorElement.detach();
							settingElement.addElement("canCreateProjects").setText("true");
						}
					}
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(file.getParentFile(), file.getName().replace("Config", "Setting")), false);
			} else if (file.getName().startsWith("PullRequestWatchs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element reasonElement = element.element("reason");
					if (reasonElement != null)
						reasonElement.detach();
					Element ignoreElement = element.element("ignore");
					ignoreElement.setName("watching");
					ignoreElement.setText(String.valueOf(!Boolean.parseBoolean(ignoreElement.getTextTrim())));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestUpdates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("uuid").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String project = element.elementTextTrim("id");
					Element publicReadElement = element.element("publicRead");
					if (publicReadElement.getTextTrim().equals("true")) 
						element.addElement("defaultPrivilege").setText("CODE_READ");
					publicReadElement.detach();
					
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						branchProtectionElement.element("verifyMerges").setName("buildMerges");
						Element verificationsElement = branchProtectionElement.element("verifications");
						verificationsElement.setName("configurations");
						for (Element verificationElement: verificationsElement.elements()) {
							String verification = verificationElement.getText();
							Map<String, Long> configurations = projectConfigurations.get(project);
							if (configurations == null) {
								configurations = new HashMap<>();
								projectConfigurations.put(project, configurations);
							}
							Long configurationId = configurations.get(verification);
							if (configurationId == null) {
								configurationId = ++configurationCount;
								configurations.put(verification, configurationId);
								Element configurationElement = configurationListElement.addElement("io.onedev.server.model.Configuration");
								configurationElement.addAttribute("revision", "0.0");
								configurationElement.addElement("id").setText(String.valueOf(configurationId));
								configurationElement.addElement("project").setText(project);
								configurationElement.addElement("name").setText(verification);
								configurationElement.addElement("buildCleanupRule").addAttribute("class", "io.onedev.server.model.support.configuration.DoNotCleanup");
							}
							for (String request: openRequests) {
								Element requestBuildElement = requestBuildListElement.addElement("io.onedev.server.model.PullRequestBuild");
								requestBuildElement.addAttribute("revision", "0.0");
								requestBuildElement.addElement("id").setText(String.valueOf(++requestBuildCount));
								requestBuildElement.addElement("request").setText(request);
								requestBuildElement.addElement("configuration").setText(String.valueOf(configurationId));
							}
						}
						Element submitterElement = branchProtectionElement.element("submitter");
						String submitterClass = submitterElement.attributeValue("class");
						submitterClass = submitterClass.replace("io.onedev.server.model.support.submitter.", 
								"io.onedev.server.model.support.usermatcher.");
						submitterElement.attribute("class").setValue(submitterClass);
						
						Element reviewRequirementSpecElement = branchProtectionElement.element("reviewRequirementSpec");
						if (reviewRequirementSpecElement != null) {
							reviewRequirementSpecElement.setName("reviewRequirement");
						}
						
						for (Element fileProtectionElement: branchProtectionElement.element("fileProtections").elements()) {
							reviewRequirementSpecElement = fileProtectionElement.element("reviewRequirementSpec");
							reviewRequirementSpecElement.setName("reviewRequirement");
						}
					}
					for (Element tagProtectionElement: element.element("tagProtections").elements()) {
						Element submitterElement = tagProtectionElement.element("submitter");
						String submitterClass = submitterElement.attributeValue("class");
						submitterClass = submitterClass.replace("io.onedev.server.model.support.submitter.", 
								"io.onedev.server.model.support.usermatcher.");
						submitterElement.attribute("class").setValue(submitterClass);
					}
				}				
				dom.writeToFile(file, false);
			}
		}

		requestReviewsDOM.writeToFile(new File(dataDir, "PullRequestReviews.xml"), false);
		configurationsDOM.writeToFile(new File(dataDir, "Configurations.xml"), false);
		requestBuildsDOM.writeToFile(new File(dataDir, "PullRequestBuilds.xml"), false);
	}
	
	private void migrate17(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Issue")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element issueWorkflowElement = element.element("issueWorkflow");
					if (issueWorkflowElement != null)
						issueWorkflowElement.detach();
					Element savedIssueQueriesElement = element.element("savedIssueQueries");
					if (savedIssueQueriesElement != null)
						savedIssueQueriesElement.detach();
					Element issueListFieldsElement = element.element("issueListFields");
					if (issueListFieldsElement != null)
						issueListFieldsElement.detach();
					Element issueBoardsElement = element.element("issueBoards");
					if (issueBoardsElement != null)
						issueBoardsElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}	
	
	private void migrate18(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("LICENSE"))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}	
	
	private void migrate19(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element commitMessageTransformsElement = element.addElement("commitMessageTransforms");
					Element commitMessageTransformSettingElement = element.element("commitMessageTransformSetting");
					if (commitMessageTransformSettingElement != null) {
						commitMessageTransformSettingElement.detach();
						commitMessageTransformSettingElement.setName("io.onedev.server.model.support.CommitMessageTransform");
						commitMessageTransformsElement.add(commitMessageTransformSettingElement);
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}	
	
	private void migrate20(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldElement: valueElement.element("fieldSpecs").elements()) {
								fieldElement.addElement("canBeChangedBy").addAttribute("class", "io.onedev.server.model.support.usermatcher.Anyone");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate21(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						branchProtectionElement.element("branch").setName("branches");
						for (Element fileProtectionElement: branchProtectionElement.element("fileProtections").elements()) {
							fileProtectionElement.element("path").setName("paths");
						}
					}
					for (Element tagProtectionElement: element.element("tagProtections").elements()) {
						tagProtectionElement.element("tag").setName("tags");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("LICENSE")) {
						element.element("value").addAttribute("class", "io.onedev.commons.utils.license.LicenseDetail");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate22(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("IssueFieldUnarys.xml")) {
				File renamedFile = new File(dataDir, file.getName().replace("IssueFieldUnarys", "IssueFieldEntitys"));
				try {
					FileUtils.moveFile(file, renamedFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(renamedFile);
				for (Element element: dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.IssueFieldEntity");
				}
				dom.writeToFile(renamedFile, false);
			}
		}
	}
	
	private void migrate23(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Build2s.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element runInstanceIdElement = element.element("runInstanceId");
					if (runInstanceIdElement != null)
						runInstanceIdElement.detach();
					Element errorMessageElement = element.element("errorMessage");
					if (errorMessageElement != null)
						errorMessageElement.setName("statusMessage");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("uuid").detach();
					if (element.element("issueSetting") == null)
						element.addElement("issueSetting");
					if (element.element("savedCommitQueries") == null) 
						element.addElement("savedCommitQueries");
					if (element.element("savedPullRequestQueries") == null) 
						element.addElement("savedPullRequestQueries");
					if (element.element("savedCodeCommentQueries") == null) 
						element.addElement("savedCodeCommentQueries");
					if (element.element("savedBuildQueries") == null) 
						element.addElement("savedBuildQueries");
					if (element.element("webHooks") == null) 
						element.addElement("webHooks");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("uuid").detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private String escapeValue24(String value) {
		StringBuilder builder = new StringBuilder();
		for (int i=0; i<value.length(); i++) {
			char ch = value.charAt(i);
			if ("\\()".indexOf(ch) != -1)
				builder.append("\\");
			builder.append(ch);
		}
		return builder.toString();
	}
	
	private void migrateUserMatcher24(Element userMatcherElement) {
		String userMatcher;
		String userMatcherClass = userMatcherElement.attributeValue("class");
		if (userMatcherClass.contains("Anyone")) {
			userMatcher = "anyone";
		} else if (userMatcherClass.contains("CodeWriters")) {
			userMatcher = "code writers";
		} else if (userMatcherClass.contains("CodeReaders")) {
			userMatcher = "code readers";
		} else if (userMatcherClass.contains("IssueReaders")) {
			userMatcher = "issue readers";
		} else if (userMatcherClass.contains("ProjectAdministrators")) {
			userMatcher = "project administrators";
		} else if (userMatcherClass.contains("SpecifiedUser")) {
			userMatcher = "user(" + escapeValue24(userMatcherElement.elementText("userName").trim()) + ")";
		} else {
			userMatcher = "group(" + escapeValue24(userMatcherElement.elementText("groupName").trim()) + ")";
		}
		userMatcherElement.clearContent();
		userMatcherElement.remove(userMatcherElement.attribute("class"));
		userMatcherElement.setText(userMatcher);
	}
	
	private void migrateTransitionSpecsElement24(Element transitionSpecsElement) {
		for (Element transitionElement: transitionSpecsElement.elements()) {
			Element triggerElement = transitionElement.element("trigger");
			if (triggerElement.attributeValue("class").contains("PressButtonTrigger"))
				migrateUserMatcher24(triggerElement.element("authorized"));
		}
	}
	
	private void migrate24(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							migrateTransitionSpecsElement24(valueElement.element("defaultTransitionSpecs"));
							for (Element fieldElement: valueElement.element("fieldSpecs").elements())
								migrateUserMatcher24(fieldElement.element("canBeChangedBy"));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element issueSettingElement = element.element("issueSetting");
					Element transitionsElement = issueSettingElement.element("transitionSpecs");
					if (transitionsElement != null) 
						migrateTransitionSpecsElement24(transitionsElement);
					for (Element branchProtectionElement: element.element("branchProtections").elements())
						migrateUserMatcher24(branchProtectionElement.element("submitter"));
					for (Element tagProtectionElement: element.element("tagProtections").elements())
						migrateUserMatcher24(tagProtectionElement.element("submitter"));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// from 2.0 to 3.0
	private void migrate25(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace(".support.setting.", ".support.administration.");
				content = content.replace(".support.authenticator.", ".support.administration.authenticator.");
				
				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element: dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key"); 
					if (key.equals("ISSUE") || key.equals("JOB_EXECUTORS")) {
						element.detach();
					} else if (key.equals("BACKUP")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element folderElement = valueElement.element("folder");
							if (folderElement != null)
								folderElement.detach();
						}
					} else if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null) 
							valueElement.element("enableAnonymousAccess").setText("false");
					} else if (key.equals("SYSTEM")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element storagePathElement = valueElement.element("storagePath");
							String storagePath = storagePathElement.getText();
							storagePathElement.detach();
							try {
								File projectsDir = new File(storagePath, "projects");
								if (projectsDir.exists()) {
									Path target = projectsDir.toPath();
								    File linkDir = new File(Bootstrap.installDir, "site/projects");
								    if (linkDir.exists())
								    	throw new ExplicitException("Directory already exists: " + linkDir);
								    Files.createSymbolicLink(linkDir.toPath(), target);							
								}
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
						}
					} else if (key.equals("AUTHENTICATOR")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element defaultGroupNamesElement = valueElement.element("defaultGroupNames");
							if (defaultGroupNamesElement != null)
								defaultGroupNamesElement.detach();
						}
					} 
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Groups.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) 
					element.element("canCreateProjects").setName("createProjects");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					element.addElement("userProjectQueries");
					
					element.addElement("userIssueQueries");
					element.addElement("userIssueQueryWatches");
					element.addElement("issueQueryWatches");
					
					element.addElement("userPullRequestQueries");
					element.addElement("userPullRequestQueryWatches");
					element.addElement("pullRequestQueryWatches");
					
					element.addElement("userBuildQueries");
					element.addElement("userBuildQuerySubscriptions");
					element.addElement("buildQuerySubscriptions");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("GroupAuthorizations.xml") 
					|| file.getName().startsWith("UserAuthorizations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Element privilegeElement = element.element("privilege");
					String privilege = privilegeElement.getTextTrim();
					privilegeElement.detach();

					String roleId;
					switch (privilege) {
					case "ISSUE_READ":
						roleId = "4";
						break;
					case "CODE_READ":
						roleId = "3";
						break;
					case "CODE_WRITE":
						roleId = "2";
						break;
					default:
						roleId = "1";
					}
					element.addElement("role").setText(roleId);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements())
					element.element("numberStr").detach();
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element rangeElement = element.element("markPos").element("range");
					rangeElement.element("beginLine").setName("fromRow");
					rangeElement.element("endLine").setName("toRow");
					rangeElement.element("beginChar").setName("fromColumn");
					rangeElement.element("endChar").setName("toColumn");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace("DO_NOT_MERGE", "CREATE_MERGE_COMMIT");

				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element: dom.getRootElement().elements())
					element.element("numberStr").detach();
				
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element defaultPrivilegeElement = element.element("defaultPrivilege");
					if (defaultPrivilegeElement != null)
						defaultPrivilegeElement.detach();
					element.addElement("owner").setText("1");
					
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						Element submitterElement = branchProtectionElement.element("submitter");
						submitterElement.setName("user");
						submitterElement.setText("anyone");
						branchProtectionElement.element("configurations").detach();
						branchProtectionElement.element("buildMerges").detach();
						branchProtectionElement.addElement("jobNames");
						for (Element fileProtectionElement: branchProtectionElement.element("fileProtections").elements())
							fileProtectionElement.addElement("jobNames");
					}
					
					for (Element tagProtectionElement: element.element("tagProtections").elements())
						tagProtectionElement.element("submitter").setName("user");
					element.addElement("secrets");
					element.element("commitMessageTransforms").detach();
					element.element("webHooks").detach();
					element.addElement("webHooks");
					element.element("issueSetting").detach();
					element.addElement("issueSetting");
					
					element.element("savedBuildQueries").detach();
					Element buildSettingElement = element.addElement("buildSetting");
					buildSettingElement.addElement("buildsToPreserve").setText("all");
					
					element.element("savedCommitQueries").detach();
					element.element("savedCodeCommentQueries").detach();
					element.element("savedPullRequestQueries").detach();
					
					element.addElement("pullRequestSetting");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml") 
					|| file.getName().startsWith("Configurations.xml")
					|| file.getName().startsWith("IssueQuerySettings.xml")
					|| file.getName().startsWith("PullRequestQuerySettings.xml")
					|| file.getName().startsWith("PullRequestChanges.xml")
					|| file.getName().startsWith("CodeCommentQuerySettings.xml")
					|| file.getName().startsWith("PullRequestWatchs.xml")
					|| file.getName().startsWith("IssueWatchs.xml")
					|| file.getName().startsWith("CommitQuerySettings.xml")
					|| file.getName().startsWith("PullRequestBuilds.xml")
					|| file.getName().startsWith("BuildQuerySettings.xml")
					|| file.getName().startsWith("Builds.xml")
					|| file.getName().startsWith("Build2s.xml")
					|| file.getName().startsWith("BuildDependences.xml")
					|| file.getName().startsWith("BuildParams.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("IssueFieldEntitys.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace("io.onedev.server.model.IssueFieldEntity", 
						"io.onedev.server.model.IssueField");

				FileUtils.deleteFile(file);
				
				File renamedFile = new File(dataDir, file.getName().replace(
						"IssueFieldEntitys.xml", "IssueFields.xml"));
				FileUtils.writeFile(renamedFile, content, StandardCharsets.UTF_8.name());
			}
		}
        try (InputStream is = getClass().getResourceAsStream("migrate25_roles.xml")) {
        	Preconditions.checkNotNull(is);
        	FileUtils.writeFile(
        			new File(dataDir, "Roles.xml"), 
        			StringUtils.join(IOUtils.readLines(is, StandardCharsets.UTF_8.name()), "\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
		
	}
	
	private void migrate26(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element createdAtElement = element.element("createdAt");
					createdAtElement.setName("createDate");
					element.addElement("updateDate").setText(createdAtElement.getText());
				}
				dom.writeToFile(file, false);
			} 		
		}
	}
	
	private void migrate27(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					buildSettingElement.element("buildsToPreserve").detach();
					buildSettingElement.addElement("preservations");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				FileUtils.deleteFile(file);
			}
		}
	}
	
	private void migrate28(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element buildSettingElement = element.addElement("buildSetting");
					buildSettingElement.addElement("secrets");
					buildSettingElement.addElement("buildPreservations");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("secrets").detach();
					element.element("buildSetting").detach();
					Element buildSettingElement = element.addElement("buildSetting");
					buildSettingElement.addElement("secrets");
					buildSettingElement.addElement("buildPreservations");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate29(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements())
					element.addElement("webHooks");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: element.element("branchProtections").elements())
						branchProtectionElement.element("user").setName("userMatch");
					for (Element tagProtectionElement: element.element("tagProtections").elements())
						tagProtectionElement.element("user").setName("userMatch");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate30(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key"); 
					if (key.equals("JOB_EXECUTORS")) 
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate31(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element editableIssueFieldsElement = element.element("editableIssueFields");
					editableIssueFieldsElement.detach();
					element.addElement("editableIssueFields").addAttribute(
							"class", "io.onedev.server.model.support.role.AllIssueFields");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate32(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key"); 
					if (key.equals("ISSUE"))
						element.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) { 
				FileUtils.deleteFile(file);
			}
		}
	}
	
	private void migrate33(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element branchProtectionElement: element.element("branchProtections").elements()) {
						branchProtectionElement.element("noCreation").setName("preventCreation");
						branchProtectionElement.element("noDeletion").setName("preventDeletion");
						branchProtectionElement.element("noForcedPush").setName("preventForcedPush");
					}
					for (Element tagProtectionElement: element.element("tagProtections").elements()) {
						tagProtectionElement.element("noCreation").setName("preventCreation");
						tagProtectionElement.element("noDeletion").setName("preventDeletion");
						tagProtectionElement.element("noUpdate").setName("preventUpdate");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate34(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element stateElement: valueElement.element("stateSpecs").elements()) {
								Element categoryElement = stateElement.element("category");
								stateElement.addElement("done").setText(String.valueOf(categoryElement.getTextTrim().equals("CLOSED")));
								categoryElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Milestones.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("numOfOpenIssues").setName("numOfIssuesTodo");
					element.element("numOfClosedIssues").setName("numOfIssuesDone");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate35(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("updateDate").detach();
					Element createDateElement = element.element("createDate");
					Element lastUpdateElement = element.addElement("lastUpdate");
					Element userElement = element.element("user");
					Element lastUpdateUserElement = lastUpdateElement.addElement("user");
					if (userElement != null)
						lastUpdateUserElement.setText(userElement.getTextTrim());
					else
						lastUpdateUserElement.setText("1");
					lastUpdateElement.addElement("activity").setText("created");
					Element dateElement = lastUpdateElement.addElement("date");
					dateElement.addAttribute("class", createDateElement.attributeValue("class"));
					dateElement.setText(createDateElement.getTextTrim());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("updateDate").detach();
					Element submitDateElement = element.element("submitDate");
					Element lastUpdateElement = element.addElement("lastUpdate");
					Element submitterElement = element.element("submitter");
					Element lastUpdateUserElement = lastUpdateElement.addElement("user");
					if (submitterElement != null)
						lastUpdateUserElement.setText(submitterElement.getTextTrim());
					else
						lastUpdateUserElement.setText("1");
					lastUpdateElement.addElement("activity").setText("opened");
					Element dateElement = lastUpdateElement.addElement("date");
					dateElement.addAttribute("class", submitDateElement.attributeValue("class"));
					dateElement.setText(submitDateElement.getTextTrim());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("updateDate").detach();
					Element submitDateElement = element.element("submitDate");
					Element lastUpdateElement = element.addElement("lastUpdate");
					Element submitterElement = element.element("submitter");
					Element lastUpdateUserElement = lastUpdateElement.addElement("user");
					if (submitterElement != null)
						lastUpdateUserElement.setText(submitterElement.getTextTrim());
					else
						lastUpdateUserElement.setText("1");
					lastUpdateElement.addElement("activity").setText("opened");
					Element dateElement = lastUpdateElement.addElement("date");
					dateElement.addAttribute("class", submitDateElement.attributeValue("class"));
					dateElement.setText(submitDateElement.getTextTrim());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					Element namedQueriesElement = buildSettingElement.element("namedQueries");
					if (namedQueriesElement != null) {
						for (Element queryElement: namedQueriesElement.elements())
							queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
					}
					Element secretsElement = buildSettingElement.element("secrets");
					secretsElement.setName("jobSecrets");
					for (Element secretElement: secretsElement.elements())
						secretElement.setName("io.onedev.server.model.support.build.JobSecret");
					for (Element buildPreservationElement: buildSettingElement.element("buildPreservations").elements())
						buildPreservationElement.setName("io.onedev.server.model.support.build.BuildPreservation");
					buildSettingElement.addElement("actionAuthorizations");
					
					for (Element tagProtectionElement: element.element("tagProtections").elements()) {
						Element buildBranchesElement = tagProtectionElement.element("buildBranches");
						if (buildBranchesElement != null)
							buildBranchesElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element queryElement: element.element("userBuildQueries").elements())
						queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
					Element buildSettingElement = element.element("buildSetting");
					Element secretsElement = buildSettingElement.element("secrets");
					secretsElement.setName("jobSecrets");
					for (Element secretElement: secretsElement.elements())
						secretElement.setName("io.onedev.server.model.support.build.JobSecret");
					for (Element buildPreservationElement: buildSettingElement.element("buildPreservations").elements())
						buildPreservationElement.setName("io.onedev.server.model.support.build.BuildPreservation");
					buildSettingElement.addElement("actionAuthorizations");
					Element passwordElement = element.element("password");
					if (passwordElement == null)
						element.addElement("password").setText("external_managed");
					else if (StringUtils.isBlank(passwordElement.getText()))
						passwordElement.setText("external_managed");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("BuildQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					for (Element queryElement: element.element("userQueries").elements()) 
						queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element stateElement: valueElement.element("stateSpecs").elements()) {
								stateElement.element("done").detach();
							}
						}
					} else if (element.elementTextTrim("key").equals("BUILD")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element queryElement: valueElement.element("namedQueries").elements()) 
								queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Milestones.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("numOfIssuesTodo").detach();
					element.element("numOfIssuesDone").detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}	

	// Database schema changed
	private void migrate36(File dataDir, Stack<Integer> versions) {	
	}
	
	private void migrate37(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("LICENSE"))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}	
	
	private Long getForkedRoot38(Map<Long, Long> forkedFroms, Long projectId) {
		Long forkedFrom = forkedFroms.get(projectId);
		if (forkedFrom != null)
			return getForkedRoot38(forkedFroms, forkedFrom);
		else
			return projectId;
	}
	
	// from 3.0.10 to 3.0.11
	private void migrate38(File dataDir, Stack<Integer> versions) {
		Map<Long, Long> forkedFroms = new HashMap<>();
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Node node: dom.selectNodes("//io.onedev.server.model.support.pullrequest.NamedPullRequestQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node: dom.selectNodes("//io.onedev.server.model.support.issue.NamedIssueQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node: dom.selectNodes("//io.onedev.server.model.support.build.NamedBuildQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node: dom.selectNodes("//io.onedev.server.model.support.NamedProjectQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node: dom.selectNodes("//issueQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.getTextTrim().equals("all"))
							element.detach();
					}
				}
				for (Node node: dom.selectNodes("//io.onedev.server.model.support.build.BuildPreservation")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						Element conditionElement = element.element("condition");
						if (conditionElement.getTextTrim().equals("all"))
							conditionElement.detach();
					}
				}
				for (Node node: dom.selectNodes("//listFields")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						Element stateElement = element.addElement("string");
						stateElement.setText("State");
						stateElement.detach();
						element.elements().add(0, stateElement);
					}
				}
				dom.writeToFile(file, false);
			}
			if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					String className = dataElement.attributeValue("class");
					if (className.contains("IssueCommittedData") || className.contains("IssuePullRequest")) 
						element.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Long projectId = Long.valueOf(element.elementTextTrim("id"));
					Element forkedFromElement = element.element("forkedFrom");
					if (forkedFromElement != null)
						forkedFroms.put(projectId, Long.valueOf(forkedFromElement.getTextTrim()));
					else
						forkedFroms.put(projectId, null);
				}				
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.addElement("enableStartTLS").setText("true");
							valueElement.element("enableSSL").detach();
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
		
		Map<Long, Long> forkedRoots = new HashMap<>();
		for (Long projectId: forkedFroms.keySet()) {
			forkedRoots.put(projectId, getForkedRoot38(forkedFroms, projectId));
		}
		
		Map<Long, Set<Long>> issueNumbers = new HashMap<>();
		Map<Long, Set<Long>> buildNumbers = new HashMap<>();
		Map<Long, Set<Long>> pullRequestNumbers = new HashMap<>();
		
		for (Long forkedRoot: forkedRoots.values()) {
			issueNumbers.put(forkedRoot, new HashSet<>());
			buildNumbers.put(forkedRoot, new HashSet<>());
			pullRequestNumbers.put(forkedRoot, new HashSet<>());
		}
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Long issueNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					if (projectId.equals(forkedRoots.get(projectId)))
						issueNumbers.get(projectId).add(issueNumber); 
				}
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Long buildNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					if (projectId.equals(forkedRoots.get(projectId)))
						buildNumbers.get(projectId).add(buildNumber);
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Long requestNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("targetProject"));
					if (projectId.equals(forkedRoots.get(projectId)))
						pullRequestNumbers.get(projectId).add(requestNumber);
				}
			} 
		}
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Element numberElement = element.element("number");
					Long issueNumber = Long.valueOf(numberElement.getTextTrim());
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					Long forkedRoot = forkedRoots.get(projectId);
					element.addElement("numberScope").setText(forkedRoot.toString());
					if (!projectId.equals(forkedRoot)) {
						Set<Long> issueNumbersOfForkedRoot = issueNumbers.get(forkedRoot);
						if (issueNumbersOfForkedRoot.contains(issueNumber)) {
							issueNumber = Collections.max(issueNumbersOfForkedRoot) + 1;
							numberElement.setText(issueNumber.toString());
						} 
						issueNumbersOfForkedRoot.add(issueNumber);
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Element numberElement = element.element("number");
					Long buildNumber = Long.valueOf(numberElement.getTextTrim());
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					Long forkedRoot = forkedRoots.get(projectId);
					element.addElement("numberScope").setText(forkedRoot.toString());
					if (!projectId.equals(forkedRoot)) {
						Set<Long> buildNumbersOfForkedRoot = buildNumbers.get(forkedRoot);
						if (buildNumbersOfForkedRoot.contains(buildNumber)) {
							buildNumber = Collections.max(buildNumbersOfForkedRoot) + 1;
							numberElement.setText(buildNumber.toString());
						} 
						buildNumbersOfForkedRoot.add(buildNumber);
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Element numberElement = element.element("number");
					Long requestNumber = Long.valueOf(numberElement.getTextTrim());
					Long projectId = Long.valueOf(element.elementTextTrim("targetProject"));
					Long forkedRoot = forkedRoots.get(projectId);
					element.addElement("numberScope").setText(forkedRoot.toString());
					if (!projectId.equals(forkedRoot)) {
						Set<Long> requestNumbersOfForkedRoot = pullRequestNumbers.get(forkedRoot);
						if (requestNumbersOfForkedRoot.contains(requestNumber)) {
							requestNumber = Collections.max(requestNumbersOfForkedRoot) + 1;
							numberElement.setText(requestNumber.toString());
						} 
						requestNumbersOfForkedRoot.add(requestNumber);
					}
				}
				dom.writeToFile(file, false);
			} 
		}
	}
	
	// from 3.0.11 to 3.0.12
	private void migrate39(File dataDir, Stack<Integer> versions) {
	}

	// from 3.0.x to 3.1.x
	private void migrate40(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				try {
					String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
					content = StringUtils.replace(content, "io.onedev.server.issue.", 
							"io.onedev.server.model.support.issue.");
					content = StringUtils.replace(content, "io.onedev.server.util.inputspec.", 
							"io.onedev.server.model.support.inputspec.");
					FileUtils.writeFile(file, content, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		
		long maxRoleId = 0;
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					long roleId = Long.parseLong(element.elementTextTrim("id"));
					if (roleId > maxRoleId) 
						maxRoleId = roleId;
				}
			}
		}
		
		boolean hasOwnerRole = false;
		String idOfRolePreviouslyUsingOwnerId = null;
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element idElement = element.element("id");
					if (idElement.getText().trim().equals("1")) {
						if (element.elementText("manageProject").equals("true")) {
							element.element("name").setText("Owner");
							hasOwnerRole = true;
						} else {
							idOfRolePreviouslyUsingOwnerId = String.valueOf(maxRoleId+1);
							idElement.setText(idOfRolePreviouslyUsingOwnerId);
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
		if (!hasOwnerRole) {
			File dataFile = new File(dataDir, "Roles.xml");
			VersionedXmlDoc dom;
			if (dataFile.exists()) { 
				dom = VersionedXmlDoc.fromFile(dataFile);
			} else {
				dom = new VersionedXmlDoc();
				dom.addElement("list");
			}
			
			Element ownerRoleElement = dom.getRootElement().addElement("io.onedev.server.model.Role");
			ownerRoleElement.addAttribute("revision", "0.0");
			ownerRoleElement.addElement("id").setText("1");
			ownerRoleElement.addElement("name").setText("Owner");
			ownerRoleElement.addElement("manageProject").setText("true");
			ownerRoleElement.addElement("managePullRequests").setText("false");
			ownerRoleElement.addElement("manageCodeComments").setText("false");
			ownerRoleElement.addElement("codePrivilege").setText("NONE");
			ownerRoleElement.addElement("manageIssues").setText("false");
			ownerRoleElement.addElement("scheduleIssues").setText("false");
			ownerRoleElement.addElement("editableIssueFields").addAttribute("class", 
					"io.onedev.server.model.support.role.AllIssueFields");
			ownerRoleElement.addElement("manageBuilds").setText("false");
			ownerRoleElement.addElement("jobPrivileges");
			dom.writeToFile(dataFile, false);
		}
		
		if (idOfRolePreviouslyUsingOwnerId != null) {
			for (File file: dataDir.listFiles()) {
				if (file.getName().startsWith("UserAuthorizations.xml") 
						|| file.getName().startsWith("GroupAuthorizations.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element: dom.getRootElement().elements()) {
						Element roleElement = element.element("role");
						if (roleElement.getText().trim().equals("1"))
							roleElement.setText(idOfRolePreviouslyUsingOwnerId);
					}
					dom.writeToFile(file, false);
				}
			}
		}
		
		Map<String, Element> userBuildSettingElements = new HashMap<>();
		Map<String, Element> userWebHooksElements = new HashMap<>();
		Map<String, String> projectOwners = new HashMap<>();
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("defaultTransitionSpecs").setName("transitionSpecs");
							valueElement.element("defaultPromptFieldsUponIssueOpen").setName("promptFieldsUponIssueOpen");
							valueElement.element("defaultBoardSpecs").setName("boardSpecs");
							for (Node node: valueElement.selectNodes("//uuid"))
								node.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					projectOwners.put(element.elementText("id").trim(), element.elementText("owner").trim());
					
					Element transitionSpecsElement = element.element("transitionSpecs");
					if (transitionSpecsElement != null)
						transitionSpecsElement.detach();
					Element promptFieldsUponIssueOpenElement = element.element("promptFieldsUponIssueOpen");
					if (promptFieldsUponIssueOpenElement != null)
						promptFieldsUponIssueOpenElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String id = element.elementText("id").trim();
					Element buildSettingElement = element.element("buildSetting");
					buildSettingElement.detach();
					userBuildSettingElements.put(id, buildSettingElement);
					Element webHooksElement = element.element("webHooks");
					webHooksElement.detach();
					userWebHooksElements.put(id, webHooksElement);
				}
				dom.writeToFile(file, false);
			}
		}
		
		long maxUserAuthorizationId = 0;
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element ownerElement = element.element("owner");
					ownerElement.detach();
					
					String ownerId = ownerElement.getText().trim();
					
					for (Element webHookElement: userWebHooksElements.get(ownerId).elements())
						element.element("webHooks").add(webHookElement.createCopy());
					
					Element buildSettingElement = element.element("buildSetting");
					Element userBuildSettingElement = userBuildSettingElements.get(ownerId);
					
					for (Element buildPreservationElement: userBuildSettingElement.element("buildPreservations").elements()) 
						buildSettingElement.element("buildPreservations").add(buildPreservationElement.createCopy());
					
					for (Element actionAuthorizationElement: userBuildSettingElement.element("actionAuthorizations").elements()) 
						buildSettingElement.element("actionAuthorizations").add(actionAuthorizationElement.createCopy());
					
					Element jobSecretsElement = buildSettingElement.element("jobSecrets");
					Set<String> existingJobSecretNames = new HashSet<>();
					for (Element jobSecretElement: jobSecretsElement.elements()) 
						existingJobSecretNames.add(jobSecretElement.elementText("name").trim());
					
					for (Element jobSecretElement: userBuildSettingElement.element("jobSecrets").elements()) {
						if (!existingJobSecretNames.contains(jobSecretElement.elementText("name").trim())) 
							jobSecretsElement.add(jobSecretElement.createCopy());
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("UserAuthorizations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					long userAuthorizationId = Long.parseLong(element.elementText("id").trim());
					if (userAuthorizationId > maxUserAuthorizationId)
						maxUserAuthorizationId = userAuthorizationId;
					String projectId = element.elementText("project").trim();
					String userId = element.elementText("user").trim();
					Element roleElement = element.element("role");
					if (userId.equals(projectOwners.get(projectId))) {
						roleElement.setText("1");
						projectOwners.remove(projectId);
					}
				}				
				dom.writeToFile(file, false);
			}
		}
		
		File dataFile = new File(dataDir, "UserAuthorizations.xml");
		VersionedXmlDoc dom;
		if (dataFile.exists()) { 
			dom = VersionedXmlDoc.fromFile(dataFile);
		} else {
			dom = new VersionedXmlDoc();
			dom.addElement("list");
		}
		for (Map.Entry<String, String> entry: projectOwners.entrySet()) {
			Element userAuthorizationElement = dom.getRootElement().addElement("io.onedev.server.model.UserAuthorization");
			userAuthorizationElement.addAttribute("revision", "0.0");
			userAuthorizationElement.addElement("id").setText(String.valueOf(++maxUserAuthorizationId));
			userAuthorizationElement.addElement("project").setText(entry.getKey());
			userAuthorizationElement.addElement("user").setText(entry.getValue());
			userAuthorizationElement.addElement("role").setText("1");
		}
		dom.writeToFile(dataFile, false);
	}
	
	private void migrate41(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) 
							valueElement.addElement("sendAsHtml").setText("true");
					}
				}
				dom.writeToFile(file, false);
			}
		}	
	}
	
	// Migrate to 3.2.0
	private void migrate42(File dataDir, Stack<Integer> versions) {
		Map<String, String> commentRequests = new HashMap<>();
		Map<String, String> requestTargetHeads = new HashMap<>();
		Map<String, String> requestBaseCommits = new HashMap<>();
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeCommentRelations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements())
					commentRequests.put(element.elementTextTrim("comment"), element.elementTextTrim("request"));
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					String id = element.elementTextTrim("id");
					requestBaseCommits.put(id, element.elementTextTrim("baseCommitHash"));
					Element lastMergePreviewElement = element.element("lastMergePreview");
					if (lastMergePreviewElement != null) {
						Element targetHeadElement = lastMergePreviewElement.element("targetHead");
						requestTargetHeads.put(id, targetHeadElement.getTextTrim());
						targetHeadElement.setName("targetHeadCommitHash");
						lastMergePreviewElement.element("requestHead").setName("headCommitHash");
						Element mergedElement = lastMergePreviewElement.element("merged");
						if (mergedElement != null)
							mergedElement.setName("mergeCommitHash");
					} 
					element.element("headCommitHash").detach();
				}
				dom.writeToFile(file, false);
			} 
		}
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				try {
					String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
					content = StringUtils.replace(content, 
							"io.onedev.server.model.support.administration.authenticator.ldap.", 
							"io.onedev.server.plugin.authenticator.ldap.");
					content = StringUtils.replace(content, 
							"io.onedev.server.model.support.issue.transitiontrigger.DiscardPullRequest", 
							"io.onedev.server.model.support.issue.transitiontrigger.DiscardPullRequestTrigger");
					content = StringUtils.replace(content, 
							"io.onedev.server.model.support.issue.transitiontrigger.MergePullRequest", 
							"io.onedev.server.model.support.issue.transitiontrigger.MergePullRequestTrigger");
					content = StringUtils.replace(content, 
							"io.onedev.server.model.support.issue.transitiontrigger.OpenPullRequest", 
							"io.onedev.server.model.support.issue.transitiontrigger.OpenPullRequestTrigger");
					FileUtils.writeFile(file, content, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("SSH")) {
						Element valueElement = element.element("value");
						if (valueElement != null) 
							valueElement.element("privateKey").setName("pemPrivateKey");
					} else if (keyElement.getTextTrim().equals("JOB_SCRIPTS")) {
						keyElement.setText("GROOVY_SCRIPTS");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					Element actionAuthorizationsElement = buildSettingElement.element("actionAuthorizations");
					if (actionAuthorizationsElement.elements().isEmpty()) {
						actionAuthorizationsElement.addElement("io.onedev.server.model.support.build.actionauthorization.CreateTagAuthorization");
						actionAuthorizationsElement.addElement("io.onedev.server.model.support.build.actionauthorization.CloseMilestoneAuthorization");
					}
					element.addElement("issueManagementEnabled").setText("true");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element compareContextElement = element.element("compareContext");
					compareContextElement.element("compareCommit").setName("compareCommitHash");
					Element markPosElement = element.element("markPos");
					markPosElement.setName("mark");
					markPosElement.element("commit").setName("commitHash");
					String requestId = commentRequests.get(element.elementTextTrim("id"));
					if (requestId != null)
						element.addElement("request").setText(requestId);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) 
					element.addElement("submitReason").setText("Unknown");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentRelations.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("PullRequestBuilds.xml")) {
				try {
					String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8.name());
					content = StringUtils.replace(content, "PullRequestBuild", "PullRequestVerification");
					FileUtils.deleteFile(file);
					String newFileName = StringUtils.replace(file.getName(), "PullRequestBuild", "PullRequestVerification");
					FileUtils.writeFile(new File(dataDir, newFileName), content, StandardCharsets.UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (file.getName().startsWith("PullRequestReviews.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					Element excludeDateElement = element.element("excludeDate");
					if (excludeDateElement != null)
						excludeDateElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.addElement("accessToken").setText(RandomStringUtils.randomAlphanumeric(40));
					element.addElement("ssoInfo").addElement("subject").setText(UUID.randomUUID().toString());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestUpdates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) { 
					element.element("mergeBaseCommitHash").detach();
					String requestId = element.elementTextTrim("request");
					String targetHead = requestTargetHeads.get(requestId);
					Element targetHeadCommitHashElement = element.addElement("targetHeadCommitHash");
					if (targetHead != null) 
						targetHeadCommitHashElement.setText(targetHead);
					else
						targetHeadCommitHashElement.setText(requestBaseCommits.get(requestId));
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	// Migrate to 3.2.2
	private void migrate43(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("issueTemplates");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate44_abbreviate(Element element, int maxLen) {
		if (element != null) {
			String text = StringUtils.abbreviate(element.getText().trim(), maxLen);
			element.setText(text);
		}
	}
	
	// Migrate to 4.0.5
	private void migrate44(File dataDir, Stack<Integer> versions) {
		Map<String, String> verifications = new HashMap<>();
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequestVerifications.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					verifications.put(element.elementTextTrim("build"), element.elementTextTrim("request"));
				}
			}
		}
		
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.addElement("triggerId").setText(UUID.randomUUID().toString());
					String requestId = verifications.get(element.elementTextTrim("id"));
					if (requestId != null)
						element.addElement("request").setText(requestId);
					Element updatedRefElement = element.element("updatedRef");
					if (updatedRefElement != null)
						updatedRefElement.setName("refName");
					migrate44_abbreviate(element.element("errorMessage"), 12000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 15000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 15000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 12000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	// Migrate to 4.0.6
	private void migrate45(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) 
					element.element("triggerId").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	// migrate to 4.0.7
	private void migrate46(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						for (Element executorElement: valueElement.elements()) {
							if (executorElement.getName().contains("KubernetesExecutor")) {
								Element serviceAccountElement = executorElement.element("serviceAccount");
								if (serviceAccountElement != null)
									serviceAccountElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	// migrate to 4.0.8
	private void migrate47(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element refNameElement = element.element("refName");
					if (refNameElement == null)
						element.addElement("refName").setText("unknown");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	// migrate to 4.1.0
	private void migrate48(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
				content = StringUtils.replace(content, 
						"\" is before \"", "\" is until \"");
				content = StringUtils.replace(content, 
						"\" is after \"", "\" is since \"");
				FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}	
	}
	
	// migrate to 4.2.0
	private void migrate49(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					buildSettingElement.addElement("defaultFixedIssueFilters");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
}
