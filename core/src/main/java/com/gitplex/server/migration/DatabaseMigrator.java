package com.gitplex.server.migration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.inject.Singleton;

import org.dom4j.Element;

import com.gitplex.server.GitPlex;
import com.gitplex.server.manager.CodeCommentManager;
import com.gitplex.server.model.CodeComment;
import com.gitplex.server.util.FileUtils;
import com.gitplex.server.util.StringUtils;
import com.google.common.base.Charsets;

@Singleton
@SuppressWarnings("unused")
public class DatabaseMigrator {
	
	private void migrate1(File dataDir, Stack<Integer> versions) {
		for (File file: dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
			VersionedDocument dom = VersionedDocument.fromFile(file);
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
				String content = FileUtils.readFileToString(file, Charsets.UTF_8);
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
				FileUtils.writeStringToFile(file, content, Charsets.UTF_8);
				
				if (file.getName().equals("VersionTables.xml")) {
					FileUtils.moveFile(file, new File(file.getParentFile(), "ModelVersions.xml"));
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}	
	}
	
	private void migrateIntegrationStrategy(Element integrationStrategyElement) {
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SYSTEM")) {
						Element settingElement = element.element("setting");
						settingElement.addElement("curlConfig")
								.addAttribute("class", "com.gitplex.server.git.config.SystemCurl");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Accounts.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("reviewEffort").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Depots.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					element.element("gateKeepers").detach();
					element.element("integrationPolicies").detach();
					element.addElement("branchProtections");
					element.addElement("tagProtections");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedDocument dom = VersionedDocument.fromFile(file);
				for (Element element: dom.getRootElement().elements()) {
					Element assigneeElement = element.element("assignee");
					if (assigneeElement != null)
						assigneeElement.detach();
					migrateIntegrationStrategy(element.element("integrationStrategy"));
					Element lastIntegrationPreviewElement = element.element("lastIntegrationPreview");
					if (lastIntegrationPreviewElement != null) {
						lastIntegrationPreviewElement.setName("lastMergePreview");
						Element integratedElement = lastIntegrationPreviewElement.element("integrated");
						if (integratedElement != null)
							integratedElement.setName("merged");
						migrateIntegrationStrategy(lastIntegrationPreviewElement.element("integrationStrategy"));
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
					String content = FileUtils.readFileToString(renamedFile, Charsets.UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Account", 
							"com.gitplex.server.model.User");
					VersionedDocument dom = VersionedDocument.fromXML(content);
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
			VersionedDocument userAuthorizationsDom = new VersionedDocument();
			Element userAuthorizationListElement = userAuthorizationsDom.addElement("list");
			
			for (File file: dataDir.listFiles()) {
				if (file.getName().startsWith("Depots.xml")) {
					File renamedFile = new File(dataDir, file.getName().replace("Depots.xml", "Projects.xml"));
					FileUtils.moveFile(file, renamedFile);
					String content = FileUtils.readFileToString(renamedFile, Charsets.UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Depot", 
							"com.gitplex.server.model.Project");
					VersionedDocument dom = VersionedDocument.fromXML(content);
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
					VersionedDocument dom = VersionedDocument.fromFile(file);
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
					VersionedDocument dom = VersionedDocument.fromFile(file);
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
				VersionedDocument dom = VersionedDocument.fromFile(file);
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
		VersionedDocument dom = VersionedDocument.fromFile(new File(dataDir, "Configs.xml"));
		for (Element element: dom.getRootElement().elements()) {
			if (element.elementText("key").equals("SYSTEM")) {
				String storagePath = element.element("setting").elementText("storagePath");
				File codeCommentsFromWeiFeng = new File(storagePath, "CodeComments.xml");
				if (codeCommentsFromWeiFeng.exists()) {
					dom = VersionedDocument.fromFile(codeCommentsFromWeiFeng);
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
	
}
