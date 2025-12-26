package io.onedev.server.web.component.taskbutton;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.buildspec.job.log.JobLogEntry;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.job.log.StyleBuilder;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.taskbutton.TaskResult.PlainMessage;

public abstract class TaskButton extends AjaxButton {

	@Inject
	private TaskFutureService taskFutureService;

	@Inject
	private ExecutorService executorService;

	public TaskButton(String id) {
		super(id);
	}

	@Override
	protected void onError(AjaxRequestTarget target, Form<?> form) {
		target.add(form);
	}

	@Override
	protected void onInitialize() {
		super.onInitialize();
		setOutputMarkupPlaceholderTag(true);
	}
	
	protected String getTitle() {
		return _T(StringUtils.capitalize(WordUtils.uncamel(getId()).toLowerCase()));
	}
	
	@Override
	protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
		super.updateAjaxAttributes(attributes);
		attributes.getAjaxCallListeners().add(new IAjaxCallListener() {
			
			@Override
			public CharSequence getSuccessHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getPrecondition(Component component) {
				return String.format("onedev.server.taskButtonFormDirty = $('#%s').closest('form').hasClass('dirty');",
						getMarkupId());
			}
			
			@Override
			public CharSequence getInitHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getFailureHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getDoneHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getCompleteHandler(Component component) {
				return String.format(""
						+ "if (onedev.server.taskButtonFormDirty) "
						+ "  onedev.server.form.markDirty($('#%s').closest('form'));",
						getMarkupId());
			}
			
			@Override
			public CharSequence getBeforeSendHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getBeforeHandler(Component component) {
				return null;
			}
			
			@Override
			public CharSequence getAfterHandler(Component component) {
				return null;
			}
			
		});
	}

	protected void onCompleted(AjaxRequestTarget target, boolean successful) {
	}
	
	protected void onCancelled(AjaxRequestTarget target) {
	}
	
	protected void submitTask(AjaxRequestTarget target) {
		String taskId = getSession().getId() + ":" + getPath();

		List<JobLogEntryEx> messages = new ArrayList<>();
		messages.add(new JobLogEntryEx(new JobLogEntry(new Date(), _T("Please wait..."))));
		var application = Application.get();
		var requestCycle = RequestCycle.get();
		TaskFuture prevFuture = taskFutureService.getTaskFutures().put(taskId, new TaskFuture(executorService.submit(new Callable<TaskResult>() {

			@Override
			public TaskResult call() throws Exception {
				TaskLogger logger = new TaskLogger() {

					private final Map<String, StyleBuilder> styleBuilders = new ConcurrentHashMap<>();
					
					@Override
					public void log(String message, String sessionId) {
						synchronized (messages) {
							StyleBuilder styleBuilder;
							if (sessionId != null) {
								styleBuilder = styleBuilders.get(sessionId);
								if (styleBuilder == null) {
									styleBuilder = new StyleBuilder();
									styleBuilders.put(sessionId, styleBuilder);
								}
							} else {
								styleBuilder = new StyleBuilder();
							}
							messages.add(JobLogEntryEx.parse(message, styleBuilder));
						}
					}
					
				};		
				var oldApplication = ThreadContext.getApplication();
				var oldRequestCycle = ThreadContext.getRequestCycle();
				ThreadContext.setApplication(application);
				ThreadContext.setRequestCycle(requestCycle);
				try {
					return runTask(logger);
				} catch (Exception e) {	
					ExplicitException explicitException = ExceptionUtils.find(e, ExplicitException.class);
					if (explicitException != null) {
						logger.error(explicitException.getMessage());
					} else {
						UnauthorizedException unauthorizedException = ExceptionUtils.find(e, UnauthorizedException.class);
						if (unauthorizedException != null && unauthorizedException.getMessage() != null)
							logger.error(unauthorizedException.getMessage());
						else								
							logger.error(null, e);
					}
					return new TaskResult(false, new PlainMessage(_T("Error executing task")));
				} finally {
					ThreadContext.setApplication(oldApplication);
					ThreadContext.setRequestCycle(oldRequestCycle);
				} 
			}
			
		}), messages));
		
		if (prevFuture != null)
			prevFuture.cancel(true);
		
		new ModalPanel(target) {
			
			private TaskResult result;
			
			@Override
			protected void onClosed() {
				super.onClosed();
				TaskFuture future = taskFutureService.getTaskFutures().remove(taskId);
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (future != null) {
					future.cancel(true);
					onCancelled(target);
				} else {
					onCompleted(target, result != null && result.isSuccessful());
				}
			}

			@Override
			protected Component newContent(String id) {
				return new TaskFeedbackPanel(id, getTitle()) {

					@Override
					protected void onClose(AjaxRequestTarget target) {
						close();
					}

					@Override
					protected List<JobLogEntryEx> getLogEntries() {
						TaskFuture future = taskFutureService.getTaskFutures().get(taskId);
						if (future != null) 
							return future.getLogEntries();
						else
							return new ArrayList<>();
					}

					@Override
					protected TaskResult getResult() {
						TaskFuture future = taskFutureService.getTaskFutures().get(taskId);
						if (future != null && future.isDone() && !future.isCancelled()) { 
							try {
								result = future.get();
								return result;
							} catch (InterruptedException | ExecutionException e) {
								throw new RuntimeException(e);
							}
						} else {
							return null;
						}
					}

				};
			}
			
		};
	}
	
	@Override
	protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
		super.onSubmit(target, form);
		target.focusComponent(null);
		target.add(form);
		submitTask(target);
	}
	
	protected abstract TaskResult runTask(TaskLogger logger) throws InterruptedException;
	
}
