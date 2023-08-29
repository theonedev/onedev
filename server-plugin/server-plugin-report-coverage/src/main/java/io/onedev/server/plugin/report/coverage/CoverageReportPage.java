package io.onedev.server.plugin.report.coverage;

import com.google.common.collect.Lists;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LockUtils;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.floating.FloatingPanel;
import io.onedev.server.web.component.menu.MenuItem;
import io.onedev.server.web.component.menu.MenuLink;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import javax.annotation.Nullable;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

@SuppressWarnings("serial")
public class CoverageReportPage extends BuildReportPage {

	private static final String PARAM_PACKAGE = "package";
	
	private static final String PARAM_FILTER = "filter";
	
	private static final String PARAM_ORDER_BY = "order-by";
	
	private String packageName;
	
	private com.google.common.base.Optional<PatternSet> filterPatterns;
	
	private State state = new State();
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer itemsContainer;
	
	private final IModel<CoverageReport> reportDataModel = new LoadableDetachableModel<CoverageReport>() {

		@Override
		protected CoverageReport load() {
			Long projectId = getProject().getId();
			return OneDev.getInstance(ProjectManager.class).runOnActiveServer(projectId, new GetCoverageReport(projectId, getBuild().getNumber(), getReportName()));
		}
		
	};
	
	public CoverageReportPage(PageParameters params) {
		super(params);
		
		packageName = params.get(PARAM_PACKAGE).toOptionalString();
		state.filter = params.get(PARAM_FILTER).toOptionalString();
		String orderByString = params.get(PARAM_ORDER_BY).toOptionalString();
		if (orderByString != null)
			state.orderBy = CoverageOrderBy.valueOf(orderByString);
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();

		if (packageName != null)
			add(new Label("coverageTitle", packageName));
		else
			add(new Label("coverageTitle", "Overall"));
		
		add(new CoverageInfoPanel<CoverageInfo>("coverages", new LoadableDetachableModel<CoverageInfo>() {

			@Override
			protected CoverageInfo load() {
				if (packageName != null) {
					return getReportData().getPackageCoverages().stream()
							.filter(it->it.getName().equals(packageName)).findFirst().get();
				} else {
					return getReportData().getOverallCoverages();
				}
			}
			
		}));

		add(new Label("itemsTitle", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (packageName != null) 
					return "Items";
				else
					return "Categories";
			}
			
		}));
		
		form = new Form<Void>("form");
		
		TextField<String> input = new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.filter;
			}

			@Override
			public void setObject(String object) {
				state.filter = object;
			}
			
		});
		input.add(AttributeAppender.append("placeholder", new AbstractReadOnlyModel<String>() {

			@Override
			public String getObject() {
				if (packageName != null)
					return "Filter items...";
				else
					return "Filter categories...";
			}
			
		}));
		input.add(new PatternSetAssistBehavior() {
			
			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				List<String> names;
				if (packageName != null) {
					Optional<CategoryCoverageInfo> packageCoverages = getReportData().getPackageCoverages()
							.stream().filter(it->it.getName().equals(packageName)).findFirst();
					if (packageCoverages.isPresent())
						names = packageCoverages.get().getItemCoverages().stream().map(it->it.getName()).collect(Collectors.toList());
					else
						names = new ArrayList<>();
				} else {
					names = getReportData().getPackageCoverages().stream().map(it->it.getName()).collect(Collectors.toList());
				}
				return SuggestionUtils.suggest(names, matchWith);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Name containing spaces or starting with dash needs to be quoted",
						"Use '*' or '?' for wildcard match. Prefix with '-' to exclude"
						);
			}
			
		});
		form.add(input);
		
		input.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				pushState(target);
				parseFilterPatterns();
				target.add(feedback);
				target.add(itemsContainer);
			}
			
		});
		
		form.add(new MenuLink("orderBy") {

			private MenuItem newMenuItem(FloatingPanel dropdown, CoverageOrderBy orderBy) {
				return new MenuItem() {

					@Override
					public String getLabel() {
						return orderBy.getDisplayName();
					}

					@Override
					public WebMarkupContainer newLink(String id) {
						return new AjaxLink<Void>(id) {

							@Override
							public void onClick(AjaxRequestTarget target) {
								dropdown.close();
								state.orderBy = orderBy;
								target.add(itemsContainer);
							}

						};
					}

					@Override
					public boolean isSelected() {
						return orderBy == state.orderBy;
					}

				};				
			}
			@Override
			protected List<MenuItem> getMenuItems(FloatingPanel dropdown) {
				List<MenuItem> menuItems = new ArrayList<>();
				menuItems.add(newMenuItem(dropdown, CoverageOrderBy.DEFAULT));
				var overallCoverages = getReportData().getOverallCoverages();
				if (overallCoverages.getMethodCoverage() >= 0) {
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.LEAST_METHOD_COVERAGE));
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.MOST_METHOD_COVERAGE));
				}
				if (overallCoverages.getBranchCoverage() >= 0) {
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.LEAST_BRANCH_COVERAGE));
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.MOST_BRANCH_COVERAGE));
				}
				if (overallCoverages.getStatementCoverage() >= 0) {
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.LEAST_STATEMENT_COVERAGE));
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.MOST_STATEMENT_COVERAGE));
				}
				if (overallCoverages.getLineCoverage() >= 0) {
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.LEAST_LINE_COVERAGE));
					menuItems.add(newMenuItem(dropdown, CoverageOrderBy.MOST_LINE_COVERAGE));
				}
				return menuItems;
			}

		});
		
		form.add(feedback = new FencedFeedbackPanel("feedback", form));
		feedback.setOutputMarkupPlaceholderTag(true);

		form.add(new AjaxButton("submit") {

			@Override
			protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
				super.updateAjaxAttributes(attributes);
				attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(this));
			}
			
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
				super.onSubmit(target, form);
				pushState(target);
				parseFilterPatterns();
				target.add(feedback);
				target.add(itemsContainer);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);		
		
		parseFilterPatterns();
		
		itemsContainer = new WebMarkupContainer("itemsContainer");
		itemsContainer.setOutputMarkupId(true);
		add(itemsContainer);
		
		PageableListView<NamedCoverageInfo> itemsView = 
				new PageableListView<NamedCoverageInfo>("items", new LoadableDetachableModel<>() {

					@SuppressWarnings("unchecked")
					@Override
					protected List<NamedCoverageInfo> load() {
						if (filterPatterns != null) {
							List<? extends NamedCoverageInfo> coverages;
							if (packageName != null) {
								Optional<CategoryCoverageInfo> packageCoverages = getReportData().getPackageCoverages()
										.stream().filter(it -> it.getName().equals(packageName)).findFirst();
								if (packageCoverages.isPresent())
									coverages = packageCoverages.get().getItemCoverages();
								else
									coverages = new ArrayList<>();
							} else {
								coverages = getReportData().getPackageCoverages();
							}
							if (filterPatterns.isPresent()) {
								Matcher matcher = new StringMatcher();
								coverages = coverages.stream()
										.filter(it -> filterPatterns.get().matches(matcher, it.getName()))
										.collect(Collectors.toList());
							}
							coverages.sort((Comparator<CoverageInfo>) (o1, o2) -> state.orderBy.compare(o1, o2));
							return (List<NamedCoverageInfo>) coverages;
						} else {
							return new ArrayList<>();
						}
					}

				}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<NamedCoverageInfo> item) {
				NamedCoverageInfo coverageInfo = item.getModelObject();
				
				Link<Void> nameLink;
				if (coverageInfo instanceof CategoryCoverageInfo) {
					State state = new State();
					state.orderBy = CoverageReportPage.this.state.orderBy;
					PageParameters params = paramsOf(getBuild(), getReportName(), 
							coverageInfo.getName(), state);
					nameLink = new BookmarkablePageLink<Void>("name", CoverageReportPage.class, params);
				} else {
					ProjectBlobPage.State state = new ProjectBlobPage.State();
					state.blobIdent = new BlobIdent(getBuild().getCommitHash(), 
							((ItemCoverageInfo) coverageInfo).getBlobPath(), FileMode.REGULAR_FILE.getBits());
					state.coverageReport = getReportName();
					PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
					nameLink = new BookmarkablePageLink<Void>("name", ProjectBlobPage.class, params);
				}
				nameLink.add(new Label("label", coverageInfo.getName()));
					
				item.add(nameLink);
				
				item.add(new CoverageInfoPanel<NamedCoverageInfo>("coverages", item.getModel()));
			}
			
		};
		itemsContainer.add(itemsView);
		itemsContainer.add(new OnePagingNavigator("pagingNavigator", itemsView, null));
		itemsContainer.add(new NoRecordsPlaceholder("noRecords", itemsView));
	}

	private void parseFilterPatterns() {
		if (state.filter != null) {
			try {
				filterPatterns = com.google.common.base.Optional.of(PatternSet.parse(state.filter));
			} catch (Exception e) {
				filterPatterns = null;
				form.error("Malformed filter");
			}
		} else {
			filterPatterns = com.google.common.base.Optional.absent();
		}
	}
	
	private CoverageReport getReportData() {
		return reportDataModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		reportDataModel.detach();
		super.onDetach();
	}

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CoverageReportCssResourceReference()));
	}

	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(CoverageReportPage.class, paramsOf(getBuild(), getReportName(), packageName, state));
		pushState(target, url.toString(), state);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		parseFilterPatterns();
		target.add(form);
		target.add(itemsContainer);
	}
	
	private static PageParameters paramsOf(Build build, String reportName, 
			@Nullable String packageName, State state) {
		PageParameters params = BuildReportPage.paramsOf(build, reportName);
		if (packageName != null)
			params.add(PARAM_PACKAGE, packageName);
		if (state.filter != null)
			params.add(PARAM_FILTER, state.filter);
		if (state.orderBy != null)
			params.add(PARAM_ORDER_BY, state.orderBy);
		return params;
	}
	
	public static class State implements Serializable {
		
		@Nullable
		public String filter;
		
		public CoverageOrderBy orderBy = CoverageOrderBy.DEFAULT;
		
	}
	
	private static class GetCoverageReport implements ClusterTask<CoverageReport> {

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String reportName;
		
		private GetCoverageReport(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}
		
		@Override
		public CoverageReport call() throws Exception {
			return LockUtils.read(CoverageReport.getReportLockName(projectId, buildNumber), new Callable<CoverageReport>() {

				@Override
				public CoverageReport call() throws Exception {
					return CoverageReport.readFrom(new File(Build.getStorageDir(projectId, buildNumber), CoverageReport.CATEGORY + "/" + reportName));
				}
				
			});
		}
		
	}
}