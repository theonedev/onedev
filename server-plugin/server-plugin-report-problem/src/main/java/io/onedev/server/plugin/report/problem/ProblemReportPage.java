package io.onedev.server.plugin.report.problem;

import static io.onedev.server.web.translation.Translation._T;
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;

import org.jspecify.annotations.Nullable;

import org.apache.commons.lang3.SerializationException;
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
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.commons.utils.LockUtils;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.commons.utils.match.Matcher;
import io.onedev.commons.utils.match.PathMatcher;
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.BlobTarget;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.codequality.ProblemTarget;
import io.onedev.server.service.BuildService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.exception.ExceptionUtils;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;
import io.onedev.server.web.util.SuggestionUtils;

public class ProblemReportPage extends BuildReportPage {

	private static final String PARAM_NAME = "name";
	
	protected static final int MAX_PROBLEMS_TO_DISPLAY = 200;
	
	private String keyName;
	
	private Optional<PatternSet> targetNamePatterns;
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer groupsContainer;
	
	private List<String> keyNames;
	
	private Collection<ProblemTarget.GroupKey> expandedKeys = new HashSet<>();
	
	private final IModel<ProblemReport> reportModel = new LoadableDetachableModel<ProblemReport>() {

		@Override
		protected ProblemReport load() {
			try {
				Long projectId = getProject().getId();
				Long buildNumber = getBuild().getNumber();

				var report = OneDev.getInstance(ProjectService.class).runOnActiveServer(projectId, new GetProblemReport(projectId, buildNumber, getReportName()));
				for (var problem: report.getProblems()) {
					if (problem.getTarget() == null)
						return null;
				}
				return report;
			} catch (Exception e) {
				if (ExceptionUtils.find(e, SerializationException.class) != null)
					return null;
				else
					throw ExceptionUtils.unchecked(e);
			}
		}
		
	};
	
