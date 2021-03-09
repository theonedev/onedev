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
import org.server.plugin.report.checkstyle.ViolationRule.Violation;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import io.onedev.commons.codeassist.InputSuggestion;
import io.onedev.commons.codeassist.parser.TerminalExpect;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Build;
import io.onedev.server.util.match.Matcher;
import io.onedev.server.util.match.StringMatcher;
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
public class CheckstyleRulesPage extends CheckstyleReportPage {

	private static final String PARAM_RULE = "rule";
	
	private String rule;
	
	private Optional<PatternSet> rulePatterns;
	
	private Form<?> form;
	
	private Component feedback;
	
	private WebMarkupContainer rulesContainer;
	
	private List<String> ruleNames;
	
	private Collection<String> expandedRules = new HashSet<>();
	
	public CheckstyleRulesPage(PageParameters params) {
		super(params);
		
		rule = params.get(PARAM_RULE).toOptionalString();
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		ruleNames = getReportData().getViolationRules().stream()
				.map(it->it.getName())
				.collect(Collectors.toList());
		
		form = new Form<Void>("form");
		
		TextField<String> input = new TextField<String>("input", new PropertyModel<String>(this, "rule"));
		input.add(new PatternSetAssistBehavior() {
			
			@Override
			protected List<InputSuggestion> suggest(String matchWith) {
				return SuggestionUtils.suggestPaths(ruleNames, matchWith);
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
				parseRulePatterns();
				target.add(feedback);
				target.add(rulesContainer);
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
				parseRulePatterns();
				target.add(feedback);
				target.add(rulesContainer);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);		

		parseRulePatterns();
		
		rulesContainer = new WebMarkupContainer("rulesContainer");
		rulesContainer.setOutputMarkupId(true);
		add(rulesContainer);
		
		PageableListView<ViolationRule> rulesView;
		rulesContainer.add(rulesView = new PageableListView<ViolationRule>("rules", 
				new LoadableDetachableModel<List<ViolationRule>>() {

			@Override
			protected List<ViolationRule> load() {
				if (rulePatterns != null) {
					if (rulePatterns.isPresent()) {
						Matcher matcher = new StringMatcher();
						return getReportData().getViolationRules().stream()
								.filter(it->rulePatterns.get().matches(matcher, it.getName()))
								.collect(Collectors.toList());
					} else {
						return getReportData().getViolationRules();
					}
				} else {
					return new ArrayList<>();
				}
			}
			
		}, WebConstants.PAGE_SIZE) {

			@Override
			protected void populateItem(ListItem<ViolationRule> item) {
				ViolationRule rule = item.getModelObject();
				String ruleName = rule.getName();
				
				AjaxLink<Void> toggleLink = new AjaxLink<Void>("toggle") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						if (expandedRules.contains(ruleName))
							expandedRules.remove(ruleName);
						else
							expandedRules.add(ruleName);
						target.add(item);
					}
					
				};
				
				toggleLink.add(newSeverityIcon("icon", rule.getSeverity()));
				
				toggleLink.add(new Label("label", ruleName));
				toggleLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return expandedRules.contains(ruleName)? "expanded": "collapsed";
					}
					
				}));
				
				item.add(toggleLink);
				
				item.add(new Label("tooManyViolations", 
						"Too many violations, displaying first " + MAX_VIOLATIONS_TO_DISPLAY) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedRules.contains(ruleName) 
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
						item.add(new Label("message", violation.getMessage()));
						
						ProjectBlobPage.State state = new ProjectBlobPage.State();
						state.blobIdent = new BlobIdent(getBuild().getCommitHash(), 
								violation.getFile(), FileMode.REGULAR_FILE.getBits());
						state.problemReport = getReportName();
						state.position = SourceRendererProvider.getPosition(violation.getRange());
						PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
						BookmarkablePageLink<Void> rangeLink = new BookmarkablePageLink<Void>("range", 
								ProjectBlobPage.class, params);
						rangeLink.add(new Label("label", violation.describePosition()));
						item.add(rangeLink);
						
						params = CheckstyleFilesPage.paramsOf(
								getBuild(), getReportName(), violation.getFile());
						BookmarkablePageLink<Void> fileLink = new BookmarkablePageLink<Void>("file", 
								CheckstyleFilesPage.class, params); 
						fileLink.add(new Label("label", "File: " + violation.getFile()));
						item.add(fileLink);
					}

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(expandedRules.contains(ruleName));
					}
					
				});		
				item.setOutputMarkupId(true);
			}
			
		});
		if (!rulesView.getModelObject().isEmpty())
			expandedRules.add(rulesView.getModelObject().iterator().next().getName());
		
		rulesContainer.add(new OnePagingNavigator("pagingNavigator", rulesView, null));
		rulesContainer.add(new NoRecordsPlaceholder("noRecords", rulesView));
	}
	
	private void pushState(AjaxRequestTarget target) {
		CharSequence url = urlFor(CheckstyleRulesPage.class, paramsOf(getBuild(), getReportName(), rule));
		pushState(target, url.toString(), rule);
	}
	
	@Override
	protected void onPopState(AjaxRequestTarget target, Serializable data) {
		super.onPopState(target, data);
		rule = (String) data;
		parseRulePatterns();
		target.add(form);
		target.add(rulesContainer);
	}
	
	private void parseRulePatterns() {
		if (rule != null) {
			try {
				rulePatterns = Optional.of(PatternSet.parse(rule));
			} catch (Exception e) {
				rule = null;
				form.error("Malformed filter");
			}
		} else {
			rulePatterns = Optional.absent();
		}
	}
	
	public static PageParameters paramsOf(Build build, String reportName, @Nullable String rule) {
		PageParameters params = paramsOf(build, reportName);
		if (rule != null)
			params.add(PARAM_RULE, rule);
		return params;
	}
}
