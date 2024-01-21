package io.onedev.server.plugin.report.unittest;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.chart.pie.PieChartPanel;
import io.onedev.server.web.component.chart.pie.PieSlice;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.util.SuggestionUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxCheckBox;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.List.of;
import static java.util.stream.Collectors.toSet;
import static org.unbescape.html.HtmlEscape.escapeHtml5;

@SuppressWarnings("serial")
public class UnitTestCasesPage extends UnitTestReportPage {

	private static final String PARAM_TEST_SUITE = "test-suite";
	
	private static final String PARAM_NAME = "name";
	
	private static final String PARAM_STATUS = "status";
	
	private static final String PARAM_LONGEST_DURATION_FIRST = "longestDurationFirst";
	
	private State state = new State();
	
	private Optional<PatternSet> testSuitePatterns;
	
	private Optional<PatternSet> namePatterns;
	
	private Form<?> testSuiteForm;
	
	private Form<?> nameForm;
	
	private Component testSuiteFeedback;
	
	private Component nameFeedback;
	
	private Component summary;
	
	private Component orderBy;
	
	private WebMarkupContainer detail;
	
	public UnitTestCasesPage(PageParameters params) {
		super(params);
		
		state.testSuite = params.get(PARAM_TEST_SUITE).toOptionalString();
		testSuitePatterns = parseTestSuitePatterns();		
		state.name = params.get(PARAM_NAME).toOptionalString();
		namePatterns = parseNamePatterns();

		if (params.get(PARAM_STATUS).toString() != null) {
			state.statuses = new HashSet<>();
			if (params.get(PARAM_STATUS).toString().length() != 0) {
				for (StringValue each : params.getValues(PARAM_STATUS))
					state.statuses.add(Status.valueOf(each.toString().toUpperCase()));
			}
		}
		updateActualStatuses();
		
		state.longestDurationFirst = params.get(PARAM_LONGEST_DURATION_FIRST).toBoolean(false);
	}

