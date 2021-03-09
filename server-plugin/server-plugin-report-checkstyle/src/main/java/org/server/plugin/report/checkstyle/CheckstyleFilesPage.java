package org.server.plugin.report.checkstyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.list.PageableListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.eclipse.jgit.lib.FileMode;
import org.server.plugin.report.checkstyle.ViolationFile.Violation;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
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
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class CheckstyleFilesPage extends CheckstyleReportPage {

	private static final String PARAM_FILE = "file";
	
	private String file;
	
	private Optional<PatternSet> filePatterns;
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer filesContainer;
	
	private List<String> filePaths;
	
	private Collection<String> expandedFiles = new HashSet<>();
	
	public CheckstyleFilesPage(PageParameters params) {
		super(params);
		
		file = params.get(PARAM_FILE).toOptionalString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		filePaths = getReportData().getViolationFiles().stream()
				.map(it->it.getPath())
				.collect(Collectors.toList());	
		
		form = new Form<Void>("form");
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "file"));
		input.add(new PatternSetAssistBehavior() {
			
			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				return SuggestionUtils.suggestPaths(filePaths, matchWith);
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
		
		PageableListView<ViolationFile> filesView;
		filesContainer.add(filesView = new PageableListView<ViolationFile>("files", 
				new LoadableDetachableModel<List<ViolationFile>>() {

			@Override
			protected List<ViolationFile> load() {
				if (filePatterns != null) {
					if (filePatterns.isPresent()) {
						Matcher matcher = new PathMatcher();
						return getReportData().getViolationFiles().stream()
								.filter(it->filePatterns.get().matches(matcher, it.getPath()))
								.collect(Collectors.toList());
					} else {
						return getReportData().getViolationFiles();
					}
				} else {
					return new ArrayList<>();
				}
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<ViolationFile> item) {
				ViolationFile file = item.getModelObject();
				String filePath = file.getPath();
				
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

				item.add(new Label("numOfErrors", file.getNumOfErrors() + " errors")
						.setVisible(file.getNumOfErrors() != 0));
				item.add(new Label("numOfWarnings", file.getNumOfWarnings() + " warnings")
						.setVisible(file.getNumOfWarnings() != 0));
				item.add(new Label("numOfInfos", file.getNumOfInfos() + " infos")
						.setVisible(file.getNumOfInfos() != 0));
				
				item.add(new Label("tooManyViolations", 
						"Too many violations, displaying first " + MAX_VIOLATIONS_TO_DISPLAY) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedFiles.contains(filePath) 
								&& item.getModelObject().getViolations().size() > MAX_VIOLATIONS_TO_DISPLAY);
					}
					
				});
				
				item.add(new ListView<Violation>("violations", new LoadableDetachableModel<List<Violation>>() {

					@Override
					protected List<Violation> load() {
						List<Violation> violations = item.getModelObject().getViolations();
						if (violations.size() > MAX_VIOLATIONS_TO_DISPLAY)
							return violations.subList(0, MAX_VIOLATIONS_TO_DISPLAY);
						else
							return violations;
					}
					
				}) {

					@Override
					protected void populateItem(ListItem<Violation> item) {
						Violation violation = item.getModelObject();
						item.add(newSeverityIcon("icon", violation.getSeverity()));
						item.add(new Label("message", violation.getMessage()));
						
						ProjectBlobPage.State state = new ProjectBlobPage.State();
						state.blobIdent = new BlobIdent(getBuild().getCommitHash(), 
								filePath, FileMode.REGULAR_FILE.getBits());
						state.problemReport = getReportName();
						state.position = SourceRendererProvider.getPosition(violation.getRange());
						PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
						BookmarkablePageLink<Void> rangeLink = new BookmarkablePageLink<Void>("range", 
								ProjectBlobPage.class, params);
						rangeLink.add(new Label("label", violation.describePosition()));
						item.add(rangeLink);
						
						params = CheckstyleRulesPage.paramsOf(
								getBuild(), getReportName(), violation.getRule());
						BookmarkablePageLink<Void> ruleLink = new BookmarkablePageLink<Void>("rule", 
								CheckstyleRulesPage.class, params); 
						ruleLink.add(new Label("label", "Rule: " + violation.getRule()));
						item.add(ruleLink);
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
			expandedFiles.add(filesView.getModelObject().iterator().next().getPath());
		
		filesContainer.add(new OnePagingNavigator("pagingNavigator", filesView, null));
		filesContainer.add(new NoRecordsPlaceholder("noRecords", filesView));
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(CheckstyleFilesPage.class, paramsOf(getBuild(), getReportName(), file));
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
	
	public static PageParameters paramsOf(Build build, String reportName, @Nullable String file) {
		PageParameters params = paramsOf(build, reportName);
		if (file != null)
			params.add(PARAM_FILE, file);
		return params;
	}
}
