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
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.eclipse.jgit.lib.FileMode;

import com.pmease.commons.git.Change;
import com.pmease.commons.git.TreeNode;
import com.pmease.commons.wicket.behavior.StickyBehavior;
import com.pmease.gitplex.core.comment.InlineCommentSupport;
import com.pmease.gitplex.core.model.Repository;

@SuppressWarnings("serial")
public abstract class CompareResultPanel extends Panel {

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

		if (file != null) {
			activeChange = findChange(file);
		} else {
			List<Change> changes = repoModel.getObject().getChanges(oldCommit, newCommit);
			if (!changes.isEmpty())
				activeChange = changes.get(0);
		}
	}
	
	private Change findChange(String file) {
		Change change = null;
		for (Change each: repoModel.getObject().getChanges(oldCommitHash, newCommitHash)) {
			if (file.equals(each.getOldPath()) || file.equals(each.getNewPath())) { 
				change = each;
				break;
			}
		}
		if (change == null) {
			List<TreeNode> result = repoModel.getObject().git().listTree(newCommitHash, file);
			if (!result.isEmpty() && result.get(0).getMode() != FileMode.TYPE_TREE) {
				change = new Change(UNCHANGED, oldCommitHash, newCommitHash, file, file, 
						result.get(0).getMode(), result.get(0).getMode());
			}
		}
		return change;
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(changeNav = newChangeNav(activeChange == null || activeChange.getStatus() != UNCHANGED));

		if (activeChange != null) 
			changeContent = new BlobDiffPanel("content", repoModel, activeChange, getInlineCommentSupport(activeChange));
		else 
			changeContent = new WebMarkupContainer("content");
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
					change, getInlineCommentSupport(change));
			CompareResultPanel.this.replace(changeContent);
			target.add(changeContent);

			String script = String.format("$('#%s').find('a.active').removeClass('active');", changeNav.getMarkupId());
			target.prependJavaScript(script);
			
			target.add(this);
			
			onSelection(target, change);
		}
		
	}
	
	protected abstract void onSelection(AjaxRequestTarget target, Change change);
	
	public void select(AjaxRequestTarget target, String file) {
		activeChange = findChange(file);
		if (activeChange != null) {
			if (changeNav instanceof DiffTreePanel) {
				((DiffTreePanel) changeNav).reveal(activeChange);
			} else if (activeChange.getStatus() == UNCHANGED) {
				replace(changeNav = newChangeNav(false));
			}
			changeContent = new BlobDiffPanel(changeContent.getId(), repoModel, 
					activeChange, getInlineCommentSupport(activeChange));
		} else {
			changeContent = new WebMarkupContainer(changeContent.getId());
			changeContent.setOutputMarkupId(true);
		}
		target.add(changeNav);
		replace(changeContent);
		target.add(changeContent);
	}
	
	public void toggleChangedOnly(AjaxRequestTarget target) {
		changeNav = newChangeNav(!(changeNav instanceof ChangedFilesPanel));
		replace(changeNav);
		target.add(changeNav);
	}

}
