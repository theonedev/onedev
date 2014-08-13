package com.pmease.gitplex.web.page.repository.info.code.commit.diff;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.parboiled.common.Preconditions;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.pmease.commons.hibernate.dao.Dao;
import com.pmease.gitplex.core.GitPlex;
import com.pmease.gitplex.core.model.OldCommitComment;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.web.GitPlexSession;
import com.pmease.gitplex.web.component.comment.OldCommitCommentEditor;
import com.pmease.gitplex.web.component.comment.OldCommitCommentPanel;
import com.pmease.gitplex.web.component.comment.event.CommitCommentEvent;
import com.pmease.gitplex.web.component.label.AgeLabel;
import com.pmease.gitplex.web.component.user.AvatarMode;
import com.pmease.gitplex.web.component.user.UserLink;
import com.pmease.gitplex.web.git.GitUtils;
import com.pmease.gitplex.web.model.CommitCommentModel;
import com.pmease.gitplex.web.page.repository.info.code.commit.RepoCommitPage;

@SuppressWarnings("serial")
public class CommentListPanel extends Panel {

	private final IModel<Repository> repositoryModel;
	private final IModel<String> commitModel;
	
	private boolean showAllNotes = false;
	
	public CommentListPanel(String id,
			IModel<Repository> repositoryModel, 
			IModel<String> commitModel) {
		super(id);
		
		this.repositoryModel = repositoryModel;
		this.commitModel = commitModel;
		
		setOutputMarkupId(true);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		final WebMarkupContainer commentsHolder = new WebMarkupContainer("commentlist");
		commentsHolder.setOutputMarkupId(true);
		add(commentsHolder);
		
		WebMarkupContainer commentsHeader = new WebMarkupContainer("commentsHeader");
		commentsHeader.setOutputMarkupId(true);
		add(commentsHeader);
		commentsHeader.add(new Label("count", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getCommentsOnCommit().size();
			}
			
		}));
		
		commentsHeader.add(new Label("inlinecount", new AbstractReadOnlyModel<Integer>() {

			@Override
			public Integer getObject() {
				return getCommentsOnLine().size();
			}
			
		}));
		
		commentsHeader.add(new Label("sha", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				return GitUtils.abbreviateSHA(getCommit(), 6);
			}
			
		}));
		
		CheckBox check = new CheckBox("showAllTrigger", new PropertyModel<Boolean>(this, "showAllNotes"));
		commentsHeader.add(check);
		
		check.add(new AjaxFormComponentUpdatingBehavior("change") {

			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				target.add(commentsHolder);
			}
		});
		
		IModel<List<OldCommitComment>> commentsModel = new LoadableDetachableModel<List<OldCommitComment>>() {

			@Override
			protected List<OldCommitComment> load() {
				if (showAllNotes) {
					return getCommitComments();
				} else {
					return getCommentsOnCommit();
				}
			}
		};
		
		commentsHolder.add(new ListView<OldCommitComment>("comments", commentsModel) {

			@Override
			protected void populateItem(ListItem<OldCommitComment> item) {
				OldCommitComment c = item.getModelObject();
				item.add(new UserLink("author", Model.of(c.getAuthor()), AvatarMode.AVATAR));
				item.add(new OldCommitCommentPanel("message", new CommitCommentModel(c)) {
					@Override
					protected Component createCommentHead(String id) {
						
						OldCommitComment comment = getCommitComment();
						
						Fragment frag = new Fragment(id, "commenthead", CommentListPanel.this);
						frag.add(new UserLink("author", Model.of(comment.getAuthor()), AvatarMode.NAME));
						AbstractLink link = new BookmarkablePageLink<Void>("commitlink", 
								RepoCommitPage.class,
								RepoCommitPage.paramsOf(getRepository(), getCommit(), null));
						frag.add(link);
						link.add(new Label("sha", GitUtils.abbreviateSHA(getCommit(), 6)));
						frag.add(new AgeLabel("age", Model.of(comment.getUpdateDate())));
						frag.add(newEditLink("editlink"));
						frag.add(newRemoveLink("removelink"));
						
						WebMarkupContainer lineLink = new WebMarkupContainer("linelink");
						lineLink.setVisibilityAllowed(!Strings.isNullOrEmpty(comment.getLine()));
						lineLink.add(AttributeModifier.replace("href", "#" + comment.getLine()));
						lineLink.add(new Label("lineid", getShortLineId(comment.getLine())));
						frag.add(lineLink);
						
						return frag;
					}
				});
			}
		});
		
		WebMarkupContainer formHolder = new WebMarkupContainer("formholder") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisibilityAllowed(GitPlexSession.getCurrentUser().isPresent());
			}
		};
		
		add(formHolder);
		if (GitPlexSession.getCurrentUser().isPresent()) {
			formHolder.add(new UserLink("author", Model.of(GitPlexSession.getCurrentUser().get()), AvatarMode.AVATAR));
			formHolder.add(new OldCommitCommentEditor("form") {

				@Override
				protected void onCancel(AjaxRequestTarget target, Form<?> form) {
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					OldCommitComment c = new OldCommitComment();
					c.setAuthor(GitPlexSession.getCurrentUser().get());
					c.setCommit(getCommit());
					c.setRepository(getRepository());
					c.setContent(getCommentText());
					
					clearInput();
					
					GitPlex.getInstance(Dao.class).persist(c);
					onAddComment(target, c);
				}
				
				@Override
				protected Component createCancelButton(String id, Form<?> form) {
					return new WebMarkupContainer(id).setVisibilityAllowed(false);
				}
				
				@Override
				protected IModel<String> getSubmitButtonLabel() {
					return Model.of("Comment on this commit");
				}
				
				@Override
				protected Form<?> createForm(String id) {
					return new Form<Void>(id);
				}
			});
		} else {
			formHolder.add(new WebMarkupContainer("author").setVisibilityAllowed(false));
			formHolder.add(new WebMarkupContainer("form").setVisibilityAllowed(false));
		}
	}

	private List<OldCommitComment> getCommitComments() {
		Page page = getPage();
		Preconditions.checkState(page instanceof CommitCommentsAware);
		return ((CommitCommentsAware) page).getCommitComments();
	}
	
	private List<OldCommitComment> getCommentsOnCommit() {
		return Lists.newArrayList(Iterables.filter(getCommitComments(), new Predicate<OldCommitComment>() {

			@Override
			public boolean apply(OldCommitComment input) {
				return Strings.isNullOrEmpty(input.getLine());
			}
			
		}));
	}
	
	private List<OldCommitComment> getCommentsOnLine() {
		return Lists.newArrayList(Iterables.filter(getCommitComments(), new Predicate<OldCommitComment>() {

			@Override
			public boolean apply(OldCommitComment input) {
				return !Strings.isNullOrEmpty(input.getLine());
			}
			
		}));
	}
	
	public @Nullable String getShortLineId(String line) {
		if (Strings.isNullOrEmpty(line)) {
			return null;
		}
		
		int pos = line.indexOf("-");
		String hash = line.substring(0, pos);
		return GitUtils.abbreviateSHA(hash, 6) + line.substring(pos);
	}
	
	private void onAddComment(AjaxRequestTarget target, OldCommitComment c) {
		target.add(this);
	}
	
	@Override
	public void onEvent(IEvent<?> sink) {
		if (sink.getPayload() instanceof CommitCommentEvent) {
			CommitCommentEvent e = (CommitCommentEvent) sink.getPayload();
			if (showAllNotes || !e.getComment().isLineComment()) {
				e.getTarget().add(this);
			}
		}
	}
	
	private Repository getRepository() {
		return repositoryModel.getObject();
	}
	
	private String getCommit() {
		return commitModel.getObject();
	}

	@Override
	public void onDetach() {
		if (repositoryModel != null) {
			repositoryModel.detach();
		}
		
		if (commitModel != null) {
			commitModel.detach();
		}
		
		super.onDetach();
	}
}
