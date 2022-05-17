package io.onedev.server.buildspec.step;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.hibernate.validator.constraints.NotEmpty;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.MapDifference;
import com.google.common.collect.MapDifference.ValueDifference;
import com.google.common.collect.Maps;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.loader.ListenerRegistry;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.command.Commandline;
import io.onedev.commons.utils.command.LineConsumer;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.BuildSpec;
import io.onedev.server.event.RefUpdated;
import io.onedev.server.git.RefInfo;
import io.onedev.server.model.Build;
import io.onedev.server.model.Project;
import io.onedev.server.util.match.WildcardUtils;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.Interpolative;

@Editable(order=60, name="Pull from Remote", group=StepGroup.REPOSITORY_SYNC, description=""
		+ "This step pulls specified refs from remote. For security reason, it is only allowed "
		+ "to run from default branch")
public class PullRepository extends SyncRepository {

	private static final long serialVersionUID = 1L;
	
	private static final int LFS_FETCH_BATCH = 100;

	private String refs = "refs/heads/* refs/tags/*";
	
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
		return BuildSpec.suggestVariables(matchWith, false, false);
	}
	
	@Override
	public Map<String, byte[]> run(Build build, File inputDir, TaskLogger logger) {
		Project project = build.getProject();
		if (!project.isCommitOnBranches(build.getCommitId(), project.getDefaultBranch())) 
			throw new ExplicitException("For security reason, this step is only allowed to run from default branch");
		
		String remoteUrl = getRemoteUrlWithCredential(build);
		
		Commandline git = newGit(project);
		
		String defaultBranch = project.getDefaultBranch();
		ObjectId baseCommitId = project.getRevCommit(defaultBranch, true).copy();
		
		Map<String, ObjectId> oldCommitIds = getCommitIds(project);
		
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
		} finally {
			Map<String, ObjectId> newCommitIds = getCommitIds(project);
			MapDifference<String, ObjectId> difference = Maps.difference(oldCommitIds, newCommitIds);

			try {
				if (isWithLfs()) {
					List<ObjectId> lfsFetchCommitIds = new ArrayList<>();
					
					try (RevWalk revWalk = new RevWalk(project.getRepository())) {
						if (!difference.entriesOnlyOnRight().isEmpty()) {
							for (Map.Entry<String, ObjectId> entry: difference.entriesOnlyOnRight().entrySet()) {
								revWalk.markStart(revWalk.lookupCommit(entry.getValue()));
							}
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
					
					for (List<ObjectId> batch: Lists.partition(lfsFetchCommitIds, LFS_FETCH_BATCH)) {
						git.clearArgs();
						git.addArgs("lfs", "fetch", remoteUrl);
						for (ObjectId commitId: batch)
							git.addArgs(commitId.name());
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
					registry.post(new RefUpdated(project, entry.getKey(), entry.getValue(), ObjectId.zeroId()));
				for (Map.Entry<String, ObjectId> entry: difference.entriesOnlyOnRight().entrySet()) 
					registry.post(new RefUpdated(project, entry.getKey(), ObjectId.zeroId(), entry.getValue()));
				for (Map.Entry<String, ValueDifference<ObjectId>> entry: difference.entriesDiffering().entrySet()) {
					registry.post(new RefUpdated(project, entry.getKey(), 
							entry.getValue().leftValue(), entry.getValue().rightValue()));
				}
			}
		}

		return null;
	}
	
	private Map<String, ObjectId> getCommitIds(Project project) {
		Map<String, ObjectId> commitIds = new HashMap<>();
		for (RefInfo refInfo: project.getBranchRefInfos()) {
			boolean matches = false;
			for (String pattern: Splitter.on(" ").omitEmptyStrings().trimResults().split(getRefs())) {
				if (WildcardUtils.matchString(pattern, refInfo.getRef().getName())) {
					matches = true;
					break;
				}
			}
			if (matches) 
				commitIds.put(refInfo.getRef().getName(), refInfo.getPeeledObj().copy());
		}
		return commitIds;
	}
	
}
