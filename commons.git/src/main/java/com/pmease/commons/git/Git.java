package com.pmease.commons.git;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.eclipse.jgit.lib.Constants;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.pmease.commons.git.command.AddCommand;
import com.pmease.commons.git.command.AddNoteCommand;
import com.pmease.commons.git.command.AddSubModuleCommand;
import com.pmease.commons.git.command.AheadBehindCommand;
import com.pmease.commons.git.command.BlameCommand;
import com.pmease.commons.git.command.CalcMergeBaseCommand;
import com.pmease.commons.git.command.CheckoutCommand;
import com.pmease.commons.git.command.CherryPickCommand;
import com.pmease.commons.git.command.CloneCommand;
import com.pmease.commons.git.command.CommitCommand;
import com.pmease.commons.git.command.ConsumeCommand;
import com.pmease.commons.git.command.DeleteRefCommand;
import com.pmease.commons.git.command.FetchCommand;
import com.pmease.commons.git.command.InitCommand;
import com.pmease.commons.git.command.IsAncestorCommand;
import com.pmease.commons.git.command.IsBinaryCommand;
import com.pmease.commons.git.command.IsTreeLinkCommand;
import com.pmease.commons.git.command.ListBranchCommand;
import com.pmease.commons.git.command.ListChangedFilesCommand;
import com.pmease.commons.git.command.ListCherriesCommand;
import com.pmease.commons.git.command.ListSubModulesCommand;
import com.pmease.commons.git.command.LogCommand;
import com.pmease.commons.git.command.MergeCommand;
import com.pmease.commons.git.command.MergeCommand.FastForwardMode;
import com.pmease.commons.git.command.MoveCommand;
import com.pmease.commons.git.command.ParseRevisionCommand;
import com.pmease.commons.git.command.PushCommand;
import com.pmease.commons.git.command.RemoveCommand;
import com.pmease.commons.git.command.ResetCommand;
import com.pmease.commons.git.command.ShowCommand;
import com.pmease.commons.git.command.ShowSymbolicRefCommand;
import com.pmease.commons.git.command.UpdateRefCommand;
import com.pmease.commons.git.command.UpdateSymbolicRefCommand;
import com.pmease.commons.util.GeneralException;
import com.pmease.commons.util.execution.StreamConsumer;

@SuppressWarnings("serial")
public class Git implements Serializable {

	private final File repoDir;

	public Git(File repoDir) {
		this.repoDir = repoDir;
	}

	public File repoDir() {
		return repoDir;
	}

	/**
	 * Create branch even in a bare repository.
	 * 
	 * @param branchName
	 * 			name of the branch to create
	 * @param commitHash
	 * 			commit hash of the branch 
	 * @return
	 * 			this git object
	 */
	public Git createBranch(String branchName, String commitHash) {
		if (new ListBranchCommand(repoDir).call().contains(branchName))
			throw new GeneralException(String.format("Branch %s already exists.", branchName));

		new UpdateRefCommand(repoDir).refName(Constants.R_HEADS + branchName).revision(commitHash)
				.oldRevision(GitUtils.NULL_SHA1).call();
		
		return this;
	}

	/**
	 * delete ref even in a bare repository.
	 * 
	 * @param refName
	 * 			name of the ref to delete
	 * @return
	 * 			this git object
	 */
	public Git deleteRef(String refName) {
		new DeleteRefCommand(repoDir).refName(refName).call();
		return this;
	}

	public Commit showRevision(String revision) {
		List<Commit> commits = new LogCommand(repoDir).revisions(Lists.newArrayList(revision)).count(1).call();
		Preconditions.checkState(commits.size() == 1);
		return commits.get(0);
	}

	public Git init(boolean bare) {
		new InitCommand(repoDir).bare(bare).call();
		return this;
	}

	public Git add(String... paths) {
		new AddCommand(repoDir).addPaths(paths).call();
		return this;
	}
	
	public Git rm(String... paths) {
		new RemoveCommand(repoDir).paths(paths).call();
		return this;
	}
	
	public Git mv(String source, String destination) {
		new MoveCommand(repoDir).source(source).destination(destination).call();
		return this;
	}

	public Git clone(String from, boolean bare, boolean shared, boolean noCheckout, @Nullable String branch) {
		new CloneCommand(repoDir).from(from).bare(bare).shared(shared).noCheckout(noCheckout).branch(branch).call();
		return this;
	}

	public Git clone(Git from, boolean bare, boolean shared, boolean noCheckout, @Nullable String branch) {
		new CloneCommand(repoDir)
				.from(from.repoDir.getAbsolutePath()).bare(bare)
				.shared(shared).noCheckout(noCheckout).branch(branch).call();
		return this;
	}

