package io.onedev.server.buildspec.step;

import io.onedev.server.annotation.Editable;
import io.onedev.server.annotation.Interpolative;
import io.onedev.server.buildspec.step.commandinterpreter.Interpreter;
import io.onedev.server.buildspec.step.commandinterpreter.ShellInterpreter;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

import static io.onedev.server.buildspec.step.StepGroup.UTILITIES;

@Editable(order=1110, group = UTILITIES, name="Generate File Checksum", description = "" +
		"This step can only be executed by a docker aware executor")
public class GenerateChecksumStep extends CommandStep {

	private static final long serialVersionUID = 1L;
	
	private String files;

	private String targetFile;
	
	@Editable(order=100, description = "Specify files to create md5 checksum from. Multiple files " +
			"should be separated by space. <a href='https://www.linuxjournal.com/content/globstar-new-bash-globbing-option' target='_blank'>Globstar</a> patterns accepted. " +
			"Non-absolute file is considered to be relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a>")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getFiles() {
		return files;
	}

	public void setFiles(String files) {
		this.files = files;
	}

	@Editable(order=200, description = "Specify a file relative to <a href='https://docs.onedev.io/concepts#job-workspace' target='_blank'>job workspace</a> to write checksum into")
	@Interpolative(variableSuggester="suggestVariables")
	@NotEmpty
	public String getTargetFile() {
		return targetFile;
	}

	public void setTargetFile(String targetFile) {
		this.targetFile = targetFile;
	}

	@Override
	public boolean isRunInContainer() {
		return true;
	}

	@Override
	public String getImage() {
		return "ubuntu:20.04";
	}

	@Override
	public String getRunAs() {
		return null;
	}

	@Override
	public boolean isUseTTY() {
		return false;
	}

	@Override
	public List<RegistryLogin> getRegistryLogins() {
		return new ArrayList<>();
	}

	@Override
	public Interpreter getInterpreter() {
		return new ShellInterpreter() {
			
			@Override
			public String getShell() {
				return "bash";
			}

			@Override
			public String getCommands() {
				var commandsBuilder = new StringBuilder();
				commandsBuilder.append("set -e\n");
				commandsBuilder.append("shopt -s globstar\n");
				commandsBuilder.append("cat `ls -1 ").append(files).append(" 2>/dev/null` | md5sum | awk '{ print $1 }' > ").append(targetFile).append("\n");
				commandsBuilder.append("echo Generated checksum: `cat ").append(targetFile).append("`").append("\n");
				return commandsBuilder.toString();
			}
		};
	}
	
}
