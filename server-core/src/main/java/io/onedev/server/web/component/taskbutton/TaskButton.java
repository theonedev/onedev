package io.onedev.server.web.component.taskbutton;

import static io.onedev.server.web.translation.Translation._T;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

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
import org.joda.time.DateTime;
import org.quartz.ScheduleBuilder;
import org.quartz.SimpleScheduleBuilder;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.commons.utils.ExplicitException;
import io.onedev.commons.utils.TaskLogger;
import io.onedev.commons.utils.WordUtils;
import io.onedev.server.OneDev;
import io.onedev.server.buildspec.job.log.JobLogEntry;
import io.onedev.server.buildspec.job.log.JobLogEntryEx;
import io.onedev.server.event.Listen;
import io.onedev.server.event.system.SystemStarted;
import io.onedev.server.event.system.SystemStopping;
import io.onedev.server.job.log.StyleBuilder;
import io.onedev.server.taskschedule.SchedulableTask;
import io.onedev.server.taskschedule.TaskScheduler;
import io.onedev.server.web.component.modal.ModalPanel;
import io.onedev.server.web.component.taskbutton.TaskResult.PlainMessage;

public abstract class TaskButton extends AjaxButton {

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

	private Map<String, TaskFuture> getTaskFutures() {
		return TaskFutureManager.taskFutures;
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
		String path = getPath();
		
		ExecutorService executorService = OneDev.getInstance(ExecutorService.class);
		List<JobLogEntryEx> messages = new ArrayList<>();
		messages.add(new JobLogEntryEx(new JobLogEntry(new Date(), _T("Please wait..."))));
		var application = Application.get();
		var requestCycle = RequestCycle.get();
		TaskFuture future = getTaskFutures().put(path, new TaskFuture(executorService.submit(new Callable<TaskResult>() {

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
		
		if (future != null && !future.isDone())
			future.cancel(true);
		
		new ModalPanel(target) {
			
			private TaskResult result;
			
			@Override
			protected void onClosed() {
				super.onClosed();
				TaskFuture future = getTaskFutures().remove(path);
				
				AjaxRequestTarget target = RequestCycle.get().find(AjaxRequestTarget.class);
				if (future != null && !future.isDone()) {
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
						TaskFuture future = getTaskFutures().get(path);
						if (future != null) 
							return future.getLogEntries();
						else
							return new ArrayList<>();
					}

					@Override
					protected TaskResult getResult() {
						TaskFuture future = getTaskFutures().get(path);
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
	
	/**
	 * @param logger
	 * @return html display to user showing task execution result
	 */
	protected abstract TaskResult runTask(TaskLogger logger) throws InterruptedException;

	@Singleton
	public static class TaskFutureManager implements SchedulableTask {

		private static final Map<String, TaskFuture> taskFutures = new ConcurrentHashMap<>();

		@Inject
		private TaskScheduler taskScheduler;
		
		private String taskId;

		@Listen
		public void on(SystemStarted event) {
			taskId = taskScheduler.schedule(this);
		}
		
		@Listen
		public void on(SystemStopping event) {
			if (taskId != null)
				taskScheduler.unschedule(taskId);
		}

		@Override
		public void execute() {
			for (Iterator<Map.Entry<String, TaskFuture>> it = taskFutures.entrySet().iterator(); it.hasNext();) {
				TaskFuture taskFuture = it.next().getValue();
				if (taskFuture.isTimedout()) {
					if (!taskFuture.isDone())
						taskFuture.cancel(true);
					it.remove();
				}
			}
		}

		@Override
		public ScheduleBuilder<?> getScheduleBuilder() {
			return SimpleScheduleBuilder.repeatMinutelyForever();
		}
		
	}
	
	private static class TaskFuture implements Future<TaskResult> {

		private final Future<TaskResult> wrapped;
		
		private final List<JobLogEntryEx> logEntries;
		
		private volatile Date lastActive = new Date();
		
		public TaskFuture(Future<TaskResult> wrapped, List<JobLogEntryEx> logEntries) {
			this.wrapped = wrapped;
			this.logEntries = logEntries;
		}
		
		@Override
		public boolean cancel(boolean mayInterruptIfRunning) {
			return wrapped.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled() {
			return wrapped.isCancelled();
		}

		@Override
		public boolean isDone() {
			return wrapped.isDone();
		}
		
		public boolean isTimedout() {
			return lastActive.before(new DateTime().minusMinutes(1).toDate());
		}

		@Override
		public TaskResult get() throws InterruptedException, ExecutionException {
			return wrapped.get();
		}

		@Override
		public TaskResult get(long timeout, TimeUnit unit)
				throws InterruptedException, ExecutionException, TimeoutException {
			return get(timeout, unit);
		}
		
		public List<JobLogEntryEx> getLogEntries() {
			lastActive = new Date();
			synchronized (logEntries) {
				List<JobLogEntryEx> copy = new ArrayList<>(logEntries);
				logEntries.clear();
				return copy;
			}
		}
		
	}
	
}
