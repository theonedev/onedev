package gitplex.product;

import java.io.File;
import java.io.IOException;

import com.pmease.commons.util.execution.Commandline;
import com.pmease.commons.util.execution.LineConsumer;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		Commandline cmd = new Commandline("git");
		cmd.addArgs("log", "--", "中文.txt");
		cmd.workingDir(new File("w:\\linux"));
		cmd.execute(new LineConsumer() {

			@Override
			public void consume(String line) {
				System.out.println(line);
			}
			
		}, new LineConsumer() {

			@Override
			public void consume(String line) {
				System.err.println(line);
			}
			
		});
	}
	
}