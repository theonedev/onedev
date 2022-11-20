package io.onedev.server.git.service;

import java.io.File;
import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.SerializationUtils;
import org.bouncycastle.openpgp.PGPSecretKeyRing;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.errors.RevisionSyntaxException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.lib.ObjectDatabase;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.TagBuilder;
import org.eclipse.jgit.lib.TreeFormatter;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren;
import org.eclipse.jgit.revwalk.LastCommitsOfChildren.Value;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.RevWalkUtils;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;

import io.onedev.commons.loader.ManagedSerializedForm;
import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.FileUtils;
import io.onedev.commons.utils.LinearRange;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.cluster.ClusterManager;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.entitymanager.SettingManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.DefaultBranchChanged;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.BlameBlock;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobContent;
import io.onedev.server.git.BlobEdits;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.BlobIdentFilter;
import io.onedev.server.git.CommandUtils;
import io.onedev.server.git.GitTask;
import io.onedev.server.git.GitUtils;
import io.onedev.server.git.Submodule;
import io.onedev.server.git.command.BlameCommand;
import io.onedev.server.git.command.GetRawCommitCommand;
import io.onedev.server.git.command.GetRawTagCommand;
import io.onedev.server.git.command.ListChangedFilesCommand;
import io.onedev.server.git.command.RevListOptions;
import io.onedev.server.git.command.RevListCommand;
import io.onedev.server.git.exception.NotFileException;
import io.onedev.server.git.exception.NotTreeException;
import io.onedev.server.git.exception.ObjectAlreadyExistsException;
import io.onedev.server.git.exception.ObjectNotFoundException;
import io.onedev.server.model.Project;
import io.onedev.server.persistence.SessionManager;
import io.onedev.server.persistence.annotation.Sessional;
import io.onedev.server.storage.StorageManager;

@Singleton
public class DefaultGitService implements GitService, Serializable {

	private static final Logger logger = LoggerFactory.getLogger(DefaultGitService.class);
	
	private static final int LAST_COMMITS_CACHE_THRESHOLD = 1000;
	
	private final ProjectManager projectManager;
	
	private final SettingManager settingManager;
	
	private final SessionManager sessionManager;
	
	private final ClusterManager clusterManager;
	
	private final StorageManager storageManager;
	
	private final ListenerRegistry listenerRegistry;
	
	@Inject
	public DefaultGitService(ProjectManager projectManager, SettingManager settingManager,
			SessionManager sessionManager, ClusterManager clusterManager, 
			StorageManager storageManager, ListenerRegistry listenerRegistry) {
		this.projectManager = projectManager;
		this.sessionManager = sessionManager;
		this.settingManager = settingManager;
		this.clusterManager = clusterManager;
		this.storageManager = storageManager;
		this.listenerRegistry = listenerRegistry;
	}

	public Object writeReplace() throws ObjectStreamException {
		return new ManagedSerializedForm(GitService.class);
	}

	private Repository getRepository(Long projectId) {
		return projectManager.getRepository(projectId);
	}
	
	private File getGitDir(Long projectId) {
		return storageManager.getProjectGitDir(projectId);
	}

