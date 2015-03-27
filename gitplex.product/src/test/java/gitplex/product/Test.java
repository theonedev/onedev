package gitplex.product;

import java.io.IOException;

import org.apache.lucene.util.automaton.CharacterRunAutomaton;
import org.apache.lucene.util.automaton.RegExp;

public class Test {

	@org.junit.Test
	public void test() throws IOException {
		System.out.println(new CharacterRunAutomaton(new RegExp("\\w").toAutomaton()).run("a"));
	}	

}