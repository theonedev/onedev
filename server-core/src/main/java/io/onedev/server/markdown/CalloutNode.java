package io.onedev.server.markdown;

import java.util.List;

import com.vladsch.flexmark.util.ast.Block;
import com.vladsch.flexmark.util.ast.BlockContent;
import com.vladsch.flexmark.util.sequence.BasedSequence;

/**
 * AST node representing a callout block.
 * 
 * A callout block is a blockquote with a special syntax like:
 * > [!NOTE]
 * > Content of the callout
 */
public class CalloutNode extends Block {
    
    private BasedSequence calloutType = BasedSequence.NULL;
    private BasedSequence calloutTitle = BasedSequence.NULL;
    
    public CalloutNode() {
    }

    public CalloutNode(BasedSequence chars) {
        super(chars);
    }

    public CalloutNode(BasedSequence chars, List<BasedSequence> segments) {
        super(chars, segments);
    }

    public CalloutNode(BlockContent blockContent) {
        super(blockContent);
    }

    public BasedSequence[] getSegments() {
        return new BasedSequence[] { calloutType, calloutTitle };
    }

    public void getAstExtra(StringBuilder out) {
        if (!calloutType.isNull()) {
            out.append(" type:").append(calloutType);
        }
        if (!calloutTitle.isNull()) {
            out.append(" title:").append(calloutTitle);
        }
    }

    public BasedSequence getCalloutType() {
        return calloutType;
    }

    public void setCalloutType(BasedSequence calloutType) {
        this.calloutType = calloutType;
    }

    public BasedSequence getCalloutTitle() {
        return calloutTitle;
    }

    public void setCalloutTitle(BasedSequence calloutTitle) {
        this.calloutTitle = calloutTitle;
    }
    
    public String getCalloutTypeString() {
        return calloutType.toString().toLowerCase();
    }
    
    public String getCalloutTitleString() {
        return calloutTitle.isNull() ? getDefaultTitle() : calloutTitle.toString();
    }
    
    private String getDefaultTitle() {
        String type = getCalloutTypeString();
        switch (type) {
            case "note":
                return "Note";
            case "tip":
                return "Tip";
            case "important":
                return "Important";
            case "warning":
                return "Warning";
            case "caution":
                return "Caution";
            default:
                return type.substring(0, 1).toUpperCase() + type.substring(1);
        }
    }
        
}
