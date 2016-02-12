package com.pmease.gitplex.web.page.repository.tags;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.RestartResponseException;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.AbstractLink;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.CssResourceReference;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevObject;
import org.eclipse.jgit.revwalk.RevTag;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;

import com.pmease.commons.git.GitUtils;
import com.pmease.commons.util.StringUtils;
import com.pmease.commons.wicket.ConfirmOnClick;
import com.pmease.commons.wicket.behavior.OnTypingDoneBehavior;
import com.pmease.commons.wicket.component.clearable.ClearableTextField;
import com.pmease.gitplex.core.gatekeeper.GateKeeper;
import com.pmease.gitplex.core.gatekeeper.checkresult.CheckResult;
import com.pmease.gitplex.core.gatekeeper.checkresult.Passed;
import com.pmease.gitplex.core.model.RepoAndRevision;
import com.pmease.gitplex.core.model.Repository;
import com.pmease.gitplex.core.model.User;
import com.pmease.gitplex.core.security.SecurityUtils;
import com.pmease.gitplex.web.component.UserLink;
import com.pmease.gitplex.web.component.hashandcode.HashAndCodePanel;
import com.pmease.gitplex.web.page.repository.NoCommitsPage;
import com.pmease.gitplex.web.page.repository.RepositoryPage;
import com.pmease.gitplex.web.page.repository.commit.CommitDetailPage;
import com.pmease.gitplex.web.page.repository.compare.RevisionComparePage;
import com.pmease.gitplex.web.page.repository.file.RepoFilePage;
import com.pmease.gitplex.web.page.repository.file.RepoFileState;
import com.pmease.gitplex.web.utils.DateUtils;

import de.agilecoders.wicket.core.markup.html.bootstrap.navigation.BootstrapPagingNavigator;

@SuppressWarnings("serial")
public class RepoTagsPage extends RepositoryPage {

	private TextField<String> searchInput;
	
	private String searchFor;
	
	public RepoTagsPage(PageParameters params) {
		super(params);
		
		if (!getRepository().git().hasCommits()) 
			throw new RestartResponseException(NoCommitsPage.class, paramsOf(getRepository()));
	}
	
