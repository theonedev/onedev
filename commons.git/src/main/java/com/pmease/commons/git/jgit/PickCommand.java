package com.pmease.commons.git.jgit;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.GitCommand;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.lib.CommitBuilder;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectInserter;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.merge.MergeStrategy;
import org.eclipse.jgit.merge.ThreeWayMerger;
import org.eclipse.jgit.revwalk.RevCommit;

import com.google.common.base.Preconditions;

public class PickCommand extends GitCommand<AnyObjectId> {

	private ObjectId to;
	
	private ObjectId from;
	
	private PersonIdent committer;
	
	public PickCommand(Repository repo) {
		super(repo);
	}

	public PickCommand setTo(ObjectId to) {
		this.to = to;
		return this;
	}

	public PickCommand setFrom(ObjectId from) {
		this.from = from;
		return this;
	}
	
	public PickCommand setCommitter(PersonIdent committer) {
		this.committer = committer;
		return this;
	}

	@Override
	public ObjectId call() throws GitAPIException {
		Preconditions.checkNotNull(to, "Upstream has to be specified.");
		Preconditions.checkNotNull(from, "Downstream has to be specified.");
		Preconditions.checkNotNull("committer", "Committer has to be specified.");
		
		checkCallable();
		
		LogCommand log = new Git(getRepository()).log();
		try {
			log.addRange(to, from);
		} catch (MissingObjectException | IncorrectObjectTypeException e) {
			throw new RuntimeException(e);
		}
		
		List<RevCommit> commitsToPick = new ArrayList<>();

		for (RevCommit commit: log.call()) {
			if (commit.getParentCount() == 1)
				commitsToPick.add(commit);
		}

		Collections.reverse(commitsToPick);
		
		ObjectId currentId = to;

		final ObjectInserter inserter = getRepository().newObjectInserter();
		try {
			for (RevCommit commitToPick: commitsToPick) {
				if (commitToPick.getParent(0).equals(currentId)) {
					currentId = commitToPick;
					continue;
				}
	
				ThreeWayMerger merger = MergeStrategy.RECURSIVE.newMerger(getRepository(), true);
				merger.setObjectInserter(new ObjectInserter.Filter() {
					
					@Override
					protected ObjectInserter delegate() {
						return inserter;
					}
				});
				merger.setBase(commitToPick.getParent(0));
				
				if (merger.merge(currentId, commitToPick)) {
					CommitBuilder cb = new CommitBuilder();
				    cb.setTreeId(Preconditions.checkNotNull(merger.getResultTreeId()));
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

}
