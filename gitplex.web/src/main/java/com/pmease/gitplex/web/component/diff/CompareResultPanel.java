package com.pmease.gitplex.web.component.diff;

import static com.pmease.commons.git.Change.Status.UNCHANGED;

import java.util.List;
import java.util.Objects;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.visit.IVisit;
import org.apache.wicket.util.visit.IVisitor;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.Change;
import com.pmease.commons.git.RevAwareChange;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.gitplex.core.comment.InlineComment;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.comment.InlineContext;
import com.pmease.gitplex.core.comment.InlineContextAware;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class CompareResultPanel extends Panel implements InlineContextAware {

	private final IModel<Repository> repoModel;
	
	private final String oldCommitHash;
	
	private final String newCommitHash;
	
	private Change activeChange;
	
	private WebMarkupContainer changeNav;
	
	private Component changeContent;
	
	public CompareResultPanel(String id, IModel<Repository> repoModel, 
			String oldCommit, String newCommit, @Nullable String file) {
		super(id);
		
		this.repoModel = repoModel;
		this.oldCommitHash = oldCommit;
		this.newCommitHash = newCommit;

		List<Change> changes = repoModel.getObject().getChanges(oldCommit, newCommit);
		if (file != null) {
			for (Change each: repoModel.getObject().getChanges(oldCommit, newCommit)) {
				if (each.getPath().equals(file)) { 
					activeChange = each;
					break;
				}
			}
			if (activeChange == null) {
				List<TreeNode> result = repoModel.getObject().git().listTree(newCommit, file);
				if (!result.isEmpty() && result.get(0).getMode() != FileMode.TYPE_TREE) {
					activeChange = new Change(UNCHANGED, file, file, 
							result.get(0).getMode(), result.get(0).getMode());
				}
			}
		} else if (!changes.isEmpty()) {
			activeChange = changes.get(0);
		}
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(changeNav = newChangeNav(activeChange == null || activeChange.getStatus() != UNCHANGED));

		if (activeChange != null) {
			changeContent = new BlobDiffPanel("content", repoModel, 
					new RevAwareChange(activeChange, oldCommitHash, newCommitHash), 
					getInlineCommentSupport(activeChange));
		} else {
			changeContent = new Label("content", "<div class='error fa fa-alert-o'> File not exist.</div>");
			changeContent.setEscapeModelStrings(false);
		}
		changeContent.setOutputMarkupId(true);
		add(changeContent);
	}

	private WebMarkupContainer newChangeNav(boolean changedOnly) {
		if (changedOnly) {
			changeNav = new ChangedFilesPanel("nav", repoModel, oldCommitHash, newCommitHash) {
				
				@Override
				protected WebMarkupContainer newBlobLink(String id, Change change) {
					return new BlobLink(id, change);
				}
			};
		} else {
			changeNav = new DiffTreePanel("nav", repoModel, oldCommitHash, newCommitHash) {

				@Override
				protected WebMarkupContainer newBlobLink(String id, final Change change) {
					return new BlobLink(id, change);
				}

				@Override
				protected void onInitialize() {
					super.onInitialize();
					
					if (activeChange != null)
						reveal(activeChange);
				}
				
			};
		}
		changeNav.add(new StickyBehavior(this));
		return changeNav;
	}
	
	public boolean isChangedOnly() {
		return changeNav instanceof ChangedFilesPanel;
	}

	@Override
	protected void onDetach() {
		repoModel.detach();
		super.onDetach();
	}

	protected abstract InlineCommentSupport getInlineCommentSupport(Change change);
	
	private class BlobLink extends AjaxLink<Void> {
		
		private final Change change;
		
		BlobLink(String id, Change change) {
			super(id);
			
			this.change = change;
		}

		@Override
		protected void onComponentTag(ComponentTag tag) {
			super.onComponentTag(tag);
			
			if (activeChange != null 
					&& Objects.equals(change.getOldPath(), activeChange.getOldPath()) 
					&& Objects.equals(change.getNewPath(), activeChange.getNewPath())) {
				tag.put("class", "active");
			}
		}

		@Override
		public void onClick(AjaxRequestTarget target) {
			activeChange = change;
			changeContent = new BlobDiffPanel(changeContent.getId(), repoModel, 
					new RevAwareChange(change, oldCommitHash, newCommitHash), getInlineCommentSupport(change));
			CompareResultPanel.this.replace(changeContent);
			target.add(changeContent);

			String script = String.format("$('#%s').find('a.active').removeClass('active');", changeNav.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(this);
			
			onChangeSelection(target, change);
		}
		
	}
	
	protected abstract void onChangeSelection(AjaxRequestTarget target, Change change);
	
	public void toggleChangedOnly(AjaxRequestTarget target) {
		changeNav = newChangeNav(!(changeNav instanceof ChangedFilesPanel));
		replace(changeNav);
		target.add(changeNav);
	}

	@Override
	public InlineContext getInlineContext(final InlineComment comment) {
		return visitChildren(InlineContextAware.class, new IVisitor<Component, InlineContext>() {

			@Override
			public void component(Component object, IVisit<InlineContext> visit) {
				InlineContextAware inlineContextAware = (InlineContextAware) object;
				visit.stop(inlineContextAware.getInlineContext(comment));
			}

		});
	}
	
}
