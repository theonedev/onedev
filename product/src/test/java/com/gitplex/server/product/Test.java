package com.gitplex.server.product;

import java.io.IOException;
import java.net.URISyntaxException;

import com.vladsch.flexmark.ast.HtmlInline;
import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.ast.NodeVisitor;
import com.vladsch.flexmark.ast.VisitHandler;
import com.vladsch.flexmark.ast.Visitor;
import com.vladsch.flexmark.parser.Parser;

public class Test {

	@org.junit.Test
	public void test() throws IOException, URISyntaxException {
		Node doc = Parser.builder().build().parse(""
				+ "[link](url)\n\n"
				+ "* item1\n"
				+ "* item2 <script>alert('tt');</script>");
		
		VisitHandler<HtmlInline> visitHandler = new VisitHandler<>(HtmlInline.class, new Visitor<HtmlInline>() {

			@Override
			public void visit(HtmlInline node) {
				System.out.println(node.getChildChars());
			}
			
		});
		new NodeVisitor(visitHandler).visit(doc);
	}

}