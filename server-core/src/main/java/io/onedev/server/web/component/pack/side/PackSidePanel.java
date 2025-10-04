package io.onedev.server.web.component.pack.side;

import static io.onedev.server.search.commit.Revision.Type.COMMIT;
import static io.onedev.server.search.entity.issue.IssueQuery.merge;
import static io.onedev.server.web.translation.Translation._T;

import java.io.Serializable;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;

import com.google.common.collect.Lists;

import io.onedev.server.OneDev;
import io.onedev.server.service.PackLabelService;
import io.onedev.server.service.PackService;
import io.onedev.server.entityreference.BuildReference;
import io.onedev.server.model.Pack;
import io.onedev.server.model.Project;
import io.onedev.server.search.commit.CommitQuery;
import io.onedev.server.search.commit.Revision;
import io.onedev.server.search.commit.RevisionCriteria;
import io.onedev.server.search.entity.issue.FixedBetweenCriteria;
import io.onedev.server.search.entity.issue.IssueQuery;
import io.onedev.server.search.entity.issue.IssueQueryLexer;
import io.onedev.server.search.entity.issue.IssueQueryParseOption;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.DateUtils;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.component.entity.labels.EntityLabelsPanel;
import io.onedev.server.web.component.pack.choice.PackChoiceProvider;
import io.onedev.server.web.component.pack.choice.SelectPackToActChoice;
import io.onedev.server.web.component.select2.Response;
import io.onedev.server.web.component.select2.ResponseFiller;
import io.onedev.server.web.component.user.ident.Mode;
import io.onedev.server.web.component.user.ident.UserIdentPanel;
import io.onedev.server.web.editable.InplacePropertyEditLink;
import io.onedev.server.web.page.project.builds.detail.dashboard.BuildDashboardPage;
import io.onedev.server.web.page.project.commits.ProjectCommitsPage;
import io.onedev.server.web.page.project.issues.list.ProjectIssueListPage;
import io.onedev.server.web.util.editbean.LabelsBean;

public abstract class PackSidePanel extends Panel {

