package io.onedev.server.git.service;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.*;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.*;
import io.onedev.server.git.command.*;
import io.onedev.server.git.exception.NotFileException;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.*;
import org.eclipse.jgit.revwalk.*;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReadWriteLock;

import static io.onedev.server.git.command.LogCommand.Field.*;

@Singleton
public class DefaultGitService implements GitService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultGitService.class);
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final SessionManager sessionManager;
	
	private final ClusterManager clusterManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultGitService(ProjectManager projectManager, SettingManager settingManager, 
							 SessionManager sessionManager, ClusterManager clusterManager, 
							 ListenerRegistry listenerRegistry) {
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.settingManager = settingManager;
		this.clusterManager = clusterManager;
		this.listenerRegistry = listenerRegistry;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GitService.class);
	}

	private Repository getRepository(Long projectId) {
		return projectManager.getRepository(projectId);
	}
	
	private File getGitDir(Long projectId) {
		return projectManager.getGitDir(projectId);
	}

	private <T> T runOnProjectServer(Long projectId, ClusterTask<T> task) {
		return projectManager.runOnActiveServer(projectId, task);
	}
	
	private LineConsumer newInfoLogger() {
		return new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.debug(line);
			}
			
		};
	}

	private LineConsumer newErrorLogger() {
		return new LineConsumer() {

			@Override
			public void consume(String line) {
				logger.error(line);
			}
			
		};
	}
	
	@Override
	public String getDefaultBranch(Project project) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> GitUtils.getDefaultBranch(getRepository(projectId)));
	}

	@Override
	public void setDefaultBranch(Project project, String defaultBranch) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, () -> {
			GitUtils.setDefaultBranch(getRepository(projectId), defaultBranch);
			return null;
		});
		listenerRegistry.post(new DefaultBranchChanged(project, defaultBranch));
	}
	
	@Override
	public ObjectId resolve(Project project, String revision, boolean errorIfInvalid) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> GitUtils.resolve(getRepository(projectId), revision, errorIfInvalid));
	}

	@Override
	public RevCommit getCommit(Project project, ObjectId revId) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
				return GitUtils.parseCommit(revWalk, revId);
			}
		});
	}

	@Override
	public List<RevCommit> getCommits(Project project, List<ObjectId> revIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			List<RevCommit> commits = new ArrayList<>();
			try (var revWalk = new RevWalk(getRepository(projectId))) {
				for (var revId: revIds)
					commits.add(revWalk.parseCommit(revId));
			}
			return commits;
		});
	}
	
	@Override
	public int getMode(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
				RevCommit commit = revWalk.parseCommit(revId);
				TreeWalk treeWalk = TreeWalk.forPath(getRepository(projectId), path, commit.getTree());
				if (treeWalk != null) 
					return treeWalk.getRawMode(0);
				else 
					return 0;
			}
		});
	}

	@Sessional
	@Override
	public ObjectId createBranch(Project project, String branchName, String branchRevision) {
		ObjectId revId = project.getObjectId(branchRevision, true);
		Long projectId = project.getId();
		ObjectId commitId = runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)){
				CreateBranchCommand command = Git.wrap(repository).branchCreate();
				command.setName(branchName);
				RevCommit commit = revWalk.parseCommit(revId);
				command.setStartPoint(commit);
				command.call();
				return commit.copy();
			} catch (GitAPIException e) {
				throw new RuntimeException(e);
			}
			
		});
		project.cacheObjectId(branchName, commitId);
		listenerRegistry.post(new RefUpdated(project, 
				GitUtils.branch2ref(branchName), ObjectId.zeroId(), commitId));
		return commitId;
	}

	@Sessional
	@Override
	public TaggingResult createTag(Project project, String tagName, String tagRevision, 
			PersonIdent taggerIdent, String tagMessage, boolean signRequired) {
		Long projectId = project.getId();
		ObjectId revId = project.getObjectId(tagRevision, true);
		
		TaggingResult tagAndCommitId = runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (	RevWalk revWalk = new RevWalk(getRepository(projectId)); 
					ObjectInserter inserter = getRepository(projectId).newObjectInserter();) {
				TagBuilder tagBuilder = new TagBuilder();
				tagBuilder.setTag(tagName);
				if (tagMessage != null) {
					if (!tagMessage.endsWith("\n"))
						tagBuilder.setMessage(tagMessage + "\n");
					else
						tagBuilder.setMessage(tagMessage);
				}
				tagBuilder.setTagger(taggerIdent);
				
				RevCommit commit = revWalk.parseCommit(revId);
				tagBuilder.setObjectId(commit);

				PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
				if (signingKey != null) { 
					GitUtils.sign(tagBuilder, signingKey);
				} else if (signRequired) {
					throw new ExplicitException("Tag signature required, please generate "
							+ "system GPG signing key first");
				}

				ObjectId tagId = inserter.insert(tagBuilder);
				inserter.flush();

				String refName = GitUtils.tag2ref(tagName);
				RefUpdate refUpdate = repository.updateRef(refName);
				refUpdate.setNewObjectId(tagId);
				GitUtils.updateRef(refUpdate);

				return new TaggingResult(tagId, commit.copy());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		project.cacheObjectId(tagName, tagAndCommitId.getTagId());
		listenerRegistry.post(new RefUpdated(project, 
				GitUtils.tag2ref(tagName), ObjectId.zeroId(), tagAndCommitId.getCommitId()));
		return tagAndCommitId;
	}

	@Override
	public int countRefs(Long projectId, String prefix) {
		return runOnProjectServer(projectId, () -> getRepository(projectId).getRefDatabase().getRefsByPrefix(prefix).size());
	}

	@Override
	public String getClosestPath(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevTree revTree = revWalk.parseCommit(revId).getTree();
				String currentPath = path;
				while (TreeWalk.forPath(repository, currentPath, revTree) == null) {
					if (path.contains("/")) {
						currentPath = StringUtils.substringBeforeLast(path, "/");
					} else {
						currentPath = null;
						break;
					}
				}
				return currentPath;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}	
		});
	}

	@Override
	public List<RefFacade> getCommitRefs(Project project, String prefix) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			List<RefFacade> refs = new ArrayList<>(GitUtils.getCommitRefs(repository, prefix));
			Collections.sort(refs);
			Collections.reverse(refs);
			return refs;
		});
	}

	@Override
	public RefFacade getRef(Project project, String revision) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				Ref ref = repository.findRef(revision);
				return ref != null ? new RefFacade(revWalk, ref) : null;
			}
		});
	}

	@Override
	public void deleteBranch(Project project, String branchName) {
		Long projectId = project.getId();
		
		runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				ObjectId commitId = revWalk.parseCommit(repository.resolve(branchName)).copy();
				Git.wrap(repository).branchDelete().setForce(true).setBranchNames(branchName).call();
				
				String refName = GitUtils.branch2ref(branchName);
				sessionManager.runAsync(new Runnable() {

					@Override
					public void run() {
						Project innerProject = projectManager.load(projectId);
						listenerRegistry.post(new RefUpdated(innerProject, refName, commitId, ObjectId.zeroId()));
					}
					
				});
				
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			
			return null;
		});
		project.cacheObjectId(GitUtils.branch2ref(branchName), null);
	}
	
	@Override
	public void deleteTag(Project project, String tagName) {
		Long projectId = project.getId();
		
		runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				ObjectId commitId = revWalk.parseCommit(repository.resolve(tagName)).copy();
				Git.wrap(repository).tagDelete().setTags(tagName).call();
				
				String refName = GitUtils.tag2ref(tagName);
				sessionManager.runAsync(() -> {
					Project innerProject = projectManager.load(projectId);
					listenerRegistry.post(new RefUpdated(innerProject, refName, commitId, ObjectId.zeroId()));
				});
				
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			
			return null;
		});
		project.cacheObjectId(GitUtils.tag2ref(tagName), null);
	}

	@Sessional
	@Override
	public void fetch(Project targetProject, Project sourceProject, String... refSpecs) {
		Long sourceProjectId = sourceProject.getId();
		Long targetProjectId = targetProject.getId();
		String sourceProjectPath = sourceProject.getPath();
		
		runOnProjectServer(targetProjectId, () -> {
			fetch(targetProjectId, sourceProjectId, sourceProjectPath, refSpecs);
			return null;
		});
	}
	
	private void fetch(Long targetProjectId, Long sourceProjectId, 
			String sourceProjectPath, String... refSpecs) {
		String sourceActiveServer = projectManager.getActiveServer(sourceProjectId, true);
		if (sourceActiveServer.equals(clusterManager.getLocalServerAddress())) {
			Commandline git = CommandUtils.newGit();
			fetch(git, targetProjectId, getGitDir(sourceProjectId).getAbsolutePath(), refSpecs);
		} else {
			CommandUtils.callWithClusterCredential(git -> {
				String remoteUrl = clusterManager.getServerUrl(sourceActiveServer) 
						+ "/" + sourceProjectPath;
				fetch(git, targetProjectId, remoteUrl, refSpecs);
				return null;
			});		
		}
	}
	
	private void fetch(Commandline git, Long projectId, String remoteUrl, String... refSpecs) {
		git.workingDir(getGitDir(projectId));
		git.addArgs("fetch", "--quiet", remoteUrl);
		git.addArgs(refSpecs);
		git.execute(newInfoLogger(), newErrorLogger()).checkReturnCode();
	}
	
	@Sessional
	@Override
	public void push(Project sourceProject, String sourceRev, 
					 Project targetProject, String targetRev) {
		Long targetProjectId = targetProject.getId();
		Long sourceProjectId = sourceProject.getId();
		String targetProjectPath = targetProject.getPath();
		
		runOnProjectServer(sourceProjectId, () -> {
			String targetActiveServer = projectManager.getActiveServer(targetProjectId, true);
			
			// Do not optimize to push to local directory when source and target are on same host, as otherwise
			// environments in git pre/post receive hooks will not be set
			CommandUtils.callWithClusterCredential(git -> {
				git.workingDir(getGitDir(sourceProjectId));
				git.addArgs("push", "--quiet", clusterManager.getServerUrl(targetActiveServer) + "/" + targetProjectPath);
				git.addArgs(sourceRev + ":" + targetRev);
				git.execute(newInfoLogger(), newErrorLogger()).checkReturnCode();
				return null;
			});
			return null;
		});
	}

	@Override
	@Sessional
	public void pushLfsObjects(Project sourceProject, String sourceRef,
							   Project targetProject, String targetRef,
							   ObjectId commitId) {
		Long targetProjectId = targetProject.getId();
		Long sourceProjectId = sourceProject.getId();
		String targetProjectPath = targetProject.getPath();

		runOnProjectServer(sourceProjectId, () -> {
			String targetActiveServer = projectManager.getActiveServer(targetProjectId, true);

			CommandUtils.callWithClusterCredential((GitTask<Void>) git -> {
				git.workingDir(getGitDir(sourceProjectId));
				
				String remoteUrl = clusterManager.getServerUrl(targetActiveServer) + "/" + targetProjectPath;						
				AtomicReference<String> remoteCommitId = new AtomicReference<>(null);
				git.addArgs("ls-remote", remoteUrl, "HEAD", targetRef);
				git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						String refName = line.substring(40).trim();
						if (refName.equals("HEAD")) {
							if (remoteCommitId.get() == null)
								remoteCommitId.set(line.substring(0, 40));
						} else {
							remoteCommitId.set(line.substring(0, 40));
						}
					}

				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warn(line);
					}

				});

				if (remoteCommitId.get() != null) {
					git.clearArgs();
					git.addArgs("fetch", "--quiet", remoteUrl, remoteCommitId.get());
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.debug(line);
						}

					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warn(line);
						}

					}).checkReturnCode();

					Repository repository = projectManager.getRepository(sourceProjectId);
					String mergeBaseId = GitUtils.getMergeBase(repository,
							ObjectId.fromString(remoteCommitId.get()), commitId).name();

					if (!mergeBaseId.equals(commitId.name())) {
						String input = String.format("%s %s %s %s\n", sourceRef, commitId.name(),
								targetRef, remoteCommitId.get());
						git.clearArgs();
						git.addArgs("lfs", "pre-push", remoteUrl, remoteUrl);
						git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.debug(line);
							}

						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.warn(line);
							}

						}, new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8))).checkReturnCode();
					}
				} else {
					git.clearArgs();
					git.addArgs("lfs", "push", "--all", remoteUrl, commitId.name());
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.debug(line);
						}

					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warn(line);
						}

					}).checkReturnCode();
				}
				return null;
			});
			return null;
		});
	}
	
	@Sessional
	@Override
	public void updateRef(Project project, String refName, ObjectId newObjectId, ObjectId expectedOldObjectId) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, () -> {
			RefUpdate refUpdate = GitUtils.getRefUpdate(getRepository(projectId), refName);
			refUpdate.setNewObjectId(newObjectId);
			refUpdate.setExpectedOldObjectId(expectedOldObjectId);
			GitUtils.updateRef(refUpdate);
			return null;
		});

		project.cacheObjectId(refName, newObjectId);
		
		if (expectedOldObjectId != null 
				&& (GitUtils.ref2branch(refName) != null || GitUtils.ref2tag(refName) != null)) {
			listenerRegistry.post(new RefUpdated(project, refName, expectedOldObjectId, newObjectId));
		}
	}

	@Override
	public <T extends Serializable> Collection<T> filterParents(Project project, ObjectId commitId, 
			Map<ObjectId, T> values, int limit) {
		Long projectId = project.getId();

		return runOnProjectServer(projectId, () -> {
			Collection<T> filteredValues = new HashSet<>();
			try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
				revWalk.markStart(revWalk.lookupCommit(commitId));
				RevCommit nextCommit;
				while ((nextCommit = revWalk.next()) != null) {
					T value = values.remove(nextCommit);
					if (value != null) {
						filteredValues.add(value);
						if (filteredValues.size() >= limit || values.isEmpty())
							break;
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			return filteredValues;
		});
	}

	@Override
	public List<RevCommit> sortValidCommits(Project project, Collection<ObjectId> commitIds) {
		Long projectId = project.getId();

		return runOnProjectServer(projectId, () -> {
			List<RevCommit> validCommits = new ArrayList<>();
			try (var revWalk = new RevWalk(getRepository(projectId))) {
				for (var commitId: commitIds) {
					try {
						validCommits.add(revWalk.parseCommit(commitId));
					} catch (MissingObjectException e) {
					}
				}
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			Collections.sort(validCommits, new Comparator<>() {

				@Override
				public int compare(RevCommit o1, RevCommit o2) {
					return o2.getCommitTime() - o1.getCommitTime();
				}
				
			});
			return validCommits;
		});
	}

	@Sessional
	@Override
	public ObjectId commit(Project project, BlobEdits blobEdits, String refName, 
			ObjectId expectedOldCommitId, ObjectId parentCommitId, PersonIdent authorAndCommitter, 
			String commitMessage, boolean signRequired) {
		Long projectId = project.getId();
		ObjectId commitId = runOnProjectServer(projectId, new ClusterTask<>() {

			private ObjectId insertTree(RevTree revTree, TreeWalk treeWalk, ObjectInserter inserter,
										String parentPath, Set<String> currentOldPaths, Map<String, BlobContent> currentNewBlobs) {
				try {
					List<TreeFormatterEntry> entries = new ArrayList<>();
					while (revTree != null && treeWalk.next()) {
						String name = treeWalk.getNameString();
						if (currentOldPaths.contains(name)) {
							currentOldPaths.remove(name);
							BlobContent currentNewBlob = currentNewBlobs.remove(name);
							if (currentNewBlob != null) {
								ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, currentNewBlob.getBytes());
								entries.add(new TreeFormatterEntry(name, currentNewBlob.getMode(), blobId));
							}
						} else if (currentNewBlobs.containsKey(name)) {
							if ((treeWalk.getRawMode(0) & FileMode.TYPE_MASK) == FileMode.TYPE_TREE) {
								throw new ObjectAlreadyExistsException("Path already exist: " + treeWalk.getPathString());
							} else {
								BlobContent currentNewBlob = currentNewBlobs.remove(name);
								ObjectId blobId = inserter.insert(Constants.OBJ_BLOB, currentNewBlob.getBytes());
								entries.add(new TreeFormatterEntry(name, currentNewBlob.getMode(), blobId));
							}
						} else {
							Set<String> childOldPaths = new HashSet<>();
							for (Iterator<String> it = currentOldPaths.iterator(); it.hasNext(); ) {
								String currentOldPath = it.next();
								if (currentOldPath.startsWith(name + "/")) {
									childOldPaths.add(currentOldPath.substring(name.length() + 1));
									it.remove();
								}
							}
							Map<String, BlobContent> childNewBlobs = new HashMap<>();
							for (Iterator<Map.Entry<String, BlobContent>> it = currentNewBlobs.entrySet().iterator();
								 it.hasNext(); ) {
								Map.Entry<String, BlobContent> entry = it.next();
								if (entry.getKey().startsWith(name + "/")) {
									childNewBlobs.put(entry.getKey().substring(name.length() + 1), entry.getValue());
									it.remove();
								}
							}
							if (!childOldPaths.isEmpty() || !childNewBlobs.isEmpty()) {
								if ((treeWalk.getFileMode(0).getBits() & FileMode.TYPE_TREE) != 0) {
									TreeWalk childTreeWalk = TreeWalk.forPath(treeWalk.getObjectReader(), treeWalk.getPathString(),
											revTree);
									Preconditions.checkNotNull(childTreeWalk);
									childTreeWalk.enterSubtree();
									ObjectId childTreeId = insertTree(revTree, childTreeWalk, inserter, treeWalk.getPathString(),
											childOldPaths, childNewBlobs);
									if (childTreeId != null)
										entries.add(new TreeFormatterEntry(name, FileMode.TREE.getBits(), childTreeId));
								} else {
									throw new NotTreeException("Path does not represent a tree: " + treeWalk.getPathString());
								}
							} else {
								entries.add(new TreeFormatterEntry(name, treeWalk.getFileMode(0).getBits(), treeWalk.getObjectId(0)));
							}
						}
					}

					if (!currentOldPaths.isEmpty()) {
						String nonExistPath = currentOldPaths.iterator().next();
						if (parentPath != null)
							nonExistPath = parentPath + "/" + nonExistPath;
						throw new ObjectNotFoundException("Unable to find path " + nonExistPath);
					}

					if (!currentNewBlobs.isEmpty()) {
						Set<String> files = new HashSet<>();
						for (Map.Entry<String, BlobContent> entry : currentNewBlobs.entrySet()) {
							String path = entry.getKey();
							if (!path.contains("/")) {
								files.add(path);
								entries.add(new TreeFormatterEntry(path, entry.getValue().getMode(),
										inserter.insert(Constants.OBJ_BLOB, entry.getValue().getBytes())));
								files.add(path);
							}
						}
						Set<String> topLevelPathSegments = new LinkedHashSet<>();
						for (String path : currentNewBlobs.keySet()) {
							if (path.contains("/")) {
								String topLevelPathSegment = StringUtils.substringBefore(path, "/");
								if (files.contains(topLevelPathSegment)) {
									String blobPath = topLevelPathSegment;
									if (parentPath != null)
										blobPath = parentPath + "/" + path;
									throw new ObjectAlreadyExistsException("Overlapped blob path: " + blobPath);
								} else {
									topLevelPathSegments.add(topLevelPathSegment);
								}
							}
						}
						for (String topLevelPathSegment : topLevelPathSegments) {
							Map<String, BlobContent> childNewBlobs = new HashMap<>();
							for (Map.Entry<String, BlobContent> entry : currentNewBlobs.entrySet()) {
								String path = entry.getKey();
								if (path.startsWith(topLevelPathSegment + "/"))
									childNewBlobs.put(path.substring(topLevelPathSegment.length() + 1), entry.getValue());
							}
							if (parentPath == null)
								parentPath = topLevelPathSegment;
							else
								parentPath += "/" + topLevelPathSegment;
							ObjectId childTreeId = insertTree(revTree, treeWalk, inserter, parentPath,
									Sets.newHashSet(), childNewBlobs);
							if (childTreeId != null)
								entries.add(new TreeFormatterEntry(topLevelPathSegment, FileMode.TREE.getBits(), childTreeId));
						}
					}
					if (!entries.isEmpty()) {
						TreeFormatter formatter = new TreeFormatter();
						Collections.sort(entries);
						for (TreeFormatterEntry entry : entries)
							formatter.append(entry.name, FileMode.fromBits(entry.mode), entry.id);
						return inserter.insert(formatter);
					} else {
						return null;
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public ObjectId call() {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository);
					 TreeWalk treeWalk = new TreeWalk(repository);
					 ObjectInserter inserter = repository.newObjectInserter();) {

					CommitBuilder commit = new CommitBuilder();

					commit.setAuthor(authorAndCommitter);
					commit.setCommitter(authorAndCommitter);
					commit.setMessage(commitMessage);

					RevTree revTree;
					if (!parentCommitId.equals(ObjectId.zeroId())) {
						commit.setParentId(parentCommitId);
						revTree = revWalk.parseCommit(parentCommitId).getTree();
						treeWalk.addTree(revTree);
					} else {
						revTree = null;
					}

					ObjectId treeId = insertTree(revTree, treeWalk, inserter, null,
							new HashSet<>(blobEdits.getOldPaths()),
							new HashMap<>(blobEdits.getNewBlobs()));

					if (treeId != null)
						commit.setTreeId(treeId);
					else
						commit.setTreeId(inserter.insert(new TreeFormatter()));

					PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
					if (signingKey != null) {
						GitUtils.sign(commit, signingKey);
					} else if (signRequired) {
						throw new ExplicitException("Commit signature required, please generate "
								+ "system GPG signing key first");
					}

					ObjectId commitId = inserter.insert(commit);
					inserter.flush();
					RefUpdate ru = repository.updateRef(refName);
					ru.setRefLogIdent(authorAndCommitter);
					ru.setNewObjectId(commitId);
					ru.setExpectedOldObjectId(expectedOldCommitId);
					GitUtils.updateRef(ru);

					return commitId;
				} catch (RevisionSyntaxException | IOException e) {
					throw new RuntimeException(e);
				}
			}

		});
		
		project.cacheObjectId(refName, commitId);
		listenerRegistry.post(new RefUpdated(project, refName, expectedOldCommitId, commitId));
		
		return commitId;
	}

	@Override
	public PathChange getPathChange(Project project, ObjectId oldRevId, ObjectId newRevId, String path) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevCommit oldCommit = revWalk.parseCommit(oldRevId);
				RevCommit newCommit = revWalk.parseCommit(newRevId);
				TreeWalk treeWalk = TreeWalk.forPath(repository, path, 
						oldCommit.getTree().getId(), newCommit.getTree().getId());
				if (treeWalk != null) {
					return new PathChange(treeWalk.getObjectId(0), treeWalk.getObjectId(1), 
							treeWalk.getFileMode(0).getBits(), treeWalk.getFileMode(1).getBits());
				} else {
					return null;
				}
			}
		});
	}

	@Override
	public Blob getBlob(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, new ClusterTask<>() {

			private Map<String, String> getSubmodules() {
				Map<String, String> submodules = new HashMap<>();

				Blob blob = getBlob(".gitmodules");
				if (blob != null) {
					String content = new String(blob.getBytes());

					String path = null;
					String url = null;

					for (String line : StringUtils.splitAndTrim(content, "\r\n")) {
						if (line.startsWith("[") && line.endsWith("]")) {
							if (path != null && url != null)
								submodules.put(path, url);

							path = url = null;
						} else if (line.startsWith("path")) {
							path = StringUtils.substringAfter(line, "=").trim();
						} else if (line.startsWith("url")) {
							url = StringUtils.substringAfter(line, "=").trim();
						}
					}
					if (path != null && url != null)
						submodules.put(path, url);
				}

				return submodules;
			}

			private Blob getBlob(String path) {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					Blob blob = null;
					RevCommit commit = GitUtils.parseCommit(revWalk, revId);
					if (commit != null) {
						TreeWalk treeWalk = TreeWalk.forPath(repository, path, commit.getTree());
						if (treeWalk != null) {
							BlobIdent blobIdent = new BlobIdent(revId.name(), path, treeWalk.getRawMode(0));
							ObjectId blobId = treeWalk.getObjectId(0);
							if (blobIdent.isGitLink()) {
								String url = getSubmodules().get(blobIdent.path);
								if (url == null) {
									logger.error("Unable to find submodule (revision: {}, path: {})",
											revId.name(), path);
									blob = new Blob(blobIdent, blobId, treeWalk.getObjectReader());
								} else {
									String hash = blobId.name();
									blob = new Blob(blobIdent, blobId, new Submodule(url, hash).toString().getBytes());
								}
							} else if (blobIdent.isTree()) {
								throw new NotFileException("Path '" + blobIdent.path + "' is a tree");
							} else {
								blob = new Blob(blobIdent, blobId, treeWalk.getObjectReader());
							}
						}
					}
					return blob;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public Blob call() {
				return getBlob(path);
			}

		});
	}

	@Override
	public BlobIdent getBlobIdent(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();

		return runOnProjectServer(projectId, new ClusterTask<>() {

			@Override
			public BlobIdent call() {
				if (path.length() == 0) {
					return new BlobIdent(revId.name(), null, FileMode.TREE.getBits());
				} else {
					Repository repository = getRepository(projectId);
					try (RevWalk revWalk = new RevWalk(repository)) {
						RevCommit commit = GitUtils.parseCommit(revWalk, revId);
						if (commit != null) {
							TreeWalk treeWalk = TreeWalk.forPath(repository, path, commit.getTree());
							if (treeWalk != null)
								return new BlobIdent(revId.name(), path, treeWalk.getRawMode(0));
						}
						return null;
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				}
			}

		});
	}
	
	@Override
	public void deleteRefs(Project project, Collection<String> refs) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			for (String ref: refs) 
				GitUtils.deleteRef(GitUtils.getRefUpdate(repository, ref));
			return null;
		});
	}

	@Override
	public ObjectId merge(Project project, ObjectId targetCommitId, ObjectId sourceCommitId, boolean squash,
			PersonIdent committer, PersonIdent author, String commitMessage, boolean useOursOnConflict) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> GitUtils.merge(getRepository(projectId), targetCommitId, sourceCommitId, squash, 
				committer, author, commitMessage, useOursOnConflict));
	}

	@Override
	public boolean isMergedInto(Project project, Map<String, String> gitEnvs, ObjectId base, ObjectId tip) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> GitUtils.isMergedInto(getRepository(projectId), gitEnvs, base, tip));
	}

	@Override
	public ObjectId rebase(Project project, ObjectId source, ObjectId target, PersonIdent committer) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> GitUtils.rebase(getRepository(projectId), source, target, committer));
	}

	@Override
	public ObjectId amendCommits(Project project, ObjectId startCommitId, ObjectId endCommitId, 
			String oldCommitterName, PersonIdent newCommitter) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
			Repository repository = getRepository(projectId);
			try (	RevWalk revWalk = new RevWalk(repository);
					ObjectInserter inserter = repository.newObjectInserter()) {
				RevCommit startCommit = revWalk.parseCommit(startCommitId);
				RevCommit endCommit = revWalk.parseCommit(endCommitId);
				List<RevCommit> commits = RevWalkUtils.find(revWalk, startCommit, endCommit);
				Collections.reverse(commits);
				startCommit = endCommit;
				for (RevCommit commit: commits) {
					PersonIdent committer = commit.getCommitterIdent();
					if (committer.getName().equals(oldCommitterName) || !commit.getParent(0).equals(endCommit)) {
						CommitBuilder commitBuilder = new CommitBuilder();
						commitBuilder.setAuthor(commit.getAuthorIdent());
						commitBuilder.setCommitter(committer);
						commitBuilder.setParentId(startCommit.copy());
						commitBuilder.setMessage(commit.getFullMessage());
						commitBuilder.setTreeId(commit.getTree().getId());
						if (signingKey != null)
							GitUtils.sign(commitBuilder, signingKey);
						startCommit = revWalk.parseCommit(inserter.insert(commitBuilder));
					} else {
						startCommit = commit;
					}
				}
				inserter.flush();
				return startCommit.copy();
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
			
		});
	}

	@Override
	public ObjectId amendCommit(Project project, ObjectId commitId, PersonIdent author, PersonIdent committer,
			String commitMessage) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			PGPSecretKeyRing signingKey = settingManager.getGpgSetting().getSigningKey();
			Repository repository = getRepository(projectId);
			try (	RevWalk revWalk = new RevWalk(repository);
					ObjectInserter inserter = repository.newObjectInserter()) {
				RevCommit commit = revWalk.parseCommit(commitId);
				CommitBuilder commitBuilder = new CommitBuilder();
				if (author != null)
					commitBuilder.setAuthor(author);
				else
					commitBuilder.setAuthor(commit.getAuthorIdent());
				commitBuilder.setCommitter(committer);
				commitBuilder.setMessage(commitMessage);
				commitBuilder.setTreeId(commit.getTree());
				commitBuilder.setParentIds(commit.getParents());
				if (signingKey != null)
					GitUtils.sign(commitBuilder, signingKey);
				ObjectId newCommitId = inserter.insert(commitBuilder);
				inserter.flush();
				return newCommitId;
			} catch (Exception e) {
				throw ExceptionUtils.unchecked(e);
			}
		});
	}

	@Override
	public List<RevCommit> getReachableCommits(Project project, Collection<ObjectId> startCommitIds, 
			Collection<ObjectId> uninterestingCommitIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
				List<RevCommit> commits = new ArrayList<>();
				for (ObjectId startCommitId: startCommitIds)
					revWalk.markStart(revWalk.parseCommit(startCommitId));
				
				for (var uninterestingCommitId: uninterestingCommitIds) 
					revWalk.markUninteresting(revWalk.parseCommit(uninterestingCommitId));
				revWalk.forEach(c->commits.add(c));
				return commits;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Collection<String> getChangedFiles(Project project, ObjectId oldCommitId, ObjectId newCommitId, 
			Map<String, String> gitEnvs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			if (gitEnvs != null && !gitEnvs.isEmpty()) {
				return new ListChangedFilesCommand(getGitDir(projectId), oldCommitId.name(), newCommitId.name(), gitEnvs)
						.run();
			} else {
				Repository repository = getRepository(projectId);
				return GitUtils.getChangedFiles(repository, oldCommitId, newCommitId);
			}
		});
	}

	@Override
	public ObjectId getMergeBase(Project project1, ObjectId commitId1, Project project2, ObjectId commitId2) {
		Long projectId1 = project1.getId();
		Long projectId2 = project2.getId();
		String projectPath2 = project2.getPath();
		return runOnProjectServer(projectId1, () -> {
			if (!projectId1.equals(projectId2))
				fetch(projectId1, projectId2, projectPath2, commitId2.name());
			return GitUtils.getMergeBase(getRepository(projectId1), commitId1, commitId2);
		});
	}			
	
	@Override
	public boolean hasObjects(Project project, ObjectId... objIds) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, () -> {
			ObjectDatabase database = getRepository(projectId).getObjectDatabase();
			for (var objId: objIds) {
				if (!database.has(objId))
					return false;
			}
			return true;
		});
	}

	@Override
	public Collection<ObjectId> filterNonExistants(Project project, Collection<ObjectId> objIds) {
		Long projectId = project.getId();

		return runOnProjectServer(projectId, () -> {
			var nonExistants = new ArrayList<ObjectId>();
			ObjectDatabase database = getRepository(projectId).getObjectDatabase();
			for (var objId: objIds) {
				if (!database.has(objId))
					nonExistants.add(objId);
			}
			return nonExistants;
		});
	}
	
	@Override
	public List<BlobIdent> getChildren(Project project, ObjectId revId, String path, 
			BlobIdentFilter filter, boolean expandSingle) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			Repository repository = getRepository(projectId);
			try (RevWalk revWalk = new RevWalk(repository)) {
				RevTree revTree = revWalk.parseCommit(revId).getTree();
				TreeWalk treeWalk;
				if (path != null) {
					treeWalk = TreeWalk.forPath(repository, path, revTree);
					treeWalk.enterSubtree();
				} else {
					treeWalk = new TreeWalk(repository);
					treeWalk.addTree(revTree);
				}
				
				List<BlobIdent> children = new ArrayList<>();
				while (treeWalk.next()) {
					BlobIdent child = new BlobIdent(revId.name(), treeWalk.getPathString(), treeWalk.getRawMode(0));
					if (filter.filter(child))
						children.add(child);
				}
				
				if (expandSingle) {
					for (int i=0; i<children.size(); i++) {
						BlobIdent child = children.get(i);
						while (child.isTree()) {
							treeWalk = TreeWalk.forPath(repository, child.path, revTree);
							Preconditions.checkNotNull(treeWalk);
							treeWalk.enterSubtree();
							if (treeWalk.next()) {
								BlobIdent grandChild = new BlobIdent(revId.name(), 
										treeWalk.getPathString(), treeWalk.getRawMode(0));
								if (treeWalk.next() || !grandChild.isTree()) 
									break;
								else
									child = grandChild;
							} else {
								break;
							}
						}
						children.set(i, child);
					}
				}
				
				Collections.sort(children);
				return children;
			} catch (IOException e) {
				throw new RuntimeException(e);
			} 
		});
	}

	@Override
	public LastCommitsOfChildren getLastCommitsOfChildren(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			String normalizedPath = path;
			if (normalizedPath == null)
				normalizedPath = "";
			
			final File cacheDir = new File(
					projectManager.getInfoDir(projectId), 
					"last_commits/" + normalizedPath + "/onedev_last_commits");
			
			final ReadWriteLock lock;
			try {
				lock = LockUtils.getReadWriteLock(cacheDir.getCanonicalPath());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			final Set<ObjectId> commitIds = new HashSet<>(); 
			
			lock.readLock().lock();
			try {
				if (cacheDir.exists()) {
					for (String each: cacheDir.list()) 
						commitIds.add(ObjectId.fromString(each));
				} 	
			} finally {
				lock.readLock().unlock();
			}
			
			LastCommitsOfChildren.Cache cache;
			if (!commitIds.isEmpty()) {
				cache = commitId -> {
					if (commitIds.contains(commitId)) {
						lock.readLock().lock();
						try {
							byte[] bytes = FileUtils.readFileToByteArray(new File(cacheDir, commitId.name()));
							return (Map<String, Value>) SerializationUtils.deserialize(bytes);
						} catch (IOException e) {
							throw new RuntimeException(e);
						} finally {
							lock.readLock().unlock();
						}
					} else {
						return null;
					}
				};
			} else {
				cache = null;
			}

			long time = System.currentTimeMillis();
			Repository repository = getRepository(projectId);
			LastCommitsOfChildren lastCommits = new LastCommitsOfChildren(repository, revId, normalizedPath, cache);
			long elapsed = System.currentTimeMillis()-time;
			if (elapsed > LAST_COMMITS_CACHE_THRESHOLD) {
				lock.writeLock().lock();
				try {
					if (!cacheDir.exists())
						FileUtils.createDir(cacheDir);
					FileUtils.writeByteArrayToFile(
							new File(cacheDir, revId.name()), 
							SerializationUtils.serialize(lastCommits));
				} catch (IOException e) {
					throw new RuntimeException(e);
				} finally {
					lock.writeLock().unlock();
				}
			}
			return lastCommits;
		});
	}

	@Override
	public List<DiffEntryFacade> diff(Project project, AnyObjectId oldRevId, AnyObjectId newRevId) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			List<DiffEntryFacade> diffEntries = new ArrayList<>();
			for (DiffEntry diffEntry: GitUtils.diff(getRepository(projectId), oldRevId, newRevId)) {
				diffEntries.add(new DiffEntryFacade(
						diffEntry.getChangeType(), 
						diffEntry.getOldPath(), diffEntry.getNewPath(), 
						diffEntry.getOldMode().getBits(), diffEntry.getNewMode().getBits()));
			}
			return diffEntries;
		});
	}
	
	@Override
	public Map<ObjectId, AheadBehind> getAheadBehinds(Project project, ObjectId baseId, 
			Collection<ObjectId> compareIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
				Map<ObjectId, AheadBehind> aheadBehinds = new HashMap<>();
				RevCommit baseCommit = revWalk.lookupCommit(baseId);
				revWalk.markStart(baseCommit);
				Map<ObjectId, RevCommit> compareCommits = new HashMap<>();
				for (ObjectId compareId: compareIds) {
					RevCommit compareCommit = revWalk.lookupCommit(compareId);
					compareCommits.put(compareId, compareCommit);
					revWalk.markStart(compareCommit);
				}
				revWalk.setRevFilter(RevFilter.MERGE_BASE);
				RevCommit mergeBase = revWalk.next();
				
				revWalk.reset();
				revWalk.setRevFilter(RevFilter.ALL);

				if (mergeBase != null) {
					revWalk.markStart(baseCommit);
					revWalk.markUninteresting(mergeBase);
					Set<ObjectId> baseSet = new HashSet<>();
					for (RevCommit commit: revWalk) 
						baseSet.add(commit.copy());
					revWalk.reset();
					
					for (ObjectId compareId: compareIds) {
						RevCommit compareCommit = Preconditions.checkNotNull(compareCommits.get(compareId));
						revWalk.markStart(compareCommit);
						revWalk.markUninteresting(mergeBase);
						Set<ObjectId> compareSet = new HashSet<>();
						for (RevCommit commit: revWalk) 
							compareSet.add(commit.copy());
						revWalk.reset();
						
						int ahead = 0;
						for (ObjectId each: compareSet) {
							if (!baseSet.contains(each))
								ahead++;
						}
						int behind = 0;
						for (ObjectId each: baseSet) {
							if (!compareSet.contains(each))
								behind++;
						}
						aheadBehinds.put(compareId, new AheadBehind(ahead, behind));
					}					
				} else {
					for (ObjectId compareId: compareIds) {
						RevCommit compareCommit = Preconditions.checkNotNull(compareCommits.get(compareId));
						revWalk.markUninteresting(baseCommit);
						revWalk.markStart(compareCommit);
						int ahead = 0;
						for (@SuppressWarnings("unused") var commit: revWalk)
							ahead++;
						revWalk.reset();
						
						revWalk.markUninteresting(compareCommit);
						revWalk.markStart(baseCommit);
						int behind = 0;
						for (@SuppressWarnings("unused") var commit: revWalk)
							behind++;
						revWalk.reset();
						
						aheadBehinds.put(compareId, new AheadBehind(ahead, behind));
					}					
				}
				return aheadBehinds;
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
	}

	@Override
	public Collection<BlameBlock> blame(Project project, ObjectId revId, String file, LinearRange range) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> new BlameCommand(projectManager.getGitDir(projectId), revId, file)
				.range(range).run());
	}

	@Override
	public byte[] getRawCommit(Project project, ObjectId revId, Map<String, String> envs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			File gitDir = projectManager.getGitDir(projectId);
			return new GetRawCommitCommand(gitDir, revId.name(), envs).run();
		});
	}

	@Override
	public byte[] getRawTag(Project project, ObjectId tagId, Map<String, String> envs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> {
			File gitDir = projectManager.getGitDir(projectId);
			return new GetRawTagCommand(gitDir, tagId.name(), envs).run();
		});
	}

	@Nullable
	@Override
	public CommitMessageError checkCommitMessages(Project project, String branch, User user,
									  ObjectId oldId, ObjectId newId, 
									  @Nullable Map<String, String> envs) {
		Long projectId = project.getId();
		var branchProtection = project.getBranchProtection(branch, user);
		if (branchProtection.getMaxCommitMessageLineLength() != null
				|| branchProtection.isEnforceConventionalCommits()) {
			return runOnProjectServer(projectId, () -> {
				Map<ObjectId, String> commitMessages = new LinkedHashMap<>();
				Set<ObjectId> mergeCommits = new HashSet<>();
				if (envs != null) {
					File gitDir = projectManager.getGitDir(projectId);

					List<String> revisions;
					if (oldId.equals(ObjectId.zeroId()))
						revisions = Lists.newArrayList(newId.name());
					else
						revisions = Lists.newArrayList("^" + oldId.name(), newId.name());

					var logCommand = new LogCommand(gitDir, revisions) {

						@Override
						protected void consume(GitCommit commit) {
							var commitId = ObjectId.fromString(commit.getHash());
							if (commit.getParentHashes().size() > 1)
								mergeCommits.add(commitId);
							var commitMessage = commit.getSubject();
							if (commit.getBody() != null)
								commitMessage += "\n\n" + commit.getBody();
							commitMessages.put(commitId, commitMessage);
						}

					};
					if (oldId.equals(ObjectId.zeroId()))
						logCommand.limit(1);
					logCommand.envs(envs).fields(EnumSet.of(SUBJECT, BODY, PARENTS)).run();
				} else {
					var repository = projectManager.getRepository(projectId);
					if (!oldId.equals(ObjectId.zeroId())) {
						for (var commit: GitUtils.getReachableCommits(repository, 
								Sets.newHashSet(oldId), Sets.newHashSet(newId))) {
							commitMessages.put(commit.copy(), commit.getFullMessage());
							if (commit.getParentCount() > 1)
								mergeCommits.add(commit.copy());
						}
					} else {
						var commit = repository.parseCommit(newId);
						commitMessages.put(commit.copy(), commit.getFullMessage());
						if (commit.getParentCount() > 1)
							mergeCommits.add(commit.copy());
					}
				}
				for (var entry: commitMessages.entrySet()) {
					var errorMessage = branchProtection.checkCommitMessage(
							entry.getValue(), mergeCommits.contains(entry.getKey()));
					if (errorMessage != null) {
						return new CommitMessageError(entry.getKey(), errorMessage);
					}
				}
				return null;
			});
		} else {
			return null;
		}
	}
	
	@Override
	public List<String> revList(Project project, RevListOptions options) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, () -> new RevListCommand(getGitDir(projectId)).options(options).run());
	}
	
}
