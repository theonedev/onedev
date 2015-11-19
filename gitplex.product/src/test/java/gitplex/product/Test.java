package gitplex.product;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;

import com.pmease.commons.lang.extractors.java.JavaLexer;

public class Test {

	@org.junit.Test
	public void test() throws IOException, NoSuchMethodException, SecurityException, InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Class<? extends Lexer> lexerClass = JavaLexer.class;
		Constructor<? extends Lexer> ct = lexerClass.getConstructor(CharStream.class);
		Lexer lexer = ct.newInstance(new ANTLRInputStream("import com.pmease"));
		lexer.removeErrorListeners();
	}
	
}