	public Git reset(@Nullable String mode, @Nullable String commit) {
		new ResetCommand(repoDir).mode(mode).commit(commit).call();
		return this;
	}

	public Git commit(String message, boolean add, boolean amend) {
		new CommitCommand(repoDir).message(message).amend(amend).call();
		return this;
	}

	public Collection<String> listChangedFiles(String fromRev, String toRev, @Nullable String path) {
		return new ListChangedFilesCommand(repoDir).fromRev(fromRev).toRev(toRev).path(path).call();
	}

	public Git checkout(String revision, @Nullable String newBranch) {
		new CheckoutCommand(repoDir).revision(revision).newBranch(newBranch).call();
		return this;
	}

	/**
	 * Update specified symbolic ref name with specified ref name.
	 * 
	 * @param symbolicRefName
	 * 			symbolic ref name to be updated
	 * @param refName
	 * 			ref name to point to
	 * @param reason
	 * 			reason to update the symbolic ref
	 */
	public Git updateSymbolicRef(String symbolicRefName, String refName, @Nullable String reason) {
		new UpdateSymbolicRefCommand(repoDir).symbolicRefName(symbolicRefName).refName(refName)
				.reason(reason).call();
		return this;
	}

	/**
	 * Show ref name for specified symbolic ref name.
	 * 
	 * @param symbolicRefName
	 * 			symbolic ref name to be shown
	 * @return
	 * 			ref name of specified symbolic ref name
	 */
	public String showSymbolicRef(String symbolicRefName) {
		return new ShowSymbolicRefCommand(repoDir).symbolicRefName(symbolicRefName).call();
	}

	/**
	 * Update specified ref with specified revision.
	 * 
	 * @param refName
	 * 			reference to be updated
	 * @param revision
	 * 			new revision of the reference
	 * @param oldRevision
	 * 			optional old revision of the reference
	 * @param reason
	 * 			reason to update the ref
	 * @return
	 * 			<tt>true</tt> if success, or <tt>false</tt> if the reference 
	 * 			can not be locked due to old revision not matching
	 */
	public Git updateRef(String refName, String revision, 
			@Nullable String oldRevision, @Nullable String reason) {
		new UpdateRefCommand(repoDir).refName(refName)
				.revision(revision).oldRevision(oldRevision)
				.reason(reason).call();
		return this;
	}
	
	public Git deleteRef(String refName, @Nullable String oldRevision, @Nullable String reason) {
		new DeleteRefCommand(repoDir).refName(refName).oldRevision(oldRevision).reason(reason).call();
		return this;
	}

	public String merge(String revision, @Nullable FastForwardMode fastForwardMode, 
			@Nullable String strategy, @Nullable String strategyOption, @Nullable String message) {
		return new MergeCommand(repoDir).revision(revision).fastForwardMode(fastForwardMode)
				.strategy(strategy).strategyOption(strategyOption).message(message).call();
	}

	public String squash(String revision, @Nullable String strategy, @Nullable String strategyOption, 
			@Nullable String message) {
		return new MergeCommand(repoDir).revision(revision).squash(true)
				.strategy(strategy).strategyOption(strategyOption).message(message).call();
	}

	public Git fetch(String from, String... refspec) {
		new FetchCommand(repoDir).from(from).refspec(refspec).call();
		return this;
	}
	
	public Git fetch(Git from, String... refspec) {
		new FetchCommand(repoDir).from(from.repoDir.getAbsolutePath()).refspec(refspec).call();
		return this;
	}

	public Git push(String to, String... refspec) {
		new PushCommand(repoDir).to(to).refspec(refspec).call();
		return this;
	}

	public Git push(Git to, String... refspec) {
		new PushCommand(repoDir).to(to.repoDir.getAbsolutePath()).refspec(refspec).call();
		return this;
	}

	/**
	 * Parse commit hash of specified revision. 
	 * 
	 * @param revision
	 * 			revision to be parsed
	 * @param shouldExist
	 * 			indicate whether or not the revision should already exist
	 * @return
	 * 			commit hash of specified revision, or <tt>null</tt> if revision 
	 * 			does not exist, and <tt>shouldExist</tt> is specified as 
	 * 			<tt>false</tt>
	 * @throws
	 * 			IllegalStateException if specified revision does not exist 
	 * 			while <tt>shouldExist<tt> is specified as <tt>true</tt>
	 */
	public @Nullable String parseRevision(String revision, boolean shouldExist) {
		String commit = new ParseRevisionCommand(repoDir).revision(revision).call();
		if (commit == null) {
			if (shouldExist)
				throw new IllegalStateException();
			else
				return null;
		} else {
			return commit;
		}
	}

	public String calcMergeBase(String rev1, String rev2) {
		return new CalcMergeBaseCommand(repoDir).rev1(rev1).rev2(rev2).call();
	}
	
