package io.onedev.server.web.component.diff.text;

import static io.onedev.server.util.diff.DiffRenderer.toHtml;
import static io.onedev.server.web.translation.Translation._T;
import static org.apache.wicket.ajax.attributes.CallbackParameter.explicit;
import static org.unbescape.javascript.JavaScriptEscape.escapeJavaScript;

import java.text.MessageFormat;
import java.util.List;

import org.apache.commons.lang3.Strings;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;

import io.onedev.server.util.diff.DiffBlock;
import io.onedev.server.util.diff.DiffMatchPatch.Operation;
import io.onedev.server.util.diff.DiffUtils;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.web.asset.icon.IconScope;
import io.onedev.server.web.behavior.AbstractPostAjaxBehavior;
import io.onedev.server.web.component.diff.DiffExpandSupport;
import io.onedev.server.web.component.svg.SpriteImage;

public class PlainTextDiffPanel extends Panel {
    
    private final boolean showLineNumbers;

    private final DiffExpandSupport expandSupport = new DiffExpandSupport();
    
    private final List<DiffBlock<String>> diffBlocks;
    
    private final String fileName;

    private AbstractPostAjaxBehavior callbackBehavior;
    
    public PlainTextDiffPanel(String id, List<String> oldLines, List<String> newLines) {
        this(id, oldLines, newLines, false, null);
    }

    public PlainTextDiffPanel(String id, List<String> oldLines, List<String> newLines, boolean showLineNumbers) { 
        this(id, oldLines, newLines, showLineNumbers, null);
    }