	private void updateActualStatuses() {
		if (state.statuses == null) {
			state.actualStatuses = new HashSet<>();
			var report = getReport();
			if (report != null && namePatterns != null) {
				var seenStatuses = report.getTestCases(testSuitePatterns.orNull(), namePatterns.orNull(), of(Status.values()))
						.stream().map(UnitTestReport.TestCase::getStatus).collect(toSet());
				for (var status: Status.values()) {
					if (seenStatuses.contains(status))
						state.actualStatuses.add(status);
				}
			} else {
				state.actualStatuses.addAll(Arrays.asList(Status.values()));
			}
		} else {
			state.actualStatuses = new HashSet<>(state.statuses);
		}
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(UnitTestCasesPage.class, paramsOf(getBuild(), getReportName(), state));
		pushState(target, url.toString(), state);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		testSuitePatterns = parseTestSuitePatterns();
		if (testSuitePatterns == null)
			testSuiteForm.error("Malformed test suite filter");			
		namePatterns = parseNamePatterns();
		if (namePatterns == null)
			nameForm.error("Malformed name filter");
		updateActualStatuses();
		target.add(testSuiteForm);
		target.add(nameForm);
		target.add(summary);
		target.add(orderBy);
		target.add(detail);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getReport() != null) {
			var fragment = new Fragment("report", "validFrag", this);
			testSuiteForm = new Form<Void>("testSuiteForm");

			TextField<String> input = new TextField<String>("input", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return state.testSuite;
				}

				@Override
				public void setObject(String object) {
					state.testSuite = object;
				}

			});
			input.add(new PatternSetAssistBehavior() {

				@Override
				protected List<InputSuggestion> suggest(String matchWith) {
					return SuggestionUtils.suggest(
							getReport().getTestSuites().stream().map(it->it.getName()).collect(Collectors.toList()),
							matchWith);
				}

				@Override
				protected List<String> getHints(TerminalExpect terminalExpect) {
					return Lists.newArrayList(
							"Path containing spaces or starting with dash needs to be quoted",
							"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
					);
				}

			});
			testSuiteForm.add(input);

			input.add(new AjaxFormComponentUpdatingBehavior("clear") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					pushState(target);
					testSuitePatterns = parseTestSuitePatterns();
					if (testSuitePatterns == null)
						testSuiteForm.error("Malformed test suite filter");
					updateActualStatuses();
					target.add(testSuiteFeedback);
					target.add(summary);
					target.add(detail);
				}

			});

			testSuiteForm.add(testSuiteFeedback = new FencedFeedbackPanel("feedback", testSuiteForm));
			testSuiteFeedback.setOutputMarkupPlaceholderTag(true);

			testSuiteForm.add(new AjaxButton("submit") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(this));
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					pushState(target);
					testSuitePatterns = parseTestSuitePatterns();
					if (testSuitePatterns == null)
						testSuiteForm.error("Malformed test suite filter");
					updateActualStatuses();
					target.add(testSuiteFeedback);
					target.add(summary);
					target.add(detail);
				}

			});

			fragment.add(testSuiteForm);

			nameForm = new Form<Void>("nameForm");

			input = new TextField<String>("input", new IModel<String>() {

				@Override
				public void detach() {
				}

				@Override
				public String getObject() {
					return state.name;
				}

				@Override
				public void setObject(String object) {
					state.name = object;
				}

			});
			input.add(new PatternSetAssistBehavior() {

				@Override
				protected List<InputSuggestion> suggest(String matchWith) {
					return SuggestionUtils.suggest(
							getReport().getTestCases(null, null, null).stream().map(it->it.getName()).distinct().collect(Collectors.toList()),
							matchWith);
				}

				@Override
				protected List<String> getHints(TerminalExpect terminalExpect) {
					return Lists.newArrayList(
							"Path containing spaces or starting with dash needs to be quoted",
							"Use '**', '*' or '?' for <a href='https://docs.onedev.io/appendix/path-wildcard' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
					);
				}

			});
			nameForm.add(input);

			input.add(new AjaxFormComponentUpdatingBehavior("clear") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					pushState(target);
					namePatterns = parseNamePatterns();
					if (namePatterns == null)
						nameForm.error("Malformed name filter");						
					updateActualStatuses();
					target.add(nameFeedback);
					target.add(summary);
					target.add(detail);
				}

			});

			nameForm.add(nameFeedback = new FencedFeedbackPanel("feedback", nameForm));
			nameFeedback.setOutputMarkupPlaceholderTag(true);

			nameForm.add(new AjaxButton("submit") {

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new ConfirmLeaveListener(this));
				}

				@Override
				protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
					super.onSubmit(target, form);
					pushState(target);
					namePatterns = parseNamePatterns();
					if (namePatterns == null)
						nameForm.error("Malformed name filter");
					updateActualStatuses();
					target.add(nameFeedback);
					target.add(summary);
					target.add(detail);
				}

			});

			fragment.add(nameForm);

			if (testSuitePatterns == null)
				testSuiteForm.error("Malformed test suite filter");
			if (namePatterns == null)
				nameForm.error("Malformed name filter");

			fragment.add(summary = new PieChartPanel("summary", new LoadableDetachableModel<List<PieSlice>>() {

				@Override
				protected List<PieSlice> load() {
					if (testSuitePatterns != null && namePatterns != null) {
						List<PieSlice> slices = new ArrayList<>();
						for (Status status: Status.values()) {
							int numOfTestCases = getReport().getTestCases(
									testSuitePatterns.orNull(), namePatterns.orNull(), Sets.newHashSet(status)).size();
							slices.add(new PieSlice(status.name().toLowerCase().replace("_", " "),
									numOfTestCases, status.getColor(), state.actualStatuses.contains(status)));
						}
						return slices;
					} else {
						return null;
					}
				}

			}) {

				@Override
				protected void onSelectionChange(AjaxRequestTarget target, String sliceName) {
					Status status = Status.valueOf(sliceName.toUpperCase().replace(" ", "_"));
					if (state.actualStatuses.contains(status))
						state.actualStatuses.remove(status);
					else
						state.actualStatuses.add(status);
					state.statuses = new HashSet<>(state.actualStatuses);
					pushState(target);
					target.add(detail);
				}

			});

			fragment.add(orderBy = new AjaxCheckBox("longestDurationFirst", new IModel<Boolean>() {

				@Override
				public void detach() {
				}

				@Override
				public Boolean getObject() {
					return state.longestDurationFirst;
				}

				@Override
				public void setObject(Boolean object) {
					state.longestDurationFirst = object;
				}

			}) {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					pushState(target);
					target.add(detail);
				}

				@Override
				protected void onConfigure() {
					super.onConfigure();
					setVisible(getReport().hasTestCaseDuration());
				}

			});

			detail = new WebMarkupContainer("detail");
			detail.setOutputMarkupId(true);
			fragment.add(detail);

			PageableListView<TestCase> testCasesView;
			detail.add(testCasesView = new PageableListView<>("testCases",
					new LoadableDetachableModel<List<TestCase>>() {

						@Override
						protected List<TestCase> load() {
							List<TestCase> testCases;
							if (testSuitePatterns != null && namePatterns != null)
								testCases = getReport().getTestCases(testSuitePatterns.orNull(), namePatterns.orNull(), state.actualStatuses);
							else
								testCases = new ArrayList<>();
							
							testCases.sort(comparingInt(o -> o.getStatus().ordinal()));
							if (state.longestDurationFirst) {
								testCases.sort((o1, o2) -> {
									if (o1.getDuration() < o2.getDuration())
										return 1;
									else if (o1.getDuration() > o2.getDuration())
										return -1;
									else
										return 0;
								});
							}
							return testCases;
						}

					}, WebConstants.PAGE_SIZE) {

				@Override
				protected void populateItem(ListItem<TestCase> item) {
					TestCase testCase = item.getModelObject();
					item.add(new TestStatusBadge("status", testCase.getStatus()));

					var name = escapeHtml5(testCase.getName());
					if (testCase.getTestSuite().getBlobPath() != null && SecurityUtils.canReadCode(getProject())) {
						var sourceViewState = new ProjectBlobPage.State();
						sourceViewState.blobIdent = new BlobIdent(getBuild().getCommitHash(), testCase.getTestSuite().getBlobPath());
						if (testCase.getTestSuite().getPosition() != null)
							sourceViewState.position = BlobRenderer.getSourcePosition(testCase.getTestSuite().getPosition());
						
						var blobUrl = urlFor(ProjectBlobPage.class, ProjectBlobPage.paramsOf(getProject(), sourceViewState));
						name += " (<a href='" + blobUrl + "' target='_blank'>" + escapeHtml5(testCase.getTestSuite().getName()) + "</a>)";
					} else {
						name += " (" + escapeHtml5(testCase.getTestSuite().getName()) + ")";
					}
					if (testCase.getStatusText() != null && !testCase.getStatusText().equalsIgnoreCase(testCase.getStatus().name().replace("_", " "))) {
						name = escapeHtml5("[" + testCase.getStatusText() + "] ") + name;
					}
					item.add(new Label("name", name).setEscapeModelStrings(false));
					if (getReport().hasTestCaseDuration())
						item.add(new Label("duration", DurationFormatUtils.formatDuration(testCase.getDuration(), "s.SSS 's'")));
					else
						item.add(new WebMarkupContainer("duration").setVisible(false));

					Component detailViewer = testCase.renderDetail("detail", getBuild());
					if (detailViewer != null)
						item.add(detailViewer);
					else
						item.add(new WebMarkupContainer("detail").setVisible(false));
				}

			});

			detail.add(new OnePagingNavigator("pagingNavigator", testCasesView, null));
			detail.add(new NoRecordsPlaceholder("noRecords", testCasesView));
			add(fragment);
		} else {
			add(new Fragment("report", "invalidFrag", this));
		}
	}
	
	private Optional<PatternSet> parseTestSuitePatterns() {
		if (state.testSuite != null) {
			try {
				return Optional.of(PatternSet.parse(state.testSuite));
			} catch (Exception e) {
				return null;
			}
		} else {
			return Optional.absent();
		}
	}
	
	private Optional<PatternSet> parseNamePatterns() {
		if (state.name != null) {
			try {
				return Optional.of(PatternSet.parse(state.name));
			} catch (Exception e) {
				return null;
			}
		} else {
			return Optional.absent();
		}
	}
	
	public static PageParameters paramsOf(Build build, String reportName, State state) {
		PageParameters params = paramsOf(build, reportName);
		if (state.testSuite != null)
			params.add(PARAM_TEST_SUITE, state.testSuite);
		if (state.name != null)
			params.add(PARAM_NAME, state.name);
		if (state.statuses != null) {
			if (!state.statuses.isEmpty()) {
				for (Status status: state.statuses)
					params.add(PARAM_STATUS, status.name().toLowerCase());
			} else {
				params.add(PARAM_STATUS, "");
			}
		}
		if (state.longestDurationFirst)
			params.add(PARAM_LONGEST_DURATION_FIRST, state.longestDurationFirst);
		
		return params;
	}
	
	public static class State implements Serializable {
		
		@Nullable
		public String testSuite;
		
		@Nullable
		public String name;
		
		public boolean longestDurationFirst;
		
		public Collection<Status> statuses;
		
		public Collection<Status> actualStatuses;
		
	}
	
}
