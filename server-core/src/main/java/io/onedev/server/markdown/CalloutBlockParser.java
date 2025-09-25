package io.onedev.server.markdown;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.parser.block.AbstractBlockParser;
import com.vladsch.flexmark.parser.block.BlockContinue;
import com.vladsch.flexmark.parser.block.BlockParser;
import com.vladsch.flexmark.parser.block.BlockParserFactory;
import com.vladsch.flexmark.parser.block.BlockStart;
import com.vladsch.flexmark.parser.block.CustomBlockParserFactory;
import com.vladsch.flexmark.parser.block.MatchedBlockParser;
import com.vladsch.flexmark.parser.block.ParserState;
import com.vladsch.flexmark.parser.core.BlockQuoteParser;
import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.data.DataHolder;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * Parser for GFM callout blocks.
 * 
 * Recognizes patterns like:
 * > [!NOTE] Optional title
 * > Content
 */
public class CalloutBlockParser extends AbstractBlockParser {
    
    private static final Pattern CALLOUT_PATTERN = Pattern.compile(
        "^\\s*>\\s*\\[!([A-Za-z]+)\\](?:\\s+(.*))?\\s*$"
    );
    
    private final CalloutNode block = new CalloutNode();
    private BlockContent content = new BlockContent();
    
    public CalloutBlockParser(DataHolder options, BasedSequence calloutType, BasedSequence calloutTitle) {
        block.setCalloutType(calloutType);
        block.setCalloutTitle(calloutTitle);
    }

    @Override
    public CalloutNode getBlock() {
        return block;
    }

    @Override
    public BlockContinue tryContinue(ParserState state) {
        BasedSequence line = state.getLine();
        
        // Check if line starts with > (blockquote continuation)
        if (line.length() > 0 && line.charAt(0) == '>') {
            int nextNonSpace = state.getNextNonSpaceIndex();
            if (nextNonSpace < line.length() && line.charAt(nextNonSpace) == '>') {
                int contentStart = nextNonSpace + 1;
                if (contentStart < line.length() && line.charAt(contentStart) == ' ') {
                    contentStart++;
                }
                return BlockContinue.atIndex(contentStart);
            }
        }
        
        return BlockContinue.none();
    }

    @Override
    public void addLine(ParserState state, BasedSequence line) {
        content.add(line, state.getIndent());
    }

    @Override
    public void closeBlock(ParserState state) {
        block.setContent(content.getLines());
        content = null;
    }

    @Override
    public boolean isContainer() {
        return true;
    }

    @Override
    public boolean canContain(ParserState state, BlockParser blockParser, Block block) {
        return true;
    }

    public static class Factory implements CustomBlockParserFactory {
        
        @Override
        public Set<Class<?>> getAfterDependents() {
            return null;
        }

        @Override
        public Set<Class<?>> getBeforeDependents() {
            // Parse before blockquote parser
            return Set.of(BlockQuoteParser.class);
        }

        @Override
        public boolean affectsGlobalScope() {
            return false;
        }

        @Override
        public BlockParserFactory apply(DataHolder options) {
            return new BlockParserFactory() {
                @Override
                public BlockStart tryStart(ParserState state, MatchedBlockParser matchedBlockParser) {
                    BasedSequence line = state.getLine();
                    
                    Matcher matcher = CALLOUT_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String calloutTitle = matcher.group(2);
                        
                        BasedSequence calloutTypeSeq = line.subSequence(matcher.start(1), matcher.end(1));
                        BasedSequence calloutTitleSeq = calloutTitle != null ? 
                            line.subSequence(matcher.start(2), matcher.end(2)) : 
                            BasedSequence.NULL;
                        
                        return BlockStart.of(new CalloutBlockParser(options, calloutTypeSeq, calloutTitleSeq))
                            .atIndex(line.length());
                    }
                    
                    return BlockStart.none();
                }
            };
        }
    }
}
