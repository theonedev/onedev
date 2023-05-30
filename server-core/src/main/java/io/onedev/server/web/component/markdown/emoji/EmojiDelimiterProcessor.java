package io.onedev.server.web.component.markdown.emoji;

import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.core.delimiter.Delimiter;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.misc.CharPredicate;
import com.vladsch.flexmark.util.sequence.BasedSequence;

public class EmojiDelimiterProcessor implements DelimiterProcessor {

    @Override
    public char getOpeningCharacter() {
        return ':';
    }

    @Override
    public char getClosingCharacter() {
        return ':';
    }

    @Override
    public int getMinLength() {
        return 1;
    }

    @Override
    public int getDelimiterUse(DelimiterRun opener, DelimiterRun closer) {
        if (opener.length() >= 1 && closer.length() >= 1) {
            return 1;
        } else {
            return 1;
        }
    }

    @Override
    public Node unmatchedDelimiterNode(InlineParser inlineParser, final DelimiterRun delimiter) {
        return null;
    }

    @Override
    public void process(Delimiter opener, Delimiter closer, int delimitersUsed) {
        // Normal case, wrap nodes between delimiters in emoji node.
        // don't allow any spaces between delimiters
        if (opener.getInput().subSequence(opener.getEndIndex(), closer.getStartIndex()).indexOfAny(CharPredicate.WHITESPACE) == -1) {
            EmojiNode emoji = new EmojiNode(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
            opener.moveNodesBetweenDelimitersTo(emoji, closer);
        } else {
            opener.convertDelimitersToText(delimitersUsed, closer);
        }
    }

	@Override
	public boolean canBeOpener(String before, String after, boolean leftFlanking, boolean rightFlanking,
			boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
			boolean afterIsWhiteSpace) {
        return leftFlanking && (beforeIsPunctuation || beforeIsWhitespace);
	}

	@Override
	public boolean canBeCloser(String before, String after, boolean leftFlanking, boolean rightFlanking,
			boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace,
			boolean afterIsWhiteSpace) {
        return rightFlanking && (afterIsPunctuation || afterIsWhiteSpace);
	}

	@Override
	public boolean skipNonOpenerCloser() {
		return false;
	}
	
}
