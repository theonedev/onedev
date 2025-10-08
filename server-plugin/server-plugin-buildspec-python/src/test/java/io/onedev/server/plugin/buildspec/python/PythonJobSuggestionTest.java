package io.onedev.server.plugin.buildspec.python;

import com.google.common.io.Resources;
import io.onedev.server.buildspec.job.Job;
import io.onedev.server.buildspec.step.CommandStep;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.model.Project;
import io.onedev.server.util.IOUtils;
import org.eclipse.jgit.lib.ObjectId;
import org.junit.Test;

import org.jspecify.annotations.Nullable;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class PythonJobSuggestionTest {

	@Test
	public void testPoetryLock() {
		Project project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("poetry.lock"))
					return new Blob(blobIdent, ObjectId.zeroId(), "pytest".getBytes());
				else if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject1.toml"));
				else
					return null;
			}
		};
		var jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		var buildAndTestCommands = getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands();
		assertTrue(buildAndTestCommands.contains("poetry install --with dev") && 
				buildAndTestCommands.contains("poetry add pytest-cov") && 
				buildAndTestCommands.contains("poetry run pytest --cov"));

		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("poetry.lock"))
					return new Blob(blobIdent, ObjectId.zeroId(), new byte[0]);
				else if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject2.toml"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		buildAndTestCommands = getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands();
		assertTrue(!buildAndTestCommands.contains("poetry install --with") && 
				buildAndTestCommands.contains("poetry add coverage") &&
				buildAndTestCommands.contains("poetry run coverage"));
	}
	
	@Test
	public void testPyproject() {
		Project project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject1.toml"));
				else
					return null;
			}
		};
		var jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		var commands = getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands();
		assertTrue(!commands.contains("pip install -e .[dev]") && 
				commands.contains("pytest --cov") && 
				commands.contains("pytest-cov"));

		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject2.toml"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		commands = getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands();
		assertTrue(!commands.contains("pip install -e .[") && 
				commands.contains("pip install coverage") && 
				commands.contains("coverage run -m unittest"));
		
		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject3.toml"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		assertTrue(getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands().contains("pip install -e .[test]"));
		
		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("pyproject.toml"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("pyproject1.toml"));
				else if (blobIdent.path.equals("setup.cfg"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("setup.cfg"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		assertTrue(getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands().contains("pip install -e .[dev]"));
	}
	
	@Test
	public void testSetupPy() {
		Project project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("setup.py")) 
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("setup1.py"));
				else 
					return null;
			}
		};
		var jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		assertTrue(getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands().contains("pip install -e .[dev]"));

		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("setup.py"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("setup2.py"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		assertTrue(getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands().contains("pip install -e .[test]"));

		project = new Project() {
			@Override
			public Blob getBlob(BlobIdent blobIdent, boolean mustExist) {
				if (blobIdent.path.equals("setup.py"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("setup3.py"));
				else if (blobIdent.path.equals("setup.cfg"))
					return new Blob(blobIdent, ObjectId.zeroId(), readTestResource("setup.cfg"));
				else
					return null;
			}
		};
		jobs = new PythonJobSuggestion().suggestJobs(project, ObjectId.zeroId());
		assertTrue(getBuildAndTestStep(jobs.iterator().next()).getInterpreter().getCommands().contains("pip install -e .[dev]"));
	}
	
	private byte[] readTestResource(String path) {
		try (var is = Resources.getResource(PythonJobSuggestionTest.class, path).openStream()) {
			return IOUtils.toByteArray(is);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Nullable
	private CommandStep getBuildAndTestStep(Job job) {
		for (var step: job.getSteps()) {
			if (step.getName().equals("build and test"))
				return (CommandStep) step;
		}
		return null;
	}

}