package io.onedev.server.web.component.logging;

import static java.util.stream.Collectors.toList;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.Panel;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;

import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.logging.LogService;
import io.onedev.server.logging.LogSnippet;
import io.onedev.server.logging.LoggingSupport;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.page.base.BasePage;

public abstract class LogPanel extends Panel {

	private static final int MAX_LOG_ENTRIES = 1000;
	
	@Inject
	private LogService logService;

	private int nextOffset;
	
	private AbstractPostAjaxBehavior resumeBehavior;
	
	public LogPanel(String id) {
		super(id);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ChangeObserver() {
			
			private void appendRecentLogEntries(IPartialPageRequestHandler handler) {
				List<JobLogEntryEx> logEntries = logService.readLogEntries(getLoggingSupport(), nextOffset, 0);

				if (!logEntries.isEmpty()) {
					nextOffset += logEntries.size();
					
					String script = String.format("onedev.server.log.appendLogEntries('%s', %s);", 
							getMarkupId(), asJSON(logEntries));
					handler.appendJavaScript(script);
				}
			}
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				appendRecentLogEntries(handler);
			}
			
			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(getLoggingSupport().getChangeObservable());
			}
			
		});
		
		if (getPauseSupport() != null) {
			add(new ChangeObserver() {
				
				@Override
				public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
					handler.appendJavaScript(String.format(
							"onedev.server.log.pauseUpdated('%s', %b)",
							getMarkupId(), getPauseSupport().isPaused()));
				}
				
				@Override
				public Collection<String> findObservables() {
					return Sets.newHashSet(getPauseSupport().getStatusChangeObservable());
				}
				
			});

			if (getPauseSupport().canResume()) {
				resumeBehavior = new AbstractPostAjaxBehavior() {
					
					@Override
					protected void respond(AjaxRequestTarget target) {
						getPauseSupport().resume();
						BasePage page = (BasePage) getPage();
						page.notifyObservableChange(target, getPauseSupport().getStatusChangeObservable());
					}
					
				};
				add(resumeBehavior);
			}	
		}
				
		setOutputMarkupId(true);
	}
	
	private String asJSON(List<JobLogEntryEx> entries) {
		entries = entries.stream().map(JobLogEntryEx::transformEmojis).collect(toList());
		try {
			return OneDev.getInstance(ObjectMapper.class).writeValueAsString(entries);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Override
	public void renderHead(IHeaderResponse response) {
		super.renderHead(response);
		
		response.render(JavaScriptHeaderItem.forReference(new LogResourceReference()));
		
		LogSnippet snippet = logService.readLogSnippetReversely(getLoggingSupport(), MAX_LOG_ENTRIES+1);
		
		nextOffset = snippet.offset + snippet.entries.size();
		
		String resumeCallback;
		if (resumeBehavior != null)
			resumeCallback = resumeBehavior.getCallbackFunction().toString();
		else
			resumeCallback = "undefined";
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"onedev.server.log.onDomReady('%s', %s, %d, %b, %s);", 
				getMarkupId(), asJSON(snippet.entries), MAX_LOG_ENTRIES, 
				getPauseSupport() != null && getPauseSupport().isPaused(), 
				resumeCallback)));
	}

	protected abstract LoggingSupport getLoggingSupport();
	
	@Nullable
	protected abstract PauseSupport getPauseSupport();
	
}