	public PackSidePanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		var build = getPack().getBuild();
		if (build != null) {
			var label = BuildReference.TYPE + " " + build.getReference().toString(getPack().getProject());
			var buildLink = new BookmarkablePageLink<Void>("publisher",
					BuildDashboardPage.class, BuildDashboardPage.paramsOf(build)) {
				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					if (!isEnabled())
						tag.setName("span");
				}

				@Override
				public IModel<?> getBody() {
					return Model.of(label);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setEnabled(SecurityUtils.canAccessBuild(build));
				}
			};
			add(buildLink);
		} else {
			add(new UserIdentPanel("publisher", getPack().getUser(), Mode.NAME));
		}

		add(new Label("publishDate", new LoadableDetachableModel<String>() {

			@Override
			protected String load() {
				return DateUtils.formatAge(getPack().getPublishDate());
			}

		}));
		
		add(new Label("totalSize", new LoadableDetachableModel<String>() {
			@Override
			protected String load() {
				return FileUtils.byteCountToDisplaySize(getPack().getSize());
			}
		}));
		
		if (build != null) {
			var choiceProvider = new PackChoiceProvider() {

				@Override
				public void query(String term, int page, Response<Pack> response) {
					int count = (page+1) * WebConstants.PAGE_SIZE + 1;
					var packService = OneDev.getInstance(PackService.class);
					if (term.length() == 0)
						term = null;
					List<Pack> packs = packService.queryPrevComparables(getPack(), term, count);
					new ResponseFiller<>(response).fill(packs, page, WebConstants.PAGE_SIZE);
				}
			};
			if (SecurityUtils.canAccessProject(build.getProject())) {
				add(new SelectPackToActChoice("fixedIssuesSince", choiceProvider) {
					@Override
					protected void onSelect(AjaxRequestTarget target, Pack selection) {
						var build = getPack().getBuild();
						
						var parseOption = new IssueQueryParseOption()
								.withCurrentUserCriteria(true)
								.withCurrentProjectCriteria(true);
						var baseQuery = IssueQuery.parse(
								build.getProject(), 
								build.getProject().getHierarchyDefaultFixedIssueQuery(build.getJobName()), 
								parseOption, true);
						FixedBetweenCriteria fixedBetweenCriteria = new FixedBetweenCriteria(
								build.getProject(),
								IssueQueryLexer.Commit, selection.getBuild().getCommitHash(), 
								IssueQueryLexer.Commit, build.getCommitHash());
						var queryString = merge(baseQuery, new IssueQuery(fixedBetweenCriteria)).toString();
						setResponsePage(ProjectIssueListPage.class, ProjectIssueListPage.paramsOf(build.getProject(), queryString, 0));
					}

					@Override
					protected String getPlaceholder() {
						return _T("Fixed issues since...");
					}
				});
			} else {
				add(new WebMarkupContainer("fixedIssuesSince").setVisible(false));
			}
			if (SecurityUtils.canReadCode(build.getProject())) {
				add(new SelectPackToActChoice("changesSince", choiceProvider) {
					@Override
					protected void onSelect(AjaxRequestTarget target, Pack selection) {
						var revisionCriteria = new RevisionCriteria(Lists.newArrayList(
								new Revision(COMMIT, selection.getBuild().getCommitHash(), true),
								new Revision(COMMIT, getPack().getBuild().getCommitHash(), false)));
						var commitQuery = new CommitQuery(Lists.newArrayList(revisionCriteria));
						setResponsePage(ProjectCommitsPage.class, ProjectCommitsPage.paramsOf(getPack().getBuild().getProject(), commitQuery.toString(), null));
					}

					@Override
					protected String getPlaceholder() {
						return _T("Code changes since...");
					}
				});
			} else {
				add(new WebMarkupContainer("changesSince").setVisible(false));
			}
		} else {
			add(new WebMarkupContainer("fixedIssuesSince").setVisible(false));
			add(new WebMarkupContainer("changesSince").setVisible(false));
		}

		WebMarkupContainer labelsContainer = new WebMarkupContainer("labels") {

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getPack().getLabels().isEmpty() || SecurityUtils.canWritePack(getPack().getProject()));
			}

		};
		labelsContainer.setOutputMarkupId(true);

		if (SecurityUtils.canWritePack(getPack().getProject())) {
			labelsContainer.add(new InplacePropertyEditLink("head") {

				@Override
				protected void onUpdated(IPartialPageRequestHandler handler, Serializable bean, String propertyName) {
					LabelsBean labelsBean = (LabelsBean) bean;
					OneDev.getInstance(PackLabelService.class).sync(getPack(), labelsBean.getLabels());
					handler.add(labelsContainer);
				}

				@Override
				protected String getPropertyName() {
					return "labels";
				}

				@Override
				protected Project getProject() {
					return getPack().getProject();
				}

				@Override
				protected Serializable getBean() {
					return LabelsBean.of(getPack());
				}

				@Override
				protected void onComponentTag(ComponentTag tag) {
					super.onComponentTag(tag);
					tag.setName("a");
				}

			});
		} else {
			labelsContainer.add(new WebMarkupContainer("head"));
		}
		labelsContainer.add(new EntityLabelsPanel<>("body", new AbstractReadOnlyModel<Pack>() {

			@Override
			public Pack getObject() {
				return getPack();
			}

		}));
		labelsContainer.add(new WebMarkupContainer("labelsHelp")
				.setVisible(SecurityUtils.canWritePack(getPack().getProject())));
		add(labelsContainer);
		
		if (SecurityUtils.canWritePack(getPack().getProject()))
			add(newDeleteLink("delete"));
		else
			add(new WebMarkupContainer("delete").setVisible(false));
		
		setOutputMarkupId(true);
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new PackSideCssResourceReference()));
	}

	protected abstract Pack getPack();

	protected abstract Component newDeleteLink(String componentId);
	
}
