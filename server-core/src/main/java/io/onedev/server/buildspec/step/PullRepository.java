package io.onedev.server.buildspec.step;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.StringUtils;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.event.ListenerRegistry;
import io.onedev.server.event.project.RefUpdated;
import io.onedev.server.git.service.RefFacade;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.EditContext;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.editable.annotation.ChoiceProvider;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;
import io.onedev.server.web.editable.annotation.ShowCondition;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;

import javax.validation.constraints.NotEmpty;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Editable(order=60, name="Pull from Remote", group=StepGroup.REPOSITORY_SYNC, description=""
		+ "This step pulls specified refs from remote. For security reason, it is only allowed "
		+ "to run from default branch")
public class PullRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;
	
	private static final int LFS_FETCH_BATCH = 100;

	private boolean syncToChildProject;
	
	private String childProject;

	private String refs = "refs/heads/* refs/tags/*";
	
	@Editable(order=205, description="If enabled, sync to child project instead of current project")
	public boolean isSyncToChildProject() {
		return syncToChildProject;
	}

	public void setSyncToChildProject(boolean syncToChildProject) {
		this.syncToChildProject = syncToChildProject;
	}

	@Editable(order=210, description="Select child project to sync to")
	@ShowCondition("isSyncToChildProjectEnabled")
	@ChoiceProvider("getChildProjects")
	@NotEmpty
	public String getChildProject() {
		return childProject;
	}

	public void setChildProject(String childProject) {
		this.childProject = childProject;
	}

	@SuppressWarnings("unused")
	private static boolean isSyncToChildProjectEnabled() {
		return (boolean) EditContext.get().getInputValue("syncToChildProject");
	}
	
	@SuppressWarnings("unused")
	private static List<String> getChildProjects() {
		int prefixLen = Project.get().getPath().length() + 1;
		List<String> choices = new ArrayList<>(Project.get().getDescendants()
				.stream().map(pr -> pr.getPath().substring(prefixLen)).collect(Collectors.toList()));
		Collections.sort(choices);
		return choices;
	}

	@Editable(order=410, description="Specify space separated refs to pull from remote. '*' can be used in ref "
			+ "name for wildcard match<br>"
			+ "<b class='text-danger'>NOTE:</b> branch/tag protection rule will be ignored when update "
			+ "branches/tags via this step")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getRefs() {
		return refs;
	}

	public void setRefs(String refs) {
		this.refs = refs;
	}

	static List<InputSuggestion> suggestVariables(String matchWith) {
		return BuildSpec.suggestVariables(matchWith, false, false, false);
	}
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		Project buildProject = build.getProject();
		if (!buildProject.isCommitOnBranches(build.getCommitId(), buildProject.getDefaultBranch()))
			throw new ExplicitException("For security reason, this step is only allowed to run from default branch");
		
		String remoteUrl = getRemoteUrlWithCredential(build);
		Project targetProject = getTargetProject(build);
		
		Commandline git = newGit(targetProject);
		
		String defaultBranch = targetProject.getDefaultBranch();
		ObjectId baseCommitId;
		if (defaultBranch != null)
			baseCommitId = targetProject.getRevCommit(defaultBranch, true).copy();
		else
			baseCommitId = null;
		
		Map<String, ObjectId> oldCommitIds = getCommitIds(targetProject);
		
		git.addArgs("fetch");
		
		git.addArgs(remoteUrl);
		if (isForce())
			git.addArgs("--force");
		
		for (String each: Splitter.on(' ').omitEmptyStrings().trimResults().split(getRefs()))
			git.addArgs(each + ":" + each);

		try {
			git.execute(new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.log(line);
				}
				
			}, new LineConsumer() {
	
				@Override
				public void consume(String line) {
					logger.warning(line);
				}
				
			}).checkReturnCode();
			
			if (defaultBranch == null) {
				logger.log("Determining remote head branch...");
				
				git.clearArgs();
				git.addArgs("remote", "show", remoteUrl);
				
				AtomicReference<String> headBranch = new AtomicReference<>(null);
				git.execute(new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.log(line);
						if (line.trim().startsWith("HEAD branch:"))
							headBranch.set(line.trim().substring("HEAD branch:".length()).trim());
					}
					
				}, new LineConsumer() {

					@Override
					public void consume(String line) {
						logger.warning(line);
					}
					
				}).checkReturnCode();

				if (headBranch.get() != null) {
					if (targetProject.getObjectId(Constants.R_HEADS + headBranch.get(), false) == null) {
						logger.log("Remote head branch not synced, using first branch as default");
						headBranch.set(null);
					}
				} else {
					logger.log("Remote head branch not found, using first branch as default");
				}
				if (headBranch.get() == null) {
					git.clearArgs();
					git.addArgs("branch");
					
					git.execute(new LineConsumer() {

						@Override
						public void consume(String line) {
							if (headBranch.get() == null)
								headBranch.set(StringUtils.stripStart(line.trim(), "*").trim());
						}
						
					}, new LineConsumer() {

						@Override
						public void consume(String line) {
							logger.warning(line);
						}
						
					}).checkReturnCode();
				}
				if (headBranch.get() != null)
					targetProject.setDefaultBranch(headBranch.get());
			}
		} finally {
			Map<String, ObjectId> newCommitIds = getCommitIds(targetProject);
			MapDifference<String, ObjectId> difference = Maps.difference(oldCommitIds, newCommitIds);

			try {
				if (isWithLfs()) {
					List<ObjectId> lfsFetchCommitIds = new ArrayList<>();
					
					if (baseCommitId != null) {
						Repository repository = OneDev.getInstance(ProjectManager.class)
								.getRepository(targetProject.getId());
						try (RevWalk revWalk = new RevWalk(repository)) {
							if (!difference.entriesOnlyOnRight().isEmpty()) {
								for (Map.Entry<String, ObjectId> entry: difference.entriesOnlyOnRight().entrySet()) {
									revWalk.markStart(revWalk.lookupCommit(entry.getValue()));
								}
								if (baseCommitId != null)
									revWalk.markUninteresting(revWalk.lookupCommit(baseCommitId));
							}
							
							for (Map.Entry<String, ValueDifference<ObjectId>> entry: difference.entriesDiffering().entrySet()) {
								revWalk.markStart(revWalk.lookupCommit(entry.getValue().rightValue()));
								revWalk.markUninteresting(revWalk.lookupCommit(entry.getValue().leftValue()));
							}
							
							RevCommit commit;
							while ((commit = revWalk.next()) != null) 
								lfsFetchCommitIds.add(commit.copy());
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					} else {
						lfsFetchCommitIds.addAll(newCommitIds.values());
					}
					
					for (List<ObjectId> batch: Lists.partition(lfsFetchCommitIds, LFS_FETCH_BATCH)) {
						git.clearArgs();
						git.addArgs("lfs", "fetch", remoteUrl);
						for (ObjectId commitId: batch)
							git.addArgs(commitId.name());
						if (baseCommitId == null)
							git.addArgs("--all");
						git.execute(new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.log(line);
							}
							
						}, new LineConsumer() {

							@Override
							public void consume(String line) {
								logger.warning(line);
							}
							
						}).checkReturnCode();
					}
				}
			} finally {
				ListenerRegistry registry = OneDev.getInstance(ListenerRegistry.class);
				for (Map.Entry<String, ObjectId> entry: difference.entriesOnlyOnLeft().entrySet())
					registry.post(new RefUpdated(targetProject, entry.getKey(), entry.getValue(), ObjectId.zeroId()));
				for (Map.Entry<String, ObjectId> entry: difference.entriesOnlyOnRight().entrySet())
					registry.post(new RefUpdated(targetProject, entry.getKey(), ObjectId.zeroId(), entry.getValue()));
				for (Map.Entry<String, ValueDifference<ObjectId>> entry: difference.entriesDiffering().entrySet()) {
					registry.post(new RefUpdated(targetProject, entry.getKey(),
							entry.getValue().leftValue(), entry.getValue().rightValue()));
				}
			}
		}

		return null;
	}
	
	private Map<String, ObjectId> getCommitIds(Project project) {
		Map<String, ObjectId> commitIds = new HashMap<>();
		for (RefFacade ref: project.getBranchRefs()) {
			boolean matches = false;
			for (String pattern: Splitter.on(" ").omitEmptyStrings().trimResults().split(getRefs())) {
				if (WildcardUtils.matchString(pattern, ref.getName())) {
					matches = true;
					break;
				}
			}
			if (matches) 
				commitIds.put(ref.getName(), ref.getPeeledObj().copy());
		}
		return commitIds;
	}
	
	private Project getTargetProject(Build build) {
		Project buildProject = build.getProject();
		if (isSyncToChildProject()) {
			String projectPath = buildProject.getPath() + "/" + getChildProject();
			Project childProject = OneDev.getInstance(ProjectManager.class).findByPath(projectPath);
			if (childProject == null)
				throw new ExplicitException("Unable to find child project: " + getChildProject());
			return childProject;
		} else {
			return buildProject;
		}
	}
}
