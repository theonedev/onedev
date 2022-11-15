package io.onedev.server.plugin.report.problem;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

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
import io.onedev.server.OneDev;
import io.onedev.server.cluster.ClusterTask;
import io.onedev.server.codequality.CodeProblem;
import io.onedev.server.codequality.CodeProblem.Severity;
import io.onedev.server.entitymanager.ProjectManager;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebConstants;
import io.onedev.server.web.ajaxlistener.ConfirmLeaveListener;
import io.onedev.server.web.behavior.PatternSetAssistBehavior;
import io.onedev.server.web.component.NoRecordsPlaceholder;
import io.onedev.server.web.component.pagenavigator.OnePagingNavigator;
import io.onedev.server.web.component.svg.SpriteImage;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.BlobRenderer;
import io.onedev.server.web.page.project.builds.detail.report.BuildReportPage;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class ProblemReportPage extends BuildReportPage {

	private static final String PARAM_FILE = "file";
	
	protected static final int MAX_PROBLEMS_TO_DISPLAY = 200;
	
	private String file;
	
	private Optional<PatternSet> filePatterns;
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer filesContainer;
	
	private List<String> filePaths;
	
	private Collection<String> expandedFiles = new HashSet<>();
	
	private final IModel<ProblemReport> reportModel = new LoadableDetachableModel<ProblemReport>() {

		@Override
		protected ProblemReport load() {
			Long projectId = getProject().getId();
			Long buildNumber = getBuild().getNumber();
			
			return OneDev.getInstance(ProjectManager.class).runOnProjectServer(projectId, new GetProblemReport(projectId, buildNumber, getReportName()));
		}
		
	};
	
	public ProblemReportPage(PageParameters params) {
		super(params);
		
		file = params.get(PARAM_FILE).toOptionalString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		filePaths = getReport().getProblemFiles().stream()
				.map(it->it.getBlobPath())
				.collect(Collectors.toList());	
		
		form = new Form<Void>("form");
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "file"));
		input.add(new PatternSetAssistBehavior() {
			
			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				return SuggestionUtils.suggestByPattern(filePaths, matchWith);
			}
			
			@Override
			protected List<String> getHints(TerminalExpect terminalExpect) {
				return Lists.newArrayList(
						"Path containing spaces or starting with dash needs to be quoted",
						"Use '*' or '?' for wildcard match. Prefix with '-' to exclude"
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
				target.add(filesContainer);
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
				target.add(filesContainer);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);		

		parseFilePatterns();
		
		filesContainer = new WebMarkupContainer("filesContainer");
		filesContainer.setOutputMarkupId(true);
		add(filesContainer);
		
		PageableListView<ProblemFile> filesView;
		filesContainer.add(filesView = new PageableListView<ProblemFile>("files", 
				new LoadableDetachableModel<List<ProblemFile>>() {

			@Override
			protected List<ProblemFile> load() {
				if (filePatterns != null) {
					if (filePatterns.isPresent()) {
						Matcher matcher = new PathMatcher();
						return getReport().getProblemFiles().stream()
								.filter(it->filePatterns.get().matches(matcher, it.getBlobPath()))
								.collect(Collectors.toList());
					} else {
						return getReport().getProblemFiles();
					}
				} else {
					return new ArrayList<>();
				}
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<ProblemFile> item) {
				ProblemFile file = item.getModelObject();
				String filePath = file.getBlobPath();
				
				AjaxLink<Void> toggleLink = new AjaxLink<Void>("toggle") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (expandedFiles.contains(filePath))
							expandedFiles.remove(filePath);
						else
							expandedFiles.add(filePath);
						target.add(item);
					}
					
				};
				toggleLink.add(new Label("label", filePath));
				toggleLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return expandedFiles.contains(filePath)? "expanded": "collapsed";
					}
					
				}));
				
				item.add(toggleLink);
				
				ProjectBlobPage.State state = new ProjectBlobPage.State();
				state.blobIdent = new BlobIdent(getBuild().getCommitHash(), filePath, 
						FileMode.REGULAR_FILE.getBits());
				state.problemReport = getReportName();
				PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
				item.add(new BookmarkablePageLink<Void>("view", ProjectBlobPage.class, params));

				item.add(new Label("numOfProblems", file.getProblems().size() + " problems"));
				
				item.add(new Label("tooManyProblems", 
						"Too many problems, displaying first " + MAX_PROBLEMS_TO_DISPLAY) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedFiles.contains(filePath) 
								&& item.getModelObject().getProblems().size() > MAX_PROBLEMS_TO_DISPLAY);
					}
					
				});
				
				item.add(new ListView<CodeProblem>("problems", new LoadableDetachableModel<List<CodeProblem>>() {

					@Override
					protected List<CodeProblem> load() {
						List<CodeProblem> problems = item.getModelObject().getProblems();
						if (problems.size() > MAX_PROBLEMS_TO_DISPLAY)
							return problems.subList(0, MAX_PROBLEMS_TO_DISPLAY);
						else
							return problems;
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<CodeProblem> item) {
						CodeProblem problem = item.getModelObject();
						item.add(newSeverityIcon("icon", problem.getSeverity()));
						item.add(new Label("message", problem.getMessage()).setEscapeModelStrings(false));
						
						ProjectBlobPage.State state = new ProjectBlobPage.State();
						state.blobIdent = new BlobIdent(getBuild().getCommitHash(), 
								filePath, FileMode.REGULAR_FILE.getBits());
						state.problemReport = getReportName();
						state.position = BlobRenderer.getSourcePosition(problem.getRange());
						PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
						BookmarkablePageLink<Void> rangeLink = new BookmarkablePageLink<Void>("range", 
								ProjectBlobPage.class, params);
						rangeLink.add(new Label("label", describe(problem.getRange())));
						item.add(rangeLink);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedFiles.contains(filePath));
					}
					
				});
				item.setOutputMarkupId(true);
			}
			
		});
		if (!filesView.getModelObject().isEmpty())
			expandedFiles.add(filesView.getModelObject().iterator().next().getBlobPath());
		
		filesContainer.add(new OnePagingNavigator("pagingNavigator", filesView, null));
		filesContainer.add(new NoRecordsPlaceholder("noRecords", filesView));
	}
	
	private ProblemReport getReport() {
		return reportModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		reportModel.detach();
		super.onDetach();
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(ProblemReportPage.class, paramsOf(getBuild(), getReportName(), file));
		pushState(target, url.toString(), file);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		file = (String) data;
		parseFilePatterns();
		target.add(form);
		target.add(filesContainer);
	}
	
	private void parseFilePatterns() {
		if (file != null) {
			try {
				filePatterns = Optional.of(PatternSet.parse(file));
			} catch (Exception e) {
				file = null;
				form.error("Malformed filter");
			}
		} else {
			filePatterns = Optional.absent();
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new ProblemReportCssResourceReference()));
	}
	
	protected String describe(PlanarRange range) {
		if (range.getToRow() == -1)
			return "Line: " + (range.getFromRow()+1);
		else
			return "Line: " + (range.getFromRow()+1) + " - " + (range.getToRow()+1);
	}
	
	protected SpriteImage newSeverityIcon(String componentId, Severity severity) {
		String iconHref;
		String iconClass;
		switch (severity) {
		case HIGH:
			iconHref = "times-circle-o";
			iconClass = "text-danger";
			break;
		case MEDIUM:
			iconHref = "warning-o";
			iconClass = "text-warning";
			break;
		default:
			iconClass = "text-info";
			iconHref = "info-circle-o";
		}
		
		SpriteImage icon = new SpriteImage(componentId, iconHref);
		icon.add(AttributeAppender.append("class", iconClass));
		return icon;
	}
	
	public static PageParameters paramsOf(Build build, String reportName, @Nullable String file) {
		PageParameters params = paramsOf(build, reportName);
		if (file != null)
			params.add(PARAM_FILE, file);
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
					File reportDir = new File(Build.getDir(projectId, buildNumber), ProblemReport.CATEGORY + "/" + reportName);				
					return ProblemReport.readFrom(reportDir);
				}
				
			});
		}
		
	}
	
}
