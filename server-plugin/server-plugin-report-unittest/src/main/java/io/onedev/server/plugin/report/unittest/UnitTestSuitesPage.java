package io.onedev.server.plugin.report.unittest;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.plugin.report.unittest.UnitTestReport.Status;
import io.onedev.server.plugin.report.unittest.UnitTestReport.TestSuite;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.chart.pie.PieChartPanel;
import io.onedev.server.web.component.chart.pie.PieSlice;
import io.onedev.server.web.component.link.ViewStateAwarePageLink;
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
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.StringValue;
import org.eclipse.jgit.lib.FileMode;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;
import static java.util.List.of;
import static java.util.stream.Collectors.toSet;

@SuppressWarnings("serial")
public class UnitTestSuitesPage extends UnitTestReportPage {

	private static final String PARAM_NAME = "name";
	
	private static final String PARAM_STATUS = "status";
	
	private static final String PARAM_LONGEST_DURATION_FIRST = "longestDurationFirst";
	
	private State state = new State();
	
	private Optional<PatternSet> namePatterns;
	
	private Form<?> form;

	private Component feedback;
	
	private Component summary;

	private Component orderBy;
	
	private WebMarkupContainer detail;
	
	public UnitTestSuitesPage(PageParameters params) {
		super(params);
		
		state.name = params.get(PARAM_NAME).toOptionalString();
		state.longestDurationFirst = params.get(PARAM_LONGEST_DURATION_FIRST).toBoolean(false);

		namePatterns = parseNamePatterns();

		if (params.get(PARAM_STATUS).toString() != null) {
			state.statuses = new HashSet<>();
			if (params.get(PARAM_STATUS).toString().length() != 0) {
				for (StringValue each : params.getValues(PARAM_STATUS))
					state.statuses.add(Status.valueOf(each.toString().toUpperCase()));
			}
		}
		updateActualStatuses();
	}

	private void updateActualStatuses() {
		if (state.statuses == null) {
			state.actualStatuses = new HashSet<>();
			var report = getReport();
			if (report != null && namePatterns != null) {
				var seenStatuses = report.getTestSuites(namePatterns.orNull(), of(Status.values()))
						.stream().map(TestSuite::getStatus).collect(toSet());
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
		CharSequence url = urlFor(UnitTestSuitesPage.class, paramsOf(getBuild(), getReportName(), state));
		pushState(target, url.toString(), state);
	}

	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		state = (State) data;
		namePatterns = parseNamePatterns();
		if (namePatterns == null)
			form.error("Malformed name filter");
		updateActualStatuses();
		target.add(form);
		target.add(summary);
		target.add(orderBy);
		target.add(detail);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getReport() != null) {
			var fragment = new Fragment("report", "validFrag", this);
			form = new Form<Void>("form");
			TextField<String> nameFilter = new TextField<String>("name", new IModel<String>() {

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
			nameFilter.add(new PatternSetAssistBehavior() {

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
			form.add(nameFilter);

			nameFilter.add(new AjaxFormComponentUpdatingBehavior("clear") {

				@Override
				protected void onUpdate(AjaxRequestTarget target) {
					pushState(target);
					namePatterns = parseNamePatterns();
					if (namePatterns == null)
						form.error("Malformed name filter");
					updateActualStatuses();
					target.add(feedback);
					target.add(summary);
					target.add(detail);
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
					namePatterns = parseNamePatterns();
					if (namePatterns == null)
						form.error("Malformed name filter");
					updateActualStatuses();
					target.add(feedback);
					target.add(summary);
					target.add(detail);
				}

			});

			fragment.add(form);

			if (namePatterns == null)
				form.error("Malformed name filter");

			fragment.add(summary = new PieChartPanel("summary", new LoadableDetachableModel<List<PieSlice>>() {

				@Override
				protected List<PieSlice> load() {
					if (namePatterns != null) {
						List<PieSlice> slices = new ArrayList<>();
						for (Status status: Status.values()) {
							int numOfTestSuites = getReport().getTestSuites(
									namePatterns.orNull(), Sets.newHashSet(status)).size();
							slices.add(new PieSlice(status.name().toLowerCase().replace("_", " "),
									numOfTestSuites, status.getColor(), state.actualStatuses.contains(status)));
						}
						return slices;
					} else {
						return null;
					}
				}

			}) {

				@Override
				protected void onSelectionChange(AjaxRequestTarget target, String sliceName) {
					Status status = Status.valueOf(sliceName.replace(' ', '_').toUpperCase());
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

			});

			detail = new WebMarkupContainer("detail");
			detail.setOutputMarkupId(true);
			fragment.add(detail);

			PageableListView<TestSuite> testSuitesView;
			detail.add(testSuitesView = new PageableListView<TestSuite>("testSuites",
					new LoadableDetachableModel<List<TestSuite>>() {

						@Override
						protected List<TestSuite> load() {
							if (namePatterns != null) {
								List<TestSuite> testSuites = getReport().getTestSuites(namePatterns.orNull(), state.actualStatuses);
								testSuites.sort(comparingInt(o -> o.getStatus().ordinal()));
								if (state.longestDurationFirst) {
									testSuites.sort((o1, o2) -> {
										if (o1.getDuration() < o2.getDuration())
											return 1;
										else if (o1.getDuration() > o2.getDuration())
											return -1;
										else
											return 0;
									});
								}
								return testSuites;
							} else {
								return new ArrayList<>();
							}
						}

					}, WebConstants.PAGE_SIZE) {

				@Override
				protected void populateItem(ListItem<TestSuite> item) {
					TestSuite testSuite = item.getModelObject();
					item.add(new TestStatusBadge("status", testSuite.getStatus()));

					UnitTestCasesPage.State state = new UnitTestCasesPage.State();
					state.testSuite = testSuite.getName();
					PageParameters params = UnitTestCasesPage.paramsOf(getBuild(), getReportName(), state);
					Link<Void> link = new ViewStateAwarePageLink<Void>("testCases",
							UnitTestCasesPage.class, params);
					link.add(new Label("label", testSuite.getName()));

					if (testSuite.getBlobPath() != null && SecurityUtils.canReadCode(getProject())) {
						var sourceViewState = new ProjectBlobPage.State();
						sourceViewState.blobIdent = new BlobIdent(getBuild().getCommitHash(), testSuite.getBlobPath());
						if (testSuite.getPosition() != null)
							sourceViewState.position = BlobRenderer.getSourcePosition(testSuite.getPosition());
						item.add(new ViewStateAwarePageLink<Void>("viewSource", ProjectBlobPage.class,
								ProjectBlobPage.paramsOf(getProject(), sourceViewState)));
					} else {
						item.add(new WebMarkupContainer("viewSource").setVisible(false));
					}

					item.add(new Label("duration", DurationFormatUtils.formatDuration(testSuite.getDuration(), "s.SSS 's'")));
					item.add(link);

					Component detailViewer = testSuite.renderDetail("detail", getBuild());
					if (detailViewer != null)
						item.add(detailViewer);
					else
						item.add(new WebMarkupContainer("detail").setVisible(false));
				}

			});

			detail.add(new OnePagingNavigator("pagingNavigator", testSuitesView, null));
			detail.add(new NoRecordsPlaceholder("noRecords", testSuitesView));			
			add(fragment);
		} else {
			add(new Fragment("report", "invalidFrag", this));
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
		public String name;
		
		public boolean longestDurationFirst;
		
		public Collection<Status> actualStatuses;
		
		public Collection<Status> statuses;
	}
	
}
