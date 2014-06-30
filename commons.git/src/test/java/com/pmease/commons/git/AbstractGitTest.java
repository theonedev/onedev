package com.pmease.commons.git;

import org.junit.Assert;
import org.mockito.Mockito;

import com.pmease.commons.git.command.GitCommand;
import com.pmease.commons.loader.AppLoader;
import com.pmease.commons.loader.AppLoaderMocker;

public abstract class AbstractGitTest extends AppLoaderMocker {

	/**
	 * Subclass should call super.setup() as first statement if it wants to override this method.
	 */
	@SuppressWarnings("serial")
	@Override
	protected void setup() {
		Mockito.when(AppLoader.getInstance(GitConfig.class)).thenReturn(new GitConfig() {

			@Override
			public String getExecutable() {
				return "git";
			}
			
		});
		
	    Assert.assertTrue(GitCommand.checkError("git") == null);
	    
	    
	}

	/**
	 * Subclass should call super.teardown() as last statement if it wants to override this method.
	 */
	@Override
	protected void teardown() {
		
	}

}
