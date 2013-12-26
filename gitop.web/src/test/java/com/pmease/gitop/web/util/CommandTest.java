package com.pmease.gitop.web.util;

import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;
import org.codehaus.plexus.util.cli.DefaultConsumer;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.junit.Test;

public class CommandTest {

	@Test public void testCommand() throws CommandLineException {
		Commandline cl = new Commandline();
		cl.setExecutable("/usr/local/bin/git");
		cl.addArguments(new String[] { "show", "4a187e7" , "VS2012.2 TFS Server ENU.iso" });
		CommandLineUtils.executeCommandLine(cl, new StreamConsumer() {

			@Override
			public void consumeLine(String line) {
				
			}
			
		}, new DefaultConsumer());
	}
}
