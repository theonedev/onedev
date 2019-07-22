package io.onedev.server.web.component.markdown.emoji;

import com.vladsch.flexmark.ast.Node;
import com.vladsch.flexmark.internal.Delimiter;
import com.vladsch.flexmark.parser.InlineParser;
import com.vladsch.flexmark.parser.delimiter.DelimiterProcessor;
import com.vladsch.flexmark.parser.delimiter.DelimiterRun;
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
    public boolean canBeOpener(boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return leftFlanking;
    }

    @Override
    public boolean canBeCloser(boolean leftFlanking, boolean rightFlanking, boolean beforeIsPunctuation, boolean afterIsPunctuation, boolean beforeIsWhitespace, boolean afterIsWhiteSpace) {
        return rightFlanking;
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
        if (opener.getInput().subSequence(opener.getEndIndex(), closer.getStartIndex()).indexOfAny(BasedSequence.WHITESPACE_CHARS) == -1) {
            EmojiNode emoji = new EmojiNode(opener.getTailChars(delimitersUsed), BasedSequence.NULL, closer.getLeadChars(delimitersUsed));
            opener.moveNodesBetweenDelimitersTo(emoji, closer);
        } else {
            opener.convertDelimitersToText(delimitersUsed, closer);
        }
    }
}
