package io.onedev.server.data.migration;

import static io.onedev.server.util.DateUtils.toLocalDate;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.PublicKey;
import java.text.MessageFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import javax.inject.Singleton;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.apache.sshd.common.config.keys.KeyUtils;
import org.apache.sshd.common.digest.BuiltinDigests;
import org.dom4j.Element;
import org.dom4j.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.thoughtworks.xstream.core.JVM;

import io.onedev.commons.bootstrap.Bootstrap;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspecmodel.inputspec.InputSpec;
import io.onedev.server.service.SettingService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.markdown.MentionParser;
import io.onedev.server.model.Issue;
import io.onedev.server.model.IssueComment;
import io.onedev.server.model.IssueStateHistory;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.PullRequestComment;
import io.onedev.server.model.User;
import io.onedev.server.model.support.TimeGroups;
import io.onedev.server.ssh.SshKeyUtils;
import io.onedev.server.util.CryptoUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.util.DirectoryVersionUtils;
import io.onedev.server.util.Pair;
import io.onedev.server.util.ParsedEmailAddress;
import io.onedev.server.util.patternset.PatternSet;
import oshi.SystemInfo;
import oshi.hardware.HardwareAbstractionLayer;


@Singleton
@SuppressWarnings("unused")
public class DataMigrator {

	private static final Logger logger = LoggerFactory.getLogger(DataMigrator.class);

	private static final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'");