    public PlainTextDiffPanel(String id, List<String> oldLines, List<String> newLines, boolean showLineNumbers, String fileName) { 
        super(id);
        this.showLineNumbers = showLineNumbers;
        this.fileName = fileName;
        diffBlocks = DiffUtils.diff(oldLines, newLines, WhitespaceOption.IGNORE_TRAILING);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        
        add(new Label("content", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return renderDiffs();
            }
        }).setEscapeModelStrings(false));
        
        add(callbackBehavior = new AbstractPostAjaxBehavior() {

            @Override
            protected void respond(AjaxRequestTarget target) {
                IRequestParameters params = RequestCycle.get().getRequest().getPostParameters();
                if ("expand".equals(params.getParameterValue("action").toString(""))) {                    
                    int index = params.getParameterValue("param1").toInt();
                    DiffBlock<String> block = diffBlocks.get(index);
                    var lastContextSizes = expandSupport.getContextSizes(index, block.getElements().size(), diffBlocks.size());
                    var direction = DiffExpandSupport.Direction.fromString(
                            params.getParameterValue("param2").toString("both"));
                    var contextSizes = expandSupport.expand(index, block.getElements().size(), diffBlocks.size(), direction);
                    
                    StringBuilder builder = new StringBuilder();
                    expandSupport.appendEquals(builder, index, lastContextSizes, contextSizes,
                            block, diffBlocks.size(), new ExpandCallbackImpl());
                    
                    String expanded = Strings.CS.replace(builder.toString(), "\n", "");
                    String script = String.format("onedev.server.plainTextDiff.expand('%s', %d, \"%s\");",
                            getMarkupId(), index, escapeJavaScript(expanded));
                    target.appendJavaScript(script);
                }
            }
            
        });
        
        setOutputMarkupId(true);
    }

    private String renderDiffs() {        
        boolean hasChanges = diffBlocks.stream().anyMatch(block -> block.getOperation() != Operation.EQUAL);
        if (!hasChanges) {
            return "<div class='text-muted text-center p-4'>" + _T("Nothing changed yet") + "</div>";
        }
        
        StringBuilder builder = new StringBuilder();
        builder.append("<table class='text-diff");
        if (fileName != null)
            builder.append(" cm-s-eclipse");
        builder.append("'><colgroup>");
        if (showLineNumbers) {
            int baseLineNumColumnWidth = 66;
            int operationColumnWidth = 15;
            builder.append(String.format("<col width='%d'></col><col width='%d'></col><col width='%d'></col><col></col></colgroup>",
                    baseLineNumColumnWidth, baseLineNumColumnWidth, operationColumnWidth));
        } else {
            builder.append("<col width='15'></col><col></col></colgroup>");
        }
        
        ExpandCallbackImpl callback = new ExpandCallbackImpl();
        for (int i = 0; i < diffBlocks.size(); i++) {
            DiffBlock<String> block = diffBlocks.get(i);
            if (block.getOperation() == Operation.EQUAL) {
                var contextSizes = expandSupport.getContextSizes(i, block.getElements().size(), diffBlocks.size());
                expandSupport.appendEquals(builder, i, new DiffExpandSupport.ContextSizes(0, 0), contextSizes,
                        block, diffBlocks.size(), callback);
            } else if (block.getOperation() == Operation.DELETE) {
                for (int j = 0; j < block.getElements().size(); j++)
                    appendDelete(builder, block, j);
            } else {
                for (int j = 0; j < block.getElements().size(); j++)
                    appendInsert(builder, block, j);
            }
        }
        
        builder.append("</table>");
        return builder.toString();
    }
    
    private void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex, int lastContextSize) {
        if (lastContextSize != 0)
            builder.append("<tr class='code expanded'>");
        else
            builder.append("<tr class='code original'>");
        
        int oldLineNo = block.getOldStart() + lineIndex;
        int newLineNo = block.getNewStart() + lineIndex;
        
        if (showLineNumbers) {
            builder.append("<td class='number noselect'>").append(oldLineNo + 1).append("</td>");
            builder.append("<td class='number noselect'>").append(newLineNo + 1).append("</td>");
        }
        
        builder.append("<td class='operation'>&nbsp;</td>");
        builder.append("<td class='content equal'>");
        appendLine(builder, block.getElements().get(lineIndex));
        builder.append("</td>");
        builder.append("</tr>");
    }
    
    private void appendDelete(StringBuilder builder, DiffBlock<String> block, int lineIndex) {
        builder.append("<tr class='code original'>");
        
        int oldLineNo = block.getOldStart() + lineIndex;
        
        if (showLineNumbers) {
            builder.append("<td class='number noselect old'>").append(oldLineNo + 1).append("</td>");
            builder.append("<td class='number noselect old'></td>");
        }
        
        builder.append("<td class='operation old'>-</td>");
        builder.append("<td class='content old'>");
        appendLine(builder, block.getElements().get(lineIndex));
        builder.append("</td></tr>");
    }
    
    private void appendInsert(StringBuilder builder, DiffBlock<String> block, int lineIndex) {
        builder.append("<tr class='code original'>");
        
        int newLineNo = block.getNewStart() + lineIndex;
        
        if (showLineNumbers) {
            builder.append("<td class='number noselect new'></td>");
            builder.append("<td class='number noselect new'>").append(newLineNo + 1).append("</td>");
        }
        
        builder.append("<td class='operation new'>+</td>");
        builder.append("<td class='content new'>");
        appendLine(builder, block.getElements().get(lineIndex));
        builder.append("</td></tr>");
    }
    
    private void appendExpander(StringBuilder builder, int blockIndex, int skippedLines,
            boolean canExpandUp, boolean canExpandDown) {
        builder.append("<tr class='expander expander").append(blockIndex).append("'>");
        
        String expandSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>",
                SpriteImage.getVersionedHref(IconScope.class, "expand2"));
        String upSvg = String.format("<svg class='icon rotate-270'><use xlink:href='%s'/></svg>",
                SpriteImage.getVersionedHref(IconScope.class, "arrow"));
        String downSvg = String.format("<svg class='icon rotate-90'><use xlink:href='%s'/></svg>",
                SpriteImage.getVersionedHref(IconScope.class, "arrow"));
        String ellipsisSvg = String.format("<svg class='icon'><use xlink:href='%s'/></svg>",
                SpriteImage.getVersionedHref(IconScope.class, "ellipsis"));
        
        var skippedMessage = MessageFormat.format(_T("skipped {0} lines"), String.valueOf(skippedLines));
        if (canExpandUp && canExpandDown) {
            String upScript = String.format("javascript:$('#%s').data('callback')('expand', %d, 'up');", getMarkupId(), blockIndex);
            String bothScript = String.format("javascript:$('#%s').data('callback')('expand', %d, 'both');", getMarkupId(), blockIndex);
            String downScript = String.format("javascript:$('#%s').data('callback')('expand', %d, 'down');", getMarkupId(), blockIndex);
            builder.append("<td colspan='").append(showLineNumbers ? 4 : 2).append("' class='expander directional noselect'><div class='expander-controls'>")
                    .append("<a class='expand-up' aria-label='").append(_T("Show more lines above"))
                    .append("' data-tippy-content='").append(_T("Show more lines above")).append("' href=\"")
                    .append(upScript).append("\">").append(upSvg).append("</a>")
                    .append("<a class='expand-both' aria-label='").append(_T("Show more lines"))
                    .append("' data-tippy-content='").append(_T("Show more lines")).append("' href=\"")
                    .append(bothScript).append("\">").append(expandSvg).append(" <span>").append(skippedMessage).append("</span></a>")
                    .append("<a class='expand-down' aria-label='").append(_T("Show more lines below"))
                    .append("' data-tippy-content='").append(_T("Show more lines below")).append("' href=\"")
                    .append(downScript).append("\">").append(downSvg).append("</a></div></td></tr>");
            return;
        }

        String direction = canExpandUp ? "up" : "down";
        String script = String.format("javascript:$('#%s').data('callback')('expand', %d, '%s');",
                getMarkupId(), blockIndex, direction);
        
        if (showLineNumbers) {
            builder.append("<td colspan='2' class='expander noselect'><a data-tippy-content='").append(_T("Show more lines")).append("' href=\"")
                    .append(script).append("\">").append(expandSvg).append("</a></td>");
        } else {
            builder.append("<td class='expander noselect'><a data-tippy-content='").append(_T("Show more lines")).append("' href=\"")
                    .append(script).append("\">").append(expandSvg).append("</a></td>");
        }
        
        builder.append("<td colspan='2' class='skipped noselect'>").append(ellipsisSvg).append(" ")
                .append(skippedMessage).append(" ").append(ellipsisSvg).append("</td>");
        builder.append("</tr>");
    }
    
    private void appendLine(StringBuilder builder, String line) {
        if (line.length() == 0)
            builder.append("&nbsp;");
        else
            builder.append(toHtml(line, null));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);
        response.render(JavaScriptHeaderItem.forReference(new PlainTextDiffResourceReference()));
        
        CharSequence callback = callbackBehavior.getCallbackFunction(
                explicit("action"), explicit("param1"), explicit("param2"));
        String fileNameParam = fileName != null ? "'" + escapeJavaScript(fileName) + "'" : "null";
        String script = String.format("onedev.server.plainTextDiff.onDomReady('%s', %s, %s);", 
                getMarkupId(), callback, fileNameParam);
        response.render(OnDomReadyHeaderItem.forScript(script));
    }
    
    private class ExpandCallbackImpl implements DiffExpandSupport.ExpandCallback {
        @Override
        public void appendEqual(StringBuilder builder, DiffBlock<String> block, int lineIndex, int lastContextSize) {
            PlainTextDiffPanel.this.appendEqual(builder, block, lineIndex, lastContextSize);
        }
        
        @Override
        public void appendExpander(StringBuilder builder, int blockIndex, int skippedLines,
                boolean canExpandUp, boolean canExpandDown) {
            PlainTextDiffPanel.this.appendExpander(builder, blockIndex, skippedLines, canExpandUp, canExpandDown);
        }
    }
    
}