	public ProblemReportPage(PageParameters params) {
		super(params);
		
		keyName = params.get(PARAM_NAME).toOptionalString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getReport() != null) {
			var fragment = new Fragment("report", "validFrag", this);
			keyNames = getReport().getProblemGroups().stream()
					.map(it->it.getKey().getName())
					.distinct()
					.collect(toList());

			form = new Form<Void>("form");

			TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "keyName"));
			input.add(new PatternSetAssistBehavior() {

				@Override
				protected List<InputSuggestion> suggest(String matchWith) {
					return SuggestionUtils.suggestPathsByPathPattern(keyNames, matchWith, true);
				}

				@Override
				protected List<String> getHints(TerminalExpect terminalExpect) {
					return Lists.newArrayList(
							_T("Target containing spaces or starting with dash needs to be quoted"),
							_T("Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude")
					);
				}

			});
			form.add(input);

			input.add(new AjaxFormComponentUpdatingBehavior("clear") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					pushState(target);
					parseFilePatterns();
					target.add(feedback);
					target.add(groupsContainer);
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
					parseFilePatterns();
					target.add(feedback);
					target.add(groupsContainer);
				}

			});
			form.setOutputMarkupId(true);
			fragment.add(form);

			parseFilePatterns();

			groupsContainer = new WebMarkupContainer("groupsContainer");
			groupsContainer.setOutputMarkupId(true);
			fragment.add(groupsContainer);

			PageableListView<ProblemGroup> filesView;
			groupsContainer.add(filesView = new PageableListView<>("groups",
					new LoadableDetachableModel<List<ProblemGroup>>() {

						@Override
						protected List<ProblemGroup> load() {
							if (targetNamePatterns != null) {
								if (targetNamePatterns.isPresent()) {
									Matcher matcher = new PathMatcher();
									var problemGroups = getReport().getProblemGroups().stream()
											.filter(it -> targetNamePatterns.get().matches(matcher, it.getKey().getName().toLowerCase()))
											.collect(toList());
									problemGroups.sort(getReport().newProblemGroupComparator());
									return problemGroups;
								} else {
									var problemGroups = new ArrayList<>(getReport().getProblemGroups());
									problemGroups.sort(getReport().newProblemGroupComparator());
									return problemGroups;
								}
							} else {
								return new ArrayList<>();
							}
						}

					}, WebConstants.PAGE_SIZE) {

				@Override
				protected void populateItem(ListItem<ProblemGroup> item) {
					var group = item.getModelObject();
					var groupKey = group.getKey();
					var keyName = groupKey.getName();

					AjaxLink<Void> toggleLink = new AjaxLink<Void>("toggle") {

						@Override
						public void onClick(AjaxRequestTarget target) {
							if (expandedKeys.contains(groupKey))
								expandedKeys.remove(groupKey);
							else
								expandedKeys.add(groupKey);
							target.add(item);
						}

					};
					toggleLink.add(groupKey.render("key"));
						
					toggleLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

						@Override
						public String getObject() {
							return expandedKeys.contains(groupKey) ? "expanded" : "collapsed";
						}

					}));

					item.add(toggleLink);

					ProjectBlobPage.State state = new ProjectBlobPage.State();
					state.blobIdent = new BlobIdent(getBuild().getCommitHash(), keyName,
							FileMode.REGULAR_FILE.getBits());
					state.problemReport = getReportName();
					PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
					item.add(new BookmarkablePageLink<Void>("view", ProjectBlobPage.class, params)
							.setVisible(groupKey instanceof BlobTarget.GroupKey));

					item.add(new Label("tooManyProblems",
							MessageFormat.format(_T("Too many problems, displaying first {0}"), MAX_PROBLEMS_TO_DISPLAY)) {

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(expandedKeys.contains(groupKey)
									&& item.getModelObject().getProblems().size() > MAX_PROBLEMS_TO_DISPLAY);
						}

					});

					item.add(new ListView<>("problems", new LoadableDetachableModel<List<CodeProblem>>() {

						@Override
						protected List<CodeProblem> load() {
							List<CodeProblem> problems = new ArrayList<>(item.getModelObject().getProblems());
							problems.sort((o1, o2) -> {
								if (o1.getSeverity() != o2.getSeverity())
									return o1.getSeverity().ordinal() - o2.getSeverity().ordinal();
								PlanarRange location1 = null;
								if (o1.getTarget() instanceof BlobTarget)
									location1 = ((BlobTarget) o1.getTarget()).getLocation();
								PlanarRange location2 = null;
								if (o2.getTarget() instanceof BlobTarget)
									location2 = ((BlobTarget) o2.getTarget()).getLocation();
								
								if (location1 != null) {
									if (location2 != null) {
										if (location1.getFromRow() != location2.getFromRow())
											return location1.getFromRow() - location2.getFromRow();
										else
											return location1.getFromColumn() - location2 .getFromColumn();
									} else {
										return -1;
									}
								} else {
									if (location2 != null)
										return 1;
									else
										return 0;
								}
							});
							if (problems.size() > MAX_PROBLEMS_TO_DISPLAY)
								return problems.subList(0, MAX_PROBLEMS_TO_DISPLAY);
							else
								return problems;
						}

					}) {

						@Override
						protected void populateItem(ListItem<CodeProblem> item) {
							CodeProblem problem = item.getModelObject();
							var severityLabel = new Label("severity", _T("severity:" + problem.getSeverity().name()));
							item.add(severityLabel);
							if (problem.getSeverity() == Severity.CRITICAL || problem.getSeverity() == Severity.HIGH)
								severityLabel.add(AttributeAppender.append("class", "badge-danger"));
							else if (problem.getSeverity() == Severity.MEDIUM)
								severityLabel.add(AttributeAppender.append("class", "badge-warning"));
							else
								severityLabel.add(AttributeAppender.append("class", "badge-secondary"));
							
							item.add(new Label("message", problem.getMessage()).setEscapeModelStrings(false));

							if (problem.getTarget() instanceof BlobTarget 
									&& ((BlobTarget) problem.getTarget()).getLocation() != null) {
								var location = ((BlobTarget) problem.getTarget()).getLocation();
								ProjectBlobPage.State state = new ProjectBlobPage.State();
								state.blobIdent = new BlobIdent(getBuild().getCommitHash(),
										keyName, FileMode.REGULAR_FILE.getBits());
								state.problemReport = getReportName();
								state.position = BlobRenderer.getSourcePosition(location);
								PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
								BookmarkablePageLink<Void> locationLink = new BookmarkablePageLink<Void>("location",
										ProjectBlobPage.class, params);
								locationLink.add(new Label("label", describe(location)));
								item.add(locationLink);
							} else {
								var locationLink = new WebMarkupContainer("location");
								locationLink.add(new WebMarkupContainer("label"));
								locationLink.setVisible(false);
								item.add(locationLink);
							}
						}

						@Override
						protected void onConfigure() {
							super.onConfigure();
							setVisible(expandedKeys.contains(groupKey));
						}

					});
					item.setOutputMarkupId(true);
				}

				@Override
				protected void onBeforeRender() {
					expandedKeys.clear();
					if (!getModelObject().isEmpty()) 
						expandedKeys.add(getModelObject().iterator().next().getKey());
					super.onBeforeRender();
				}
			});

			groupsContainer.add(new OnePagingNavigator("pagingNavigator", filesView, null));
			groupsContainer.add(new NoRecordsPlaceholder("noRecords", filesView));			
			add(fragment);
		} else {
			add(new Fragment("report", "invalidFrag", this));
		}
	}
	
	@Nullable
	private ProblemReport getReport() {
		return reportModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		reportModel.detach();
		super.onDetach();
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(ProblemReportPage.class, paramsOf(getBuild(), getReportName(), keyName));
		pushState(target, url.toString(), keyName);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		keyName = (String) data;
		parseFilePatterns();
		target.add(form);
		target.add(groupsContainer);
	}
	
	private void parseFilePatterns() {
		if (keyName != null) {
			try {
				targetNamePatterns = Optional.of(PatternSet.parse(keyName.toLowerCase()));
			} catch (Exception e) {
				keyName = null;
				form.error(_T("Malformed filter"));
			}
		} else {
			targetNamePatterns = Optional.absent();
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProblemReportCssResourceReference()));
	}
	
	protected String describe(PlanarRange range) {
		return _T("Line: ") + (range.getFromRow()+1) + " - " + (range.getToRow()+1);
	}
	
	public static PageParameters paramsOf(Build build, String reportName, @Nullable String file) {
		PageParameters params = paramsOf(build, reportName);
		if (file != null)
			params.add(PARAM_NAME, file);
		return params;
	}
	
	private static class GetProblemReport implements ClusterTask<ProblemReport> {

		private final Long projectId;
		
		private final Long buildNumber;
		
		private final String reportName;
		
		private GetProblemReport(Long projectId, Long buildNumber, String reportName) {
			this.projectId = projectId;
			this.buildNumber = buildNumber;
			this.reportName = reportName;
		}
		
		@Override
		public ProblemReport call() throws Exception {
			return LockUtils.read(ProblemReport.getReportLockName(projectId, buildNumber), new Callable<ProblemReport>() {

				@Override
				public ProblemReport call() throws Exception {
					File reportDir = new File(OneDev.getInstance(BuildService.class).getBuildDir(projectId, buildNumber), ProblemReport.CATEGORY + "/" + reportName);
					return ProblemReport.readFrom(reportDir);
				}
				
			});
		}
		
	}
	
}
