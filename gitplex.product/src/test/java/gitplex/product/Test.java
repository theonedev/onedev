package gitplex.product;

import java.io.IOException;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.NoHeadException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmease.commons.lang.TokenPosition;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoHeadException, GitAPIException {
		TokenPosition hightlight = new TokenPosition(10, null);
		System.out.println(new ObjectMapper().writeValueAsString(hightlight));
	}
	
}