package io.onedev.server.plugin.report.unittest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestCase;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.chart.pie.PieChartPanel;
import io.onedev.server.web.component.chart.pie.PieSlice;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.util.SuggestionUtils;

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
		state.name = params.get(PARAM_NAME).toOptionalString();
		
		state.statuses = new LinkedHashSet<>();

		if (!"none".equals(params.get(PARAM_STATUS).toString())) {
			for (StringValue each: params.getValues(PARAM_STATUS)) 
				state.statuses.add(Status.valueOf(each.toString().toUpperCase()));
			
			if (state.statuses.isEmpty()) {
				state.statuses.add(Status.PASSED);
				state.statuses.add(Status.FAILED);
			}
		}
		
		state.longestDurationFirst = params.get(PARAM_LONGEST_DURATION_FIRST).toBoolean(false);
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(UnitTestCasesPage.class, paramsOf(getBuild(), getReportName(), state));
		pushState(target, url.toString(), state);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		parseTestSuitePatterns();
		parseNamePatterns();
		target.add(testSuiteForm);
		target.add(nameForm);
		target.add(summary);
		target.add(orderBy);
		target.add(detail);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
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
				parseTestSuitePatterns();
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
				parseTestSuitePatterns();
				target.add(testSuiteFeedback);
				target.add(summary);
				target.add(detail);
			}
			
		});
		
		add(testSuiteForm);
		
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
				parseNamePatterns();
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
				parseNamePatterns();
				target.add(nameFeedback);
				target.add(summary);
				target.add(detail);
			}
			
		});
		
		add(nameForm);
		
		parseTestSuitePatterns();
		parseNamePatterns();
		
		add(summary = new PieChartPanel("summary", new LoadableDetachableModel<List<PieSlice>>() {

			@Override
			protected List<PieSlice> load() {
				if (testSuitePatterns != null && namePatterns != null) {
					List<PieSlice> slices = new ArrayList<>();
					for (Status status: Status.values()) {
						int numOfTestCases = getReport().getTestCases(
								testSuitePatterns.orNull(), namePatterns.orNull(), Sets.newHashSet(status)).size();
						slices.add(new PieSlice(status.name().toLowerCase(), numOfTestCases, 
								status.getColor(), state.statuses.contains(status)));
					}
					return slices;
				} else {
					return null;
				}
			}
			
		}) {

			@Override
			protected void onSelectionChange(AjaxRequestTarget target, String sliceName) {
				Status status = Status.valueOf(sliceName.toUpperCase());
				if (state.statuses.contains(status))
					state.statuses.remove(status);
				else
					state.statuses.add(status);
				pushState(target);
				target.add(detail);
			}
			
		});
		
		add(orderBy = new AjaxCheckBox("longestDurationFirst", new IModel<Boolean>() {

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
		add(detail);
		
		PageableListView<TestCase> testCasesView;
		detail.add(testCasesView = new PageableListView<TestCase>("testCases", 
				new LoadableDetachableModel<List<TestCase>>() {

			@Override
			protected List<TestCase> load() {
				List<TestCase> testCases;
				if (testSuitePatterns != null && namePatterns != null)
					testCases = getReport().getTestCases(testSuitePatterns.orNull(), namePatterns.orNull(), state.statuses);
				else
					testCases = new ArrayList<>();
				if (state.longestDurationFirst) {
					testCases.sort(new Comparator<TestCase>() {

						@Override
						public int compare(TestCase o1, TestCase o2) {
							if (o1.getDuration() < o2.getDuration())
								return 1;
							else if (o1.getDuration() > o2.getDuration())
								return -1;
							else 
								return 0;
						}
						
					});
				}
				return testCases;
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TestCase> item) {
				TestCase testCase = item.getModelObject();
				item.add(new TestStatusBadge("status", testCase.getStatus()));
				item.add(new Label("name", testCase.getName() + " (" + testCase.getTestSuite().getName() + ")"));
				if (getReport().hasTestCaseDuration())
					item.add(new Label("duration", DurationFormatUtils.formatDuration(testCase.getDuration(), "s.SSS 's'")));
				else
					item.add(new WebMarkupContainer("duration").setVisible(false));
				
				Component messageViewer = testCase.renderMessage("message", getBuild()); 
				if (messageViewer != null)
					item.add(messageViewer);
				else
					item.add(new WebMarkupContainer("message").setVisible(false));
			}
			
		});
		
		detail.add(new OnePagingNavigator("pagingNavigator", testCasesView, null));
		detail.add(new NoRecordsPlaceholder("noRecords", testCasesView));
	}
	
	private void parseTestSuitePatterns() {
		if (state.testSuite != null) {
			try {
				testSuitePatterns = Optional.of(PatternSet.parse(state.testSuite));
			} catch (Exception e) {
				testSuitePatterns = null;
				testSuiteForm.error("Malformed test suite filter");
			}
		} else {
			testSuitePatterns = Optional.absent();
		}
	}
	
	private void parseNamePatterns() {
		if (state.name != null) {
			try {
				namePatterns = Optional.of(PatternSet.parse(state.name));
			} catch (Exception e) {
				namePatterns = null;
				nameForm.error("Malformed name filter");
			}
		} else {
			namePatterns = Optional.absent();
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
				if (!state.statuses.containsAll(Arrays.asList(Status.values()))) {
					for (Status status: state.statuses)
						params.add(PARAM_STATUS, status.name().toLowerCase());
				}
			} else {
				params.add(PARAM_STATUS, "none");
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
		
	}
	
}
