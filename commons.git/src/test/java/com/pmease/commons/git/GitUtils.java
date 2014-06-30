package com.pmease.commons.git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.annotation.Nullable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.MergeCommand.FastForwardMode;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.RefUpdate;
import org.eclipse.jgit.lib.RefUpdate.Result;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeMessageFormatter;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.Merger;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.util.SystemReader;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

public class GitUtils {

	public static <T> T call(GitCommand<T> command) {
		try {
			return command.call();
		} catch (GitAPIException e) {
			throw new RuntimeException(e);
		}
	}

	public static AbstractTreeIterator getTreeIterator(Repository repo,
			AnyObjectId commitId) {
		CanonicalTreeParser p = new CanonicalTreeParser();
		ObjectReader or = repo.newObjectReader();
		try {
			p.reset(or, new RevWalk(repo).parseTree(commitId));
			return p;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			or.release();
		}
	}

	public static byte[] readFile(Repository repo, AnyObjectId commitId,
			String path) {
		RevWalk revWalk = new RevWalk(repo);
		try {
			RevCommit commit = revWalk.parseCommit(commitId);
			TreeWalk treeWalk = TreeWalk
					.forPath(repo, "file", commit.getTree());
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				repo.open(treeWalk.getObjectId(0)).copyTo(baos);
				return baos.toByteArray();
			} finally {
				treeWalk.release();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			revWalk.release();
		}
	}

	/**
	 * Rebase specified commits in specified repository. Unlike the ordinary RebaseCommand, this 
	 * does not require a working tree, and can operate against a bare repository. 
	 * 
	 * @param repo
	 * 			repository to rebase commits inside
	 * @param from
	 * 			the commit from which to pick commits
	 * @param to
	 * 			the commit on top of which to put picked commits
	 * @param committer
	 *			committer of new commits 
	 * @return
	 * 			tip commit id of rebased commits. It might have below values:
	 * 			<ul>
	 * 			<li> same value as param <tt>to</tt> if <tt>from</tt> is already merged to <tt>to</tt>
	 * 			<li> same value as param <tt>from</tt> if <tt>to</tt> can be fast forwarded to 
	 * 			<tt>from</tt>, and every commit in the path from <tt>from</tt> to <tt>to</tt> has 
	 * 			single parent
	 * 			<li> tip commit id of rebased commits on top of <tt>to</tt> otherwise      
	 */
	public static AnyObjectId rebaseCommits(Repository repo, AnyObjectId from,
			AnyObjectId to, PersonIdent committer) {
		LogCommand log = new Git(repo).log();
		try {
			log.addRange(to, from);
		} catch (MissingObjectException | IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		}

		List<RevCommit> commitsToPick = new ArrayList<>();

		for (RevCommit commit : call(log)) {
			if (commit.getParentCount() == 1)
				commitsToPick.add(commit);
		}

		Collections.reverse(commitsToPick);

		AnyObjectId currentId = to;

		final ObjectInserter inserter = repo.newObjectInserter();
		try {
			for (RevCommit commitToPick : commitsToPick) {
				if (commitToPick.getParent(0).equals(currentId)) {
					currentId = commitToPick;
					continue;
				}

				ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(repo,
						true);
				merger.setObjectInserter(new ObjectInserter.Filter() {

					@Override
					protected ObjectInserter delegate() {
						return inserter;
					}
				});
				merger.setBase(commitToPick.getParent(0));

				if (merger.merge(currentId, commitToPick)) {
					CommitBuilder cb = new CommitBuilder();
					cb.setTreeId(Preconditions.checkNotNull(merger
							.getResultTreeId()));
					cb.setParentId(currentId);
					cb.setAuthor(commitToPick.getAuthorIdent());
					cb.setMessage(commitToPick.getFullMessage());
					cb.setCommitter(committer);
					currentId = inserter.insert(cb);
					inserter.flush();
				} else {
					return null;
				}
			}

			return currentId;
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			inserter.release();
		}
	}

