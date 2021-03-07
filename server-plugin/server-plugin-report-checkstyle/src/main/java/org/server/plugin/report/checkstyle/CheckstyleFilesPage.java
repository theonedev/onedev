package org.server.plugin.report.checkstyle;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.util.SortableDataProvider;
import org.apache.wicket.feedback.FencedFeedbackPanel;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
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
import io.onedev.server.web.component.datatable.OneDataTable;
import io.onedev.server.web.page.project.blob.ProjectBlobPage;
import io.onedev.server.web.page.project.blob.render.renderers.source.SourceRendererProvider;
import io.onedev.server.web.util.LoadableDetachableDataProvider;
import io.onedev.server.web.util.SuggestionUtils;

@SuppressWarnings("serial")
public class CheckstyleFilesPage extends CheckstyleReportPage {

	private static final String PARAM_FILE = "file";
	
	private final IModel<List<ViolationFile>> filesModel = new LoadableDetachableModel<List<ViolationFile>>() {

		@Override
		protected List<ViolationFile> load() {
			if (filePatterns != null) {
				List<ViolationFile> files = new ArrayList<>(getReportData().getViolationFiles().values());
				if (filePatterns.isPresent()) {
					Matcher matcher = new PathMatcher();
					files = files.stream()
							.filter(it->filePatterns.get().matches(matcher, it.getPath()))
							.collect(Collectors.toList());
				}
				return files;
			} else {
				return new ArrayList<>();
			}
		}
		
	};
	
	private String file;
	
	private Optional<PatternSet> filePatterns;
	
	private Form<?> form;
	
	private Component feedback;
	
	private DataTable<ViolationFile, Void> filesTable;
	
	private List<String> filePaths;
	
	public CheckstyleFilesPage(PageParameters params) {
		super(params);
		
		file = params.get(PARAM_FILE).toOptionalString();
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		filePaths = new ArrayList<>(getReportData().getViolationFiles().keySet());			
		
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
				target.add(filesTable);
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
				target.add(filesTable);
			}
			
		});
		form.setOutputMarkupId(true);
		add(form);		

		List<IColumn<ViolationFile, Void>> columns = new ArrayList<>();
		
		columns.add(new AbstractColumn<ViolationFile, Void>(Model.of("")) {

			@Override
			public void populateItem(Item<ICellPopulator<ViolationFile>> cellItem, 
					String componentId, IModel<ViolationFile> rowModel) {
				Fragment fragment = new Fragment(componentId, "fileFrag", CheckstyleFilesPage.this);
				fragment.setOutputMarkupId(true);
				cellItem.add(fragment);
				
				ViolationFile file = rowModel.getObject();

				AtomicBoolean showViolations = new AtomicBoolean(
						filesTable.getCurrentPage() == 0 && cellItem.findParent(Item.class).getIndex() == 0);
				
				AjaxLink<Void> toggleLink = new AjaxLink<Void>("toggle") {

					@Override
					public void onClick(AjaxRequestTarget target) {
						showViolations.set(!showViolations.get());
						target.add(fragment);
					}
					
				};
				toggleLink.add(new Label("label", file.getPath()));
				toggleLink.add(AttributeAppender.append("class", new AbstractReadOnlyModel<String>() {

					@Override
					public String getObject() {
						return showViolations.get()? "expanded": "collapsed";
					}
					
				}));
				
				fragment.add(toggleLink);
				
				ProjectBlobPage.State state = new ProjectBlobPage.State();
				state.blobIdent = new BlobIdent(getBuild().getCommitHash(), file.getPath(), 
						FileMode.REGULAR_FILE.getBits());
				state.problemReport = getReportName();
				PageParameters params = ProjectBlobPage.paramsOf(getProject(), state);
				fragment.add(new BookmarkablePageLink<Void>("view", ProjectBlobPage.class, params));

				fragment.add(new Label("numOfErrors", file.getNumOfErrors() + " errors")
						.setVisible(file.getNumOfErrors() != 0));
				fragment.add(new Label("numOfWarnings", file.getNumOfWarnings() + " warnings")
						.setVisible(file.getNumOfWarnings() != 0));
				fragment.add(new Label("numOfInfos", file.getNumOfInfos() + " infos")
						.setVisible(file.getNumOfInfos() != 0));
				
				fragment.add(new Label("tooManyViolations", 
						"Too many violations, displaying first " + MAX_VIOLATIONS_TO_DISPLAY) {

					@Override
					protected void onConfigure() {
						super.onConfigure();
						setVisible(showViolations.get() 
								&& rowModel.getObject().getViolations().size() > MAX_VIOLATIONS_TO_DISPLAY);
					}
					
				});
				
				fragment.add(new ListView<Violation>("violations", new LoadableDetachableModel<List<Violation>>() {

					@Override
					protected List<Violation> load() {
						List<Violation> violations = rowModel.getObject().getViolations();
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
								rowModel.getObject().getPath(), FileMode.REGULAR_FILE.getBits());
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
						setVisible(showViolations.get());
					}
					
				});
			}

		});
		
		SortableDataProvider<ViolationFile, Void> dataProvider = 
				new LoadableDetachableDataProvider<ViolationFile, Void>() {

			@Override
			public Iterator<? extends ViolationFile> iterator(long first, long count) {
				if (getFiles().size() > first+count)
					return getFiles().subList((int)first, (int)(first+count)).iterator();
				else
					return getFiles().subList((int)first, getFiles().size()).iterator();
			}

			@Override
			public long calcSize() {
				return getFiles().size();
			}

			@Override
			public IModel<ViolationFile> model(ViolationFile object) {
				String filePath = object.getPath();
				return new LoadableDetachableModel<ViolationFile>() {

					@Override
					protected ViolationFile load() {
						return getReportData().getViolationFiles().get(filePath);
					}
					
				};
			}
			
		};			
		add(filesTable = new OneDataTable<ViolationFile, Void>("files", columns, 
				dataProvider, WebConstants.PAGE_SIZE, null));
		filesTable.setOutputMarkupId(true);
		
		parseFilePatterns();
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
		target.add(filesTable);
	}
	
	private List<ViolationFile> getFiles() {
		return filesModel.getObject();
	}
	
	@Override
	protected void onDetach() {
		filesModel.detach();
		super.onDetach();
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