	private <T> T runOnProjectServer(Long projectId, ClusterTask<T> task) {
		return projectManager.runOnProjectServer(projectId, task);
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
		return runOnProjectServer(projectId, new ClusterTask<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String call() throws Exception {
				return GitUtils.getDefaultBranch(getRepository(projectId));
			}
			
		});
	}

	@Override
	public void setDefaultBranch(Project project, String defaultBranch) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				RefUpdate refUpdate = GitUtils.getRefUpdate(getRepository(projectId), "HEAD");
				GitUtils.linkRef(refUpdate, GitUtils.branch2ref(defaultBranch));
				return null;
			}
			
		});
		listenerRegistry.post(new DefaultBranchChanged(project, defaultBranch));
	}
	
	@Override
	public ObjectId resolve(Project project, String revision, boolean errorIfInvalid) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
				return GitUtils.resolve(getRepository(projectId), revision, errorIfInvalid);
			}
			
		});
	}

	@Override
	public RevCommit getCommit(Project project, ObjectId revId) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<RevCommit>() {

			private static final long serialVersionUID = 1L;

			@Override
			public RevCommit call() throws Exception {
				try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
					return GitUtils.parseCommit(revWalk, revId);
				}
			}
			
		});
	}

	@Override
	public List<RevCommit> getCommits(Project project, List<ObjectId> revIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<RevCommit>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<RevCommit> call() throws Exception {
				List<RevCommit> commits = new ArrayList<>();
				try (var revWalk = new RevWalk(getRepository(projectId))) {
					for (var revId: revIds)
						commits.add(revWalk.parseCommit(revId));
				}
				return commits;
			}
			
		});
	}
	
	@Override
	public int getMode(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call() throws Exception {
				try (RevWalk revWalk = new RevWalk(getRepository(projectId))) {
					RevCommit commit = revWalk.parseCommit(revId);
					TreeWalk treeWalk = TreeWalk.forPath(getRepository(projectId), path, commit.getTree());
					if (treeWalk != null) 
						return treeWalk.getRawMode(0);
					else 
						return 0;
				}
			}
			
		});
	}

	@Sessional
	@Override
	public ObjectId createBranch(Project project, String branchName, String branchRevision) {
		ObjectId revId = project.getObjectId(branchRevision, true);
		Long projectId = project.getId();
		ObjectId commitId = runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
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
		
		TaggingResult tagAndCommitId = runOnProjectServer(projectId, new ClusterTask<TaggingResult>() {

			private static final long serialVersionUID = 1L;

			@Override
			public TaggingResult call() throws Exception {
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
			}
			
		});
		project.cacheObjectId(tagName, tagAndCommitId.getTagId());
		listenerRegistry.post(new RefUpdated(project, 
				GitUtils.tag2ref(tagName), ObjectId.zeroId(), tagAndCommitId.getCommitId()));
		return tagAndCommitId;
	}

	@Override
	public int countRefs(Long projectId, String prefix) {
		return runOnProjectServer(projectId, new ClusterTask<Integer>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Integer call() throws Exception {
				return getRepository(projectId).getRefDatabase().getRefsByPrefix(prefix).size();
			}
			
		});
	}

	@Override
	public String getClosestPath(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<String>() {

			private static final long serialVersionUID = 1L;

			@Override
			public String call() throws Exception {
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
			}
			
		});
	}

	@Override
	public List<RefFacade> getRefs(Project project, String prefix) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<RefFacade>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<RefFacade> call() throws Exception {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					List<Ref> refs = new ArrayList<Ref>(repository.getRefDatabase().getRefsByPrefix(prefix));
					List<RefFacade> refInfos = refs.stream()
							.map(ref->new RefFacade(revWalk, ref))
							.filter(refInfo->refInfo.getPeeledObj() instanceof RevCommit)
							.collect(Collectors.toList());
					Collections.sort(refInfos);
					Collections.reverse(refInfos);
					return refInfos;
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
			
		});
	}

	@Override
	public RefFacade getRef(Project project, String revision) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, new ClusterTask<RefFacade>() {

			private static final long serialVersionUID = 1L;

			@Override
			public RefFacade call() throws Exception {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					Ref ref = repository.findRef(revision);
					return ref != null? new RefFacade(revWalk, ref): null;
				}
			}
			
		});
	}

	@Override
	public void deleteBranch(Project project, String branchName) {
		Long projectId = project.getId();
		
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					ObjectId commitId = revWalk.parseCommit(repository.resolve(branchName)).copy();
					Git.wrap(repository).branchDelete().setForce(true).setBranchNames(branchName).call();
					
					String refName = GitUtils.branch2ref(branchName);
			    	sessionManager.runAsync(new Runnable() {

						@Override
						public void run() {
							Project project = projectManager.load(projectId);
							listenerRegistry.post(new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
						}
			    		
			    	});
			    	
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
		    	
		    	return null;
			}
			
		});
	}
	
	@Override
	public void deleteTag(Project project, String tagName) {
		Long projectId = project.getId();
		
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Repository repository = getRepository(projectId);
				try (RevWalk revWalk = new RevWalk(repository)) {
					ObjectId commitId = revWalk.parseCommit(repository.resolve(tagName)).copy();
					Git.wrap(repository).tagDelete().setTags(tagName).call();
					
					String refName = GitUtils.tag2ref(tagName);
			    	sessionManager.runAsync(new Runnable() {

						@Override
						public void run() {
							Project project = projectManager.load(projectId);
							listenerRegistry.post(new RefUpdated(project, refName, commitId, ObjectId.zeroId()));
						}
			    		
			    	});
			    	
				} catch (Exception e) {
					throw ExceptionUtils.unchecked(e);
				}
		    	
		    	return null;
			}
			
		});
	}

	@Sessional
	@Override
	public void fetch(Project targetProject, Project sourceProject, String... refSpecs) {
		Long sourceProjectId = sourceProject.getId();
		Long targetProjectId = targetProject.getId();
		String sourceProjectPath = sourceProject.getPath();
		
		runOnProjectServer(targetProjectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				fetch(targetProjectId, sourceProjectId, sourceProjectPath, refSpecs);
				return null;
			}
			
		});
	}
	
	private void fetch(Long targetProjectId, Long sourceProjectId, 
			String sourceProjectPath, String... refSpecs) {
		UUID sourceStorageServerUUID = projectManager.getStorageServerUUID(sourceProjectId, true);
		if (sourceStorageServerUUID.equals(clusterManager.getLocalServerUUID())) {
			Commandline git = CommandUtils.newGit();
			fetch(git, targetProjectId, getGitDir(sourceProjectId).getAbsolutePath(), refSpecs);
		} else {
			CommandUtils.callWithClusterCredential(new GitTask<Void>() {

				@Override
				public Void call(Commandline git) {
					String remoteUrl = clusterManager.getServerUrl(sourceStorageServerUUID) 
							+ "/" + sourceProjectPath;
					fetch(git, targetProjectId, remoteUrl, refSpecs);
					return null;
				}
				
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
	public void push(Project sourceProject, Project targetProject, String... refSpecs) {
		Long targetProjectId = targetProject.getId();
		Long sourceProjectId = sourceProject.getId();
		String targetProjectPath = targetProject.getPath();
		
		runOnProjectServer(sourceProjectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			private void push(Commandline git, String remoteUrl) {
				git.workingDir(getGitDir(sourceProjectId));
				git.addArgs("push", "--quiet", remoteUrl);
				git.addArgs(refSpecs);
				git.execute(newInfoLogger(), newErrorLogger()).checkReturnCode();
			}
			
			@Override
			public Void call() throws Exception {
				UUID targetStorageServerUUID = projectManager.getStorageServerUUID(targetProjectId, true);
				if (targetStorageServerUUID.equals(clusterManager.getLocalServerUUID())) {
					push(CommandUtils.newGit(), getGitDir(targetProjectId).getAbsolutePath());
				} else {
					CommandUtils.callWithClusterCredential(new GitTask<Void>() {

						@Override
						public Void call(Commandline git) {
							push(git, clusterManager.getServerUrl(targetStorageServerUUID) + "/" + targetProjectPath);
							return null;
						}
						
					});
				}
				return null;
			}
			
		});
	}
	
	@Sessional
	@Override
	public void updateRef(Project project, String refName, ObjectId newObjectId, ObjectId expectedOldObjectId) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				RefUpdate refUpdate = GitUtils.getRefUpdate(getRepository(projectId), refName);
				refUpdate.setNewObjectId(newObjectId);
				refUpdate.setExpectedOldObjectId(expectedOldObjectId);
				GitUtils.updateRef(refUpdate);
				return null;
			}
			
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

		return runOnProjectServer(projectId, new ClusterTask<Collection<T>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<T> call() throws Exception {
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
			}
			
		});
	}

	@Override
	public List<RevCommit> sortValidCommits(Project project, Collection<ObjectId> commitIds) {
		Long projectId = project.getId();

		return runOnProjectServer(projectId, new ClusterTask<List<RevCommit>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<RevCommit> call() throws Exception {
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
			}
			
		});
	}

	@Sessional
	@Override
	public ObjectId commit(Project project, BlobEdits blobEdits, String refName, 
			ObjectId expectedOldCommitId, ObjectId parentCommitId, PersonIdent authorAndCommitter, 
			String commitMessage, boolean signRequired) {
		Long projectId = project.getId();
		ObjectId commitId = runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

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
							for (Iterator<String> it = currentOldPaths.iterator(); it.hasNext();) {
								String currentOldPath = it.next();
								if (currentOldPath.startsWith(name + "/")) {
									childOldPaths.add(currentOldPath.substring(name.length()+1));
									it.remove();
								}
							}
							Map<String, BlobContent> childNewBlobs = new HashMap<>();
							for (Iterator<Map.Entry<String, BlobContent>> it = currentNewBlobs.entrySet().iterator(); 
									it.hasNext();) {
								Map.Entry<String, BlobContent> entry = it.next();
								if (entry.getKey().startsWith(name +"/")) {
									childNewBlobs.put(entry.getKey().substring(name.length()+1), entry.getValue());
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
						for (Map.Entry<String, BlobContent> entry: currentNewBlobs.entrySet()) {
							String path = entry.getKey();
							if (!path.contains("/")) {
								files.add(path);
								entries.add(new TreeFormatterEntry(path, entry.getValue().getMode(), 
										inserter.insert(Constants.OBJ_BLOB, entry.getValue().getBytes())));
								files.add(path);
							}
						}				
						Set<String> topLevelPathSegments = new LinkedHashSet<>();
						for (String path: currentNewBlobs.keySet()) {
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
						for (String topLevelPathSegment: topLevelPathSegments) {
							Map<String, BlobContent> childNewBlobs = new HashMap<>();
							for (Map.Entry<String, BlobContent> entry: currentNewBlobs.entrySet()) {
								String path = entry.getKey();
								if (path.startsWith(topLevelPathSegment + "/"))
									childNewBlobs.put(path.substring(topLevelPathSegment.length()+1), entry.getValue());
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
						for (TreeFormatterEntry entry: entries)
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
			public ObjectId call() throws Exception {
				Repository repository = getRepository(projectId);
				try (	RevWalk revWalk = new RevWalk(repository); 
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
		
		return runOnProjectServer(projectId, new ClusterTask<PathChange>() {

			private static final long serialVersionUID = 1L;

			@Override
			public PathChange call() throws Exception {
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
			}			
		});
	}

	@Override
	public Blob getBlob(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, new ClusterTask<Blob>() {

			private static final long serialVersionUID = 1L;

			private Map<String, String> getSubmodules() {
				Map<String, String> submodules = new HashMap<>();
				
				Blob blob = getBlob(".gitmodules");
				if (blob != null) {
					String content = new String(blob.getBytes());
					
					String path = null;
					String url = null;
					
					for (String line: StringUtils.splitAndTrim(content, "\r\n")) {
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
			public Blob call() throws Exception {
				return getBlob(path);
			}
			
		});
	}

	@Override
	public void deleteRefs(Project project, Collection<String> refs) {
		Long projectId = project.getId();
		runOnProjectServer(projectId, new ClusterTask<Void>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Void call() throws Exception {
				Repository repository = getRepository(projectId);
				for (String ref: refs) 
					GitUtils.deleteRef(GitUtils.getRefUpdate(repository, ref));
				return null;
			}
			
		});
	}

	@Override
	public ObjectId merge(Project project, ObjectId targetCommitId, ObjectId sourceCommitId, boolean squash,
			PersonIdent committer, PersonIdent author, String commitMessage, boolean useOursOnConflict) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
				return GitUtils.merge(getRepository(projectId), targetCommitId, sourceCommitId, squash, 
						committer, author, commitMessage, useOursOnConflict);
			}
			
		});
	}

	@Override
	public boolean isMergedInto(Project project, Map<String, String> gitEnvs, ObjectId base, ObjectId tip) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Boolean call() throws Exception {
				return GitUtils.isMergedInto(getRepository(projectId), gitEnvs, base, tip);
			}
			
		});
	}

	@Override
	public ObjectId rebase(Project project, ObjectId source, ObjectId target, PersonIdent committer) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
				return GitUtils.rebase(getRepository(projectId), source, target, committer);
			}
			
		});
	}

	@Override
	public ObjectId amendCommits(Project project, ObjectId startCommitId, ObjectId endCommitId, 
			String oldCommitterName, PersonIdent newCommitter) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
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
				
			}
			
		});
	}

	@Override
	public ObjectId amendCommit(Project project, ObjectId commitId, PersonIdent author, PersonIdent committer,
			String commitMessage) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
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
			}
			
		});
	}

	@Override
	public List<RevCommit> getReachableCommits(Project project, Collection<ObjectId> startCommitIds, 
			Collection<ObjectId> uninterestingCommitIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<RevCommit>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<RevCommit> call() throws Exception {
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
			}
			
		});
	}

	@Override
	public Collection<String> getChangedFiles(Project project, ObjectId oldCommitId, ObjectId newCommitId, 
			Map<String, String> gitEnvs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<Collection<String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<String> call() throws Exception {
				if (gitEnvs != null && !gitEnvs.isEmpty()) {
					return new ListChangedFilesCommand(getGitDir(projectId), oldCommitId.name(), newCommitId.name(), gitEnvs)
							.run();
				} else {
					Repository repository = getRepository(projectId);
					return GitUtils.getChangedFiles(repository, oldCommitId, newCommitId);
				}
			}
			
		});
	}

	@Override
	public ObjectId getMergeBase(Project project1, ObjectId commitId1, Project project2, ObjectId commitId2) {
		Long projectId1 = project1.getId();
		Long projectId2 = project2.getId();
		String projectPath2 = project2.getPath();
		return runOnProjectServer(projectId1, new ClusterTask<ObjectId>() {

			private static final long serialVersionUID = 1L;

			@Override
			public ObjectId call() throws Exception {
				if (!projectId1.equals(projectId2))
					fetch(projectId1, projectId2, projectPath2, commitId2.name());
				return GitUtils.getMergeBase(getRepository(projectId1), commitId1, commitId2);
			}
			
		});
	}			
	
	@Override
	public boolean hasObjects(Project project, ObjectId... objIds) {
		Long projectId = project.getId();
		
		return runOnProjectServer(projectId, new ClusterTask<Boolean>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Boolean call() throws Exception {
				ObjectDatabase database = getRepository(projectId).getObjectDatabase();
				for (var objId: objIds) {
					if (!database.has(objId))
						return false;
				}
				return true;
			}
			
		});
	}

	@Override
	public List<BlobIdent> getChildren(Project project, ObjectId revId, String path, 
			BlobIdentFilter filter, boolean expandSingle) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<BlobIdent>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<BlobIdent> call() throws Exception {
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
			}
			
		});
	}

	@Override
	public LastCommitsOfChildren getLastCommitsOfChildren(Project project, ObjectId revId, String path) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<LastCommitsOfChildren>() {

			private static final long serialVersionUID = 1L;

			@Override
			public LastCommitsOfChildren call() throws Exception {
				String normalizedPath = path;
				if (normalizedPath == null)
					normalizedPath = "";
				
				final File cacheDir = new File(
						storageManager.getProjectInfoDir(projectId), 
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
				
				org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache cache;
				if (!commitIds.isEmpty()) {
					cache = new org.eclipse.jgit.revwalk.LastCommitsOfChildren.Cache() {
			
						@SuppressWarnings("unchecked")
						@Override
						public Map<String, Value> getLastCommitsOfChildren(ObjectId commitId) {
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
			}
			
		});
	}

	@Override
	public List<DiffEntryFacade> diff(Project project, AnyObjectId oldRevId, AnyObjectId newRevId) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<DiffEntryFacade>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<DiffEntryFacade> call() throws Exception {
				List<DiffEntryFacade> diffEntries = new ArrayList<>();
				for (DiffEntry diffEntry: GitUtils.diff(getRepository(projectId), oldRevId, newRevId)) {
					diffEntries.add(new DiffEntryFacade(
							diffEntry.getChangeType(), 
							diffEntry.getOldPath(), diffEntry.getNewPath(), 
							diffEntry.getOldMode().getBits(), diffEntry.getNewMode().getBits()));
				}
				return diffEntries;
			}
			
		});
	}
	
	@Override
	public Map<ObjectId, AheadBehind> getAheadBehinds(Project project, ObjectId baseId, 
			Collection<ObjectId> compareIds) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<Map<ObjectId, AheadBehind>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Map<ObjectId, AheadBehind> call() throws Exception {
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
			}
			
		});
	}

	@Override
	public Collection<BlameBlock> blame(Project project, ObjectId revId, String file, LinearRange range) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<Collection<BlameBlock>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public Collection<BlameBlock> call() throws Exception {
				return new BlameCommand(storageManager.getProjectGitDir(projectId), revId, file)
						.range(range).run();
			}
			
		});
	}

	@Override
	public byte[] getRawCommit(Project project, ObjectId revId, Map<String, String> envs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<byte[]>() {

			private static final long serialVersionUID = 1L;

			@Override
			public byte[] call() throws Exception {
				File gitDir = storageManager.getProjectGitDir(projectId);
				return new GetRawCommitCommand(gitDir, revId.name(), envs).run();
			}
			
		});
	}

	@Override
	public byte[] getRawTag(Project project, ObjectId tagId, Map<String, String> envs) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<byte[]>() {

			private static final long serialVersionUID = 1L;

			@Override
			public byte[] call() throws Exception {
				File gitDir = storageManager.getProjectGitDir(projectId);
				return new GetRawTagCommand(gitDir, tagId.name(), envs).run();
			}
			
		});
	}

	@Override
	public List<String> revList(Project project, RevListOptions options) {
		Long projectId = project.getId();
		return runOnProjectServer(projectId, new ClusterTask<List<String>>() {

			private static final long serialVersionUID = 1L;

			@Override
			public List<String> call() throws Exception {
				return new RevListCommand(getGitDir(projectId)).options(options).run();
			}
			
		});
	}
	
}
