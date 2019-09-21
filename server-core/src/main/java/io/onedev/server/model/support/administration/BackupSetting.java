package io.onedev.server.model.support.administration;

import java.io.Serializable;
import java.text.ParseException;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.CronExpression;

import io.onedev.server.util.validation.Validatable;
import io.onedev.server.util.validation.annotation.ClassValidating;
import io.onedev.server.web.editable.annotation.Editable;

@Editable
@ClassValidating
public class BackupSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1L;
	
	private String schedule;
	
	@Editable(order=100, name="Backup Schedule", description=
		"Optionally specify a cron expression to schedule database auto-backup. The cron expression format is " +
		"<em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>." +
		"For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer " +
		"to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06'>Quartz tutorial</a>." + 
		"The backup files will be placed into <em>db-backup</em> folder under OneDev " +
		"installation directory. Leave this property empty if you do not want to enable database " +
		"auto backup.")
	@NotEmpty
	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	@Override
	public boolean isValid(ConstraintValidatorContext context) {
		boolean hasErrors = false;
		if (schedule != null) {
			try {
				new CronExpression(schedule);
			} catch (ParseException e) {
				context.buildConstraintViolationWithTemplate(e.getMessage())
						.addPropertyNode("schedule").addConstraintViolation();
				hasErrors = true;
			}
		}
		return !hasErrors;
	}

}
