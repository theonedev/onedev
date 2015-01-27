package com.pmease.gitplex.web.component.markdown;

import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.pegdown.Printer;
import org.pegdown.ast.Node;
import org.pegdown.ast.Visitor;
import org.pegdown.plugins.ToHtmlSerializerPlugin;

public class EmojiSerializer implements ToHtmlSerializerPlugin {

	@Override
	public boolean visit(Node node, Visitor visitor, Printer printer) {
		if (node instanceof EmojiNode) {
			EmojiNode emojiNode = (EmojiNode) node;
			String emojiName = emojiNode.getEmojiName();
			String emojiCode = EmojiOnes.getInstance().all().get(emojiName);
			if (emojiCode != null && RequestCycle.get() != null) {
				CharSequence emojiUrl = RequestCycle.get().urlFor(new PackageResourceReference(
						EmojiOnes.class, "emoji/" + emojiCode + ".png"), new PageParameters());
				printer.print(String.format(" <img src='%s' title='%s' alt='%s' class='emoji'></img> ", 
						emojiUrl, emojiName, emojiName, emojiName));
			} else {
				printer.print(String.format(" :%s: ", emojiNode.getEmojiName()));
			}
			return true;
		}
		return false;
	}

}