	/**
	 * Check if specified param <tt>ancestor</tt> is ancestor of specified param <tt>descendant</tt>.
	 * 
	 * @param ancestor
	 * 			ancestor to be checked
	 * @param descendant
	 * 			descendant to be checked 
	 * @return
	 * 			<tt>true</tt> if first param is ancestor of second param
	 */
	public boolean isAncestor(String ancestor, String descendant) {
		if (ancestor.equals(descendant))
			return true;
		else
			return new IsAncestorCommand(repoDir).ancestor(ancestor).descendant(descendant).call();
	}
	
	public Map<String, AheadBehind> getAheadBehinds(String baseRev, String... compareRevs) {
		AheadBehindCommand cmd = new AheadBehindCommand(repoDir);
		return cmd.baseCommit(baseRev).compareRevs(compareRevs).call();
	}
	
	/**
	 * Show content of specified path of specified revision.
	 * 
	 * @param revision
	 * 			revision of the path
	 * @param path
	 * 			path to show content
	 * @param consumer
	 * 			consumer to absorb content of specified path and revision
	 *
	 */
	public Git consume(String revision, String path, StreamConsumer consumer) {
		new ConsumeCommand(repoDir).revision(revision).path(path).consumer(consumer).call();
		return this;
	}
	
	public byte[] show(String revision, String path) {
		return new ShowCommand(repoDir).revision(revision).path(path).call();
	}
	
	public List<Commit> log(@Nullable String fromRev, @Nullable String toRev, 
			@Nullable String path, int maxCount, int skip, boolean listChangedFiles) {
		List<String> paths = new ArrayList<>();
		if (path != null)
			paths.add(path);
		
		List<String> revisions = new ArrayList<>();
		if (fromRev != null && toRev != null)
			revisions.add(fromRev + ".." + toRev);
		else if (fromRev != null && toRev == null)
			revisions.add(fromRev + "..");
		else if (fromRev == null && toRev != null)
			revisions.add(toRev);
		return new LogCommand(repoDir).revisions(revisions)
				.paths(paths).count(maxCount).skip(skip)
				.listChangedFiles(listChangedFiles).call();
	}
	
	public List<Commit> log(@Nullable Date after, @Nullable Date before, 
			@Nullable String path, int maxCount, int skip) {
		List<String> paths = new ArrayList<>();
		if (path != null)
			paths.add(path);
		return new LogCommand(repoDir).after(after).before(before)
				.paths(paths).count(maxCount).skip(skip).call();
	}
	
	public Commit retrieveLastCommmit(String revision, @Nullable String path) {
		return log(null, revision, path, 1, 0, false).get(0);
	}
	
	public Git addNote(String object, String message) {
		new AddNoteCommand(repoDir).object(object).message(message).call();
		return this;
	}
	
	/**
	 * Display commit information for every region of specified commit and file. 
	 * 
	 * @param file
	 * 			file for blame
	 * @param commitHash
	 * 			commit hash of the file for blame
	 * @return
	 * 			map of commit hash to commit blame
	 */
	public Map<String, Blame> blame(String commitHash, String file) {
		return new BlameCommand(repoDir).commitHash(commitHash).file(file).call();
	}

	public boolean isBinary(String file, String revision) {
		return new IsBinaryCommand(repoDir).file(file).revision(revision).call();
	}
	
	public boolean isTreeLink(String symlink, String revision) {
		return new IsTreeLinkCommand(repoDir).symlink(symlink).revision(revision).call();
	}
	
	public Git addSubModule(String url, String path) {
		new AddSubModuleCommand(repoDir).url(url).path(path).call();
		return this;
	}
	
	public Map<String, String> listSubModules(String revision) {
		return new ListSubModulesCommand(repoDir).revision(revision).call();
	}

	public String resolveDefaultBranch() {
		String refName = showSymbolicRef("HEAD");
		Preconditions.checkState(refName.startsWith(Constants.R_HEADS));
		return refName.substring(Constants.R_HEADS.length());
	}

	public void updateDefaultBranch(String defaultBranch) {
		updateSymbolicRef("HEAD", Constants.R_HEADS + defaultBranch, null);
	}
	
	public String cherryPick(String...revisions) {
		return new CherryPickCommand(repoDir).revisions(revisions).call();
	}

	public List<String> listCherries(String fromRev, String toRev) {
		return new ListCherriesCommand(repoDir).fromRev(fromRev).toRev(toRev).call();
	}

	public boolean hasCommits() {
		File headsDir = new File(repoDir, Constants.R_HEADS);
		return headsDir.exists() && headsDir.list().length != 0 || new File(repoDir, "packed-refs").exists();
	}
	
	public boolean isValid() {
		return new File(repoDir, "objects").exists();
	}
	
	@Override
	public String toString() {
		return repoDir.toString();
	}
}
