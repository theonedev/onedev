package io.onedev.server.web.component.commandpalette;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.core.request.mapper.MountedMapper;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;

import edu.emory.mathcs.backport.java.util.Arrays;
import edu.emory.mathcs.backport.java.util.Collections;
import io.onedev.commons.utils.PathUtils;
import io.onedev.server.OneDev;
import io.onedev.server.model.Project;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.util.ReflectionUtils;
import io.onedev.server.util.match.PathMatcher;
import io.onedev.server.util.patternset.PatternSet;
import io.onedev.server.web.WebApplication;
import io.onedev.server.web.behavior.OnTypingDoneBehavior;
import io.onedev.server.web.behavior.SelectByTypingBehavior;
import io.onedev.server.web.behavior.infinitescroll.InfiniteScrollBehavior;
import io.onedev.server.web.page.project.ProjectPage;

@SuppressWarnings("serial")
public abstract class CommandPalettePanel extends Panel {

	private static final int PAGE_SIZE = 100;
	
	private static final List<String[]> availableUrls = new ArrayList<>();

	private static final PatternSet excludedUrlPatterns = PatternSet.parse(""
			+ "test/** errors/** sso/** verify-email-address/** reset-password/** signup/** "
			+ "logout/** login/** loading/** init/** help/** builds/** issues/** "
			+ "pull-requests/** **/invalid **/blob **/${issue}/** -**/${issue} "
			+ "**/${request}/** -**/${request} **/${build}/** -**/${build} "
			+ "**/${milestone}/** -**/${milestone} **/${agent}/** -**/${agent} "
			+ "**/${group}/** -**/${group}");
	
