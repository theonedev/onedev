package io.onedev.server.buildspec.job.action;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.notification.BuildNotificationManager;
import io.onedev.server.util.script.identity.JobIdentity;
import io.onedev.server.util.script.identity.ScriptIdentity;
import io.onedev.server.web.editable.annotation.Editable;
import io.onedev.server.web.editable.annotation.NotificationReceiver;

@Editable(name="Send notification", order=200)
public class SendNotificationAction extends PostBuildAction {

	private static final long serialVersionUID = 1L;
	
	private String receivers;
	
	@Editable(order=1000)
	@NotificationReceiver
	@NotEmpty
	public String getReceivers() {
		return receivers;
	}

	public void setReceivers(String receivers) {
		this.receivers = receivers;
	}

	@Override
	public void execute(Build build) {
		Build.push(build);
		ScriptIdentity.push(new JobIdentity(build.getProject(), build.getCommitId()));
		try {
			io.onedev.server.buildspec.job.action.notificationreceiver.NotificationReceiver parsedReceiver = 
					io.onedev.server.buildspec.job.action.notificationreceiver.NotificationReceiver.parse(receivers, build);
			OneDev.getInstance(BuildNotificationManager.class).notify(build, parsedReceiver.getEmails());
		} finally {
			ScriptIdentity.pop();
			Build.pop();
		}
	}

	@Override
	public String getDescription() {
		return "Send notification to " + receivers;
	}

}
