package io.onedev.server.buildspec.job.action;

import org.hibernate.validator.constraints.NotEmpty;

import io.onedev.server.OneDev;
import io.onedev.server.model.Build;
import io.onedev.server.notification.BuildNotificationManager;
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
		io.onedev.server.buildspec.job.action.notificationreceiver.NotificationReceiver parsedReceiver = 
				io.onedev.server.buildspec.job.action.notificationreceiver.NotificationReceiver.parse(getReceivers(), build);
		OneDev.getInstance(BuildNotificationManager.class).notify(build, parsedReceiver.getEmails());
	}

	@Override
	public String getDescription() {
		return "Send notification to " + receivers;
	}

}
