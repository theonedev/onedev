package io.onedev.server.web.component.build.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.job.JobService;
import io.onedev.server.job.log.LogService;
import io.onedev.server.job.log.LogSnippet;
import io.onedev.server.model.Build;
import io.onedev.server.model.Build.Status;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.behavior.ChangeObserver;
import io.onedev.server.web.page.base.BasePage;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.core.request.handler.IPartialPageRequestHandler;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.panel.GenericPanel;
import org.apache.wicket.model.IModel;

import java.util.Collection;
import java.util.List;

import static io.onedev.server.model.Build.getDetailChangeObservable;
import static java.util.stream.Collectors.toList;

public class BuildLogPanel extends GenericPanel<Build> {

	private static final int MAX_LOG_ENTRIES = 1000;
	
	private int nextOffset;
	
	private AbstractPostAjaxBehavior resumeBehavior;
	
	public BuildLogPanel(String id, IModel<Build> model) {
		super(id, model);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();

		add(new ChangeObserver() {
			
			private void appendRecentLogEntries(IPartialPageRequestHandler handler) {
				List<JobLogEntryEx> logEntries = getLogService().readLogEntries(
						getBuild().getProject().getId(), getBuild().getNumber(), nextOffset, 0);

				if (!logEntries.isEmpty()) {
					nextOffset += logEntries.size();
					
					String script = String.format("onedev.server.buildLog.appendLogEntries('%s', %s);", 
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
				return Sets.newHashSet(Build.getLogChangeObservable(getBuild().getId()));
			}
			
		});
		
		add(new ChangeObserver() {
			
			@Override
			public void onObservableChanged(IPartialPageRequestHandler handler, Collection<String> changedObservables) {
				handler.appendJavaScript(String.format(
						"onedev.server.buildLog.buildUpdated('%s', %b)",
						getMarkupId(), getBuild().isPaused()));
			}
			
			@Override
			public Collection<String> findObservables() {
				return Sets.newHashSet(getDetailChangeObservable(getBuild().getId()));
			}
			
		});
		
		if (SecurityUtils.canRunJob(getBuild().getProject(), getBuild().getJobName())) {
			resumeBehavior = new AbstractPostAjaxBehavior() {
				
				@Override
				protected void respond(AjaxRequestTarget target) {
					OneDev.getInstance(JobService.class).resume(getBuild());
					BasePage page = (BasePage) getPage();
					page.notifyObservableChange(target, getDetailChangeObservable(getBuild().getId()));
				}
				
			};
			add(resumeBehavior);
		}
		
		setOutputMarkupId(true);
	}

	private LogService getLogService() {
		return OneDev.getInstance(LogService.class);
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
		
		response.render(JavaScriptHeaderItem.forReference(new BuildLogResourceReference()));
		
		LogSnippet snippet = getLogService().readLogSnippetReversely(
				getBuild().getProject().getId(), getBuild().getNumber(), MAX_LOG_ENTRIES+1);
		
		nextOffset = snippet.offset + snippet.entries.size();
		
		String resumeCallback;
		if (resumeBehavior != null)
			resumeCallback = resumeBehavior.getCallbackFunction().toString();
		else
			resumeCallback = "undefined";
		response.render(OnDomReadyHeaderItem.forScript(String.format(
				"onedev.server.buildLog.onDomReady('%s', %s, %d, %b, %s);", 
				getMarkupId(), asJSON(snippet.entries), MAX_LOG_ENTRIES, 
				getBuild().getStatus() == Status.RUNNING && getBuild().isPaused(), 
				resumeCallback)));
	}

	private Build getBuild() {
		return getModelObject();
	}
}
