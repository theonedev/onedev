package com.pmease.gitplex.web.component.diff;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;

import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.AnyObjectId;
import org.eclipse.jgit.util.io.NullOutputStream;

import com.pmease.commons.git.LineProcessor;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public class RevisionDiffPanel extends Panel {

	private final IModel<Repository> repoModel;
	
	private final String oldRev;
	
	private final String newRev;
	
	private final InlineCommentSupport commentSupport;
	
	private LineProcessOption lineProcessor = LineProcessOption.IGNORE_NOTHING;

	private IModel<List<BlobChange>> changesModel = new LoadableDetachableModel<List<BlobChange>>() {

		@Override
		protected List<BlobChange> load() {
			List<BlobChange> changes = new ArrayList<>();
			try (	FileRepository jgitRepo = repoModel.getObject().openAsJGitRepo();
					DiffFormatter diffFormatter = new DiffFormatter(NullOutputStream.INSTANCE);) {
		    	diffFormatter.setRepository(jgitRepo);
				AnyObjectId oldCommitId = repoModel.getObject().getObjectId(oldRev);
				AnyObjectId newCommitId = repoModel.getObject().getObjectId(newRev);
		    	for (DiffEntry entry: diffFormatter.scan(oldCommitId, newCommitId)) {
		    		changes.add(new BlobChange(oldCommitId.name(), newCommitId.name(), entry) {

						@Override
						protected Repository getRepository() {
							return repoModel.getObject();
						}

						@Override
						protected LineProcessor getLineProcessor() {
							return lineProcessor;
						}
		    			
		    		});
		    	}

		    	// Diff calculation can be slow, so we pre-load diffs of each change 
		    	// concurrently
		    	Collection<Callable<Void>> tasks = new ArrayList<>();
		    	for (final BlobChange change: changes) {
		    		// to avoid race conditions during concurrent diff calculation, we
		    		// pre-populate blob data in repository 
		    		if (change.getOldBlobIdent().path != null)
		    			repoModel.getObject().getBlob(change.getOldBlobIdent());
		    		if (change.getNewBlobIdent().path != null)
		    			repoModel.getObject().getBlob(change.getNewBlobIdent());
		    		
		    		tasks.add(new Callable<Void>() {

						@Override
						public Void call() throws Exception {
							change.getDiffs();
							return null;
						}
		    			
		    		});
		    	}
		    	
		    	GitPlex.getInstance(ForkJoinPool.class).invokeAll(tasks);
		    	
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
	    	return changes;
		}
	};
	
	public RevisionDiffPanel(String id, IModel<Repository> repoModel, String oldRev, String newRev, 
			InlineCommentSupport commentSupport) {
		super(id);
		
		this.oldRev = oldRev;
		this.newRev = newRev;
		this.repoModel = repoModel;
		this.commentSupport = commentSupport;
		
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

	}

	@Override
	protected void onDetach() {
		changesModel.detach();
		repoModel.detach();
		
		super.onDetach();
	}

}