	static {
		for (IRequestMapper mapper: OneDev.getInstance(WebApplication.class).getRequestMappers())
			availableUrls.addAll(getMountedPaths(mapper));
		
		Collections.sort(availableUrls, new Comparator<String[]>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(String[] o1, String[] o2) {
				return PathUtils.compare(Arrays.asList(o1), Arrays.asList(o2));
			}
			
		});
		
	}
	
	private static List<String[]> getMountedPaths(IRequestMapper mapper) {
		if (mapper instanceof MountedMapper) {
			try {
				Field field = ReflectionUtils.findField(mapper.getClass(), "mountSegments");
				Preconditions.checkNotNull(field);
				field.setAccessible(true);
				String[] mountSegments = (String[]) field.get(mapper);
				if (Arrays.equals(mountSegments, new String[] {"projects", "${project}", "files"}))
					mountSegments = new String[] {"projects", "${project}", "files", "#{revision-and-path}"};
				List<String[]> mountedPaths = new ArrayList<>();
				if (mountSegments != null && mountSegments.length != 0) {
					String url = Joiner.on("/").join(mountSegments);
					if (!excludedUrlPatterns.matches(new PathMatcher(), url)) 
						mountedPaths.add(mountSegments);
				}
				return mountedPaths;
			} catch (SecurityException | IllegalArgumentException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		} else if (mapper instanceof ICompoundRequestMapper) {
			List<String[]> mountedPaths = new ArrayList<>();
			ICompoundRequestMapper compoundMapper = (ICompoundRequestMapper) mapper;
			for (IRequestMapper childMapper: compoundMapper)
				mountedPaths.addAll(getMountedPaths(childMapper));
			return mountedPaths;
		} else {
			return new ArrayList<>();
		}
	}
	
	private final List<ParsedUrl> parsedUrls = new ArrayList<>();
	
	private int numSuggestionsToLoad = PAGE_SIZE;
	
	private TextField<String> input;
	
	private final IModel<List<CommandSuggestion>> suggestionsModel = 
			new LoadableDetachableModel<List<CommandSuggestion>>() {

		@Override
		protected List<CommandSuggestion> load() {
			List<CommandSuggestion> suggestions = new ArrayList<>();
			String matchWith = input.getModelObject();
			if (matchWith != null)
				matchWith = matchWith.toLowerCase();
			else
				matchWith = "";

			Map<String, SuggestionContent> suggestionMap = new LinkedHashMap<>();
			
			for (ParsedUrl url: parsedUrls) {
				int leftOver = numSuggestionsToLoad - suggestionMap.size();
				if (leftOver > 0) {
					for (Map.Entry<String, SuggestionContent> entry: url.suggest(matchWith, leftOver).entrySet()) {
						SuggestionContent content = suggestionMap.get(entry.getKey());
						if (content != null)
							content = content.mergeWith(entry.getValue());
						else
							content = entry.getValue();
						suggestionMap.put(entry.getKey(), content);
					}
				} else {
					break;
				}
			}
			
			for (Map.Entry<String, SuggestionContent> entry: suggestionMap.entrySet())
				suggestions.add(new CommandSuggestion(entry.getKey(), entry.getValue()));
			
			return suggestions;
		}

	};	
	
	public CommandPalettePanel(String id) {
		super(id);
	}
	
	private ParsedUrl newProjectAwareParsedUrl(String[] url) {
		return new ParsedUrl(url) {

			@Override
			protected Project getProject() {
				if (getPage() instanceof ProjectPage)
					return ((ProjectPage)getPage()).getProject();
				else
					return null;
			}
			
		};
		
	}
	
	@Override
	protected void onInitialize() {
		super.onInitialize();
		
		if (getPage() instanceof ProjectPage) {
			for (String[] url: availableUrls) {
				try {
					if (url.length > 2 && url[0].equals("projects") && url[1].equals("${project}")) {
						String[] relativeUrl = new String[url.length-2];
						System.arraycopy(url, 2, relativeUrl, 0, relativeUrl.length);
						parsedUrls.add(newProjectAwareParsedUrl(relativeUrl));
					}
				} catch (IgnoredUrlParam e) {
				}
			}
		}
		for (String[] url: availableUrls) {
			boolean applicable = false;
			if (SecurityUtils.isAdministrator()) {
				applicable = true;
			} else if (SecurityUtils.getUser() != null) {
				if (!url[0].equals("administration"))
					applicable = true;
			} else if (!url[0].equals("administration") && !url[0].equals("my")) {
				applicable = true;
			}
			if (applicable) {
				try {
					parsedUrls.add(new ParsedUrl(url) {
	
						@Override
						protected Project getProject() {
							return null;
						}
					});				
				} catch (IgnoredUrlParam e) {
				}
			};				
		}
		
		add(new AjaxLink<Void>("close") {

			@Override
			public void onClick(AjaxRequestTarget target) {
				onCancel(target);
			}
			
		});
		
		input = new TextField<String>("input", Model.of(""));
		input.add(new OnTypingDoneBehavior() {
			
			@Override
			protected void onTypingDone(AjaxRequestTarget target) {
				WebMarkupContainer suggestions = newSuggestions();
				replace(suggestions);
				target.add(suggestions);
				
				target.add(get("noSuggestions"));
			}
			
		});
		input.add(new SelectByTypingBehavior(this));
		add(input);

		add(newSuggestions());
		
		add(new WebMarkupContainer("noSuggestions") {
			
			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(getSuggestions().isEmpty());
			}
			
		}.setOutputMarkupPlaceholderTag(true));
		
	}
	
	private WebMarkupContainer newSuggestions() {
		return new WebMarkupContainer("suggestions") {

			private WebMarkupContainer newSuggestionItem(
					RepeatingView suggestionsView, CommandSuggestion suggestion) {
				String url, searchBase;
				if (suggestion.getContent() != null) {
					url = suggestion.getContent().getUrl();
					searchBase = suggestion.getContent().getSearchBase();
				} else {
					url = null;
					searchBase = null;
				}
				WebMarkupContainer container = new WebMarkupContainer(suggestionsView.newChildId()) {
					
					@Override
					protected void onComponentTag(ComponentTag tag) {
						super.onComponentTag(tag);
						if (url != null) {
							if (url.startsWith("/")) {
								tag.put("onclick", String.format("javascript:window.location='%s';", url));
							} else {
								Long projectId = ((ProjectPage)getPage()).getProject().getId();
								tag.put("onclick", String.format("javascript:window.location='/projects/%d/%s';", projectId, url));
							}
						}
						else
							tag.put("onclick", "javascript:void(0);");
						if (searchBase != null)
							tag.put("data-completion", searchBase);
					}
					
				};
				StringBuilder hintBuilder = new StringBuilder();

				if (searchBase != null) 
					hintBuilder.append("<span class='text-nowrap'><span class='keycap'>Tab</span> to search</span>");
				if (url != null) 
					hintBuilder.append("<span class='text-nowrap'><span class='keycap'>Enter</span> to go</span>");
				
				if (url != null || searchBase != null)
					container.add(new Label("label", suggestion.getLabel()));
				else
					container.add(new Label("label", "<i>" + suggestion.getLabel() + "</i>").setEscapeModelStrings(false));
				
				if (suggestionsView.size() == 0 && (url != null || searchBase != null))
					container.add(AttributeAppender.append("class", "active"));
				
				if (hintBuilder.length() != 0)
					container.add(new Label("hint", hintBuilder.toString()).setEscapeModelStrings(false));
				else
					container.add(new WebMarkupContainer("hint").setVisible(false));
				
				suggestionsView.add(container);
				
				return container;
			}
			
			@Override
			protected void onInitialize() {
				super.onInitialize();
				
				RepeatingView suggestionsView = new RepeatingView("suggestions");
				
				for (CommandSuggestion suggestion: suggestionsModel.getObject()) 
					newSuggestionItem(suggestionsView, suggestion);
				
				add(suggestionsView);
				
				add(new InfiniteScrollBehavior(PAGE_SIZE) {

					@Override
					protected void appendMore(AjaxRequestTarget target, int offset, int count) {
						numSuggestionsToLoad += PAGE_SIZE;
						for (int i=offset; i<offset+count; i++) {
							if (i < getSuggestions().size()) {
								CommandSuggestion suggestion = getSuggestions().get(i);
								Component suggestionItem = newSuggestionItem(suggestionsView, suggestion);
								String script = String.format("$('#%s').append('<li id=\"%s\"></li>');", 
										getMarkupId(), suggestionItem.getMarkupId());
								target.prependJavaScript(script);
								target.add(suggestionItem);
							} else {
								break;
							}
						}
					}

				});
				
				setOutputMarkupPlaceholderTag(true);
			}

			@Override
			protected void onConfigure() {
				super.onConfigure();
				setVisible(!getSuggestions().isEmpty());
			}
			
		};		
	}
	
	@Override
	protected void onDetach() {
		suggestionsModel.detach();
		super.onDetach();
	}

	private List<CommandSuggestion> getSuggestions() {
		return suggestionsModel.getObject();
	}

	protected abstract void onCancel(AjaxRequestTarget target);

	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		response.render(CssHeaderItem.forReference(new CommandPaletteCssResourceReference()));
	}

}