	private void migrate5(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
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
				FileUtils.writeStringToFile(file, content, UTF_8);

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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SYSTEM")) {
						Element settingElement = element.element("setting");
						settingElement.addElement("curlConfig")
								.addAttribute("class", "com.gitplex.server.git.config.SystemCurl");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Accounts.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("reviewEffort").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Depots.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("gateKeepers").detach();
					element.element("integrationPolicies").detach();
					element.addElement("branchProtections");
					element.addElement("tagProtections");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Accounts.xml")) {
					File renamedFile = new File(dataDir, file.getName().replace("Accounts.xml", "Users.xml"));
					FileUtils.moveFile(file, renamedFile);
					String content = FileUtils.readFileToString(renamedFile, UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Account",
							"com.gitplex.server.model.User");
					VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
					for (Element element : dom.getRootElement().elements()) {
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

			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Depots.xml")) {
					File renamedFile = new File(dataDir, file.getName().replace("Depots.xml", "Projects.xml"));
					FileUtils.moveFile(file, renamedFile);
					String content = FileUtils.readFileToString(renamedFile, UTF_8);
					content = StringUtils.replace(content, "com.gitplex.server.model.Depot",
							"com.gitplex.server.model.Project");
					VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
					for (Element element : dom.getRootElement().elements()) {
						String accountId = element.elementText("account");
						element.element("account").detach();
						String depotName = element.elementText("name");
						element.element("name").setText(accountIdToName.get(accountId) + "." + depotName);
						if (element.element("defaultPrivilege") != null)
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
					for (Element element : dom.getRootElement().elements()) {
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
					for (Element element : dom.getRootElement().elements()) {
						if (element.elementText("key").equals("SYSTEM")) {
							String storagePath = element.element("setting").elementText("storagePath");
							File storageDir = new File(storagePath);
							File repositoriesDir = new File(storageDir, "repositories");
							if (repositoriesDir.exists()) {
								File projectsDir = new File(storageDir, "projects");
								FileUtils.moveDirectory(repositoriesDir, projectsDir);
								for (File projectDir : projectsDir.listFiles()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml") || file.getName().startsWith("CodeCommentReplys.xml")
					|| file.getName().startsWith("CodeCommentStatusChanges.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						Element exprElement = branchProtectionElement.element("reviewAppointmentExpr");
						if (exprElement != null)
							exprElement.setName("reviewRequirementSpec");
						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
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
		for (Element element : dom.getRootElement().elements()) {
			if (element.elementText("key").equals("SYSTEM")) {
				String storagePath = element.element("setting").elementText("storagePath");
				File codeCommentsFromWeiFeng = new File(storagePath, "CodeComments.xml");
				if (codeCommentsFromWeiFeng.exists()) {
					dom = VersionedXmlDoc.fromFile(codeCommentsFromWeiFeng);
					for (Element commentElement : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Configs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				long maxId = 0;
				for (Element element : dom.getRootElement().elements()) {
					Long id = Long.parseLong(element.elementTextTrim("id"));
					if (maxId < id)
						maxId = id;
				}
				Element licenseConfigElement = dom.getRootElement().addElement("com.gitplex.server.model.Config");
				licenseConfigElement.addElement("id").setText(String.valueOf(maxId + 1));
				licenseConfigElement.addElement("key").setText("LICENSE");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate12(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : projectElement.element("branchProtections").elements()) {
						branchProtectionElement.addElement("enabled").setText("true");
					}
					for (Element tagProtectionElement : projectElement.element("tagProtections").elements()) {
						tagProtectionElement.addElement("enabled").setText("true");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate13(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content, "gitplex", "turbodev");
				content = StringUtils.replace(content, "GitPlex", "TurboDev");
				FileUtils.writeFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void migrate14(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : projectElement.element("branchProtections").elements()) {
						Element submitterElement = branchProtectionElement.addElement("submitter");
						submitterElement.addAttribute("class", "com.turbodev.server.model.support.submitter.Anyone");
						branchProtectionElement.addElement("noCreation").setText("true");
					}
					for (Element tagProtectionElement : projectElement.element("tagProtections").elements()) {
						tagProtectionElement.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate15(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content, "com.turbodev", "io.onedev");
				content = StringUtils.replace(content, "com/turbodev", "io/onedev");
				content = StringUtils.replace(content, "turbodev.com", "onedev.io");
				content = StringUtils.replace(content, "turbodev", "onedev");
				content = StringUtils.replace(content, "TurboDev", "OneDev");
				FileUtils.writeFile(file, content, UTF_8);
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

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element fullNameElement = element.element("fullName");
					if (fullNameElement != null)
						userNames.put(element.elementTextTrim("id"), fullNameElement.getText());
					else
						userNames.put(element.elementTextTrim("id"), element.elementText("name"));
				}
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("comment");
					Integer replyCount = codeCommentReplyCounts.get(commentId);
					if (replyCount == null)
						replyCount = 0;
					replyCount++;
					codeCommentReplyCounts.put(commentId, replyCount);
				}
			} else if (file.getName().startsWith("CodeCommentRelations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("request");
					Integer commentCount = requestCommentCounts.get(commentId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					requestCommentCounts.put(commentId, commentCount);
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("closeInfo") == null) {
						openRequests.add(element.elementTextTrim("id"));
					}
				}
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String projectId = element.elementTextTrim("id");
					StringBuilder builder = new StringBuilder();
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						Element reviewRequirementSpecElement = branchProtectionElement.element("reviewRequirementSpec");
						if (reviewRequirementSpecElement != null)
							builder.append(reviewRequirementSpecElement.getText()).append(";");

						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
							reviewRequirementSpecElement = fileProtectionElement.element("reviewRequirementSpec");
							builder.append(reviewRequirementSpecElement.getText()).append(";");
						}
					}
					reviewRequirements.put(projectId, builder.toString());
				}
			}
		}

		for (Map.Entry<String, Set<String>> entry : requestCodeComments.entrySet()) {
			Integer commentCount = requestCommentCounts.get(entry.getKey());
			if (commentCount == null)
				commentCount = 0;
			for (String commentId : entry.getValue()) {
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

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("BranchWatches.xml")
					|| file.getName().startsWith("PullRequestReferences.xml")
					|| file.getName().startsWith("PullRequestStatusChanges.xml")
					|| file.getName().startsWith("PullRequestTasks.xml")
					|| file.getName().startsWith("ReviewInvitations.xml")
					|| file.getName().startsWith("Reviews.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("UserAuthorizations.xml") || file.getName().startsWith("GroupAuthorizations.xml")) {
				try {
					String content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content, "ADMIN", "ADMINISTRATION");
					content = StringUtils.replace(content, "WRITE", "CODE_WRITE");
					content = StringUtils.replace(content, "READ", "CODE_READ");
					FileUtils.writeFile(file, content, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
					content = FileUtils.readFileToString(file, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = StringUtils.replace(content, "io.onedev.server.security.authenticator.",
						"io.onedev.server.model.support.authenticator.");
				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.Setting");
					Element settingElement = element.element("setting");
					if (settingElement != null) {
						settingElement.setName("value");
						if (element.elementTextTrim("key").equals("AUTHENTICATOR")) {
							Element authenticatorElement = settingElement.elementIterator().next();
							settingElement.addAttribute("class", authenticatorElement.getName());
							for (Element fieldElement : authenticatorElement.elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					element.element("uuid").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String project = element.elementTextTrim("id");
					Element publicReadElement = element.element("publicRead");
					if (publicReadElement.getTextTrim().equals("true"))
						element.addElement("defaultPrivilege").setText("CODE_READ");
					publicReadElement.detach();

					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.element("verifyMerges").setName("buildMerges");
						Element verificationsElement = branchProtectionElement.element("verifications");
						verificationsElement.setName("configurations");
						for (Element verificationElement : verificationsElement.elements()) {
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
							for (String request : openRequests) {
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

						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
							reviewRequirementSpecElement = fileProtectionElement.element("reviewRequirementSpec");
							reviewRequirementSpecElement.setName("reviewRequirement");
						}
					}
					for (Element tagProtectionElement : element.element("tagProtections").elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issue")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("LICENSE"))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate19(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldElement : valueElement.element("fieldSpecs").elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.element("branch").setName("branches");
						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
							fileProtectionElement.element("path").setName("paths");
						}
					}
					for (Element tagProtectionElement : element.element("tagProtections").elements()) {
						tagProtectionElement.element("tag").setName("tags");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("LICENSE")) {
						element.element("value").addAttribute("class", "io.onedev.commons.utils.license.LicenseDetail");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate22(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueFieldUnarys.xml")) {
				File renamedFile = new File(dataDir, file.getName().replace("IssueFieldUnarys", "IssueFieldEntitys"));
				try {
					FileUtils.moveFile(file, renamedFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(renamedFile);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.IssueFieldEntity");
				}
				dom.writeToFile(renamedFile, false);
			}
		}
	}

	private void migrate23(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Build2s.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					element.element("uuid").detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private String escapeValue24(String value) {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < value.length(); i++) {
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
		for (Element transitionElement : transitionSpecsElement.elements()) {
			Element triggerElement = transitionElement.element("trigger");
			if (triggerElement.attributeValue("class").contains("PressButtonTrigger"))
				migrateUserMatcher24(triggerElement.element("authorized"));
		}
	}

	private void migrate24(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							migrateTransitionSpecsElement24(valueElement.element("defaultTransitionSpecs"));
							for (Element fieldElement : valueElement.element("fieldSpecs").elements())
								migrateUserMatcher24(fieldElement.element("canBeChangedBy"));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element issueSettingElement = element.element("issueSetting");
					Element transitionsElement = issueSettingElement.element("transitionSpecs");
					if (transitionsElement != null)
						migrateTransitionSpecsElement24(transitionsElement);
					for (Element branchProtectionElement : element.element("branchProtections").elements())
						migrateUserMatcher24(branchProtectionElement.element("submitter"));
					for (Element tagProtectionElement : element.element("tagProtections").elements())
						migrateUserMatcher24(tagProtectionElement.element("submitter"));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// from 2.0 to 3.0
	private void migrate25(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace(".support.setting.", ".support.administration.");
				content = content.replace(".support.authenticator.", ".support.administration.authenticator.");

				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements())
					element.element("canCreateProjects").setName("createProjects");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements())
					element.element("numberStr").detach();
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
					content = FileUtils.readFileToString(file, UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace("DO_NOT_MERGE", "CREATE_MERGE_COMMIT");

				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element : dom.getRootElement().elements())
					element.element("numberStr").detach();

				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element defaultPrivilegeElement = element.element("defaultPrivilege");
					if (defaultPrivilegeElement != null)
						defaultPrivilegeElement.detach();
					element.addElement("owner").setText("1");

					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						Element submitterElement = branchProtectionElement.element("submitter");
						submitterElement.setName("user");
						submitterElement.setText("anyone");
						branchProtectionElement.element("configurations").detach();
						branchProtectionElement.element("buildMerges").detach();
						branchProtectionElement.addElement("jobNames");
						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements())
							fileProtectionElement.addElement("jobNames");
					}

					for (Element tagProtectionElement : element.element("tagProtections").elements())
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
					content = FileUtils.readFileToString(file, UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				content = content.replace("io.onedev.server.model.IssueFieldEntity",
						"io.onedev.server.model.IssueField");

				FileUtils.deleteFile(file);

				File renamedFile = new File(dataDir, file.getName().replace(
						"IssueFieldEntitys.xml", "IssueFields.xml"));
				FileUtils.writeFile(renamedFile, content, UTF_8);
			}
		}
		try (InputStream is = getClass().getResourceAsStream("migrate25_roles.xml")) {
			Preconditions.checkNotNull(is);
			FileUtils.writeFile(
					new File(dataDir, "Roles.xml"),
					StringUtils.join(IOUtils.readLines(is, UTF_8.name()), "\n"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

	}

	private void migrate26(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element createdAtElement = element.element("createdAt");
					createdAtElement.setName("createDate");
					element.addElement("updateDate").setText(createdAtElement.getText());
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate27(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element buildSettingElement = element.addElement("buildSetting");
					buildSettingElement.addElement("secrets");
					buildSettingElement.addElement("buildPreservations");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("webHooks");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements())
						branchProtectionElement.element("user").setName("userMatch");
					for (Element tagProtectionElement : element.element("tagProtections").elements())
						tagProtectionElement.element("user").setName("userMatch");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate30(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS"))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate31(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.element("noCreation").setName("preventCreation");
						branchProtectionElement.element("noDeletion").setName("preventDeletion");
						branchProtectionElement.element("noForcedPush").setName("preventForcedPush");
					}
					for (Element tagProtectionElement : element.element("tagProtections").elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element stateElement : valueElement.element("stateSpecs").elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					element.element("numOfOpenIssues").setName("numOfIssuesTodo");
					element.element("numOfClosedIssues").setName("numOfIssuesDone");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate35(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					Element namedQueriesElement = buildSettingElement.element("namedQueries");
					if (namedQueriesElement != null) {
						for (Element queryElement : namedQueriesElement.elements())
							queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
					}
					Element secretsElement = buildSettingElement.element("secrets");
					secretsElement.setName("jobSecrets");
					for (Element secretElement : secretsElement.elements())
						secretElement.setName("io.onedev.server.model.support.build.JobSecret");
					for (Element buildPreservationElement : buildSettingElement.element("buildPreservations").elements())
						buildPreservationElement.setName("io.onedev.server.model.support.build.BuildPreservation");
					buildSettingElement.addElement("actionAuthorizations");

					for (Element tagProtectionElement : element.element("tagProtections").elements()) {
						Element buildBranchesElement = tagProtectionElement.element("buildBranches");
						if (buildBranchesElement != null)
							buildBranchesElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element queryElement : element.element("userBuildQueries").elements())
						queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
					Element buildSettingElement = element.element("buildSetting");
					Element secretsElement = buildSettingElement.element("secrets");
					secretsElement.setName("jobSecrets");
					for (Element secretElement : secretsElement.elements())
						secretElement.setName("io.onedev.server.model.support.build.JobSecret");
					for (Element buildPreservationElement : buildSettingElement.element("buildPreservations").elements())
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
				for (Element element : dom.getRootElement().elements()) {
					for (Element queryElement : element.element("userQueries").elements())
						queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element stateElement : valueElement.element("stateSpecs").elements()) {
								stateElement.element("done").detach();
							}
						}
					} else if (element.elementTextTrim("key").equals("BUILD")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element queryElement : valueElement.element("namedQueries").elements())
								queryElement.setName("io.onedev.server.model.support.build.NamedBuildQuery");
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Milestones.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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

		for (File file : dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.pullrequest.NamedPullRequestQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.issue.NamedIssueQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.build.NamedBuildQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.NamedProjectQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.elementTextTrim("query").equals("all"))
							element.element("query").detach();
					}
				}
				for (Node node : dom.selectNodes("//issueQuery")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						if (element.getTextTrim().equals("all"))
							element.detach();
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.build.BuildPreservation")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						Element conditionElement = element.element("condition");
						if (conditionElement.getTextTrim().equals("all"))
							conditionElement.detach();
					}
				}
				for (Node node : dom.selectNodes("//listFields")) {
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
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					String className = dataElement.attributeValue("class");
					if (className.contains("IssueCommittedData") || className.contains("IssuePullRequest"))
						element.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long projectId = Long.valueOf(element.elementTextTrim("id"));
					Element forkedFromElement = element.element("forkedFrom");
					if (forkedFromElement != null)
						forkedFroms.put(projectId, Long.valueOf(forkedFromElement.getTextTrim()));
					else
						forkedFroms.put(projectId, null);
				}
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (Long projectId : forkedFroms.keySet()) {
			forkedRoots.put(projectId, getForkedRoot38(forkedFroms, projectId));
		}

		Map<Long, Set<Long>> issueNumbers = new HashMap<>();
		Map<Long, Set<Long>> buildNumbers = new HashMap<>();
		Map<Long, Set<Long>> pullRequestNumbers = new HashMap<>();

		for (Long forkedRoot : forkedRoots.values()) {
			issueNumbers.put(forkedRoot, new HashSet<>());
			buildNumbers.put(forkedRoot, new HashSet<>());
			pullRequestNumbers.put(forkedRoot, new HashSet<>());
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long issueNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					if (projectId.equals(forkedRoots.get(projectId)))
						issueNumbers.get(projectId).add(issueNumber);
				}
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long buildNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("project"));
					if (projectId.equals(forkedRoots.get(projectId)))
						buildNumbers.get(projectId).add(buildNumber);
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long requestNumber = Long.valueOf(element.elementTextTrim("number"));
					Long projectId = Long.valueOf(element.elementTextTrim("targetProject"));
					if (projectId.equals(forkedRoots.get(projectId)))
						pullRequestNumbers.get(projectId).add(requestNumber);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				try {
					String content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content, "io.onedev.server.issue.",
							"io.onedev.server.model.support.issue.");
					content = StringUtils.replace(content, "io.onedev.server.util.inputspec.",
							"io.onedev.server.model.support.inputspec.");
					FileUtils.writeFile(file, content, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		long maxRoleId = 0;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					long roleId = Long.parseLong(element.elementTextTrim("id"));
					if (roleId > maxRoleId)
						maxRoleId = roleId;
				}
			}
		}

		boolean hasOwnerRole = false;
		String idOfRolePreviouslyUsingOwnerId = null;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element idElement = element.element("id");
					if (idElement.getText().trim().equals("1")) {
						if (element.elementText("manageProject").equals("true")) {
							element.element("name").setText("Owner");
							hasOwnerRole = true;
						} else {
							idOfRolePreviouslyUsingOwnerId = String.valueOf(maxRoleId + 1);
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
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("UserAuthorizations.xml")
						|| file.getName().startsWith("GroupAuthorizations.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("defaultTransitionSpecs").setName("transitionSpecs");
							valueElement.element("defaultPromptFieldsUponIssueOpen").setName("promptFieldsUponIssueOpen");
							valueElement.element("defaultBoardSpecs").setName("boardSpecs");
							for (Node node : valueElement.selectNodes("//uuid"))
								node.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element ownerElement = element.element("owner");
					ownerElement.detach();

					String ownerId = ownerElement.getText().trim();

					for (Element webHookElement : userWebHooksElements.get(ownerId).elements())
						element.element("webHooks").add(webHookElement.createCopy());

					Element buildSettingElement = element.element("buildSetting");
					Element userBuildSettingElement = userBuildSettingElements.get(ownerId);

					for (Element buildPreservationElement : userBuildSettingElement.element("buildPreservations").elements())
						buildSettingElement.element("buildPreservations").add(buildPreservationElement.createCopy());

					for (Element actionAuthorizationElement : userBuildSettingElement.element("actionAuthorizations").elements())
						buildSettingElement.element("actionAuthorizations").add(actionAuthorizationElement.createCopy());

					Element jobSecretsElement = buildSettingElement.element("jobSecrets");
					Set<String> existingJobSecretNames = new HashSet<>();
					for (Element jobSecretElement : jobSecretsElement.elements())
						existingJobSecretNames.add(jobSecretElement.elementText("name").trim());

					for (Element jobSecretElement : userBuildSettingElement.element("jobSecrets").elements()) {
						if (!existingJobSecretNames.contains(jobSecretElement.elementText("name").trim()))
							jobSecretsElement.add(jobSecretElement.createCopy());
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("UserAuthorizations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (Map.Entry<String, String> entry : projectOwners.entrySet()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeCommentRelations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					commentRequests.put(element.elementTextTrim("comment"), element.elementTextTrim("request"));
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				try {
					String content = FileUtils.readFileToString(file, UTF_8.name());
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
					FileUtils.writeFile(file, content, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}

				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements())
					element.addElement("submitReason").setText("Unknown");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentRelations.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("PullRequestBuilds.xml")) {
				try {
					String content = FileUtils.readFileToString(file, UTF_8.name());
					content = StringUtils.replace(content, "PullRequestBuild", "PullRequestVerification");
					FileUtils.deleteFile(file);
					String newFileName = StringUtils.replace(file.getName(), "PullRequestBuild", "PullRequestVerification");
					FileUtils.writeFile(new File(dataDir, newFileName), content, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			} else if (file.getName().startsWith("PullRequestReviews.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element excludeDateElement = element.element("excludeDate");
					if (excludeDateElement != null)
						excludeDateElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("accessToken").setText(CryptoUtils.generateSecret());
					element.addElement("ssoInfo").addElement("subject").setText(UUID.randomUUID().toString());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestUpdates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequestVerifications.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					verifications.put(element.elementTextTrim("build"), element.elementTextTrim("request"));
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 15000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 15000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("description"), 12000);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate44_abbreviate(element.element("content"), 14000);
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 4.0.6
	private void migrate45(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("triggerId").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	// migrate to 4.0.7
	private void migrate46(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						for (Element executorElement : valueElement.elements()) {
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
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
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
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"\" is before \"", "\" is until \"");
				content = StringUtils.replace(content,
						"\" is after \"", "\" is since \"");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void migrate49(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					buildSettingElement.addElement("defaultFixedIssueFilters");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 4.2.0
	private void migrate50(File dataDir, Stack<Integer> versions) {
	}

	private void migrate51(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element rangeElement = element.element("mark").element("range");
					Element tabWidthElement = rangeElement.element("tabWidth");
					if (tabWidthElement == null)
						tabWidthElement = rangeElement.addElement("tabWidth");
					tabWidthElement.setText("1");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 4.2.1
	private void migrate52(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("revision").setText("0");
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 4.3.0
	private void migrate53(File dataDir, Stack<Integer> versions) {
		String anonymousGroupName = null;
		Map<String, String> groupIds = new HashMap<>();
		List<Triple<String, String, String>> authorizations = new ArrayList<>();

		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.fieldspec.",
						"io.onedev.server.model.support.issue.field.spec.");
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.fieldsupply.",
						"io.onedev.server.model.support.issue.field.supply.");
				content = StringUtils.replace(content,
						"org.server.plugin.report.checkstyle.",
						"io.onedev.server.plugin.report.checkstyle.");
				content = StringUtils.replace(content,
						"org.server.plugin.report.clover.",
						"io.onedev.server.plugin.report.clover.");

				FileUtils.writeStringToFile(file, content, UTF_8);

				if (file.getName().startsWith("Settings.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						if (element.elementTextTrim("key").equals("SECURITY")) {
							Element valueElement = element.element("value");
							if (valueElement != null) {
								Element anonymousGroupElement = valueElement.element("anonymousGroup");
								if (anonymousGroupElement != null) {
									if (valueElement.elementTextTrim("enableAnonymousAccess").equals("true"))
										anonymousGroupName = anonymousGroupElement.getText().trim();
									anonymousGroupElement.detach();
								}
							}
						}
					}
					dom.writeToFile(file, false);
				} else if (file.getName().startsWith("Groups.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements())
						groupIds.put(element.elementText("name").trim(), element.elementText("id").trim());
				} else if (file.getName().startsWith("GroupAuthorizations.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						String groupId = element.elementText("group").trim();
						String projectId = element.elementText("project").trim();
						String roleId = element.elementText("role").trim();
						authorizations.add(Triple.of(groupId, projectId, roleId));
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Map<String, String> defaultRoles = new HashMap<>();

		if (anonymousGroupName != null) {
			String anonymousGroupId = groupIds.get(anonymousGroupName);
			for (Triple<String, String, String> authorization : authorizations) {
				if (authorization.getLeft().equals(anonymousGroupId))
					defaultRoles.put(authorization.getMiddle(), authorization.getRight());
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String defaultRoleId = defaultRoles.get(element.elementText("id").trim());
					if (defaultRoleId != null)
						element.addElement("defaultRole").setText(defaultRoleId);
				}
				dom.writeToFile(file, false);
			}
		}

	}

	private void migrate54(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Groups.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("createProjects").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate55(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element ownerElement = element.element("owner");
					if (ownerElement != null)
						ownerElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate56(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("alternateEmails");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate57(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.inputspec.numberinput.",
						"io.onedev.server.model.support.inputspec.integerinput.");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}

			if (file.getName().startsWith("BuildParams.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String type = element.elementText("type").trim();
					if (type.equals("Number"))
						element.setText("Integer");
					else if (type.equals("Pull request"))
						element.setText("Pull Request");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueFields.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String type = element.elementText("type").trim();
					if (type.equals("Number"))
						element.setText("Integer");
					else if (type.equals("Pull request"))
						element.setText("Pull Request");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldSpecElement : valueElement.element("fieldSpecs").elements()) {
								if (fieldSpecElement.getName().equals("io.onedev.server.model.support.issue.field.spec.NumberField"))
									fieldSpecElement.setName("io.onedev.server.model.support.issue.field.spec.IntegerField");
								else if (fieldSpecElement.getName().equals("io.onedev.server.model.support.issue.field.spec.TextField"))
									fieldSpecElement.addElement("multiline").setText("false");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate58(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("contributedSettings");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate59(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("sendAsHtml").detach();
							Element senderAddressElement = valueElement.element("senderAddress");
							if (senderAddressElement != null) {
								senderAddressElement.setName("emailAddress");
							} else {
								String hostName;
								try {
									hostName = InetAddress.getLocalHost().getHostName();
								} catch (UnknownHostException e) {
									hostName = "localhost";
								}
								valueElement.addElement("emailAddress").setText("onedev@" + hostName);
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate60(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().equals("io.onedev.server.plugin.docker.DockerExecutor"))
									executorElement.setName("io.onedev.server.plugin.executor.docker.DockerExecutor");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void useUnknownUser(Element element, String field) {
		Element userNameElement = element.element(field + "Name");
		if (userNameElement != null) {
			userNameElement.detach();
			if (element.element(field) == null)
				element.addElement(field).setText("-2");
		}
	}

	private void migrate61(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					useUnknownUser(element.element("lastUpdate"), "user");
					useUnknownUser(element, "submitter");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					useUnknownUser(element, "submitter");
					useUnknownUser(element, "canceller");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					useUnknownUser(element.element("lastUpdate"), "user");
					useUnknownUser(element, "user");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					useUnknownUser(element, "user");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					useUnknownUser(element.element("lastUpdate"), "user");
					useUnknownUser(element, "submitter");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("data").attributeValue("class").contains("IssueDescriptionChangeData"))
						element.detach();
					else
						useUnknownUser(element, "user");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("data").attributeValue("class").contains("PullRequestDescriptionChangeData"))
						element.detach();
					else
						useUnknownUser(element, "user");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					useUnknownUser(element, "user");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					useUnknownUser(element, "user");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("id").equals("-1"))
						element.element("email").setText("system email");
				}
				if (file.getName().equals("Users.xml")) {
					Element element = dom.getRootElement().addElement("io.onedev.server.model.User");
					element.addAttribute("revision", "0.0");
					element.addElement("id").setText("-2");
					element.addElement("name").setText("Unknown");
					element.addElement("password").setText("no password");
					element.addElement("ssoInfo").addElement("subject").setText(UUID.randomUUID().toString());
					element.addElement("email").setText("unknown email");
					element.addElement("alternateEmails");
					element.addElement("accessToken").setText(CryptoUtils.generateSecret());
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
			} else if (file.getName().startsWith("Settings.xml")) {
				List<Element> oldSenderAuthorizationElements = null;

				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				long maxId = 1L;
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element receiveMailSetting = valueElement.element("receiveMailSetting");
							if (receiveMailSetting != null) {
								Element senderAuthorizationsElement = receiveMailSetting.element("senderAuthorizations");
								oldSenderAuthorizationElements = senderAuthorizationsElement.elements();
								senderAuthorizationsElement.detach();
							}
						}
					}
					long id = Long.valueOf(element.elementTextTrim("id"));
					if (id > maxId)
						maxId = id;
				}

				if (oldSenderAuthorizationElements != null && !oldSenderAuthorizationElements.isEmpty()) {
					Element serviceDeskSettingElement = dom.getRootElement().addElement("io.onedev.server.model.Setting");
					serviceDeskSettingElement.addAttribute("revision", "0.0");
					serviceDeskSettingElement.addElement("id").setText(String.valueOf(maxId + 1));
					serviceDeskSettingElement.addElement("key").setText("SERVICE_DESK_SETTING");
					Element valueElement = serviceDeskSettingElement.addElement("value");
					valueElement.addAttribute("class", "io.onedev.server.model.support.administration.ServiceDeskSetting");
					Element senderAuthorizationsElement = valueElement.addElement("senderAuthorizations");
					Element projectDesignationsElement = valueElement.addElement("projectDesignations");
					Element issueCreationSettingsElement = valueElement.addElement("issueCreationSettings");
					for (Element oldSenderAuthorizationElement : oldSenderAuthorizationElements) {
						Element senderAuthorizationElement = senderAuthorizationsElement
								.addElement("io.onedev.server.model.support.administration.SenderAuthorization");
						Element projectDesignationElement = projectDesignationsElement
								.addElement("io.onedev.server.model.support.administration.ProjectDesignation");
						Element issueCreationSettingElement = issueCreationSettingsElement
								.addElement("io.onedev.server.model.support.administration.IssueCreationSetting");

						Element senderEmailsElement = oldSenderAuthorizationElement.element("senderEmails");
						if (senderEmailsElement != null) {
							String senderEmails = senderEmailsElement.getText().trim();
							senderAuthorizationElement.addElement("senderEmails").setText(senderEmails);
							projectDesignationElement.addElement("senderEmails").setText(senderEmails);
							issueCreationSettingElement.addElement("senderEmails").setText(senderEmails);
						}

						Element authorizedProjectsElement = oldSenderAuthorizationElement.element("authorizedProjects");
						if (authorizedProjectsElement != null) {
							senderAuthorizationElement.addElement("authorizedProjects")
									.setText(authorizedProjectsElement.getText().trim());
						}
						senderAuthorizationElement.addElement("authorizedRoleName")
								.setText(oldSenderAuthorizationElement.elementText("authorizedRoleName").trim());
						projectDesignationElement.addElement("project")
								.setText(oldSenderAuthorizationElement.elementText("defaultProject").trim());
						Element issueFieldsElement = oldSenderAuthorizationElement.element("issueFields");
						issueFieldsElement.detach();
						issueCreationSettingElement.add(issueFieldsElement);
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate62(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SYSTEM")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							try {
								HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
								int cpu = hardware.getProcessor().getLogicalProcessorCount() * 1000;
								valueElement.addElement("cpu").setText(String.valueOf(cpu));
								int memory = (int) (hardware.getMemory().getTotal() / 1024 / 1024);
								valueElement.addElement("memory").setText(String.valueOf(memory));
							} catch (Exception e) {
								valueElement.addElement("cpu").setText("4000");
								valueElement.addElement("memory").setText("8000");
							}
						}
					} else if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("DockerExecutor")) {
									executorElement.setName("io.onedev.server.plugin.executor.serverdocker.ServerDockerExecutor");
									executorElement.element("capacity").detach();
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element userProjectQueriesElement = element.element("userProjectQueries");
					if (userProjectQueriesElement != null)
						userProjectQueriesElement.setName("projectQueries");
					Element userIssueQueriesElement = element.element("userIssueQueries");
					if (userIssueQueriesElement != null)
						userIssueQueriesElement.setName("issueQueries");
					Element userBuildQueriesElement = element.element("userBuildQueries");
					if (userBuildQueriesElement != null)
						userBuildQueriesElement.setName("buildQueries");
					Element userPullRequestQueriesElement = element.element("userPullRequestQueries");
					if (userPullRequestQueriesElement != null)
						userPullRequestQueriesElement.setName("pullRequestQueries");

					Element issueQueryWatchesElement = element.element("issueQueryWatches");
					for (Element issueQueryWatchElement : issueQueryWatchesElement.elements()) {
						Element queryNameElement = issueQueryWatchElement.element("string");
						queryNameElement.setText("g:" + queryNameElement.getText());
					}
					Element userIssueQueryWatchesElement = element.element("userIssueQueryWatches");
					for (Element userIssueQueryWatchElement : userIssueQueryWatchesElement.elements()) {
						Element queryNameElement = userIssueQueryWatchElement.element("string");
						queryNameElement.setText("p:" + queryNameElement.getText());
						userIssueQueryWatchElement.detach();
						issueQueryWatchesElement.add(userIssueQueryWatchElement);
					}
					userIssueQueryWatchesElement.detach();

					Element pullRequestQueryWatchesElement = element.element("pullRequestQueryWatches");
					for (Element pullRequestQueryWatchElement : pullRequestQueryWatchesElement.elements()) {
						Element queryNameElement = pullRequestQueryWatchElement.element("string");
						queryNameElement.setText("g:" + queryNameElement.getText());
					}
					Element userPullRequestQueryWatchesElement = element.element("userPullRequestQueryWatches");
					for (Element userPullRequestQueryWatchElement : userPullRequestQueryWatchesElement.elements()) {
						Element queryNameElement = userPullRequestQueryWatchElement.element("string");
						queryNameElement.setText("p:" + queryNameElement.getText());
						userPullRequestQueryWatchElement.detach();
						pullRequestQueryWatchesElement.add(userPullRequestQueryWatchElement);
					}
					userPullRequestQueryWatchesElement.detach();

					Element buildQuerySubscriptionsElement = element.element("buildQuerySubscriptions");
					for (Element queryNameElement : buildQuerySubscriptionsElement.elements())
						queryNameElement.setText("g:" + queryNameElement.getText());
					Element userBuildQuerySubscriptionsElement = element.element("userBuildQuerySubscriptions");
					for (Element queryNameElement : userBuildQuerySubscriptionsElement.elements()) {
						queryNameElement.setText("p:" + queryNameElement.getText());
						queryNameElement.detach();
						buildQuerySubscriptionsElement.add(queryNameElement);
					}
					userBuildQuerySubscriptionsElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.IssueQueryPersonalization");
					element.element("userQueries").setName("queries");
					Element queryWatchesElement = element.element("queryWatches");
					for (Element queryWatchElement : queryWatchesElement.elements()) {
						Element queryNameElement = queryWatchElement.element("string");
						queryNameElement.setText("g:" + queryNameElement.getText());
					}
					Element userQueryWatchesElement = element.element("userQueryWatches");
					for (Element userQueryWatchElement : userQueryWatchesElement.elements()) {
						Element queryNameElement = userQueryWatchElement.element("string");
						queryNameElement.setText("p:" + queryNameElement.getText());
						userQueryWatchElement.detach();
						queryWatchesElement.add(userQueryWatchElement);
					}
					userQueryWatchesElement.detach();
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("Settings", "Personalizations")), false);
			} else if (file.getName().startsWith("PullRequestQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.PullRequestQueryPersonalization");
					element.element("userQueries").setName("queries");
					Element queryWatchesElement = element.element("queryWatches");
					for (Element queryWatchElement : queryWatchesElement.elements()) {
						Element queryNameElement = queryWatchElement.element("string");
						queryNameElement.setText("g:" + queryNameElement.getText());
					}
					Element userQueryWatchesElement = element.element("userQueryWatches");
					for (Element userQueryWatchElement : userQueryWatchesElement.elements()) {
						Element queryNameElement = userQueryWatchElement.element("string");
						queryNameElement.setText("p:" + queryNameElement.getText());
						userQueryWatchElement.detach();
						queryWatchesElement.add(userQueryWatchElement);
					}
					userQueryWatchesElement.detach();
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("Settings", "Personalizations")), false);
			} else if (file.getName().startsWith("BuildQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.BuildQueryPersonalization");
					element.element("userQueries").setName("queries");
					Element querySubscriptionsElement = element.element("querySubscriptions");
					for (Element queryNameElement : querySubscriptionsElement.elements())
						queryNameElement.setText("g:" + queryNameElement.getText());
					Element userQuerySubscriptionsElement = element.element("userQuerySubscriptions");
					for (Element queryNameElement : userQuerySubscriptionsElement.elements()) {
						queryNameElement.setText("p:" + queryNameElement.getText());
						queryNameElement.detach();
						querySubscriptionsElement.add(queryNameElement);
					}
					userQuerySubscriptionsElement.detach();
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("Settings", "Personalizations")), false);
			} else if (file.getName().startsWith("CodeCommentQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.CodeCommentQueryPersonalization");
					element.element("userQueries").setName("queries");
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("Settings", "Personalizations")), false);
			} else if (file.getName().startsWith("CommitQuerySettings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.CommitQueryPersonalization");
					element.element("userQueries").setName("queries");
					Element querySubscriptionsElement = element.element("projectQuerySubscriptions");
					querySubscriptionsElement.setName("querySubscriptions");
					for (Element queryNameElement : querySubscriptionsElement.elements())
						queryNameElement.setText("g:" + queryNameElement.getText());
					Element userQuerySubscriptionsElement = element.element("userQuerySubscriptions");
					for (Element queryNameElement : userQuerySubscriptionsElement.elements()) {
						queryNameElement.setText("p:" + queryNameElement.getText());
						queryNameElement.detach();
						querySubscriptionsElement.add(queryNameElement);
					}
					userQuerySubscriptionsElement.detach();
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("Settings", "Personalizations")), false);
			}
		}
	}

	private void migrate63(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("AutoDiscoveredJobExecutor"))
									executorElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element errorMessageElement = element.element("errorMessage");
					if (errorMessageElement != null)
						errorMessageElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate64(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element timeoutElement = valueElement.element("timeout");
							int timeout = Integer.valueOf(timeoutElement.getTextTrim());
							if (timeout == 0)
								timeout = 60;
							else if (timeout < 10)
								timeout = 10;
							timeoutElement.setText(String.valueOf(timeout));
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate65(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SYSTEM")) {
						Element valueElement = element.element("value");
						valueElement.element("cpu").detach();
						valueElement.element("memory").detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}

	}

	private void migrate66(File dataDir, Stack<Integer> versions) {
		Map<String, Element> compareContexts = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element compareContextElement = element.element("compareContext");
					Element leftSideElement = compareContextElement.element("leftSide");
					Element compareCommitHashElement = compareContextElement.element("compareCommitHash");
					String compareCommitHash = compareCommitHashElement.getTextTrim();
					String commitHash = element.element("mark").elementTextTrim("commitHash");
					if (Boolean.parseBoolean(leftSideElement.getTextTrim())) {
						compareContextElement.addElement("oldCommitHash").setText(compareCommitHash);
						compareContextElement.addElement("newCommitHash").setText(commitHash);
					} else {
						compareContextElement.addElement("newCommitHash").setText(compareCommitHash);
						compareContextElement.addElement("oldCommitHash").setText(commitHash);
					}
					Element requestElement = element.element("request");
					if (requestElement != null) {
						compareContextElement.addElement("pullRequest").setText(requestElement.getTextTrim());
						requestElement.detach();
					}
					leftSideElement.detach();
					compareCommitHashElement.detach();
					compareContexts.put(element.elementTextTrim("id"), compareContextElement);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element lastCodeCommentActivityDateElement = element.element("lastCodeCommentActivityDate");
					if (lastCodeCommentActivityDateElement != null)
						lastCodeCommentActivityDateElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							String enableStartTLSElement = valueElement.elementTextTrim("enableStartTLS");
							Element receiveMailSettingElement = valueElement.element("receiveMailSetting");
							if (receiveMailSettingElement != null)
								receiveMailSettingElement.addElement("enableSSL").setText(enableStartTLSElement);
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String elementId = element.elementTextTrim("comment");
					element.add(compareContexts.get(elementId).createCopy());
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate67(File dataDir, Stack<Integer> versions) {
		Map<String, Element> compareContexts = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("JestTestMetric.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.setName("io.onedev.server.model.UnitTestMetric");

				String newFileName = file.getName().replace("Jest", "Unit");
				dom.writeToFile(new File(dataDir, newFileName), false);
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("CloverMetric.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.setName("io.onedev.server.model.CoverageMetric");

				String newFileName = file.getName().replace("Clover", "Coverage");
				dom.writeToFile(new File(dataDir, newFileName), false);
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("CheckstyleMetric.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.setName("io.onedev.server.model.ProblemMetric");

				String newFileName = file.getName().replace("Checkstyle", "Problem");
				dom.writeToFile(new File(dataDir, newFileName), false);
				FileUtils.deleteFile(file);
			}
		}
	}

	private void migrateAttachmentLinks(@Nullable Element element, Map<String, String> projectIds) {
		if (element != null) {
			String content = element.getText();
			Pattern pattern = Pattern.compile("/projects/([\\w-\\.]+)/attachment/");
			Matcher matcher = pattern.matcher(content);
			StringBuffer buffer = new StringBuffer();

			while (matcher.find()) {
				String projectName = matcher.group(1);
				String projectId = projectIds.get(projectName);
				if (projectId != null)
					matcher.appendReplacement(buffer, "/projects/" + projectId + "/attachment/");
				else
					matcher.appendReplacement(buffer, Matcher.quoteReplacement(matcher.group()));
			}

			matcher.appendTail(buffer);
			element.setText(buffer.toString());
		}
	}

	private void migrate68(File dataDir, Stack<Integer> versions) {
		Map<String, String> projectIds = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					projectIds.put(element.elementTextTrim("name"), element.elementTextTrim("id"));
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("codeManagementEnabled").setText("true");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("description"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("content"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("description"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("content"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("content"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("content"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("data").element("comment"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrateAttachmentLinks(element.element("data").element("comment"), projectIds);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Groups.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("createRootProjects").setText("false");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("createChildren").setText("false");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						for (Element executorElement : valueElement.elements()) {
							Element jobMatchElement = executorElement.element("jobMatch");
							if (jobMatchElement.getTextTrim().equals("all"))
								jobMatchElement.detach();
							else
								jobMatchElement.setName("jobRequirement");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate69(File dataDir, Stack<Integer> versions) {
	}

	// Migrate to 5.4.0
	private void migrate70(File dataDir, Stack<Integer> versions) {
		Long scheduleId = 1L;
		VersionedXmlDoc issueSchedulesDoc = new VersionedXmlDoc();
		Element listElement = issueSchedulesDoc.addElement("list");

		Set<String> promptFieldsUponIssueOpen = new HashSet<>();

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("id");
					String issueSubmitDate = element.elementTextTrim("submitDate");
					Element milestoneElement = element.element("milestone");
					if (milestoneElement != null) {
						Element scheduleElement = listElement.addElement("io.onedev.server.model.IssueSchedule");
						scheduleElement.addAttribute("revision", "0.0");
						scheduleElement.addElement("id").setText(String.valueOf(scheduleId++));
						scheduleElement.addElement("issue").setText(issueId);
						scheduleElement.addElement("milestone").setText(milestoneElement.getTextTrim());
						scheduleElement.addElement("date").addAttribute("class", "sql-timestamp").setText(issueSubmitDate);
						milestoneElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					String dataClass = dataElement.attributeValue("class");
					if (dataClass.contains("IssueMilestoneChangeData") || dataClass.contains("IssueBatchUpdateData")) {
						Element oldMilestonesElement = dataElement.addElement("oldMilestones");
						Element oldMilestoneElement = dataElement.element("oldMilestone");
						if (oldMilestoneElement != null) {
							oldMilestonesElement.addElement("string").setText(oldMilestoneElement.getText());
							oldMilestoneElement.detach();
						}
						Element newMilestonesElement = dataElement.addElement("newMilestones");
						Element newMilestoneElement = dataElement.element("newMilestone");
						if (newMilestoneElement != null) {
							newMilestonesElement.addElement("string").setText(newMilestoneElement.getText());
							newMilestoneElement.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldNameElement : valueElement.element("promptFieldsUponIssueOpen").elements())
								promptFieldsUponIssueOpen.add(fieldNameElement.getText().trim());
						}
					}
				}
			}
		}

		issueSchedulesDoc.writeToFile(new File(dataDir, "IssueSchedules.xml"), false);

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						for (Element executorElement : valueElement.elements()) {
							if (executorElement.getName().contains("KubernetesExecutor"))
								executorElement.element("createCacheLabels").detach();
						}
					} else if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldSpecElement : valueElement.element("fieldSpecs").elements()) {
								if (promptFieldsUponIssueOpen.contains(fieldSpecElement.elementText("name").trim()))
									fieldSpecElement.addElement("promptUponIssueOpen").setText("true");
								else
									fieldSpecElement.addElement("promptUponIssueOpen").setText("false");
							}
							valueElement.element("promptFieldsUponIssueOpen").detach();
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}

	}

	// Migrate to 6.0.0
	private void migrate71(File dataDir, Stack<Integer> versions) {
		Map<String, String> issueScopes = new HashMap<>();
		Map<Pair<String, String>, String> issueIds = new HashMap<>();
		Map<Pair<String, String>, String> buildIds = new HashMap<>();
		Map<Pair<String, String>, String> pullRequestIds = new HashMap<>();

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("id");
					String numberScope = element.elementTextTrim("numberScope");
					issueScopes.put(issueId, numberScope);
					issueIds.put(new Pair<>(numberScope, element.elementTextTrim("number")), issueId);
				}
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String buildId = element.elementTextTrim("id");
					String numberScope = element.elementTextTrim("numberScope");
					String number = element.elementTextTrim("number");
					buildIds.put(new Pair<>(numberScope, number), buildId);
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String pullRequestId = element.elementTextTrim("id");
					String numberScope = element.elementTextTrim("numberScope");
					String number = element.elementTextTrim("number");
					pullRequestIds.put(new Pair<>(numberScope, number), pullRequestId);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueFields.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("issue");
					String type = element.elementText("type").trim();
					String value = element.elementTextTrim("value");
					if (type.equals("Issue")) {
						String fieldIssueId = issueIds.get(new Pair<>(issueScopes.get(issueId), value));
						if (fieldIssueId != null)
							element.element("value").setText(fieldIssueId);
						else
							element.detach();
					} else if (type.equals("Build")) {
						String fieldBuildId = buildIds.get(new Pair<>(issueScopes.get(issueId), value));
						if (fieldBuildId != null)
							element.element("value").setText(fieldBuildId);
						else
							element.detach();
					} else if (type.equals("Pull Request")) {
						String fieldPullRequestId = pullRequestIds.get(new Pair<>(issueScopes.get(issueId), value));
						if (fieldPullRequestId != null)
							element.element("value").setText(fieldPullRequestId);
						else
							element.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate72(File dataDir, Stack<Integer> versions) {
		Map<String, Integer> stateOrdinals = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							int index = 0;
							for (Element stateSpecElement : valueElement.element("stateSpecs").elements())
								stateOrdinals.put(stateSpecElement.elementText("name").trim(), index++);
							for (Element boardSpecElement : valueElement.element("boardSpecs").elements())
								boardSpecElement.addElement("displayLinks");
							valueElement.addElement("listLinks");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					int ordinal = stateOrdinals.get(element.elementText("state").trim());
					element.addElement("stateOrdinal").setText(String.valueOf(ordinal));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element issueSettingElement = element.element("issueSetting");
					if (issueSettingElement.element("listFields") != null)
						issueSettingElement.addElement("listLinks");
					Element boardSpecsElement = issueSettingElement.element("boardSpecs");
					if (boardSpecsElement != null) {
						for (Element boardSpecElement : boardSpecsElement.elements())
							boardSpecElement.addElement("displayLinks");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate73(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element closeInfoElement = element.element("closeInfo");
					if (closeInfoElement != null) {
						Element statusElement = closeInfoElement.element("status");
						statusElement.detach();
						element.add(statusElement);
					} else {
						element.addElement("status").setText("OPEN");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Agents.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element osElement = element.element("os");
					String osName;

					switch (osElement.getTextTrim()) {
						case "WINDOWS":
							osName = "Windows";
							break;
						case "LINUX":
							osName = "Linux";
							break;
						case "FREEBSD":
							osName = "FreeBSD";
							break;
						case "MACOSX":
							osName = "Mac OS X";
							break;
						default:
							osName = "Other";
					}
					element.addElement("osName").setText(osName);
					osElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate74(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("triggerChain").setText(UUID.randomUUID().toString());
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate75(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("AgentTokens.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element noteElement = element.element("note");
					if (noteElement != null)
						noteElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element commentElement = element.element("data").element("comment");
					if (commentElement != null) {
						element.addElement("comment").setText(commentElement.getText());
						commentElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element commentElement = element.element("data").element("comment");
					if (commentElement != null) {
						element.addElement("comment").setText(commentElement.getText());
						commentElement.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate76(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("PERFORMANCE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.addElement("maxCodeSearchEntries").setText("100");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate77(File dataDir, Stack<Integer> versions) {
		Map<String, String> userIds = new HashMap<>();

		Long maxPullRequestCommentId = 0L;
		Long maxIssueCommentId = 0L;
		File issueCommentsFile = null;
		File pullRequestCommentsFile = null;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String id = element.elementText("id").trim();
					String name = element.elementText("name").trim();
					Element fullNameElement = element.element("fullName");
					if (fullNameElement != null)
						userIds.put(fullNameElement.getText().trim(), id);
					else
						userIds.put(name, id);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long commentId = Long.parseLong(element.elementText("id").trim());
					if (commentId > maxIssueCommentId)
						maxIssueCommentId = commentId;
				}
				issueCommentsFile = file;
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Long commentId = Long.parseLong(element.elementText("id").trim());
					if (commentId > maxPullRequestCommentId)
						maxPullRequestCommentId = commentId;
				}
				pullRequestCommentsFile = file;
			}
		}

		VersionedXmlDoc issueCommentsDom;
		if (issueCommentsFile == null) {
			issueCommentsFile = new File(dataDir, "IssueComments.xml");
			issueCommentsDom = new VersionedXmlDoc();
			issueCommentsDom.addElement("list");
		} else {
			issueCommentsDom = VersionedXmlDoc.fromFile(issueCommentsFile);
		}

		VersionedXmlDoc pullRequestCommentsDom;
		if (pullRequestCommentsFile == null) {
			pullRequestCommentsFile = new File(dataDir, "PullRequestComments.xml");
			pullRequestCommentsDom = new VersionedXmlDoc();
			pullRequestCommentsDom.addElement("list");
		} else {
			pullRequestCommentsDom = VersionedXmlDoc.fromFile(pullRequestCommentsFile);
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("issueManagementEnabled").setName("issueManagement");
					element.element("codeManagementEnabled").setName("codeManagement");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("user") == null)
						element.addElement("user").setText("-1");
					Element dataElement = element.element("data");
					Element assigneeElement = dataElement.element("assignee");
					if (assigneeElement != null) {
						String userId = userIds.get(assigneeElement.getText().trim());
						if (userId != null) {
							assigneeElement.setName("assigneeId");
							assigneeElement.setText(userId);
						} else {
							element.detach();
						}
					}
					Element reviewerElement = dataElement.element("reviewer");
					if (reviewerElement != null) {
						String userId = userIds.get(reviewerElement.getText().trim());
						if (userId != null) {
							reviewerElement.setName("reviewerId");
							reviewerElement.setText(userId);
						} else {
							element.detach();
						}
					}
					Element commentElement = element.element("comment");
					if (commentElement != null) {
						Element pullRequestCommentElement = pullRequestCommentsDom.getRootElement()
								.addElement("io.onedev.server.model.PullRequestComment");
						pullRequestCommentElement.addElement("content").setText(commentElement.getText().trim());
						pullRequestCommentElement.addAttribute("revision", "0.0");
						pullRequestCommentElement.addElement("id").setText(String.valueOf(++maxPullRequestCommentId));
						pullRequestCommentElement.addElement("request").setText(element.elementText("request").trim());
						pullRequestCommentElement.addElement("user").setText(element.elementText("user").trim());
						Element pullRequestCommentDateElement = pullRequestCommentElement.addElement("date");
						Element dateElement = element.element("date");
						pullRequestCommentDateElement.setText(dateElement.getText().trim());
						pullRequestCommentDateElement.addAttribute("class", "sql-timestamp");
						commentElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("user") == null)
						element.addElement("user").setText("-1");
					Element commentElement = element.element("comment");
					if (commentElement != null) {
						Element issueCommentElement = issueCommentsDom.getRootElement()
								.addElement("io.onedev.server.model.IssueComment");
						issueCommentElement.addElement("content").setText(commentElement.getText().trim());
						issueCommentElement.addAttribute("revision", "0.0");
						issueCommentElement.addElement("id").setText(String.valueOf(++maxIssueCommentId));
						issueCommentElement.addElement("issue").setText(element.elementText("issue").trim());
						issueCommentElement.addElement("user").setText(element.elementText("user").trim());
						Element issueCommentDateElement = issueCommentElement.addElement("date");
						Element dateElement = element.element("date");
						issueCommentDateElement.setText(dateElement.getText().trim());
						issueCommentDateElement.addAttribute("class", "sql-timestamp");
						commentElement.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}

		issueCommentsDom.writeToFile(issueCommentsFile, false);
		pullRequestCommentsDom.writeToFile(pullRequestCommentsFile, false);
	}

	private void migrate78(File dataDir, Stack<Integer> versions) {
		Map<String, Integer> issueCommentCounts = new HashMap<>();
		Map<String, Integer> pullRequestCommentCounts = new HashMap<>();

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("issue");
					Integer commentCount = issueCommentCounts.get(issueId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					issueCommentCounts.put(issueId, commentCount);
				}
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String pullRequestId = element.elementTextTrim("request");
					Integer commentCount = pullRequestCommentCounts.get(pullRequestId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					pullRequestCommentCounts.put(pullRequestId, commentCount);
				}
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Integer commentCount = issueCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.element("commentCount").setText(String.valueOf(commentCount));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Integer commentCount = pullRequestCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.element("commentCount").setText(String.valueOf(commentCount));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate79(File dataDir, Stack<Integer> versions) {
	}

	private void migrate80(File dataDir, Stack<Integer> versions) {
		Map<String, Integer> issueCommentCounts = new HashMap<>();
		Map<String, Integer> pullRequestCommentCounts = new HashMap<>();

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("issue");
					Integer commentCount = issueCommentCounts.get(issueId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					issueCommentCounts.put(issueId, commentCount);
				}
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String pullRequestId = element.elementTextTrim("request");
					Integer commentCount = pullRequestCommentCounts.get(pullRequestId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					pullRequestCommentCounts.put(pullRequestId, commentCount);
				}
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Integer commentCount = issueCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.element("commentCount").setText(String.valueOf(commentCount));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Integer commentCount = pullRequestCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.element("commentCount").setText(String.valueOf(commentCount));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Agents.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("temporal").setText(("false"));
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate81(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.element("serviceDeskName") == null)
						element.addElement("serviceDeskName").setText("<$NullServiceDesk$>" + UUID.randomUUID().toString());
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 7.0.0
	private void migrate82(File dataDir, Stack<Integer> versions) {
		Set<String> userNames = new HashSet<>();
		Map<String, String> primaryEmails = new HashMap<>();
		Map<String, String> gitEmails = new HashMap<>();
		Map<String, String> alternateEmails = new HashMap<>();

		String sshServerUrl = null;

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements())
						branchProtectionElement.addElement("signatureRequired").setText("false");
					for (Element tagProtectionElement : element.element("tagProtections").elements())
						tagProtectionElement.addElement("signatureRequired").setText("false");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String userId = element.elementText("id").trim();
					Element nameElement = element.element("name");
					String name = nameElement.getText().trim();
					if (userNames.add(name.toLowerCase()))
						nameElement.setText(name.toLowerCase());
					else
						throw new ExplicitException("Duplicated login names found when convert '" + name + "' to lowercase");
					if (userId.equals("-1")) {
						element.addElement("fullName").setText("OneDev");
						element.element("email").detach();
						element.element("alternateEmails").detach();
					} else if (userId.equals("-2")) {
						element.addElement("fullName").setText("Unknown");
						element.element("email").detach();
						element.element("alternateEmails").detach();
					} else {
						Element emailElement = element.element("email");
						String email = emailElement.getText().trim();
						if (primaryEmails.put(email.toLowerCase(), userId) != null)
							throw new ExplicitException("Duplicated email address found when convert '" + email + "' to lowercase");
						emailElement.detach();

						Element gitEmailElement = element.element("gitEmail");
						if (gitEmailElement != null) {
							String gitEmail = gitEmailElement.getText().trim();
							gitEmails.put(gitEmail.toLowerCase(), userId);
							gitEmailElement.detach();
						}

						Element alternateEmailsElement = element.element("alternateEmails");
						for (Element alternateEmailElement : alternateEmailsElement.elements()) {
							String alternateEmail = alternateEmailElement.getText().trim();
							alternateEmails.put(alternateEmail.toLowerCase(), userId);
						}
						alternateEmailsElement.detach();
					}

					Element ssoInfoElement = element.element("ssoInfo");
					Element connectorElement = ssoInfoElement.element("connector");
					if (connectorElement != null)
						element.addElement("ssoConnector").setText(connectorElement.getText().trim());
					ssoInfoElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("triggerChain").setName("pipeline");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("enforce2FA").setText("false");
					} else if (key.equals("SSH")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element sshServerUrlElement = valueElement.element("serverUrl");
							sshServerUrl = sshServerUrlElement.getText().trim();
							sshServerUrlElement.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Groups.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("enforce2FA").setText("false");
				dom.writeToFile(file, false);
			}
		}

		if (sshServerUrl != null) {
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Settings.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						if (element.elementTextTrim("key").equals("SYSTEM")) {
							Element valueElement = element.element("value");
							if (valueElement != null)
								valueElement.addElement("sshRootUrl").setText(sshServerUrl);
						}
					}
					dom.writeToFile(file, false);
				}
			}
		}

		VersionedXmlDoc emailAddressesDom;
		File emailAddressesFile = new File(dataDir, "EmailAddresss.xml");
		emailAddressesDom = new VersionedXmlDoc();
		Element listElement = emailAddressesDom.addElement("list");

		long id = 1;
		Map<String, Element> primaryEmailElements = new HashMap<>();
		for (Map.Entry<String, String> entry : primaryEmails.entrySet()) {
			Element emailAddressElement = listElement.addElement("io.onedev.server.model.EmailAddress");
			emailAddressElement.addAttribute("revision", "0.0");
			emailAddressElement.addElement("id").setText(String.valueOf(id++));
			emailAddressElement.addElement("primary").setText("true");
			emailAddressElement.addElement("git").setText("true");
			emailAddressElement.addElement("value").setText(entry.getKey());
			emailAddressElement.addElement("owner").setText(entry.getValue());
			primaryEmailElements.put(entry.getValue(), emailAddressElement);
		}

		for (Map.Entry<String, String> entry : gitEmails.entrySet()) {
			if (!primaryEmails.containsKey(entry.getKey())) {
				Element emailAddressElement = listElement.addElement("io.onedev.server.model.EmailAddress");
				emailAddressElement.addAttribute("revision", "0.0");
				emailAddressElement.addElement("id").setText(String.valueOf(id++));
				emailAddressElement.addElement("primary").setText("false");
				emailAddressElement.addElement("git").setText("true");
				emailAddressElement.addElement("value").setText(entry.getKey());
				emailAddressElement.addElement("owner").setText(entry.getValue());
				primaryEmailElements.get(entry.getValue()).element("git").setText("false");
			}
		}

		for (Map.Entry<String, String> entry : alternateEmails.entrySet()) {
			if (!primaryEmails.containsKey(entry.getKey()) && !gitEmails.containsKey(entry.getKey())) {
				Element emailAddressElement = listElement.addElement("io.onedev.server.model.EmailAddress");
				emailAddressElement.addAttribute("revision", "0.0");
				emailAddressElement.addElement("id").setText(String.valueOf(id++));
				emailAddressElement.addElement("primary").setText("false");
				emailAddressElement.addElement("git").setText("false");
				emailAddressElement.addElement("value").setText(entry.getKey());
				emailAddressElement.addElement("owner").setText(entry.getValue());
			}
		}

		emailAddressesDom.writeToFile(emailAddressesFile, true);
	}

	private void migrate83(File dataDir, Stack<Integer> versions) {
		Map<String, String> issueInfos = new HashMap<>();
		Map<String, String> pullRequestInfos = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueInfo = MessageFormat.format(
							"project id: {0}, issue number: {1}",
							element.elementText("project"), element.elementText("number"));
					issueInfos.put(element.elementTextTrim("id"), issueInfo);
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String pullRequestInfo = MessageFormat.format(
							"project id: {0}, pull request number: {1}",
							element.elementText("targetProject"), element.elementText("number"));
					pullRequestInfos.put(element.elementTextTrim("id"), pullRequestInfo);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element descriptionElement = element.element("description");
					if (descriptionElement != null) {
						String description = descriptionElement.getText().trim();
						if (description.length() > Issue.MAX_DESCRIPTION_LEN) {
							descriptionElement.setText(StringUtils.abbreviate(description, Issue.MAX_DESCRIPTION_LEN));
							logger.warn("Issue description too long and truncated ({})",
									issueInfos.get(element.elementTextTrim("id")));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element descriptionElement = element.element("description");
					if (descriptionElement != null) {
						String description = descriptionElement.getText().trim();
						if (description.length() > PullRequest.MAX_DESCRIPTION_LEN) {
							descriptionElement.setText(StringUtils.abbreviate(description, PullRequest.MAX_DESCRIPTION_LEN));
							logger.warn("Pull request description too long and truncated ({})",
									pullRequestInfos.get(element.elementTextTrim("id")));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					if (contentElement != null) {
						String content = contentElement.getText().trim();
						if (content.length() > IssueComment.MAX_CONTENT_LEN) {
							contentElement.setText(StringUtils.abbreviate(content, IssueComment.MAX_CONTENT_LEN));
							logger.warn("Issue comment too long and truncated ({})",
									issueInfos.get(element.elementText("issue")));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					if (contentElement != null) {
						String content = contentElement.getText().trim();
						if (content.length() > PullRequestComment.MAX_CONTENT_LEN) {
							contentElement.setText(StringUtils.abbreviate(content, PullRequestComment.MAX_CONTENT_LEN));
							logger.warn("Pull request comment too long and truncated ({})",
									pullRequestInfos.get(element.elementText("request")));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element receiveMailSettingElement = valueElement.element("receiveMailSetting");
							if (receiveMailSettingElement != null)
								receiveMailSettingElement.addElement("pollInterval").setText("60");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate84(File dataDir, Stack<Integer> versions) {
	}

	private void migrate85(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("uuid").setText(UUID.randomUUID().toString());
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate86(File dataDir, Stack<Integer> versions) {
	}

	private void migrate87(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("DockerExecutor"))
									executorElement.addElement("mountDockerSock").setText("false");
								else if (executorElement.getName().contains("KubernetesExecutor"))
									executorElement.addElement("mountContainerSock").setText("false");

								Element jobRequirementElement = executorElement.element("jobRequirement");
								if (jobRequirementElement != null)
									jobRequirementElement.setName("jobAuthorization");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate88(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("resolved").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate89(File dataDir, Stack<Integer> versions) {
		Map<String, Integer> pullRequestCommentCounts = new HashMap<>();

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String pullRequestId = element.elementTextTrim("request");
					Integer commentCount = pullRequestCommentCounts.get(pullRequestId);
					if (commentCount == null)
						commentCount = 0;
					commentCount++;
					pullRequestCommentCounts.put(pullRequestId, commentCount);
				}
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					String className = dataElement.attributeValue("class");
					if (className.contains("PullRequestAssigneeAddData")
							|| className.contains("PullRequestAssigneeRemoveData")
							|| className.contains("PullRequestReviewerAddData")
							|| className.contains("PullRequestReviewerRemoveData")
							|| className.contains("PullRequestReviewWithdrawData")) {
						element.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestReviews.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element resultElement = element.element("result");
					if (resultElement != null) {
						Element approvedElement = resultElement.element("approved");
						if (approvedElement != null) {
							if (approvedElement.getTextTrim().equals("true"))
								element.addElement("status").setText("APPROVED");
							else
								element.addElement("status").setText("REQUESTED_FOR_CHANGES");
						} else {
							element.addElement("status").setText("PENDING");
						}
						resultElement.detach();
					} else {
						element.addElement("status").setText("PENDING");
					}
					Element statusDateElement = element.addElement("statusDate");
					statusDateElement.addAttribute("class", "sql-timestamp");
					statusDateElement.setText("2020-01-22T16:08:49.869000000Z");
				}
				dom.writeToFile(file, false);
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Integer commentCount = pullRequestCommentCounts.get(element.elementTextTrim("id"));
					if (commentCount == null)
						commentCount = 0;
					element.element("commentCount").setText(String.valueOf(commentCount));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate90(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element closeInfoElement = element.element("closeInfo");
					if (closeInfoElement != null)
						closeInfoElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate91(File dataDir, Stack<Integer> versions) {
	}

	private void migrate92(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("confidential").setText("false");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("codePrivilege").equals("WRITE"))
						element.addElement("accessConfidentialIssues").setText("true");
					else
						element.addElement("accessConfidentialIssues").setText("false");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("IssueBatchUpdateData")) {
						dataElement.addElement("oldConfidential").setText("false");
						dataElement.addElement("newConfidential").setText("false");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SERVICE_DESK_SETTING")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element issueCreationSettingElement : valueElement.element("issueCreationSettings").elements())
								issueCreationSettingElement.addElement("confidential").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate93(File dataDir, Stack<Integer> versions) {
	}

	private void migrate94(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.addAttribute("class", "io.onedev.server.model.support.administration.mailsetting.OtherMailSetting");
							Element receiveMailSettingElement = valueElement.element("receiveMailSetting");
							if (receiveMailSettingElement != null)
								receiveMailSettingElement.setName("otherInboxPollSetting");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate95(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("GpgKeys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					byte[] bytes = contentElement.getText().getBytes(UTF_8);
					contentElement.setText(JVM.getBase64Codec().encode(bytes));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate96(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SERVICE_DESK_SETTING")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element preserveBeforeElement = valueElement.element("preserveBefore");
							if (preserveBeforeElement != null)
								preserveBeforeElement.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate97(File dataDir, Stack<Integer> versions) {
		Map<String, String> emailOwners = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("EmailAddresss.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					emailOwners.put(element.elementTextTrim("id"), element.elementTextTrim("owner"));
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("GpgKeys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element emailAddressElement = element.element("emailAddress");
					element.addElement("owner").setText(emailOwners.get(emailAddressElement.getTextTrim()));
					emailAddressElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate98(File dataDir, Stack<Integer> versions) {
	}

	private void migrate99(File dataDir, Stack<Integer> versions) {
		Map<String, String> names = new HashMap<>();
		Map<String, String> parentIds = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String id = element.elementText("id").trim();
					String name = element.elementText("name").trim();
					String parentId;
					Element parentElement = element.element("parent");
					if (parentElement != null)
						parentId = parentElement.getText().trim();
					else
						parentId = null;
					names.put(id, name);
					parentIds.put(id, parentId);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String id = element.elementText("id").trim();
					List<String> pathSegments = new ArrayList<>();

					do {
						pathSegments.add(names.get(id));
						id = parentIds.get(id);
					} while (id != null);

					Collections.reverse(pathSegments);
					element.addElement("path").setText(StringUtils.join(pathSegments, "/"));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate100(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("paused").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate101(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements())
								executorElement.addElement("shellAccessEnabled").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate102(File dataDir, Stack<Integer> versions) {
	}

	private void migrate103(File dataDir, Stack<Integer> versions) {
		VersionedXmlDoc projectUpdatesDom;
		File projectUpdatesFile = new File(dataDir, "ProjectUpdates.xml");
		projectUpdatesDom = new VersionedXmlDoc();
		Element listElement = projectUpdatesDom.addElement("list");

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String projectId = element.elementTextTrim("id");
					Element updateDateElement = element.element("updateDate");
					element.addElement("update").setText(projectId);

					Element updateElement = listElement.addElement("io.onedev.server.model.ProjectUpdate");
					updateElement.addAttribute("revision", "0.0");
					updateElement.addElement("id").setText(projectId);
					updateElement.addElement("date").setText(updateDateElement.getText().trim());
					updateDateElement.detach();

					element.addElement("codeAnalysisSetting");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("PERFORMANCE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("serverJobExecutorCpuQuota").detach();
							valueElement.element("serverJobExecutorMemoryQuota").detach();
							valueElement.element("cpuIntensiveTaskConcurrency").detach();
						}
					} else if (element.elementTextTrim("key").equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								Element mountDockerSockElement = executorElement.element("mountDockerSock");
								if (mountDockerSockElement != null && mountDockerSockElement.attribute("defined-in") != null)
									mountDockerSockElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}

		projectUpdatesDom.writeToFile(projectUpdatesFile, true);
	}

	private static final Pattern migrate104_pattern = Pattern.compile("\\(/projects/(\\d+)/attachment/(.*?)\\)");

	private String migrate104_markdown(String content) {
		StringBuffer buffer = new StringBuffer();
		Matcher matcher = migrate104_pattern.matcher(content);
		while (matcher.find()) {
			matcher.appendReplacement(buffer,
					Matcher.quoteReplacement("(/~downloads/projects/" + matcher.group(1) + "/attachments/" + matcher.group(2) + ")"));
		}
		matcher.appendTail(buffer);
		return buffer.toString();
	}

	private void migrate104(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element descriptionElement = element.element("description");
					if (descriptionElement != null)
						descriptionElement.setText(migrate104_markdown(descriptionElement.getText()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					contentElement.setText(migrate104_markdown(contentElement.getText()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element descriptionElement = element.element("description");
					if (descriptionElement != null)
						descriptionElement.setText(migrate104_markdown(descriptionElement.getText()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					contentElement.setText(migrate104_markdown(contentElement.getText()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					contentElement.setText(migrate104_markdown(contentElement.getText()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element contentElement = element.element("content");
					contentElement.setText(migrate104_markdown(contentElement.getText()));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate105(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements())
								executorElement.addElement("sitePublishEnabled").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate106(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("AUTHENTICATOR")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("authenticationRequired").setText("true");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate107(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SYSTEM")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var gitConfigElement = valueElement.element("gitConfig");
							gitConfigElement.setName("gitLocation");
							var clazz = gitConfigElement.attributeValue("class").replace(
									"io.onedev.server.git.config.",
									"io.onedev.server.git.location.");
							gitConfigElement.addAttribute("class", clazz);

							var curlConfigElement = valueElement.element("curlConfig");
							curlConfigElement.setName("curlLocation");
							clazz = curlConfigElement.attributeValue("class").replace(
									"io.onedev.server.git.config.",
									"io.onedev.server.git.location.");
							curlConfigElement.addAttribute("class", clazz);
						}
					} else if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element connectorElement : valueElement.elements()) {
								if (connectorElement.getName().contains("OpenIdConnector")) {
									Element issuerUrlElement = connectorElement.element("issuerUrl");
									issuerUrlElement.setName("configurationDiscoveryUrl");
									issuerUrlElement.setText(issuerUrlElement.getText().trim() + "/.well-known/openid-configuration");
								}
							}
						}
					} else if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("KubernetesExecutor")) {
									executorElement.addElement("cpuRequest").setText("250m");
									executorElement.addElement("memoryRequest").setText("256Mi");
								}
							}
						}
					} else if (key.equals("PERFORMANCE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							int cpuIntensiveTaskConcurrency;
							try {
								HardwareAbstractionLayer hardware = new SystemInfo().getHardware();
								cpuIntensiveTaskConcurrency = hardware.getProcessor().getLogicalProcessorCount();
							} catch (Exception e) {
								cpuIntensiveTaskConcurrency = 4;
							}
							valueElement.addElement("cpuIntensiveTaskConcurrency")
									.setText(String.valueOf(cpuIntensiveTaskConcurrency));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("gitPackConfig");

					for (var branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.setName("io.onedev.server.model.support.code.BranchProtection");
						for (var fileProtectionElement : branchProtectionElement.element("fileProtections").elements())
							fileProtectionElement.setName("io.onedev.server.model.support.code.FileProtection");
					}
					for (var tagProtectionElement : element.element("tagProtections").elements()) {
						tagProtectionElement.setName("io.onedev.server.model.support.code.TagProtection");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Agents.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("memory").detach();
					Element cpuElement = element.element("cpu");
					cpuElement.setName("cpus");
					cpuElement.setText(String.valueOf(Integer.parseInt(cpuElement.getTextTrim()) / 1000));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private Set<String> getMentioned108(Map<String, String> userIds, String content) {
		Set<String> mentioned = new HashSet<>();
		MarkdownService markdownService = OneDev.getInstance(MarkdownService.class);
		for (String userName : new MentionParser().parseMentions(markdownService.render(content))) {
			String userId = userIds.get(userName);
			if (userId != null)
				mentioned.add(userId);
		}
		return mentioned;
	}

	private void migrate108(File dataDir, Stack<Integer> versions) {
		Set<Pair<String, String>> issueMentions = new HashSet<>();
		Set<Pair<String, String>> pullRequestMentions = new HashSet<>();
		Set<Pair<String, String>> codeCommentMentions = new HashSet<>();

		Map<String, String> userIds = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					userIds.put(element.elementText("name").trim(),
							element.elementTextTrim("id"));
				}
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("id");
					Element descriptionElement = element.element("description");
					if (descriptionElement != null) {
						getMentioned108(userIds, descriptionElement.getText()).forEach(userId -> {
							issueMentions.add(new Pair<>(issueId, userId));
						});
					}
				}
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String issueId = element.elementTextTrim("issue");
					Element contentElement = element.element("content");
					if (contentElement != null) {
						getMentioned108(userIds, contentElement.getText()).forEach(userId -> {
							issueMentions.add(new Pair<>(issueId, userId));
						});
					}
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String requestId = element.elementTextTrim("id");
					Element descriptionElement = element.element("description");
					if (descriptionElement != null) {
						getMentioned108(userIds, descriptionElement.getText()).forEach(userId -> {
							pullRequestMentions.add(new Pair<>(requestId, userId));
						});
					}
				}
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String requestId = element.elementTextTrim("request");
					Element contentElement = element.element("content");
					if (contentElement != null) {
						getMentioned108(userIds, contentElement.getText()).forEach(userId -> {
							pullRequestMentions.add(new Pair<>(requestId, userId));
						});
					}
				}
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("id");
					Element contentElement = element.element("content");
					if (contentElement != null) {
						getMentioned108(userIds, contentElement.getText()).forEach(userId -> {
							codeCommentMentions.add(new Pair<>(commentId, userId));
						});
					}
				}
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String commentId = element.elementTextTrim("comment");
					Element contentElement = element.element("content");
					if (contentElement != null) {
						getMentioned108(userIds, contentElement.getText()).forEach(userId -> {
							codeCommentMentions.add(new Pair<>(commentId, userId));
						});
					}
				}
			}
		}

		VersionedXmlDoc mentionsDom;
		File mentionsFile = new File(dataDir, "IssueMentions.xml");
		mentionsDom = new VersionedXmlDoc();
		Element mentionsElement = mentionsDom.addElement("list");

		Long id = 1L;
		for (Pair<String, String> issueMention : issueMentions) {
			Element mentionElement = mentionsElement.addElement("io.onedev.server.model.IssueMention");
			mentionElement.addElement("id").setText(String.valueOf(id++));
			mentionElement.addAttribute("revision", "0.0");
			mentionElement.addElement("issue").setText(issueMention.getLeft());
			mentionElement.addElement("user").setText(issueMention.getRight());
		}
		mentionsDom.writeToFile(mentionsFile, true);

		mentionsFile = new File(dataDir, "PullRequestMentions.xml");
		mentionsDom = new VersionedXmlDoc();
		mentionsElement = mentionsDom.addElement("list");

		id = 1L;
		for (Pair<String, String> it : pullRequestMentions) {
			Element mentionElement = mentionsElement.addElement("io.onedev.server.model.PullRequestMention");
			mentionElement.addElement("id").setText(String.valueOf(id++));
			mentionElement.addAttribute("revision", "0.0");
			mentionElement.addElement("request").setText(it.getLeft());
			mentionElement.addElement("user").setText(it.getRight());
		}
		mentionsDom.writeToFile(mentionsFile, true);

		mentionsFile = new File(dataDir, "CodeCommentMentions.xml");
		mentionsDom = new VersionedXmlDoc();
		mentionsElement = mentionsDom.addElement("list");

		id = 1L;
		for (Pair<String, String> it : codeCommentMentions) {
			Element mentionElement = mentionsElement.addElement("io.onedev.server.model.CodeCommentMention");
			mentionElement.addElement("id").setText(String.valueOf(id++));
			mentionElement.addAttribute("revision", "0.0");
			mentionElement.addElement("comment").setText(it.getLeft());
			mentionElement.addElement("user").setText(it.getRight());
		}
		mentionsDom.writeToFile(mentionsFile, true);
	}

	private void migrate109(File dataDir, Stack<Integer> versions) {
		var updateIds = new HashSet<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element updateElement = element.element("update");
					updateIds.add(updateElement.getTextTrim());
					updateElement.setName("dynamics");
				}
				dom.writeToFile(file, false);
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("ProjectUpdates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (!updateIds.contains(element.elementTextTrim("id"))) {
						element.detach();
					} else {
						element.element("date").setName("lastActivityDate");
						element.setName("io.onedev.server.model.ProjectDynamics");
					}
				}
				FileUtils.deleteFile(file);
				String newFileName = file.getName().replace("Update", "Dynamics");
				dom.writeToFile(new File(file.getParent(), newFileName), false);
			} else if (file.getName().startsWith("Issues.xml")
					|| file.getName().startsWith("CodeComments.xml")
					|| file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element lastUpdateElement = element.element("lastUpdate");
					lastUpdateElement.setName("lastActivity");
					lastUpdateElement.element("activity").setName("description");
				}
				dom.writeToFile(file, false);
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				List<Node> selectedNodes = new ArrayList<>();
				selectedNodes.addAll(dom.selectNodes("//io.onedev.server.model.support.pullrequest.NamedPullRequestQuery"));
				selectedNodes.addAll(dom.selectNodes("//io.onedev.server.model.support.issue.NamedIssueQuery"));
				selectedNodes.addAll(dom.selectNodes("//io.onedev.server.model.support.NamedCodeCommentQuery"));
				selectedNodes.addAll(dom.selectNodes("//io.onedev.server.model.support.NamedProjectQuery"));
				for (Node node : selectedNodes) {
					if (node instanceof Element) {
						Element element = (Element) node;
						Element queryElement = element.element("query");
						if (queryElement != null)
							queryElement.setText(queryElement.getText().trim().replace("\"Update Date\"", "\"Last Activity Date\""));
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate110(File dataDir, Stack<Integer> versions) {
		var updateIds = new HashSet<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("accessToken").setText(CryptoUtils.generateSecret());
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate111(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element buildSettingElement = element.element("buildSetting");
					buildSettingElement.addElement("jobProperties");

					for (Element branchProtectionElement : element.element("branchProtections").elements())
						branchProtectionElement.addElement("requireStrictBuilds").setText("false");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element lastMergePreviewElement = element.element("lastMergePreview");
					if (lastMergePreviewElement != null) {
						lastMergePreviewElement.setName("mergePreview");
						String mergeCommitHash = lastMergePreviewElement.elementText("mergeCommitHash");
						if (mergeCommitHash != null)
							element.addElement("buildCommitHash").setText(mergeCommitHash);
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldSpecElement : valueElement.element("fieldSpecs").elements()) {
								if (fieldSpecElement.getName().equals("io.onedev.server.model.support.issue.field.spec.ChoiceField")) {
									Element defaultValueProviderElement = fieldSpecElement.element("defaultValueProvider");
									if (defaultValueProviderElement != null
											&& defaultValueProviderElement.attributeValue("class").contains("SpecifiedDefaultValue")) {
										Element defaultValueElement = defaultValueProviderElement.element("value");
										Element defaultValuesElement = defaultValueProviderElement.addElement("defaultValues");
										defaultValueElement.detach();
										defaultValuesElement.addElement("io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider.DefaultValue").add(defaultValueElement);
									}
									Element defaultMultiValueProviderElement = fieldSpecElement.element("defaultMultiValueProvider");
									if (defaultMultiValueProviderElement != null
											&& defaultMultiValueProviderElement.attributeValue("class").contains("SpecifiedDefaultMultiValue")) {
										Element defaultValueElement = defaultMultiValueProviderElement.element("value");
										Element defaultValuesElement = defaultMultiValueProviderElement.addElement("defaultValues");
										defaultValueElement.detach();
										defaultValuesElement.addElement("io.onedev.server.model.support.inputspec.choiceinput.defaultmultivalueprovider.DefaultMultiValue").add(defaultValueElement);
									}
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate112(File dataDir, Stack<Integer> versions) {
		var updateIds = new HashSet<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("IssueReferencedFromCommitData"))
						element.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("PullRequestReferencedFromCommitData"))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate113(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.issue.field.spec.ChoiceField")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField");
					}
				}
				for (Node node : dom.selectNodes("//defaultValueProvider")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						String clazz = element.attributeValue("class");
						if (clazz != null) {
							clazz = clazz.replace(
									"io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider.",
									"io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.");
							element.addAttribute("class", clazz);
						}
					}
				}
				for (Node node : dom.selectNodes("//defaultMultiValueProvider")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						String clazz = element.attributeValue("class");
						if (clazz != null) {
							clazz = clazz.replace(
									"io.onedev.server.model.support.inputspec.choiceinput.defaultmultivalueprovider.",
									"io.onedev.server.model.support.issue.field.spec.choicefield.defaultmultivalueprovider.");
							element.addAttribute("class", clazz);
						}
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.inputspec.choiceinput.defaultvalueprovider.DefaultValue")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.choicefield.defaultvalueprovider.DefaultValue");
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.inputspec.choiceinput.defaultmultivalueprovider.DefaultMultiValue")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.choicefield.defaultmultivalueprovider.DefaultMultiValue");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate114_reviewRequirement(@Nullable Element reviewRequirementElement) {
		if (reviewRequirementElement != null) {
			String reviewRequirement = reviewRequirementElement.getText();
			reviewRequirement = reviewRequirement.replaceAll("\\)\\s+(user|group)", ") and $1");
			reviewRequirement = reviewRequirement.replace("):all", "):2");
			reviewRequirementElement.setText(reviewRequirement);
		}
	}

	private void migrate114(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.inputspec.",
						"io.onedev.server.buildspecmodel.inputspec.");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var buildSettingElement = element.element("buildSetting");
					buildSettingElement.element("actionAuthorizations").detach();
					for (var branchProtectionElement : element.element("branchProtections").elements()) {
						migrate114_reviewRequirement(branchProtectionElement.element("reviewRequirement"));
						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
							migrate114_reviewRequirement(fileProtectionElement.element("reviewRequirement"));
						}
					}
					for (var secretElement : buildSettingElement.element("jobSecrets").elements()) {
						var authorizedBranchesElement = secretElement.element("authorizedBranches");
						if (authorizedBranchesElement != null) {
							PatternSet patterns = PatternSet.parse(authorizedBranchesElement.getText());
							var criterias = new ArrayList<>();
							var positiveCriterias = new ArrayList<>();
							for (String include : patterns.getIncludes()) {
								positiveCriterias.add("on branch \"" + include + "\"");
							}
							if (!positiveCriterias.isEmpty()) {
								criterias.add("(" + StringUtils.join(positiveCriterias, " or ") + ")");
							}
							for (String exclude : patterns.getExcludes()) {
								criterias.add("not(on branch \"" + exclude + "\")");
							}
							secretElement.addElement("authorization").setText(StringUtils.join(criterias, " and "));
							authorizedBranchesElement.detach();
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var jobExecutorElement : valueElement.elements()) {
								var jobAuthorizationElement = jobExecutorElement.element("jobAuthorization");
								if (jobAuthorizationElement != null)
									jobAuthorizationElement.setName("jobRequirement");
							}
						}
					} else if (key.equals("GROOVY_SCRIPTS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var groovyScriptElement : valueElement.elements()) {
								var criterias = new ArrayList<>();
								Element allowedProjectsElement = groovyScriptElement.element("allowedProjects");
								if (allowedProjectsElement != null) {
									PatternSet patterns = PatternSet.parse(allowedProjectsElement.getText());
									var positiveCriterias = new ArrayList<>();
									for (String include : patterns.getIncludes()) {
										positiveCriterias.add("\"Project\" is \"" + include + "\"");
									}
									if (!positiveCriterias.isEmpty()) {
										criterias.add("(" + StringUtils.join(positiveCriterias, " or ") + ")");
									}
									for (String exclude : patterns.getExcludes()) {
										criterias.add("not(\"Project\" is \"" + exclude + "\")");
									}
									allowedProjectsElement.detach();
								}

								Element allowedBranchesElement = groovyScriptElement.element("allowedBranches");
								if (allowedBranchesElement != null) {
									PatternSet patterns = PatternSet.parse(allowedBranchesElement.getText());
									var positiveCriterias = new ArrayList<>();
									for (String include : patterns.getIncludes()) {
										positiveCriterias.add("on branch \"" + include + "\"");
									}
									if (!positiveCriterias.isEmpty()) {
										criterias.add("(" + StringUtils.join(positiveCriterias, " or ") + ")");
									}
									for (String exclude : patterns.getExcludes()) {
										criterias.add("not(on branch \"" + exclude + "\")");
									}
									allowedBranchesElement.detach();
								}
								if (!criterias.isEmpty())
									groovyScriptElement.addElement("authorization").setText(StringUtils.join(criterias, " and "));
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate115(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldSpecElement : valueElement.element("fieldSpecs").elements()) {
								if (fieldSpecElement.getName().equals("io.onedev.server.model.support.issue.field.spec.UserChoiceField")) {
									Element defaultValueProviderElement = fieldSpecElement.element("defaultValueProvider");
									if (defaultValueProviderElement != null
											&& defaultValueProviderElement.attributeValue("class").contains("SpecifiedDefaultValue")) {
										Element defaultValueElement = defaultValueProviderElement.element("value");
										Element defaultValuesElement = defaultValueProviderElement.addElement("defaultValues");
										defaultValueElement.detach();
										defaultValuesElement.addElement("io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultvalueprovider.DefaultValue").add(defaultValueElement);
									}
									Element defaultMultiValueProviderElement = fieldSpecElement.element("defaultMultiValueProvider");
									if (defaultMultiValueProviderElement != null
											&& defaultMultiValueProviderElement.attributeValue("class").contains("SpecifiedDefaultMultiValue")) {
										Element defaultValueElement = defaultMultiValueProviderElement.element("value");
										Element defaultValuesElement = defaultMultiValueProviderElement.addElement("defaultValues");
										defaultValueElement.detach();
										defaultValuesElement.addElement("io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultmultivalueprovider.DefaultMultiValue").add(defaultValueElement);
									}
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate116(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Node node : dom.selectNodes("//io.onedev.server.model.support.issue.field.spec.UserChoiceField")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField");
					}
				}
				for (Node node : dom.selectNodes("//defaultValueProvider")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						String clazz = element.attributeValue("class");
						if (clazz != null) {
							clazz = clazz.replace(
									"io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultvalueprovider.",
									"io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultvalueprovider.");
							element.addAttribute("class", clazz);
						}
					}
				}
				for (Node node : dom.selectNodes("//defaultMultiValueProvider")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						String clazz = element.attributeValue("class");
						if (clazz != null) {
							clazz = clazz.replace(
									"io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultmultivalueprovider.",
									"io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultmultivalueprovider.");
							element.addAttribute("class", clazz);
						}
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultvalueprovider.DefaultValue")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultvalueprovider.DefaultValue");
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.buildspecmodel.inputspec.userchoiceinput.defaultmultivalueprovider.DefaultMultiValue")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.model.support.issue.field.spec.userchoicefield.defaultmultivalueprovider.DefaultMultiValue");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate117(File dataDir, Stack<Integer> versions) {
		var agentLastUsedDatesFile = new File(dataDir, "AgentLastUsedDates.xml");
		var agentLastUsedDatesDom = new VersionedXmlDoc();
		var agentLastUsedDatesElement = agentLastUsedDatesDom.addElement("list");

		agentLastUsedDatesDom.writeToFile(agentLastUsedDatesFile, true);
		var agentLastUsedDateId = 1L;

		var agentTokenIds = new HashSet<Long>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("AgentTokens.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					agentTokenIds.add(Long.valueOf(element.elementTextTrim("id")));
			}
		}
		var maxAgentTokenId = agentTokenIds.stream().max(naturalOrder()).orElse(0L);

		VersionedXmlDoc partialAgentTokensDom;
		Element partialAgentTokensElement;
		var partialAgentTokensFile = new File(dataDir, "AgentTokens.xml");
		if (partialAgentTokensFile.exists()) {
			partialAgentTokensDom = VersionedXmlDoc.fromFile(partialAgentTokensFile);
			partialAgentTokensElement = partialAgentTokensDom.getRootElement();
		} else {
			partialAgentTokensDom = new VersionedXmlDoc();
			partialAgentTokensElement = partialAgentTokensDom.addElement("list");
		}

		for (var file : dataDir.listFiles()) {
			if (file.getName().startsWith("ClusterCredentials.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Projects.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (var element : dom.getRootElement().elements()) {
					var withLfsElement = element.element("pullRequestSetting").element("withLFS");
					if (withLfsElement != null)
						withLfsElement.detach();
					element.element("dynamics").setName("lastEventDate");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("ProjectDynamicss.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (var element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.ProjectLastEventDate");
					element.element("lastActivityDate").setName("activity");
					var lastCommitDateElement = element.element("lastCommitDate");
					if (lastCommitDateElement != null)
						lastCommitDateElement.setName("commit");
				}
				FileUtils.deleteFile(file);
				dom.writeToFile(new File(dataDir, file.getName().replace("ProjectDynamicss", "ProjectLastEventDates")), false);
			} else if (file.getName().startsWith("Agents.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (var element : dom.getRootElement().elements()) {
					var lastUsedDateElement = element.element("lastUsedDate");
					String lastUsedDate = null;
					if (lastUsedDateElement != null) {
						lastUsedDateElement.remove(lastUsedDateElement.attribute("class"));
						lastUsedDate = lastUsedDateElement.getText().trim();
					} else {
						lastUsedDateElement = element.addElement("lastUsedDate");
					}
					lastUsedDateElement.setText(String.valueOf(agentLastUsedDateId));

					lastUsedDateElement = agentLastUsedDatesElement.addElement("io.onedev.server.model.AgentLastUsedDate");
					lastUsedDateElement.addAttribute("revision", "0.0");
					lastUsedDateElement.addElement("id").setText(String.valueOf(agentLastUsedDateId));
					if (lastUsedDate != null)
						lastUsedDateElement.addElement("value").setText(lastUsedDate);
					agentLastUsedDateId++;

					var tokenElement = element.element("token");
					var tokenId = Long.valueOf(tokenElement.getTextTrim());
					if (!agentTokenIds.remove(tokenId)) {
						tokenId = ++maxAgentTokenId;
						tokenElement.setText(String.valueOf(tokenId));
						var agentTokenElement = partialAgentTokensElement.addElement("io.onedev.server.model.AgentToken");
						agentTokenElement.addAttribute("revision", "0.0");
						agentTokenElement.addElement("id").setText(String.valueOf(tokenId));
						agentTokenElement.addElement("value").setText(UUID.randomUUID().toString());
					}
				}
				dom.writeToFile(file, false);
			}
			agentLastUsedDatesDom.writeToFile(agentLastUsedDatesFile, true);
			partialAgentTokensDom.writeToFile(partialAgentTokensFile, true);
		}
	}

	private void migrate118(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("ClusterServers.xml"))
				FileUtils.deleteFile(file);
		}
	}

	private void migrate119(File dataDir, Stack<Integer> versions) {
	}

	private void migrate120(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("ClusterServers.xml"))
				FileUtils.deleteFile(file);
		}
	}

	private void migrate121(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.element("signatureRequired").setName("commitSignatureRequired");
						branchProtectionElement.addElement("enforceConventionalCommits").setText("false");
						branchProtectionElement.addElement("commitTypes");
						branchProtectionElement.addElement("commitScopes");
					}
					for (Element tagProtectionElement : element.element("tagProtections").elements()) {
						tagProtectionElement.element("signatureRequired").setName("commitSignatureRequired");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var commitMessageFixPatternsElement = valueElement.addElement("commitMessageFixPatterns");
							var entriesElement = commitMessageFixPatternsElement.addElement("entries");
							var entryElement = entriesElement.addElement("io.onedev.server.model.support.issue.CommitMessageFixPatterns_-Entry");
							entryElement.addElement("prefix").setText("(^|\\W)(fix|fixed|fixes|fixing|resolve|resolved|resolves|resolving|close|closed|closes|closing)[\\s:]+");
							entryElement.addElement("suffix").setText("(?=$|\\W)");
							entryElement = entriesElement.addElement("io.onedev.server.model.support.issue.CommitMessageFixPatterns_-Entry");
							entryElement.addElement("prefix").setText("\\(\\s*");
							entryElement.addElement("suffix").setText("\\s*\\)\\s*$");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate122(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("enableSelfDeregister").setText("false");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate123(File dataDir, Stack<Integer> versions) {
		var issueTouchesDoc = new VersionedXmlDoc();
		var issueTouchesElement = issueTouchesDoc.addElement("list");
		var pullRequestTouchesDoc = new VersionedXmlDoc();
		var pullRequestTouchesElement = pullRequestTouchesDoc.addElement("list");
		var codeCommentTouchesDoc = new VersionedXmlDoc();
		var codeCommentTouchesElement = codeCommentTouchesDoc.addElement("list");

		var issueTouchId = 1L;
		var pullRequestTouchId = 1L;
		var codeCommentTouchId = 1L;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var issueTouchElement = issueTouchesElement.addElement("io.onedev.server.model.IssueTouch");
					issueTouchElement.addAttribute("revision", "0.0.0");
					issueTouchElement.addElement("id").setText(String.valueOf(issueTouchId++));
					issueTouchElement.addElement("project").setText(element.elementTextTrim("project"));
					issueTouchElement.addElement("issueId").setText(element.elementTextTrim("id"));
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var pullRequestTouchElement = pullRequestTouchesElement.addElement("io.onedev.server.model.PullRequestTouch");
					pullRequestTouchElement.addAttribute("revision", "0.0.0");
					pullRequestTouchElement.addElement("id").setText(String.valueOf(pullRequestTouchId++));
					pullRequestTouchElement.addElement("project").setText(element.elementTextTrim("targetProject"));
					pullRequestTouchElement.addElement("requestId").setText(element.elementTextTrim("id"));
				}
			} else if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var codeCommentTouchElement = codeCommentTouchesElement.addElement("io.onedev.server.model.CodeCommentTouch");
					codeCommentTouchElement.addAttribute("revision", "0.0.0");
					codeCommentTouchElement.addElement("id").setText(String.valueOf(codeCommentTouchId++));
					codeCommentTouchElement.addElement("project").setText(element.elementTextTrim("project"));
					codeCommentTouchElement.addElement("commentId").setText(element.elementTextTrim("id"));
				}
			} else if (file.getName().startsWith("IssueTouchs.xml")
					|| file.getName().startsWith("PullRequestTouchs.xml")
					|| file.getName().startsWith("CodeCommentTouchs.xml")) {
				FileUtils.deleteFile(file);
			}
		}

		issueTouchesDoc.writeToFile(new File(dataDir, "IssueTouchs.xml"), true);
		pullRequestTouchesDoc.writeToFile(new File(dataDir, "PullRequestTouchs.xml"), true);
		codeCommentTouchesDoc.writeToFile(new File(dataDir, "CodeCommentTouchs.xml"), true);
	}

	private void migrate124(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var accessTokenElement = element.element("accessToken");
					var accessTokensElement = element.addElement("accessTokens");
					var newAccessTokenElement = accessTokensElement.addElement("io.onedev.server.model.support.AccessToken");
					newAccessTokenElement.addElement("value").setText(accessTokenElement.getText().trim());
					newAccessTokenElement.addElement("createDate").setText("2023-05-28T22:07:56.311+01:00");
					accessTokenElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate125(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("KubernetesExecutor"))
									executorElement.element("mountContainerSock").detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate126(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("KubernetesExecutor")
										|| executorElement.getName().contains("DockerExecutor")) {
									executorElement.addElement("imageMappings");
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate127(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("SshKeys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					PublicKey publicKey = null;
					try {
						publicKey = SshKeyUtils.decodeSshPublicKey(element.elementText("content"));
					} catch (IOException | GeneralSecurityException e) {
						throw new RuntimeException(e);
					}
					var digest = KeyUtils.getFingerPrint(BuiltinDigests.sha256, publicKey);
					var digestElement = element.element("digest");
					digestElement.setName("fingerprint");
					digestElement.setText(digest);
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate128(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("KubernetesExecutor")
										|| executorElement.getName().contains("DockerExecutor")) {
									for (var registryLoginElement : executorElement.element("registryLogins").elements()) {
										registryLoginElement.setName("io.onedev.server.model.support.administration.jobexecutor.RegistryLogin");
									}
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate129(File dataDir, Stack<Integer> versions) {

	}

	private void migrate130(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : element.element("branchProtections").elements()) {
						branchProtectionElement.addElement("checkCommitMessageFooter").setText("false");
						branchProtectionElement.addElement("commitTypesForFooterCheck");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate131(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeCommentTouchs.xml") || file.getName().startsWith("PullRequestTouchs.xml")) {
				FileUtils.deleteFile(file);
			} else if (file.getName().startsWith("Alerts.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("mailError").setText("false");
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("NOTIFICATION_TEMPLATE_SETTING")) {
						keyElement.setText("EMAIL_TEMPLATES");
						Element valueElement = element.element("value");

						List<String> lines = new ArrayList<>();
						for (var lineElement : valueElement.element("issueNotificationTemplate").elements())
							lines.add(lineElement.getText());
						var issueNotificationTemplate = StringUtils.join(lines, "\n");

						String pullRequestNotificationTemplate;
						var pullRequestNotificationTemplateElement = valueElement.element("pullRequestNotificationTemplate");
						if (pullRequestNotificationTemplateElement.attributeValue("reference") != null) {
							pullRequestNotificationTemplate = issueNotificationTemplate;
						} else {
							lines.clear();
							for (var lineElement : valueElement.element("pullRequestNotificationTemplate").elements())
								lines.add(lineElement.getText());
							pullRequestNotificationTemplate = StringUtils.join(lines, "\n");
						}

						valueElement.detach();

						Element emailTemplatesElement;
						try (InputStream is = getClass().getResourceAsStream("migrate131_email_templates.xml")) {
							Preconditions.checkNotNull(is);
							var xml = IOUtils.toString(is, UTF_8.name());
							emailTemplatesElement = VersionedXmlDoc.fromXML(xml).getRootElement();
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
						emailTemplatesElement.element("issueNotification").setText(issueNotificationTemplate);
						emailTemplatesElement.element("pullRequestNotification").setText(pullRequestNotificationTemplate);
						emailTemplatesElement.setName("value");
						emailTemplatesElement.addAttribute("class", "io.onedev.server.model.support.administration.emailtemplates.EmailTemplates");
						element.add(emailTemplatesElement);
						break;
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate132(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("ALERT")) {
						var valueElement = element.element("value");
						valueElement.element("trialExpireInOneWeekAlerted").setName("trialEnterpriseLicenseExpireInOneWeekAlerted");
						valueElement.element("trialExpiredAlerted").setName("trialEnterpriseLicenseExpiredAlerted");
						valueElement.element("subscriptionExpireInOneMonthAlerted").setName("enterpriseLicenseExpireInOneMonthAlerted");
						valueElement.element("subscriptionExpireInOneWeekAlerted").setName("enterpriseLicenseExpireInOneWeekAlerted");
						valueElement.element("subscriptionExpiredAlerted").setName("enterpriseLicenseExpiredAlerted");
						valueElement.element("subscriptionExpiredBeforeReleaseDateAlerted").detach();
						valueElement.element("userLimitApproachingAlerted").setName("enterpriseLicenseUserLimitApproachingAlerted");
						valueElement.element("userLimitExceededAlerted").setName("enterpriseLicenseUserLimitExceededAlerted");
					} else if (keyElement.getTextTrim().equals("SUBSCRIPTION_DATA")) {
						keyElement.setText("LICENSE_DATA");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate133(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var buildSettingElement = element.element("buildSetting");
					for (var secretElement : buildSettingElement.element("jobSecrets").elements()) {
						secretElement.addElement("archived").setText("false");
					}
					for (var secretElement : buildSettingElement.element("jobProperties").elements()) {
						secretElement.addElement("archived").setText("false");
					}
				}
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("ALERT")) {
						var valueElement = element.element("value");
						valueElement.element("enterpriseLicenseUserLimitApproachingAlerted").detach();
						valueElement.element("enterpriseLicenseUserLimitExceededAlerted").detach();
					}
				}
				dom.writeToFile(file, false);
			}

		}
	}

	private void migrate134(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("MAIL")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							if (valueElement.attributeValue("class").contains("OtherMailSetting")) {
								var enableStartTLSElement = valueElement.element("enableStartTLS");
								var sslSettingElement = valueElement.addElement("sslSetting");
								if (enableStartTLSElement.getTextTrim().equals("true")) {
									sslSettingElement.addAttribute("class", "io.onedev.server.model.support.administration.mailsetting.SmtpExplicitSsl");
									sslSettingElement.addElement("trustAll").setText("false");
								} else {
									sslSettingElement.addAttribute("class", "io.onedev.server.model.support.administration.mailsetting.SmtpWithoutSsl");
								}
								enableStartTLSElement.detach();
								var portElement = valueElement.element("smtpPort");
								portElement.detach();
								portElement.setName("port");
								sslSettingElement.add(portElement);

								var pollSettingElement = valueElement.element("otherInboxPollSetting");
								if (pollSettingElement != null) {
									var enableSSLElement = pollSettingElement.element("enableSSL");
									sslSettingElement = pollSettingElement.addElement("sslSetting");
									if (enableSSLElement.getTextTrim().equals("true")) {
										sslSettingElement.addAttribute("class", "io.onedev.server.model.support.administration.mailsetting.ImapImplicitSsl");
										sslSettingElement.addElement("trustAll").setText("false");
									} else {
										sslSettingElement.addAttribute("class", "io.onedev.server.model.support.administration.mailsetting.ImapWithoutSsl");
									}
									enableSSLElement.detach();
									portElement = pollSettingElement.element("imapPort");
									portElement.detach();
									portElement.setName("port");
									sslSettingElement.add(portElement);
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate135(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("ALERT")) {
						var valueElement = element.element("value");
						valueElement.element("trialEnterpriseLicenseExpireInOneWeekAlerted").setName("trialSubscriptionExpireInOneWeekAlerted");
						valueElement.element("trialEnterpriseLicenseExpiredAlerted").setName("trialSubscriptionExpiredAlerted");
						valueElement.element("enterpriseLicenseExpireInOneMonthAlerted").setName("subscriptionExpireInOneMonthAlerted");
						valueElement.element("enterpriseLicenseExpireInOneWeekAlerted").setName("subscriptionExpireInOneWeekAlerted");
						valueElement.element("enterpriseLicenseExpiredAlerted").setName("subscriptionExpiredAlerted");
					} else if (keyElement.getTextTrim().equals("LICENSE_DATA")) {
						keyElement.setText("SUBSCRIPTION_DATA");
					} else if (keyElement.getTextTrim().equals("PERFORMANCE")) {
						var valueElement = element.element("value");
						var cpuIntensiveTaskConcurrencyElement = valueElement.element("cpuIntensiveTaskConcurrency");
						var cpuIntensiveTaskConcurrency = Integer.parseInt(cpuIntensiveTaskConcurrencyElement.getTextTrim()) / 2;
						if (cpuIntensiveTaskConcurrency == 0)
							cpuIntensiveTaskConcurrency = 1;
						cpuIntensiveTaskConcurrencyElement.setText(String.valueOf(cpuIntensiveTaskConcurrency));
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate136(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("checkoutPaths");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CoverageMetrics.xml")
					|| file.getName().startsWith("UnitTestMetrics.xml")
					|| file.getName().startsWith("ProblemMetrics.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				var buildAndReports = new HashSet<String>();
				for (Element element : dom.getRootElement().elements()) {
					if (file.getName().startsWith("CoverageMetrics.xml")) {
						element.element("totalMethods").detach();
						element.element("totalStatements").detach();
						element.element("totalBranches").detach();
						element.element("totalLines").detach();
					}
					var buildId = element.elementText("build").trim();
					var reportName = element.elementText("reportName").trim();
					if (!buildAndReports.add(buildId + ":" + reportName))
						element.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate137(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CoverageMetrics.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("methodCoverage").detach();
					element.element("statementCoverage").detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate138(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var jobWorkspaceElement = element.element("jobWorkspace");
					if (jobWorkspaceElement != null)
						jobWorkspaceElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate139(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Agents.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("temporal").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate140(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements())
								executorElement.addElement("htmlReportPublishEnabled").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate141(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SYSTEM"))
						element.element("value").addElement("disableAutoUpdateCheck").setText("false");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate142(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content,
							"io.onedev.server.util.channelnotification.",
							"io.onedev.server.model.support.channelnotification.");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("timeTracking").setText("false");
					element.element("issueSetting").addElement("timesheetSettings").addAttribute("class", "linked-hash-map");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("totalEstimatedTime").setText("0");
					element.addElement("totalSpentTime").setText("0");
					element.addElement("ownEstimatedTime").setText("0");
					element.addElement("ownSpentTime").setText("0");
					element.addElement("progress").setText("-1");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var dataElement = element.element("data");
					if (dataElement.attributeValue("class").startsWith("io.onedev.server.model.support.issue.changedata.IssueLink"))
						element.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				String content;
				try {
					content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content,
							"io.onedev.server.model.support.administration.mailsetting.Office365Setting",
							"io.onedev.server.plugin.mailservice.office365.Office365MailService");
					content = StringUtils.replace(content,
							"io.onedev.server.model.support.administration.mailsetting.GmailSetting",
							"io.onedev.server.plugin.mailservice.gmail.GmailMailService");
					content = StringUtils.replace(content,
							"io.onedev.server.model.support.administration.mailsetting.OtherMailSetting",
							"io.onedev.server.plugin.mailservice.smtpimap.SmtpImapMailService");
					content = StringUtils.replace(content,
							"io.onedev.server.model.support.administration.mailsetting.",
							"io.onedev.server.model.support.administration.mailservice.");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
				VersionedXmlDoc dom = VersionedXmlDoc.fromXML(content);
				for (Element element : dom.getRootElement().elements()) {
					var key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("timeTrackingSetting");
					} else if (key.equals("MAIL")) {
						element.element("key").setText("MAIL_SERVICE");
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var emailAddressElement = valueElement.element("emailAddress");
							if (emailAddressElement != null)
								emailAddressElement.setName("systemAddress");
							var otherInboxPollSettingElement = valueElement.element("otherInboxPollSetting");
							if (otherInboxPollSettingElement != null)
								otherInboxPollSettingElement.setName("inboxPollSetting");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate143(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("guest").setText("false");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("UserInvitations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("inviteAsGuest").setText("false");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("selfRegisterAsGuest").setText("false");
					} else if (key.equals("AUTHENTICATOR")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("createUserAsGuest").setText("false");
					} else if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element connectorElement : valueElement.elements())
								connectorElement.addElement("createUserAsGuest").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate144(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("EMAIL_TEMPLATES")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.element("passwordReset").detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate145(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("MAIL_SERVICE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var inboxPollSettingElement = valueElement.element("inboxPollSetting");
							if (inboxPollSettingElement != null)
								inboxPollSettingElement.addElement("monitorSystemAddressOnly").setText("true");
							var webhookSettingElement = valueElement.element("webhookSetting");
							if (webhookSettingElement != null)
								webhookSettingElement.addElement("monitorSystemAddressOnly").setText("true");
							if (valueElement.attributeValue("class").contains("GmailMailService"))
								valueElement.addElement("systemAddress").setText(valueElement.elementText("accountName").trim());
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate146(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("packManagement").setText("false");
					element.addElement("pendingDelete").setText("false");
					element.addElement("packSetting");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var codePrivilege = element.elementTextTrim("codePrivilege");
					element.addElement("packPrivilege").setText(codePrivilege);
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("packQueries");
					element.addElement("packQuerySubscriptions");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate data to 9.4.0
	private void migrate147(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("jobToken").setText(UUID.randomUUID().toString());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Packs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("version").setName("tag");
					var blobHashElement = element.element("blobHash");
					blobHashElement.setName("data");
					blobHashElement.addAttribute("class", "string");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Packs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("version").setName("tag");
					var blobHashElement = element.element("blobHash");
					blobHashElement.setName("data");
					blobHashElement.addAttribute("class", "string");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PackBlobs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("hash").setName("sha256Hash");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate148(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Dashboards.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (var widgetElement : element.element("widgets").elements())
						widgetElement.addElement("autoHeight").setText("true");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate149(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("MAIL_SERVICE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var inboxPollSettingElement = valueElement.element("inboxPollSetting");
							if (inboxPollSettingElement != null) {
								inboxPollSettingElement.element("monitorSystemAddressOnly").detach();
								inboxPollSettingElement.addElement("additionalTargetAddresses");
							}
							var webhookSettingElement = valueElement.element("webhookSetting");
							if (webhookSettingElement != null) {
								webhookSettingElement.element("monitorSystemAddressOnly").detach();
								webhookSettingElement.addElement("additionalTargetAddresses");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate150(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				var content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.field.supply.FieldSupply",
						"io.onedev.server.model.support.issue.field.instance.FieldInstance");
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.field.supply.Ignore",
						"io.onedev.server.model.support.issue.field.instance.IgnoreValue");
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.field.supply.ScriptingValue",
						"io.onedev.server.model.support.issue.field.instance.ScriptingValue");
				content = StringUtils.replace(content,
						"io.onedev.server.model.support.issue.field.supply.SpecifiedValue",
						"io.onedev.server.model.support.issue.field.instance.SpecifiedValue");
				FileUtils.writeStringToFile(file, content, UTF_8.name());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var threadingReferenceElement = element.element("threadingReference");
					if (threadingReferenceElement != null)
						threadingReferenceElement.setName("messageId");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate151(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Packs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var dataElement = element.element("data");
					if (dataElement != null) {
						if (dataElement.attributeValue("class").contains("MavenData")) {
							dataElement.element("sha256BlobHashes").attributeValue("class", "linked-hash-map");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate152(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Packs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var prerelease = element.elementTextTrim("type").equals("NuGet")
							&& element.elementText("version").contains("-");
					element.addElement("prerelease").setText(String.valueOf(prerelease));
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate153(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Packs.xml")) {
				try {
					var content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content,
							"io.onedev.server.ee.pack.",
							"io.onedev.server.plugin.pack.");
					FileUtils.writeStringToFile(file, content, UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Packs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var groupIdElement = element.element("groupId");
					if (groupIdElement != null) {
						var groupId = groupIdElement.getTextTrim();
						var artifactIdElement = element.element("artifactId");
						if (artifactIdElement != null) {
							var artifactId = artifactIdElement.getTextTrim();
							element.addElement("name").setText(groupId + ":" + artifactId);
							groupIdElement.detach();
							artifactIdElement.detach();
						} else {
							element.addElement("name").setText(groupId + ":<$NONE$>");
							element.addElement("version").setText("<$NONE$>");
							groupIdElement.detach();
						}
					}
					var tagElement = element.element("tag");
					if (tagElement != null) {
						element.addElement("name").setText("default");
						element.addElement("version").setText(tagElement.getTextTrim());
						tagElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Dashboards.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var widgetsElement = element.element("widgets");
					for (var widgetElement : widgetsElement.elements()) {
						if (widgetElement.getName().contains("ProjectOverviewWidget")) {
							widgetElement.addElement("showCodeStats").setText("true");
							widgetElement.addElement("showPullRequestStats").setText("true");
							widgetElement.addElement("showIssueStatus").setText("true");
							widgetElement.addElement("showBuildStatus").setText("true");
							widgetElement.addElement("showPackStats").setText("true");
						}
						widgetElement.detach();
						if (widgetElement.getName().contains("CompositeWidget"))
							continue;

						var newWidgetElement = widgetsElement.addElement("io.onedev.server.model.support.widget.Widget");

						var leftElement = widgetElement.element("left");
						leftElement.detach();
						newWidgetElement.add(leftElement);

						var topElement = widgetElement.element("top");
						topElement.detach();
						newWidgetElement.add(topElement);

						var rightElement = widgetElement.element("right");
						rightElement.detach();
						newWidgetElement.add(rightElement);

						var bottomElement = widgetElement.element("bottom");
						bottomElement.detach();
						newWidgetElement.add(bottomElement);

						var autoHeightElement = widgetElement.element("autoHeight");
						autoHeightElement.detach();
						newWidgetElement.add(autoHeightElement);

						var tabsElement = newWidgetElement.addElement("tabs");
						widgetElement.addAttribute("class", widgetElement.getName());
						widgetElement.setName("tab");
						tabsElement.add(widgetElement);
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate154(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("uploadCache").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 10.1.0
	private void migrate155(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								executorElement.element("cacheTTL").detach();
								if (executorElement.getName().contains("KubernetesExecutor"))
									executorElement.addElement("buildWithPV").setText("false");
							}
						}
					} else if (key.equals("BRANDING")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("url").setText("https://onedev.io");
					} else if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.addElement("corsAllowedOrigins");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	// Migrate to 10.1.1
	private void migrate156(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("AUTHENTICATOR")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var userSearchBaseElement = valueElement.element("userSearchBase");
							if (userSearchBaseElement != null) {
								valueElement.addElement("userSearchBases").addElement("string").setText(userSearchBaseElement.getText().trim());
								userSearchBaseElement.detach();
							}
						}
					} else if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var connectorElement : valueElement.elements()) {
								if (connectorElement.getName().contains("OpenIdConnector")) {
									var requestScopes = "openid email profile";
									var groupsClaim = connectorElement.elementText("groupsClaim");
									if (groupsClaim != null)
										requestScopes += " " + groupsClaim;
									connectorElement.addElement("requestScopes").setText(requestScopes);
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate157(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var connectorElement : valueElement.elements()) {
								if (connectorElement.getName().contains("EntraIdConnector"))
									connectorElement.addElement("retrieveGroups").setText("false");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate158(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (var jobPrivilegeElement : element.element("jobPrivileges").elements()) {
						if (jobPrivilegeElement.elementTextTrim("accessLog").equals("true")) {
							jobPrivilegeElement.addElement("accessPipeline").setText("true");
							var accessibleReportsElement = jobPrivilegeElement.element("accessibleReports");
							if (accessibleReportsElement == null)
								accessibleReportsElement = jobPrivilegeElement.addElement("accessibleReports");
							accessibleReportsElement.setText("*");
						} else {
							jobPrivilegeElement.addElement("accessPipeline").setText("false");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate159(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("ProblemMetrics.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("criticalSeverities").setText("0");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate160(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("ServerDockerExecutor")
										|| executorElement.getName().contains("RemoteDockerExecutor")) {
									executorElement.addElement("dockerBuilder").setText("onedev");
								}
								executorElement.element("shellAccessEnabled").detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate161(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var timeTrackingSettingElement = valueElement.element("timeTrackingSetting");
							if (timeTrackingSettingElement != null) {
								timeTrackingSettingElement.addElement("hoursPerDay").setText("8");
								timeTrackingSettingElement.addElement("daysPerWeek").setText("5");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate162(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().contains(".xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (var whitespaceOptionNode : dom.selectNodes(".//whitespaceOption")) {
					Element whitespaceOptionElement = (Element) whitespaceOptionNode;
					if (whitespaceOptionElement.getText().equals("DEFAULT"))
						whitespaceOptionElement.setText("IGNORE_TRAILING");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate163(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("pipeline").detach();
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("key").setText("<$NullKey$>" + UUID.randomUUID());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Dashboards.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (var showJobNode : dom.selectNodes(".//showJob"))
					showJobNode.detach();
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate164(File dataDir, Stack<Integer> versions) {
		VersionedXmlDoc accessTokensDom;
		File accessTokensFile = new File(dataDir, "AccessTokens.xml");
		accessTokensDom = new VersionedXmlDoc();
		Element listElement = accessTokensDom.addElement("list");
		var accessTokenId = 1;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var ownerId = element.elementTextTrim("id");
					var nameIndex = 1;
					var accessTokensElement = element.element("accessTokens");
					for (var accessTokenElement : accessTokensElement.elements()) {
						var newAccessTokenElement = listElement.addElement("io.onedev.server.model.AccessToken");
						newAccessTokenElement.addAttribute("revision", "0.0");
						newAccessTokenElement.addElement("id").setText(String.valueOf(accessTokenId++));
						newAccessTokenElement.addElement("owner").setText(ownerId);
						newAccessTokenElement.addElement("name").setText("token" + (nameIndex++));
						newAccessTokenElement.addElement("value").setText(accessTokenElement.elementText("value"));
						newAccessTokenElement.addElement("hasOwnerPermissions").setText("true");
						newAccessTokenElement.addElement("createDate").setText(accessTokenElement.elementText("createDate"));
						var expireDateElement = accessTokenElement.element("expireDate");
						if (expireDateElement != null)
							newAccessTokenElement.addElement("expireDate").setText(expireDateElement.getText());
					}
					accessTokensElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
		accessTokensDom.writeToFile(accessTokensFile, true);
	}

	private void migrate165(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Agents.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var cpusElement = element.element("cpus");
					if (cpusElement != null)
						cpusElement.setName("cpuCount");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate166(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var number = Long.parseLong(element.elementText("number"));
					element.addElement("boardPosition").setText(String.valueOf(number * -1));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("LinkSpecs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("showAlways").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate167(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Milestones.xml")) {
				File renamedFile = new File(dataDir, file.getName().replace("Milestones.xml", "Iterations.xml"));
				try {
					FileUtils.moveFile(file, renamedFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var buildSettingElement = element.element("buildSetting");
					for (var secretElement : buildSettingElement.element("jobSecrets").elements()) {
						if (secretElement.element("authorization") == null)
							secretElement.addElement("authorization").setText("on branch(\"**\")");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Iterations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.setName("io.onedev.server.model.Iteration");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						for (var fieldSpecElement : element.element("value").element("fieldSpecs").elements()) {
							if (fieldSpecElement.getName().contains("MilestoneChoiceField"))
								fieldSpecElement.setName("io.onedev.server.model.support.issue.field.spec.IterationChoiceField");
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueSchedules.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("milestone").setName("iteration");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Dashboards.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Node node : dom.selectNodes("//io.onedev.server.ee.dashboard.widgets.MilestoneListWidget")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						element.setName("io.onedev.server.ee.dashboard.widgets.IterationListWidget");
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.ee.dashboard.widgets.BurnDownChartWidget")) {
					if (node instanceof Element) {
						Element element = (Element) node;
						var milestoneNameElement = element.element("milestoneName");
						if (milestoneNameElement != null)
							milestoneNameElement.setName("iterationName");
					}
				}
				for (Node node : dom.selectNodes("//io.onedev.server.ee.dashboard.widgets.BuildListWidget")) {
					if (node instanceof Element)
						((Element) node).addElement("showDuration").setText("false");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueFields.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var typeElement = element.element("type");
					if (typeElement.getTextTrim().equals("Milestone"))
						typeElement.setText("Iteration");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("IssueMilestoneAddData"))
						dataElement.addAttribute("class", "io.onedev.server.model.support.issue.changedata.IssueIterationAddData");
					else if (dataElement.attributeValue("class").contains("IssueMilestoneRemoveData"))
						dataElement.addAttribute("class", "io.onedev.server.model.support.issue.changedata.IssueIterationRemoveData");
					else if (dataElement.attributeValue("class").contains("IssueMilestoneChangeData"))
						dataElement.addAttribute("class", "io.onedev.server.model.support.issue.changedata.IssueIterationChangeData");
					var milestoneElement = dataElement.element("milestone");
					if (milestoneElement != null)
						milestoneElement.setName("iteration");
					var oldMilestonesElement = dataElement.element("oldMilestones");
					if (oldMilestonesElement != null)
						oldMilestonesElement.setName("oldIterations");
					var newMilestonesElement = dataElement.element("newMilestones");
					if (newMilestonesElement != null)
						newMilestonesElement.setName("newIterations");
				}
				dom.writeToFile(file, false);
			}
		}

		for (File file : dataDir.listFiles()) {
			try {
				String content = FileUtils.readFileToString(file, UTF_8);
				content = StringUtils.replace(content,
						"\"Milestone\" is", "\"Iteration\" is");
				content = StringUtils.replace(content,
						"\"Milestone\"  is", "\"Iteration\" is");
				content = StringUtils.replace(content,
						"\"Milestone\"   is", "\"Iteration\" is");
				content = StringUtils.replace(content,
						"\"Milestone\"    is", "\"Iteration\" is");
				content = StringUtils.replace(content,
						"non-pull-request commits", "on branch(\"**\")");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void migrate168(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				var content = StringUtils.replace(
						FileUtils.readFileToString(file, UTF_8),
						"on branch(\"**\")",
						"on branch \"**\"");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private void migrate169(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("ServerDockerExecutor")
										|| executorElement.getName().contains("RemoteDockerExecutor")
										|| executorElement.getName().contains("KubernetesExecutor")) {
									executorElement.addElement("alwaysPullImage").setText("true");
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate170(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var issueSettingElement = element.element("issueSetting");
					var boardSpecsElement = issueSettingElement.element("boardSpecs");
					if (boardSpecsElement != null) {
						for (var boardSpecElement : boardSpecsElement.elements()) {
							for (var displayFieldElement : boardSpecElement.element("displayFields").elements()) {
								if (displayFieldElement.getTextTrim().equals("Milestone"))
									displayFieldElement.setText("Iteration");
							}
						}
					}
					var listFieldsElement = issueSettingElement.element("listFields");
					if (listFieldsElement != null) {
						for (var listFieldElement : listFieldsElement.elements()) {
							if (listFieldElement.getTextTrim().equals("Milestone"))
								listFieldElement.setText("Iteration");
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						for (var boardSpecElement : element.element("value").element("boardSpecs").elements()) {
							for (var displayFieldElement : boardSpecElement.element("displayFields").elements()) {
								if (displayFieldElement.getTextTrim().equals("Milestone"))
									displayFieldElement.setText("Iteration");
							}
						}
						for (var listFieldElement : element.element("value").element("listFields").elements()) {
							if (listFieldElement.getTextTrim().equals("Milestone"))
								listFieldElement.setText("Iteration");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate171(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			try {
				var content = StringUtils.replace(
						FileUtils.readFileToString(file, UTF_8),
						"io.onedev.server.ee.dashboard.widgets.BuildListWidget",
						"io.onedev.server.ee.dashboard.widgets.build.BuildListWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.BurnDownChartWidget",
						"io.onedev.server.ee.dashboard.widgets.iteration.BurnDownChartWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.IssueListWidget",
						"io.onedev.server.ee.dashboard.widgets.issue.IssueListWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.IterationListWidget",
						"io.onedev.server.ee.dashboard.widgets.iteration.IterationListWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.MarkdownBlobWidget",
						"io.onedev.server.ee.dashboard.widgets.markdown.MarkdownBlobWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.MarkdownWidget",
						"io.onedev.server.ee.dashboard.widgets.markdown.MarkdownWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.ProjectListWidget",
						"io.onedev.server.ee.dashboard.widgets.project.ProjectListWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.PullRequestListWidget",
						"io.onedev.server.ee.dashboard.widgets.pullrequest.PullRequestListWidget");
				content = StringUtils.replace(
						content,
						"io.onedev.server.ee.dashboard.widgets.projectoverview.ProjectOverviewWidget",
						"io.onedev.server.ee.dashboard.widgets.project.ProjectOverviewWidget");
				FileUtils.writeStringToFile(file, content, UTF_8);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}

		Map<Long, List<IssueStateHistory>> histories = new HashMap<>();
		var id = 1L;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					var className = dataElement.attributeValue("class");
					if (className.contains("IssueStateChangeData")
							|| className.contains("IssueBatchUpdateData")) {
						var oldState = dataElement.elementText("oldState").trim();
						var newState = dataElement.elementText("newState").trim();
						if (!oldState.equals(newState)) {
							var history = new IssueStateHistory();
							history.setId(Long.parseLong(element.elementText("id")));
							history.setState(newState);
							history.setDate(parseDate(element.elementText("date")));
							history.setTimeGroups(TimeGroups.of(history.getDate()));
							history.setIssue(new Issue());
							history.getIssue().setId(Long.parseLong(element.elementText("issue").trim()));
							histories.computeIfAbsent(history.getIssue().getId(), it -> new ArrayList<>()).add(history);
						}
					}
				}
			} else if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var finishDayElement = element.element("finishDay");
					if (finishDayElement != null)
						finishDayElement.detach();
					var pendingDateElement = element.element("pendingDate");
					var runningDateElement = element.element("runningDate");
					var finishDateElement = element.element("finishDate");
					if (finishDateElement != null) {
						var finishDate = parseDate(finishDateElement.getText().trim());
						var finishTimeGroupsElement = element.addElement("finishTimeGroups");
						finishTimeGroupsElement.addElement("day").setText(String.valueOf(toLocalDate(finishDate).toEpochDay()));
						finishTimeGroupsElement.addElement("week").setText(String.valueOf(toLocalDate(finishDate).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()));
						finishTimeGroupsElement.addElement("month").setText(String.valueOf(toLocalDate(finishDate).with(TemporalAdjusters.firstDayOfMonth()).toEpochDay()));
						if (runningDateElement != null)
							element.addElement("runningDuration").setText(String.valueOf(finishDate.getTime() - parseDate(runningDateElement.getText().trim()).getTime()));
					}
					if (pendingDateElement != null && runningDateElement != null)
						element.addElement("pendingDuration").setText(String.valueOf(parseDate(runningDateElement.getText().trim()).getTime() - parseDate(pendingDateElement.getText().trim()).getTime()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var submitDate = parseDate(element.elementText("submitDate").trim());
					var submitTimeGroupsElement = element.addElement("submitTimeGroups");
					submitTimeGroupsElement.addElement("day").setText(String.valueOf(toLocalDate(submitDate).toEpochDay()));
					submitTimeGroupsElement.addElement("week").setText(String.valueOf(toLocalDate(submitDate).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()));
					submitTimeGroupsElement.addElement("month").setText(String.valueOf(toLocalDate(submitDate).with(TemporalAdjusters.firstDayOfMonth()).toEpochDay()));

					if (!element.elementTextTrim("status").equals("OPEN")) {
						var closeDate = parseDate(element.element("lastActivity").elementText("date").trim());
						element.addElement("closeDate").addAttribute("class", "sql-timestamp").setText(formatDate(closeDate));
						element.addElement("duration").setText(String.valueOf(closeDate.getTime() - submitDate.getTime()));
						var closeTimeGroupsElement = element.addElement("closeTimeGroups");
						closeTimeGroupsElement.addElement("day").setText(String.valueOf(toLocalDate(closeDate).toEpochDay()));
						closeTimeGroupsElement.addElement("week").setText(String.valueOf(toLocalDate(closeDate).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()));
						closeTimeGroupsElement.addElement("month").setText(String.valueOf(toLocalDate(closeDate).with(TemporalAdjusters.firstDayOfMonth()).toEpochDay()));
					}
				}
				dom.writeToFile(file, false);
			}
		}

		VersionedXmlDoc historiesDom;
		File historiesDomFile = new File(dataDir, "IssueStateHistorys.xml");
		historiesDom = new VersionedXmlDoc();
		Element listElement = historiesDom.addElement("list");

		for (var value : histories.values()) {
			value.sort(comparing(IssueStateHistory::getDate));
			for (int i = 0; i < value.size() - 1; i++) {
				var history = value.get(i);
				var nextHistory = value.get(i + 1);
				history.setDuration(nextHistory.getDate().getTime() - history.getDate().getTime());
				migrate171_createHistoryElement(listElement, history);
			}
			if (!value.isEmpty())
				migrate171_createHistoryElement(listElement, value.get(value.size() - 1));
		}
		historiesDom.writeToFile(historiesDomFile, true);
	}

	private static Date parseDate(String dateString) {
		if (dateString.endsWith("Z")) {
			Instant instant = Instant.parse(dateString);
			return Date.from(instant);
		} else {
			OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateString);
			return Date.from(offsetDateTime.toInstant());
		}
	}

	private static String formatDate(Date date) {
		ZonedDateTime zdt = ZonedDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
		return zdt.format(dateTimeFormatter);
	}

	private Element migrate171_createHistoryElement(Element parentElement, IssueStateHistory history) {
		var historyElement = parentElement.addElement("io.onedev.server.model.IssueStateHistory");
		historyElement.addAttribute("revision", "0.0");
		historyElement.addElement("id").setText(history.getId().toString());
		historyElement.addElement("issue").setText(history.getIssue().getId().toString());
		historyElement.addElement("state").setText(history.getState());
		historyElement.addElement("date").addAttribute("class", "sql-timestamp").setText(formatDate(history.getDate()));
		var timeGroupsElement = historyElement.addElement("timeGroups");
		timeGroupsElement.addElement("day").setText(String.valueOf(toLocalDate(history.getDate()).toEpochDay()));
		timeGroupsElement.addElement("week").setText(String.valueOf(toLocalDate(history.getDate()).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()));
		timeGroupsElement.addElement("month").setText(String.valueOf(toLocalDate(history.getDate()).with(TemporalAdjusters.firstDayOfMonth()).toEpochDay()));
		if (history.getDuration() != null)
			historyElement.addElement("duration").setText(history.getDuration().toString());
		return historyElement;
	}

	private void migrate172(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var path = element.elementText("path").trim();
					element.addElement("pathLen").setText(String.valueOf(path.length()));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("notifyOwnEvents").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate173(File dataDir, Stack<Integer> versions) {
		String template;
		try (InputStream is = getClass().getResourceAsStream("migrate173_default_notification.tpl")) {
			Preconditions.checkNotNull(is);
			template = IOUtils.toString(is, UTF_8);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("EMAIL_TEMPLATES")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.addElement("buildNotification").setText(template);
							valueElement.addElement("packNotification").setText(template);
							valueElement.addElement("commitNotification").setText(template);
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate174(File dataDir, Stack<Integer> versions) {
		String initialState = null;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var stateSpecElement : valueElement.element("stateSpecs").elements()) {
								initialState = stateSpecElement.elementText("name").trim();
								break;
							}
						}
					}
				}
			} else if (file.getName().startsWith("IssueStateHistorys.xml")) {
				FileUtils.deleteFile(file);
			}
		}

		Map<Long, List<IssueStateHistory>> histories = new HashMap<>();
		var id = 1L;

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				if (initialState != null) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						var history = new IssueStateHistory();
						history.setId(id++);
						history.setState(initialState);
						history.setDate(parseDate(element.elementText("submitDate")));
						history.setTimeGroups(TimeGroups.of(history.getDate()));
						history.setIssue(new Issue());
						history.getIssue().setId(Long.parseLong(element.elementText("id").trim()));
						histories.computeIfAbsent(history.getIssue().getId(), it -> new ArrayList<>()).add(history);
					}
				}
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					var className = dataElement.attributeValue("class");
					if (className.contains("IssueStateChangeData")
							|| className.contains("IssueBatchUpdateData")) {
						var oldState = dataElement.elementText("oldState").trim();
						var newState = dataElement.elementText("newState").trim();
						if (!oldState.equals(newState)) {
							var history = new IssueStateHistory();
							history.setId(id++);
							history.setState(newState);
							history.setDate(parseDate(element.elementText("date")));
							history.setTimeGroups(TimeGroups.of(history.getDate()));
							history.setIssue(new Issue());
							history.getIssue().setId(Long.parseLong(element.elementText("issue").trim()));
							histories.computeIfAbsent(history.getIssue().getId(), it -> new ArrayList<>()).add(history);
						}
					}
				}
			}
		}

		VersionedXmlDoc historiesDom;
		File historiesDomFile = new File(dataDir, "IssueStateHistorys.xml");
		historiesDom = new VersionedXmlDoc();
		Element listElement = historiesDom.addElement("list");

		for (var value : histories.values()) {
			value.sort(comparing(IssueStateHistory::getDate));
			for (int i = 0; i < value.size() - 1; i++) {
				var history = value.get(i);
				var nextHistory = value.get(i + 1);
				history.setDuration(nextHistory.getDate().getTime() - history.getDate().getTime());
				migrate171_createHistoryElement(listElement, history);
			}
			if (!value.isEmpty())
				migrate171_createHistoryElement(listElement, value.get(value.size() - 1));
		}
		historiesDom.writeToFile(historiesDomFile, true);
	}

	private void migrate175(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var key = element.elementTextTrim("key");
					if (key.equals("SYSTEM")) {
						element.element("value").addElement("disableDashboard").setText("false");
					} else if (key.equals("ISSUE")) {
						var transitionSpecsElement = element.element("value").element("transitionSpecs");
						for (var transitionSpecElement : transitionSpecsElement.elements()) {
							var toStateElement = transitionSpecElement.element("toState");
							var triggerElement = transitionSpecElement.element("trigger");
							var triggerClass = triggerElement.attributeValue("class");
							if (triggerClass.contains("PressButtonTrigger")) {
								transitionSpecElement.addElement("toStates").addElement("string").setText(toStateElement.getText().trim());
								toStateElement.detach();
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.ManualSpec");
							} else if (triggerClass.contains("BranchUpdateTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.BranchUpdatedSpec");
							} else if (triggerClass.contains("BuildSuccessfulTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.BuildSuccessfulSpec");
							} else if (triggerClass.contains("DiscardPullRequestTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.PullRequestDiscardedSpec");
							} else if (triggerClass.contains("MergePullRequestTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.PullRequestMergedSpec");
							} else if (triggerClass.contains("NoActivityTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.NoActivitySpec");
							} else if (triggerClass.contains("OpenPullRequestTrigger")) {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.PullRequestOpenedSpec");
							} else {
								transitionSpecElement.setName("io.onedev.server.model.support.issue.transitionspec.IssueStateTransitedSpec");
							}
							for (var childElement : triggerElement.elements()) {
								if (!childElement.getName().equals("buttonLabel")) {
									childElement.detach();
									transitionSpecElement.add(childElement);
								}
							}
							triggerElement.detach();
						}
						var transitionSpecElement = transitionSpecsElement.addElement("io.onedev.server.model.support.issue.transitionspec.ManualSpec");
						transitionSpecElement.addElement("fromStates");
						transitionSpecElement.addElement("toStates");
						transitionSpecElement.addElement("removeFields");
						transitionSpecElement.addElement("authorizedRoles").addElement("string").setText("Project Owner");
						transitionSpecElement.addElement("promptFields");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate176_timeGroups(Element timeGroupsElement, String dateString) {
		var localDate = toLocalDate(parseDate(dateString));
		timeGroupsElement.clearContent();
		timeGroupsElement.addElement("day").setText(String.valueOf(localDate.toEpochDay()));
		timeGroupsElement.addElement("week").setText(String.valueOf(localDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).toEpochDay()));
		timeGroupsElement.addElement("month").setText(String.valueOf(localDate.with(TemporalAdjusters.firstDayOfMonth()).toEpochDay()));
	}
	
	private void migrate176(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var finishDate = element.elementText("finishDate");
					if (finishDate != null) 
						migrate176_timeGroups(element.element("finishTimeGroups"), finishDate.trim());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate176_timeGroups(element.element("submitTimeGroups"), element.elementText("submitDate").trim());
					var closeDate = element.elementText("closeDate");
					if (closeDate != null) 
						migrate176_timeGroups(element.element("closeTimeGroups"), closeDate.trim());
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueStateHistorys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					migrate176_timeGroups(element.element("timeGroups"), element.elementText("date").trim());
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate177(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) 
					element.addElement("disableWatchNotifications").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate178(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var ssoConnectorElement = element.element("ssoConnector");
					if (ssoConnectorElement != null)
						ssoConnectorElement.detach();
					var passwordElement = element.element("password");
					if (passwordElement.getText().trim().equals("external_managed"))
						passwordElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate179_removeSingleFile(Element element) {
		var compareContextElement = element.element("compareContext");
		if (compareContextElement != null) {
			var currentFileElement = compareContextElement.element("currentFile");
			if (currentFileElement != null)
				currentFileElement.detach();
		}
	}
	
	private void migrate179(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("CodeComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) 
					migrate179_removeSingleFile(element);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrate179_removeSingleFile(element);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentReplys.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrate179_removeSingleFile(element);
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeCommentStatusChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					migrate179_removeSingleFile(element);
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate180(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("guest").detach();
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("UserInvitations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("inviteAsGuest").detach();
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.element("selfRegisterAsGuest").detach();
					} else if (key.equals("AUTHENTICATOR")) {
						Element valueElement = element.element("value");
						if (valueElement != null)
							valueElement.element("createUserAsGuest").detach();
					} else if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element connectorElement : valueElement.elements())
								connectorElement.element("createUserAsGuest").detach();
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate181(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("externalParticipants");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Roles.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) 
					element.addElement("accessTimeTracking").setText("true");
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SYSTEM")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("gravatarEnabled").setName("useAvatarService");
							valueElement.addElement("avatarServiceUrl").setText("https://secure.gravatar.com/avatar/");
						}						
					} else if (key.equals("SERVICE_DESK_SETTING")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("senderAuthorizations").detach();
							var projectDesignationsElement = valueElement.element("projectDesignations"); 
							projectDesignationsElement.setName("defaultProjectSettings");
							for (var projectDesignationElement: projectDesignationsElement.elements()) {
								projectDesignationElement.setName("io.onedev.server.model.support.administration.DefaultProjectSetting");
							}
						}
					} else if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								if (executorElement.getName().contains("KubernetesExecutor")
										|| executorElement.getName().contains("DockerExecutor")) {
									executorElement.element("imageMappings").detach();
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}		
	}

	private void migrate182_reviewRequirement(@Nullable Element reviewRequirementElement) {
		if (reviewRequirementElement != null) {
			String reviewRequirement = reviewRequirementElement.getText();
			reviewRequirement = reviewRequirement.replaceAll("\\)\\s+and\\s+(user|group)", ") $1");
			reviewRequirementElement.setText(reviewRequirement);
		}
	}

	private void migrate182(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("pullRequestSetting").addElement("defaultAssignees");
					for (var branchProtectionElement : element.element("branchProtections").elements()) {
						migrate182_reviewRequirement(branchProtectionElement.element("reviewRequirement"));
						for (Element fileProtectionElement : branchProtectionElement.element("fileProtections").elements()) {
							migrate182_reviewRequirement(fileProtectionElement.element("reviewRequirement"));
						}
					}
					var serviceDeskNameElement = element.element("serviceDeskName");
					if (serviceDeskNameElement.getText().trim().startsWith("<$NullServiceDesk$>"))
						serviceDeskNameElement.detach();
					var keyElement = element.element("key");
					if (keyElement.getText().trim().startsWith("<$NullKey$>"))
						keyElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) 
					element.addElement("autoMerge").addElement("enabled").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate183(File dataDir, Stack<Integer> versions) {
		ParsedEmailAddress systemAddress = null;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("MAIL_SERVICE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							systemAddress = ParsedEmailAddress.parse(valueElement.elementText("systemAddress").trim());
							var inboxPollSettingElement = valueElement.element("inboxPollSetting");
							if (inboxPollSettingElement == null)
								inboxPollSettingElement = valueElement.element("webhookSetting");
							if (inboxPollSettingElement != null) {
								var additionalTargetAddressesElement = inboxPollSettingElement.element("additionalTargetAddresses");
								if (additionalTargetAddressesElement != null)
									additionalTargetAddressesElement.detach();
							}
						}
					} else if (key.equals("SERVICE_DESK_SETTING")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var defaultProjectSettingsElement = valueElement.element("defaultProjectSettings");
							if (defaultProjectSettingsElement != null)
								defaultProjectSettingsElement.detach();
							for (var issueCreationSettingElement: valueElement.element("issueCreationSettings").elements()) {
								var senderEmailsElement = issueCreationSettingElement.element("senderEmails");								
								if (senderEmailsElement != null)
									senderEmailsElement.detach();
							}
						}						
					}
				}
				dom.writeToFile(file, false);
			}
		}
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var serviceDeskNameElement = element.element("serviceDeskName"); 
					if (serviceDeskNameElement != null) {
						if (systemAddress != null)
							element.addElement("serviceDeskEmailAddress").setText(systemAddress.getSubaddress(serviceDeskNameElement.getText().trim()));
						serviceDeskNameElement.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate184(File dataDir, Stack<Integer> versions) {
	}

	private void migrate185(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (var contributedSettingElement: element.element("contributedSettings").elements()) {
						var className = contributedSettingElement.elementText("string").trim();
						if (className.contains("NtfyNotificationSetting") || className.contains("SlackNotificationSetting")
								|| className.contains("DiscordNotificationSetting")) {
							for (var wrapperElement: contributedSettingElement.elements().get(1).element("notifications").elements()) {
								wrapperElement.setName(wrapperElement.getName().replace("ChannelNotificationWrapper", "ChannelNotification"));
								var notificationElement = wrapperElement.element("channelNotification");
								for (var childElement: notificationElement.elements()) {
									childElement.detach();
									wrapperElement.add(childElement);
								}
								notificationElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate186(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Builds.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("submitSequence").setText("1");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate187(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Dashboards.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (var widgetElement : element.element("widgets").elements()) {
						widgetElement.element("autoHeight").detach();
						for (var tabElement: widgetElement.element("tabs").elements()) {
							if (tabElement.getName().endsWith("CompositeWidget")) {
								tabElement.element("tabs").detach();
								tabElement.setName("io.onedev.server.ee.dashboard.widgets.markdown.MarkdownWidget");
								tabElement.addElement("markdown").setText("Composite widget is removed, please add child tabs to top level instead");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate188(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("thumbsUpCount").setText("0");
					element.addElement("thumbsDownCount").setText("0");
					element.addElement("smileCount").setText("0");
					element.addElement("tadaCount").setText("0");
					element.addElement("confusedCount").setText("0");
					element.addElement("heartCount").setText("0");
					element.addElement("rocketCount").setText("0");
					element.addElement("eyesCount").setText("0");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("thumbsUpCount").setText("0");
					element.addElement("thumbsDownCount").setText("0");
					element.addElement("smileCount").setText("0");
					element.addElement("tadaCount").setText("0");
					element.addElement("confusedCount").setText("0");
					element.addElement("heartCount").setText("0");
					element.addElement("rocketCount").setText("0");
					element.addElement("eyesCount").setText("0");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate189(File dataDir, Stack<Integer> versions) {
		Map<String, String> pullRequestIdToProjectId = new HashMap<>();
		Map<String, String> codeCommentIdToProjectId = new HashMap<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					pullRequestIdToProjectId.put(element.elementTextTrim("id"), element.elementTextTrim("targetProject"));
					element.element("noSpaceTitle").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("CodeComments.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					codeCommentIdToProjectId.put(element.elementTextTrim("id"), element.elementTextTrim("project")); 
				}
			} else if (file.getName().startsWith("Issues.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("noSpaceTitle").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Projects.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var pendingDeleteElement = element.element("pendingDelete");
					if (pendingDeleteElement != null)
						pendingDeleteElement.detach();
				}
				dom.writeToFile(file, false);
			}
		}

		VersionedXmlDoc pullRequestTouchsDom;
		File pullRequestTouchsDomFile = new File(dataDir, "PullRequestTouchs.xml");
		pullRequestTouchsDom = new VersionedXmlDoc();
		Element listElement = pullRequestTouchsDom.addElement("list");

		long touchId = 1;
		for (var entry : pullRequestIdToProjectId.entrySet()) {
			var pullRequestTouchElement = listElement.addElement("io.onedev.server.model.PullRequestTouch");
			pullRequestTouchElement.addAttribute("revision", "0.0.0");
			pullRequestTouchElement.addElement("project").setText(entry.getValue());
			pullRequestTouchElement.addElement("requestId").setText(entry.getKey());
			pullRequestTouchElement.addElement("id").setText(String.valueOf(touchId++));
		}
		pullRequestTouchsDom.writeToFile(pullRequestTouchsDomFile, true);

		VersionedXmlDoc codeCommentTouchsDom;
		File codeCommentTouchsDomFile = new File(dataDir, "CodeCommentTouchs.xml");
		codeCommentTouchsDom = new VersionedXmlDoc();
		listElement = codeCommentTouchsDom.addElement("list");

		touchId = 1;
		for (var entry : codeCommentIdToProjectId.entrySet()) {
			var codeCommentTouchElement = listElement.addElement("io.onedev.server.model.CodeCommentTouch");
			codeCommentTouchElement.addAttribute("revision", "0.0.0");
			codeCommentTouchElement.addElement("project").setText(entry.getValue());
			codeCommentTouchElement.addElement("commentId").setText(entry.getKey());
			codeCommentTouchElement.addElement("id").setText(String.valueOf(touchId++));
		}
		codeCommentTouchsDom.writeToFile(codeCommentTouchsDomFile, true);
	}

	private void migrate190(File dataDir, Stack<Integer> versions) {
		List<Pair<File, VersionedXmlDoc>> doms = new ArrayList<>();
		List<Pair<Element, Date>> histories = new ArrayList<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueStateHistorys.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				doms.add(new Pair<>(file, dom));
				for (Element element : dom.getRootElement().elements()) {
					histories.add(new Pair<>(element, parseDate(element.elementText("date").trim())));
				}
			}
		}
		Collections.sort(histories, (h1, h2) -> h1.getRight().compareTo(h2.getRight()));
		var index = 1;
		for (var history: histories) {
			history.getLeft().element("id").setText(String.valueOf(index++));
		}
		for (var entry: doms) {
			entry.getRight().writeToFile(entry.getLeft(), false);
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Dashboards.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					for (var widgetElement : element.element("widgets").elements()) {
						for (var tabElement: widgetElement.element("tabs").elements()) {
							if (tabElement.getName().endsWith("BuildDurationStatsWidget") 
									|| tabElement.getName().endsWith("BuildFrequencyStatsWidget")) {
								var buildQueryElement = tabElement.element("buildQuery");
								if (buildQueryElement != null)
									buildQueryElement.detach();
								tabElement.addElement("displayMonths").setText("6");
							} else if (tabElement.getName().endsWith("IssueStateDurationStatsWidget")) {
								var issueQueryElement = tabElement.element("issueQuery");
								if (issueQueryElement != null)
									issueQueryElement.detach();
								tabElement.addElement("displayMonths").setText("6");
							} else if (tabElement.getName().endsWith("IssueStateFrequencyStatsWidget")) {
								var issueQueryElement = tabElement.element("issueQuery");
								if (issueQueryElement != null)
									issueQueryElement.detach();
								tabElement.addElement("displayMonths").setText("6");
								tabElement.addElement("excludeStates");
							} else if (tabElement.getName().endsWith("PullRequestDurationStatsWidget")
									|| tabElement.getName().endsWith("PullRequestFrequencyStatsWidget")) {
								var pullRequestQueryElement = tabElement.element("pullRequestQuery");
								if (pullRequestQueryElement != null)
									pullRequestQueryElement.detach();
								tabElement.addElement("displayMonths").setText("6");
							}							
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}

		var packBlobsFile = new File(dataDir, "PackBlobs.xml");
		if (packBlobsFile.exists()) {
			var packBlobsDom = VersionedXmlDoc.fromFile(packBlobsFile);

			Map<String, Map<String, String>> packBlobIds = new HashMap<>();		
			Map<String, Pair<String, String>> packBlobIdToProjectIdAndHash = new HashMap<>();
			Map<String, String> packIdToProjectId = new HashMap<>();
			int nextPackBlobId = 1;
			String packStorePath = null;
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("PackBlobAuthorizations.xml")) {
					FileUtils.deleteFile(file);
				} else if (file.getName().startsWith("PackBlobs.xml")) {
					var dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						var packBlobId = element.elementTextTrim("id");
						var projectId = element.elementTextTrim("project");
						var hash = element.elementTextTrim("sha256Hash");
						nextPackBlobId = Math.max(nextPackBlobId, Integer.parseInt(packBlobId) + 1);
						packBlobIdToProjectIdAndHash.put(packBlobId, new Pair<>(projectId, hash));
						packBlobIds.computeIfAbsent(hash, k -> new HashMap<>()).put(projectId, packBlobId);
					}
				} else if (file.getName().startsWith("Packs.xml")) {
					var dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						packIdToProjectId.put(element.elementTextTrim("id"), element.elementTextTrim("project"));
					}
				} else if (file.getName().startsWith("Settings.xml")) {
					var dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						if (element.elementTextTrim("key").equals("CONTRIBUTED_SETTINGS")) {
							var valueElement = element.element("value");
							if (valueElement != null) {
								for (var entryElement: valueElement.elements()) {
									var storageSettingElement = entryElement.element("io.onedev.server.ee.storage.StorageSetting");
									if (storageSettingElement != null) {
										var packStoreElement = storageSettingElement.element("packStore");
										if (packStoreElement != null)
											packStorePath = packStoreElement.getText().trim();
									}
								}
							}
						}
					}
				}
			}

			var projectsDir = new File(dataDir.getParentFile().getParentFile().getParentFile(), "site/projects");
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("PackBlobReferences.xml")) {
					var dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						var packId = element.elementTextTrim("pack");
						var packBlobElement = element.element("packBlob");
						var packBlobId = packBlobElement.getTextTrim();
						var targetProjectId = packIdToProjectId.get(packId);
						var packBlobProjectIdAndHash = packBlobIdToProjectIdAndHash.get(packBlobId);
						if (targetProjectId.equals(packBlobProjectIdAndHash.getLeft()))
							continue;
						var hash = packBlobProjectIdAndHash.getRight();
						var projectIdToPackBlobId = packBlobIds.get(hash);
						var packBlobCopyId = projectIdToPackBlobId.get(targetProjectId);
						if (packBlobCopyId == null) {
							packBlobCopyId = String.valueOf(nextPackBlobId++);							
							projectIdToPackBlobId.put(targetProjectId, packBlobCopyId);
							var projectDir = new File(projectsDir, packBlobProjectIdAndHash.getLeft());
							var relativeBlobFilePath = hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash;
							var packBlobFile = new File(projectDir, "packages/" + relativeBlobFilePath);
							if (!packBlobFile.exists()) {
								var errorMessage = String.format(
									"Unable to find pack blob file for migration (blob file: %s, referencing project: %s, referencing pack: %s)", 
									packBlobFile.getAbsolutePath(), targetProjectId, packId);
								throw new ExplicitException(errorMessage);
							}
							var targetProjectDir = new File(projectsDir, targetProjectId);
							var targetPackagesDir = new File(targetProjectDir, "packages");
							if (!targetPackagesDir.exists() && packStorePath != null) {
								var symlinkTargetDir = new File(packStorePath, targetProjectId);
								FileUtils.createDir(symlinkTargetDir);
								try {
									Files.createSymbolicLink(targetPackagesDir.toPath(), symlinkTargetDir.toPath());
								} catch (IOException e) {
									throw new RuntimeException(e);
								}
							}
							var targetPackBlobFile = new File(targetPackagesDir, relativeBlobFilePath);
							FileUtils.createDir(targetPackBlobFile.getParentFile());
							try {
								FileUtils.copyFile(packBlobFile, targetPackBlobFile);
							} catch (IOException e) {
								throw new RuntimeException(e);
							}
		
							var targetProjectPath = targetProjectDir.toPath();	
							var currentPath = targetPackBlobFile.getParentFile().toPath();
							while (currentPath.startsWith(targetProjectPath)) {
								var currentDir = currentPath.toFile();
								DirectoryVersionUtils.increaseVersion(currentDir);
								currentPath = currentPath.getParent();
							}
					
							var targetPackBlobElement = packBlobsDom.getRootElement().addElement("io.onedev.server.model.PackBlob");
							targetPackBlobElement.addAttribute("revision", "0.0");
							targetPackBlobElement.addElement("id").setText(packBlobCopyId);
							targetPackBlobElement.addElement("project").setText(targetProjectId);
							targetPackBlobElement.addElement("sha256Hash").setText(hash);
							targetPackBlobElement.addElement("size").setText(String.valueOf(packBlobFile.length()));
							targetPackBlobElement.addElement("createDate").addAttribute("class", "sql-timestamp").setText(formatDate(new Date()));
						}
						packBlobElement.setText(packBlobCopyId);
					}
					dom.writeToFile(file, false);
				}
			}
			packBlobsDom.writeToFile(packBlobsFile, false);
		}
	}

	private void migrate191(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				var dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SSO_CONNECTORS")) {
						var valueElement = element.element("value");
						if (valueElement != null) {
							for (Element connectorElement : valueElement.elements()) {
								var buttonImageUrlElement = connectorElement.element("buttonImageUrl");
								if (buttonImageUrlElement != null) {
									var buttonImageUrl = buttonImageUrlElement.getText().trim();
									if (buttonImageUrl.startsWith("/wicket/resource/io.onedev.server.plugin.sso.openid.openidconnector")) {
										buttonImageUrl = "/wicket/resource/io.onedev.server.plugin.sso.openid.OpenIdConnector/openid.png";
										buttonImageUrlElement.setText(buttonImageUrl);
									}
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate192(File dataDir, Stack<Integer> versions) {
		var updateIds = new HashSet<>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("IssueReferencedFromCommitData"))
						element.detach();
					else if (element.element("user") == null)
						element.addElement("user").setText(String.valueOf(User.SYSTEM_ID));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					if (dataElement.attributeValue("class").contains("PullRequestReferencedFromCommitData"))
						element.detach();
					else if (element.element("user") == null)
						element.addElement("user").setText(String.valueOf(User.SYSTEM_ID));
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("LinkSpecs.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.element("showAlways").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate193(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) 
					element.element("disableWatchNotifications").detach();
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate194(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var timeTrackingSettingElement = valueElement.element("timeTrackingSetting");
							if (timeTrackingSettingElement != null) {
								timeTrackingSettingElement.addElement("useHoursAndMinutesOnly").setText("true");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate195(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (!element.elementText("status").equals("OPEN")) {
						if (element.element("closeDate") == null) {
							element.addElement("closeDate").addAttribute("class", "sql-timestamp").setText(element.element("submitDate").getText().trim());
						}
						var closeTimeGroupsElement = element.element("closeTimeGroups");
						if (closeTimeGroupsElement == null) {
							closeTimeGroupsElement = element.addElement("closeTimeGroups");
							var submitTimeGroupsElement = element.element("submitTimeGroups");
							closeTimeGroupsElement.addElement("day").setText(submitTimeGroupsElement.elementText("day"));
							closeTimeGroupsElement.addElement("week").setText(submitTimeGroupsElement.elementText("week"));
							closeTimeGroupsElement.addElement("month").setText(submitTimeGroupsElement.elementText("month"));	
						}
						var durationElement = element.element("duration");
						if (durationElement == null) {
							element.addElement("duration").setText("0");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate196(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("serviceAccount").setText("false");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueFields.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var typeElement = element.element("type");
					if (typeElement.getText().trim().equals("Working Period")) {
						typeElement.setText("Integer");
						var valueElement = element.element("value");
						var ordinalElement = element.element("ordinal");
						if (valueElement != null) {
							valueElement.setText(ordinalElement.getText().trim());
						}
						ordinalElement.setText("-1");
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				try {
					String content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content,
							"io.onedev.server.model.support.issue.field.spec.WorkingPeriodField",
							"io.onedev.server.model.support.issue.field.spec.IntegerField");
					content = StringUtils.replace(content,
							"io.onedev.server.buildspecmodel.inputspec.workingperiodinput",
							"io.onedev.server.buildspecmodel.inputspec.integerinput");
					FileUtils.writeFile(file, content, UTF_8);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

	private void migrate197(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Users.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("disabled").setText("false");
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate198(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueFields.xml") || file.getName().startsWith("BuildParams.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var type = element.elementText("type").trim();
					var valueElement = element.element("value");
					if (valueElement != null) {
						if (type.equals(InputSpec.DATE)) {
							valueElement.setText(String.valueOf(DateUtils.parseDate(valueElement.getText().trim(), ZoneId.systemDefault(), 12, 0, 0).getTime()));
						} else if (type.equals(InputSpec.DATE_TIME)) {
							valueElement.setText(String.valueOf(DateUtils.parseDateTime(valueElement.getText().trim(), ZoneId.systemDefault()).getTime()));
						}
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Iterations.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var startDateElement = element.element("startDate");
					if (startDateElement != null) {
						element.addElement("startDay").setText(String.valueOf(toLocalDate(parseDate(startDateElement.getText().trim()), ZoneId.systemDefault()).toEpochDay()));
						startDateElement.detach();
					}	
					var dueDateElement = element.element("dueDate");
					if (dueDateElement != null) {
						element.addElement("dueDay").setText(String.valueOf(toLocalDate(parseDate(dueDateElement.getText().trim()), ZoneId.systemDefault()).toEpochDay()));
						dueDateElement.detach();
					}
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueWorks.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("day").detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) 
							valueElement.addElement("externalIssueTransformers").addElement("entries");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate199(File dataDir, Stack<Integer> versions) {
		String defaultGroupName = null;
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("SECURITY")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var defaultLoginGroupNameElement = valueElement.element("defaultLoginGroupName");
							if (defaultLoginGroupNameElement != null) {
								defaultGroupName = defaultLoginGroupNameElement.getText().trim();
								defaultLoginGroupNameElement.setName("defaultGroupName");
								break;
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
		String defaultLoginGroupId = null;
		if (defaultGroupName != null) {
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Groups.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						if (element.elementText("name").trim().equals(defaultGroupName)) {
							defaultLoginGroupId = element.elementText("id").trim();
							break;
						}
					}
				}
			}
		}
		if (defaultLoginGroupId != null) {			
			long maxMembershipId = 0L;
			int maxFileIndex = 0;
			Set<String> userIdsInDefaultLoginGroup = new HashSet<>();
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Memberships.xml")) {
					var suffix = file.getName().substring("Memberships.xml".length());					
					int fileIndex;
					if (suffix.contains(".")) {
						fileIndex = Integer.parseInt(StringUtils.substringAfter(suffix, "."));
					} else {
						fileIndex = 1;
					}
					maxFileIndex = Math.max(maxFileIndex, fileIndex);

					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						maxMembershipId = Math.max(maxMembershipId, Long.parseLong(element.elementTextTrim("id")));
						if (element.elementTextTrim("group").equals(defaultLoginGroupId)) 
							userIdsInDefaultLoginGroup.add(element.elementTextTrim("user"));
					}
				}
			}
			Set<String> userIdsNotInDefaultLoginGroup = new HashSet<>();
			for (File file : dataDir.listFiles()) {
				if (file.getName().startsWith("Users.xml")) {
					VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
					for (Element element : dom.getRootElement().elements()) {
						var userId = element.elementTextTrim("id");						
						if (!userId.startsWith("-") && !userIdsInDefaultLoginGroup.contains(userId))
							userIdsNotInDefaultLoginGroup.add(userId);
					}
				}
			}

			VersionedXmlDoc membershipsDom;
			File membershipsFile;
			if (maxFileIndex == 0) 
				membershipsFile = new File(dataDir, "Memberships.xml");
			else
				membershipsFile = new File(dataDir, "Memberships.xml." + (maxFileIndex+1));

			membershipsDom = new VersionedXmlDoc();
			var listElement = membershipsDom.addElement("list");
		
			for (String userId: userIdsNotInDefaultLoginGroup) {
				var membershipElement = listElement.addElement("io.onedev.server.model.Membership");
				membershipElement.addAttribute("revision", "0.0.0");
				membershipElement.addElement("user").setText(userId);
				membershipElement.addElement("group").setText(defaultLoginGroupId);
				membershipElement.addElement("id").setText(String.valueOf(++maxMembershipId));
			}
			membershipsDom.writeToFile(membershipsFile, true);
		}
	}

	private void migrate200(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Issues.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("descriptionRevisionCount").setText("0");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("descriptionRevisionCount").setText("0");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("revisionCount").setText("0");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("PullRequestComments.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.addElement("revisionCount").setText("0");
				}
				dom.writeToFile(file, false);
			}
		}
	}
	
	private void migrate201(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("EmailAddresss.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements())
					element.addElement("open").setText("false");
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate202(File dataDir, Stack<Integer> versions) {
		var fieldTypes = new HashMap<String, String>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					if (element.elementTextTrim("key").equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element fieldSpecElement : valueElement.element("fieldSpecs").elements()) {
								var name = fieldSpecElement.elementText("name").trim();
								switch (fieldSpecElement.getName()) {
									case "io.onedev.server.model.support.issue.field.spec.choicefield.ChoiceField":
										fieldTypes.put(name, InputSpec.ENUMERATION);
										break;
									case "io.onedev.server.model.support.issue.field.spec.IntegerField":
										fieldTypes.put(name, InputSpec.INTEGER);
										break;
									case "io.onedev.server.model.support.issue.field.spec.FloatField":
										fieldTypes.put(name, InputSpec.FLOAT);
										break;
									case "io.onedev.server.model.support.issue.field.spec.BooleanField":
										fieldTypes.put(name, InputSpec.BOOLEAN);
										break;
									case "io.onedev.server.model.support.issue.field.spec.DateField":
										fieldTypes.put(name, InputSpec.DATE);
										break;
									case "io.onedev.server.model.support.issue.field.spec.DateTimeField":
										fieldTypes.put(name, InputSpec.DATE_TIME);
										break;
									case "io.onedev.server.model.support.issue.field.spec.SecretField":
										fieldTypes.put(name, InputSpec.SECRET);
										break;
									case "io.onedev.server.model.support.issue.field.spec.userchoicefield.UserChoiceField":
										fieldTypes.put(name, InputSpec.USER);
										break;
									case "io.onedev.server.model.support.issue.field.spec.GroupChoiceField":
										fieldTypes.put(name, InputSpec.GROUP);
										break;
									case "io.onedev.server.model.support.issue.field.spec.BuildChoiceField":
										fieldTypes.put(name, InputSpec.BUILD);
										break;
									case "io.onedev.server.model.support.issue.field.spec.PullRequestChoiceField":
										fieldTypes.put(name, InputSpec.PULL_REQUEST);
										break;
									case "io.onedev.server.model.support.issue.field.spec.CommitField":
										fieldTypes.put(name, InputSpec.COMMIT);
										break;
									case "io.onedev.server.model.support.issue.field.spec.IssueChoiceField":
										fieldTypes.put(name, InputSpec.ISSUE);
										break;
									case "io.onedev.server.model.support.issue.field.spec.IterationChoiceField":
										fieldTypes.put(name, InputSpec.ITERATION);
										break;
									case "io.onedev.server.model.support.issue.field.spec.TextField":
										fieldTypes.put(name, InputSpec.TEXT);
										break;
								}
							}
						}
					}
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("IssueChanges.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					Element dataElement = element.element("data");
					var dataClass = dataElement.attributeValue("class");
					if (dataClass.contains("IssueBatchUpdateData") || dataClass.contains("IssueFieldChangeData")
							|| dataClass.contains("IssueStateChangeData")) {
						for (Element entryElement : dataElement.element("oldFields").elements()) {
							var inputElement = entryElement.element("io.onedev.server.util.Input");
							var nameElement = inputElement.element("name");
							if (nameElement != null) 
								inputElement.addElement("type").setText(fieldTypes.getOrDefault(nameElement.getText().trim(), "Unknown"));
						}
						for (Element entryElement : dataElement.element("newFields").elements()) {
							var inputElement = entryElement.element("io.onedev.server.util.Input");
							var nameElement = inputElement.element("name");
							if (nameElement != null) 
								inputElement.addElement("type").setText(fieldTypes.getOrDefault(nameElement.getText().trim(), "Unknown"));
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate203(File dataDir, Stack<Integer> versions) {
		VersionedXmlDoc baseAuthorizationsDom = new VersionedXmlDoc();
		Element baseAuthorizationsListElement = baseAuthorizationsDom.addElement("list");		
		long baseAuthorizationId = 1L;
		
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var defaultRoleElement = element.element("defaultRole");
					if (defaultRoleElement != null) {
						var baseAuthorizationElement = baseAuthorizationsListElement.addElement("io.onedev.server.model.BaseAuthorization");
						baseAuthorizationElement.addAttribute("revision", "0.0");
						baseAuthorizationElement.addElement("id").setText(String.valueOf(baseAuthorizationId++));
						baseAuthorizationElement.addElement("project").setText(element.elementText("id").trim());
						baseAuthorizationElement.addElement("role").setText(defaultRoleElement.getText().trim());
						defaultRoleElement.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}
		if (baseAuthorizationId > 1) {
			baseAuthorizationsDom.writeToFile(new File(dataDir, "BaseAuthorizations.xml"), false);
		}
	}

	private void migrate204(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("JOB_EXECUTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (Element executorElement : valueElement.elements()) {
								var jobRequirementElement = executorElement.element("jobRequirement");
								if (jobRequirementElement != null)
									jobRequirementElement.setName("jobMatch");
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}		
	}

	private void migrate205(File dataDir, Stack<Integer> versions) {
	}

	private void migrate206(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SYSTEM_UUID")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.setText(OneDev.getInstance(SettingService.class).encryptUUID(valueElement.getText().trim()));
						}
					} else if (key.equals("ALERT")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							valueElement.element("trialSubscriptionExpireInOneWeekAlerted").detach();
							valueElement.element("trialSubscriptionExpiredAlerted").detach();
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}		
	}

	private void migrate207(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.element("lastEventDate").setName("lastActivityDate");
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("ProjectLastEventDates.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					element.setName("io.onedev.server.model.ProjectLastActivityDate");
					var commitElement = element.element("commit");
					if (commitElement != null) 
						commitElement.detach();
					element.element("activity").setName("value");
				}
				dom.writeToFile(new File(dataDir, file.getName().replace("ProjectLastEventDates", "ProjectLastActivityDates")), false);
			}
		}
	}

	private void migrate208(File dataDir, Stack<Integer> versions) {		
		VersionedXmlDoc ssoProvidersDom = new VersionedXmlDoc();
		Element ssoProvidersListElement = ssoProvidersDom.addElement("list");		
		long ssoProviderId = 1L;

		var groupIds = new HashMap<String, String>();
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Groups.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					groupIds.put(element.elementText("name").trim(), element.elementText("id").trim());
				}
			} else if (file.getName().startsWith("PullRequests.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var autoMergeUserElement = element.element("autoMerge").element("user");
					if (autoMergeUserElement != null) 
						autoMergeUserElement.detach();
				}
				dom.writeToFile(file, false);
			} else if (file.getName().startsWith("IssueChanges.xml")) {
				try {
					var content = FileUtils.readFileToString(file, UTF_8);
					content = StringUtils.replace(content,
							"io.onedev.server.util.Input",
							"io.onedev.server.buildspecmodel.inputspec.Input");
					FileUtils.writeStringToFile(file, content, UTF_8.name());
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}

		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("SSO_CONNECTORS")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							for (var connectorElement : valueElement.elements()) {
								var connectorClass = connectorElement.getName();
								var nameElement = connectorElement.element("name");
								var defaultGroupElement = connectorElement.element("defaultGroup");

								var ssoProviderElement = ssoProvidersListElement.addElement("io.onedev.server.model.SsoProvider");
								ssoProviderElement.addAttribute("revision", "0.0");
								ssoProviderElement.addElement("id").setText(String.valueOf(ssoProviderId++));
								nameElement.detach();
								ssoProviderElement.add(nameElement);
								if (defaultGroupElement != null) {
									var groupId = groupIds.get(defaultGroupElement.getText().trim());
									if (groupId != null)
										ssoProviderElement.addElement("defaultGroup").setText(groupId);
									defaultGroupElement.detach();
								}
								connectorElement.setName("connector");
								connectorElement.addAttribute("class", connectorClass);
								connectorElement.detach();
								ssoProviderElement.add(connectorElement);
							}
						}
						element.detach();
					}
				}
				dom.writeToFile(file, false);
			}
		}

		if (ssoProviderId > 1) {
			ssoProvidersDom.writeToFile(new File(dataDir, "SsoProviders.xml"), false);
		}
	}

	private void migrate209(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("BRANDING")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element urlElement = valueElement.element("url");
							if (urlElement != null) {
								urlElement.detach();
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate210(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					String key = element.elementTextTrim("key");
					if (key.equals("ISSUE")) {
						Element valueElement = element.element("value");
						if (valueElement != null) {
							Element transitionSpecsElement = valueElement.element("transitionSpecs");
							for (Element transitionSpecElement : transitionSpecsElement.elements()) {
								String className = transitionSpecElement.getName();
								if (className.contains("IssueStateTransitedSpec")) {
									Element statesElement = transitionSpecElement.element("states");
									if (statesElement != null) {
										statesElement.detach();
									}
								}
							}
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate211(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : projectElement.element("branchProtections").elements()) {
						Element userMatchElement = branchProtectionElement.element("userMatch");
						if (userMatchElement == null) {
							branchProtectionElement.addElement("userMatch").setText("anyone");
						}
					}
					for (Element tagProtectionElement : projectElement.element("tagProtections").elements()) {
						Element userMatchElement = tagProtectionElement.element("userMatch");
						if (userMatchElement == null) {
							tagProtectionElement.addElement("userMatch").setText("anyone");
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate212(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Settings.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element element : dom.getRootElement().elements()) {
					var keyElement = element.element("key");
					if (keyElement.getTextTrim().equals("MAIL_SERVICE")) {
						keyElement.setText("MAIL");
						Element valueElement = element.element("value");
						if (valueElement != null) {
							var className = valueElement.attributeValue("class");
							className = className.replace(".mailservice.", ".mail.");
							className = className.replace("Office365MailService", "Office365Connector");
							className = className.replace("GmailMailService", "GmailConnector");
							className = className.replace("SmtpImapMailService", "SmtpImapConnector");
							className = className.replace("SendgridMailService", "SendgridConnector");
							valueElement.addAttribute("class", className);
						}
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}

	private void migrate213(File dataDir, Stack<Integer> versions) {
		for (File file : dataDir.listFiles()) {
			if (file.getName().startsWith("Projects.xml")) {
				VersionedXmlDoc dom = VersionedXmlDoc.fromFile(file);
				for (Element projectElement : dom.getRootElement().elements()) {
					for (Element branchProtectionElement : projectElement.element("branchProtections").elements()) {
						branchProtectionElement.addElement("disallowedFileTypes");
					}
					for (Element tagProtectionElement : projectElement.element("tagProtections").elements()) {
						tagProtectionElement.addElement("disallowedFileTypes");
					}
				}
				dom.writeToFile(file, false);
			}
		}
	}
}
