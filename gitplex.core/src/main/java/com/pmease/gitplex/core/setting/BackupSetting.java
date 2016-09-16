package com.pmease.gitplex.core.setting;

import java.io.File;
import java.io.Serializable;
import java.text.ParseException;
import java.util.UUID;

import javax.validation.ConstraintValidatorContext;

import org.hibernate.validator.constraints.NotEmpty;
import org.quartz.CronExpression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pmease.commons.bootstrap.Bootstrap;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.validation.ClassValidating;
import com.pmease.commons.validation.Validatable;
import com.pmease.commons.wicket.editable.annotation.Editable;

@Editable
@ClassValidating
public class BackupSetting implements Serializable, Validatable {
	
	private static final long serialVersionUID = 1L;
	
	private final static Logger logger = LoggerFactory.getLogger(BackupSetting.class);
	
	public static final int BATCH_SIZE = 1000;

	private String schedule;
	
	private String folder;
	
	@Editable(order=100, name="Backup Schedule", description=
		"Optionally specify a cron expression to schedule database auto-backup. The cron expression format is " +
		"<em>&lt;seconds&gt; &lt;minutes&gt; &lt;hours&gt; &lt;day-of-month&gt; &lt;month&gt; &lt;day-of-week&gt;</em>." +
		"For example, <em>0 0 1 * * ?</em> means 1:00am every day. For details of the format, refer " +
		"to <a href='http://www.quartz-scheduler.org/documentation/quartz-2.x/tutorials/tutorial-lesson-06'>Quartz tutorial</a>." + 
		"The backup files will be placed into <em>backup</em> folder under GitPlex " +
		"installation directory. Leave this property empty if you do not want to enable database " +
		"auto backup.")
	@NotEmpty
	public String getSchedule() {
		return schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	@Editable(order=200, name="Backup Directory", description=
		"Specify the directory to which the auto backup files will be stored. Non-absolute path " +
		"will be considered to be relative to installation directory of GitPlex.")
	@NotEmpty
	public String getFolder() {
		return folder;
	}

	public void setFolder(String folder) {
		this.folder = folder;
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
		if (folder != null) {
			File folder = new File(getFolder());
			if (!folder.isAbsolute())
				folder = new File(Bootstrap.installDir, getFolder());
			if (!folder.exists()) {
				context.buildConstraintViolationWithTemplate("Backup directory does not exist")
						.addPropertyNode("folder").addConstraintViolation();
				hasErrors = true;
			} else {
				// test file permission
				File tempFile = new File(folder, UUID.randomUUID().toString());
				try {
					tempFile.createNewFile();
					FileUtils.deleteFile(tempFile);
				} catch (Exception e) {
					logger.error("Error testing backup directory.", e);
					context.buildConstraintViolationWithTemplate("Unable to create test file under backup directory. "
							+ "Please make sure the user running GitPlex process has write permission to the backup directory.")
							.addPropertyNode("folder").addConstraintViolation();
					hasErrors = true;
				}
			}
		}
		if (hasErrors)
			context.disableDefaultConstraintViolation();
		return !hasErrors;
	}

}
