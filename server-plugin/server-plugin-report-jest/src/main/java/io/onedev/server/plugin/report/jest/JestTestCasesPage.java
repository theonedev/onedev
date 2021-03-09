package io.onedev.server.plugin.report.jest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.eclipse.jgit.lib.FileMode;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.jest.JestTestReportData.Status;
import io.onedev.server.plugin.report.jest.JestTestReportData.TestCase;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.chart.pie.PieChartPanel;
import io.onedev.server.web.component.chart.pie.PieSlice;
import io.onedev.server.web.component.link.ViewStateAwareAjaxLink;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class JestTestCasesPage extends JestTestReportPage {

	public static final String PARAM_FILE = "file";
	
	public static final String PARAM_NAME = "name";
	
	public static final String PARAM_STATUS = "status";
	
	private State state = new State();
	
	private Optional<PatternSet> filePatterns;
	
	private Optional<PatternSet> namePatterns;
	
	private Form<?> fileForm;
	
	private Form<?> nameForm;
	
	private Component fileFeedback;
	
	private Component nameFeedback;
	
	private Component summary;
	
	private WebMarkupContainer detail;
	
	public JestTestCasesPage(PageParameters params) {
		super(params);
		
		state.file = params.get(PARAM_FILE).toOptionalString();
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
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(JestTestCasesPage.class, paramsOf(getBuild(), getReportName(), state));
		pushState(target, url.toString(), state);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		parseFilePatterns();
		parseNamePatterns();
		target.add(fileForm);
		target.add(nameForm);
		target.add(summary);
		target.add(detail);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		fileForm = new Form<Void>("fileForm");
		
		TextField<String> input = new TextField<String>("input", new IModel<String>() {

			@Override
			public void detach() {
			}

			@Override
			public String getObject() {
				return state.file;
			}

			@Override
			public void setObject(String object) {
				state.file = object;
			}
			
		});
		input.add(new PatternSetAssistBehavior() {
			
			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				return SuggestionUtils.suggest(
						getReportData().getTestSuites().stream().map(it->it.getName()).collect(Collectors.toList()), 
						matchWith);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Path containing spaces or starting with dash needs to be quoted",
						"Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
						);
			}
			
		});
		fileForm.add(input);
		
		input.add(new AjaxFormComponentUpdatingBehavior("clear") {
			
			@Override
			protected void onUpdate(AjaxRequestTarget target) {
				pushState(target);
				parseFilePatterns();
				target.add(fileFeedback);
				target.add(summary);
				target.add(detail);
			}
			
		});
		
		fileForm.add(fileFeedback = new FencedFeedbackPanel("feedback", fileForm));
		fileFeedback.setOutputMarkupPlaceholderTag(true);

		fileForm.add(new AjaxButton("submit") {

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
				target.add(fileFeedback);
				target.add(summary);
				target.add(detail);
			}
			
		});
		
		add(fileForm);
		
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
						getReportData().getTestCases(null, null, null).stream().map(it->it.getName()).distinct().collect(Collectors.toList()), 
						matchWith);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Path containing spaces or starting with dash needs to be quoted",
						"Use '**', '*' or '?' for <a href='$docRoot/pages/path-wildcard.md' target='_blank'>path wildcard match</a>. Prefix with '-' to exclude"
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
		
		parseFilePatterns();
		parseNamePatterns();
		
		add(summary = new PieChartPanel("summary", new LoadableDetachableModel<List<PieSlice>>() {

			@Override
			protected List<PieSlice> load() {
				if (filePatterns != null && namePatterns != null) {
					List<PieSlice> slices = new ArrayList<>();
					for (Status status: Status.values()) {
						int numOfTestCases = getReportData().getTestCases(
								filePatterns.orNull(), namePatterns.orNull(), Sets.newHashSet(status)).size();
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
		
		detail = new WebMarkupContainer("detail");
		detail.setOutputMarkupId(true);
		add(detail);
		
		PageableListView<TestCase> testCasesView;
		detail.add(testCasesView = new PageableListView<TestCase>("testCases", 
				new LoadableDetachableModel<List<TestCase>>() {

			@Override
			protected List<TestCase> load() {
				if (filePatterns != null && namePatterns != null)
					return getReportData().getTestCases(filePatterns.orNull(), namePatterns.orNull(), state.statuses);
				else
					return new ArrayList<>();
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<TestCase> item) {
				TestCase testCase = item.getModelObject();
				item.add(new TestStatusBadge("status", testCase.getStatus()));
				item.add(new Label("name", testCase.getName()));
				
				AjaxLink<Void> link = new ViewStateAwareAjaxLink<Void>("testSuite") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						state.file = testCase.getTestSuite().getName();
						pushState(target);
						parseFilePatterns();
						target.add(fileForm);
						target.add(summary);
						target.add(detail);
					}
					
				};
				link.add(new Label("label", testCase.getTestSuite().getName()));
				item.add(link);
				
				BlobIdent blobIdent = new BlobIdent(getBuild().getCommitHash(), testCase.getTestSuite().getName(), 
						FileMode.REGULAR_FILE.getBits());
				if (SecurityUtils.canReadCode(getProject()) && getProject().getBlob(blobIdent, false) != null) {
					item.add(new ViewStateAwarePageLink<Void>("viewSource", ProjectBlobPage.class, 
							ProjectBlobPage.paramsOf(getProject(), blobIdent)));
				} else {
					item.add(new WebMarkupContainer("viewSource").setVisible(false));
				}
								
				RepeatingView messagesView = new RepeatingView("messages");
				for (String message: testCase.getMessages()) {
					messagesView.add(new TestMessagePanel(messagesView.newChildId(), message) {

						@Override
						protected Build getBuild() {
							return JestTestCasesPage.this.getBuild();
						}
						
					});
				}
				item.add(messagesView);
			}
			
		});
		
		detail.add(new OnePagingNavigator("pagingNavigator", testCasesView, null));
		detail.add(new NoRecordsPlaceholder("noRecords", testCasesView));
	}
	
	private void parseFilePatterns() {
		if (state.file != null) {
			try {
				filePatterns = Optional.of(PatternSet.parse(state.file));
			} catch (Exception e) {
				filePatterns = null;
				fileForm.error("Malformed file filter");
			}
		} else {
			filePatterns = Optional.absent();
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
		if (state.file != null)
			params.add(PARAM_FILE, state.file);
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
		return params;
	}
	
	public static class State implements Serializable {
		
		@Nullable
		public String file;
		
		@Nullable
		public String name;
		
		public Collection<Status> statuses;
		
	}
	
}
