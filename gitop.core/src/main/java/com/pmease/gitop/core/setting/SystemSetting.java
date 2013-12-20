package com.pmease.gitop.core.setting;

import java.io.File;
import java.io.Serializable;

import javax.validation.ConstraintValidatorContext;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import com.pmease.commons.editable.annotation.Editable;
import com.pmease.commons.git.GitConfig;
import com.pmease.commons.util.FileUtils;
import com.pmease.commons.validation.Directory;
import com.pmease.commons.validation.Validatable;

@SuppressWarnings("serial")
@Editable
public class SystemSetting implements Serializable, Validatable {
	
	private String dataPath;
	
	private GitConfig gitConfig = new SystemGit();
	
	@Editable(name="Directory to Store Application Data", order=100, description="Specify directory to store application data, "
			+ "such as managed git repositories and various settings.")
	@Directory
	@NotEmpty
	public String getDataPath() {
		return dataPath;
	}

	public void setDataPath(String dataPath) {
		this.dataPath = dataPath;
	}

	@Editable(order=200, description="Gitop relies on git command line to operate managed repositories. The minimum "
			+ "required version is 1.8.0.")
	@Valid
	@NotNull
	public GitConfig getGitConfig() {
		return gitConfig;
	}

	public void setGitConfig(GitConfig gitConfig) {
		this.gitConfig = gitConfig;
	}

	@Override
	public void validate(ConstraintValidatorContext constraintValidatorContext) {
		File dataDir = new File(dataPath);
		File testFile = new File(dataDir, "test");
		try {
			FileUtils.createDir(dataDir);
			FileUtils.writeFile(testFile, "test");
		} catch (Exception e) {
			constraintValidatorContext.buildConstraintViolationWithTemplate("Unable to ");
		} finally {
			if (testFile.exists())
				FileUtils.deleteFile(testFile);
		}
	}

}