	public static void updateRef(Repository repo, String refName, ObjectId newId, 
			@Nullable ObjectId oldID, boolean force, String refLogMessage) {
		try {
			RefUpdate refUpdate = repo.updateRef(refName);
			refUpdate.setNewObjectId(newId);
			refUpdate.setRefLogMessage(refLogMessage, false);
			refUpdate.setExpectedOldObjectId(oldID);
			refUpdate.setForceUpdate(force);
			Result rc = refUpdate.update();
			if (rc != Result.NEW && rc != Result.FAST_FORWARD && rc != Result.FORCED && rc != Result.NO_CHANGE) {
				throw new RuntimeException(String.format("Failed to update ref (ref: %s, result: %s).", refName, rc));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void deleteRef(Repository repo, String refName, @Nullable ObjectId oldID, String refLogMessage) {
		try {
			RefUpdate refUpdate = repo.updateRef(refName);
			refUpdate.setRefLogMessage(refLogMessage, false);
			refUpdate.setForceUpdate(true);
			refUpdate.setExpectedOldObjectId(oldID);
			Result rc = refUpdate.delete();
			System.out.println(rc);
			if (rc != Result.NO_CHANGE && rc != Result.FORCED) {
				throw new RuntimeException(String.format("Failed to delete ref (ref: %s, result: %s).", refName, rc));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Merge specified commit in specified repository. Unlike the ordinary merge command, this 
	 * does not need a working tree and can operate against a bare repository. 
	 * 
	 * @param repo
	 * 			repository to merge commits inside
	 * @param from
	 * 			commit to merge from
	 * @param to
	 * 			commit to merge to
	 * @param strategy
	 * 			merge strategy to be used
	 * @param ffMode
	 * 			fast forward mode to be used
	 * @param author
	 * 			author of the new merge commit if it has to be created 
	 * @param committer
	 * 			committer of the new merge commit if it has to be created
	 * @param commitMsg
	 * 			commit message of the new merge commit if it has to be created
	 * @return
	 * 			id of the resulting merge commit, it has below possible values:
	 * 			<ul>
	 * 			<li> same value as param <tt>to</tt> if <tt>from</tt> is already 
	 * 			merged into <tt>to</tt>
	 * 			<li> same value as param <tt>from</tt> if <tt>to</tt> can be fast
	 * 			forwarded to <tt>from</tt> and if param <tt>ffMode</tt> allows 
	 * 			the fast forward
	 * 			<li> id of the new merge commit if <tt>from</tt> and <tt>to</tt> 
	 * 			is diverged, or if <tt>to</tt> can be fast forwarded to <tt>from</tt>, 
	 * 			but param <tt>ffMode</tt> is set to <tt>NO_FF</tt>. Note that 
	 * 			the new commit is not attached to any ref, and you will need to 
	 * 			assign it to some ref in order not to be garbage collected in a 
	 * 			later time 
	 * 			<li><tt>null</tt> if <tt>from</tt> and <tt>to</tt> is diverged, and 
	 * 			if there are merge conflicts between them
	 * @throws
	 * 			RuntimeException if param <tt>ffMode</tt> is set to <tt>FF_ONLY</tt> 
	 * 			and <tt>to</tt> can not be fast forwarded to <tt>from</tt> 
	 */
	public static AnyObjectId mergeCommits(Repository repo, AnyObjectId from, AnyObjectId to, 
			MergeStrategy strategy, FastForwardMode ffMode, PersonIdent author, 
			PersonIdent committer, String commitMsg) {
		RevWalk revWalk = new RevWalk(repo);
		final ObjectInserter[] inserter = new ObjectInserter[]{null};
		try {
			RevCommit fromCommit = revWalk.parseCommit(from);
			RevCommit toCommit = revWalk.parseCommit(to);

			AnyObjectId treeId;
			if (revWalk.isMergedInto(fromCommit, toCommit)) {
				return to;
			} else if (revWalk.isMergedInto(toCommit, fromCommit)) {
				if (ffMode != FastForwardMode.NO_FF) {
					return from;
				} else {
					treeId = fromCommit.getTree().getId();
				}
			} else if (ffMode == FastForwardMode.FF_ONLY) {
				throw new RuntimeException("Fast forward mode is set to ff_only, "
						+ "but from revision can not be fast forwarded to to revision.");
			} else {
				inserter[0] = repo.newObjectInserter();
				Merger merger = strategy.newMerger(repo, true);
				merger.setObjectInserter(new ObjectInserter.Filter() {
	
					@Override
					protected ObjectInserter delegate() {
						return inserter[0];
					}
				});
	
				if (merger.merge(to, from)) {
					treeId = Preconditions.checkNotNull(merger.getResultTreeId());
				} else {
					return null;
				}
			}
			
			if (inserter[0] == null)
				inserter[0] = repo.newObjectInserter();
			CommitBuilder cb = new CommitBuilder();
			cb.setTreeId(treeId);
			cb.setParentId(to);
			cb.addParentId(from);
			cb.setAuthor(author);
			cb.setMessage(commitMsg);
			cb.setCommitter(committer);
			AnyObjectId mergedCommit = inserter[0].insert(cb);
			inserter[0].flush();
			return mergedCommit;
		} catch (IOException e2) {
			throw new RuntimeException(e2);
		} finally {
			if (inserter[0] != null)
				inserter[0].release();
			revWalk.release();
		}
	}
	
	public static String getMergeMessage(Ref from, Ref to) {
		return new MergeMessageFormatter().format(Lists.newArrayList(from), to);
	}
	
	public static RevCommit parseCommit(Repository repo, AnyObjectId commitId) {
		RevWalk revWalk = new RevWalk(repo);
		try {
			return revWalk.parseCommit(commitId);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			revWalk.release();
		}
	}
	
	public static PersonIdent newPersonIdent(String name, String email, Date when) {
		return new PersonIdent(name, email, when.getTime(), 
				SystemReader.getInstance().getTimezone(when.getTime()));
	}
}