	@Override
	protected String getPageTitle() {
		return getRepository() + " - Tags";
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		add(searchInput = new ClearableTextField<String>("searchTags", Model.of("")));
		
		final WebMarkupContainer tagsContainer = new WebMarkupContainer("tagsContainer");
		tagsContainer.setOutputMarkupId(true);
		add(tagsContainer);
		
		final PageableListView<Ref> tagsView;
		final IModel<List<Ref>> tagsModel = new AbstractReadOnlyModel<List<Ref>>() {

			@Override
			public List<Ref> getObject() {
				List<Ref> refs = getRepository().getTagRefs();
				searchFor = searchInput.getInput();
				if (StringUtils.isNotBlank(searchFor)) {
					searchFor = searchFor.trim().toLowerCase();
					for (Iterator<Ref> it = refs.iterator(); it.hasNext();) {
						String tag = GitUtils.ref2tag(it.next().getName());
						if (!tag.toLowerCase().contains(searchFor))
							it.remove();
					}
				} else {
					searchFor = null;
				}
				return refs;
			}
			
		}; 
		tagsContainer.add(tagsView = new PageableListView<Ref>("tags", tagsModel, 
				com.pmease.gitplex.web.Constants.DEFAULT_PAGE_SIZE) {

			@Override
			protected void populateItem(final ListItem<Ref> item) {
				Ref ref = item.getModelObject();
				final String tag = GitUtils.ref2tag(ref.getName());
				
				RepoFileState state = new RepoFileState();
				state.blobIdent.revision = tag;
				AbstractLink link = new BookmarkablePageLink<Void>("tagLink", 
						RepoFilePage.class, RepoFilePage.paramsOf(getRepository(), state));
				link.add(new Label("name", tag));
				item.add(link);

				RevObject revObject = getRepository().getRevObject(ref.getObjectId());
				if (revObject instanceof RevTag) {
					RevTag revTag = (RevTag) revObject;
					Fragment fragment = new Fragment("annotated", "annotatedFrag", this);
					fragment.add(new UserLink("author", revTag.getTaggerIdent()));
					fragment.add(new Label("age", DateUtils.formatAge(revTag.getTaggerIdent().getWhen())));
					Label message = new Label("message", revTag.getFullMessage());
					message.setOutputMarkupId(true);
					fragment.add(message);
					String toggleScript = String.format("$('#%s').toggle();", message.getMarkupId());
					fragment.add(new WebMarkupContainer("messageToggle").add(AttributeAppender.append("onclick", toggleScript)));
					item.add(fragment);
				} else {
					item.add(new WebMarkupContainer("annotated").setVisible(false));
				}

				RevCommit commit = getRepository().getRevCommit(ref.getObjectId());
				item.add(new HashAndCodePanel("hashAndCode", repoModel, commit.name()));
				PageParameters params = CommitDetailPage.paramsOf(getRepository(), commit.name());
				link = new BookmarkablePageLink<Void>("commitLink", CommitDetailPage.class, params);
				link.add(new Label("shortMessage", commit.getShortMessage()));
				item.add(link);
				
				link = new Link<Void>("compare") {

					@Override
					public void onClick() {
						try (	FileRepository jgitRepo = getRepository().openAsJGitRepo();
								RevWalk revWalk = new RevWalk(jgitRepo);) {
							RevCommit currentCommit = revWalk.lookupCommit(
									getRepository().getRevCommit(item.getModelObject().getObjectId()).getId());
							Ref prevAncestorRef = null;
							for (int i=item.getIndex()+1; i<tagsModel.getObject().size(); i++) {
								Ref prevRef = tagsModel.getObject().get(i);
								revWalk.setRevFilter(RevFilter.MERGE_BASE);
								revWalk.markStart(currentCommit);
								
								RevCommit prevCommit = revWalk.lookupCommit(
										getRepository().getRevCommit(prevRef.getObjectId()).getId());
								revWalk.markStart(prevCommit);
								if (prevCommit.equals(revWalk.next())) {
									prevAncestorRef = prevRef;
									break;
								}
								revWalk.reset();
							}
							RepoAndRevision target;
							if (prevAncestorRef != null) {
								target = new RepoAndRevision(getRepository(), prevAncestorRef.getName());
							} else {
								target = new RepoAndRevision(getRepository(), item.getModelObject().getName());
							}
							RepoAndRevision source = new RepoAndRevision(getRepository(), item.getModelObject().getName());
							PageParameters params = RevisionComparePage.paramsOf(getRepository(), target, source, null);
							setResponsePage(RevisionComparePage.class, params);
						} catch (IOException e) {
							throw new RuntimeException(e);
						}
					}
					
				};
				link.setVisible(item.getIndex()<size()-1);
				
				item.add(link);
				
				item.add(new Link<Void>("delete") {

					@Override
					public void onClick() {
						getRepository().deleteTag(tag);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();

						if (SecurityUtils.canModify(getRepository(), GitUtils.tag2ref(tag))) {
							User currentUser = getCurrentUser();
							if (currentUser != null) {
								GateKeeper gateKeeper = getRepository().getGateKeeper();
								CheckResult checkResult = gateKeeper.checkFile(currentUser, getRepository(), tag, null);
								setVisible(checkResult instanceof Passed);
							} else {
								setVisible(false);
							}
						} else {
							setVisible(false);
						}
					}
					
				}.add(new ConfirmOnClick("Do you really want to delete this tag?")));
			}
			
		});

		final PagingNavigator pagingNavigator;
		add(pagingNavigator = new BootstrapPagingNavigator("tagsPageNav", tagsView) {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(tagsView.getPageCount() > 1);
			}
			
		});
		pagingNavigator.setOutputMarkupPlaceholderTag(true);
		
		searchInput.add(new OnTypingDoneBehavior(200) {

			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				target.add(tagsContainer);
				target.add(pagingNavigator);
			}
			
		});
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CssResourceReference(RepoTagsPage.class, "repo-tags.css")));
	}

	@Override
	protected void onSelect(AjaxRequestTarget target, Repository repository) {
		setResponsePage(RepoTagsPage.class, paramsOf(repository));
	}
}
