package io.onedev.server.ai;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.lang3.StringUtils.isBlank;

import java.io.IOException;
import java.io.Serializable;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.BadRequestException;

import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.apache.commons.fileupload2.core.FileUploadByteCountLimitException;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.jspecify.annotations.Nullable;

import com.vladsch.flexmark.ast.LinkNodeBase;
import com.vladsch.flexmark.util.sequence.BasedSequence;

import io.onedev.commons.utils.ExceptionUtils;
import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.attachment.AttachmentStorageSupport;
import io.onedev.server.attachment.AttachmentTooLargeException;
import io.onedev.server.attachment.ProjectAttachmentSupport;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.service.UrlService;
import io.onedev.server.util.FilenameUtils;
import io.onedev.server.util.SiteSyncUtils;

/** The ordinary JSON or text post, optionally followed by streaming multipart attachments. */
public class TodPost {

    final Map<String, Serializable> data;
    private final FileItemInputIterator parts;

    TodPost(Map<String, Serializable> data, @Nullable FileItemInputIterator parts) {
        this.data = data;
        this.parts = parts;
    }

    boolean hasUploads() {
        return parts != null;
    }

    @Nullable
    String removeDescription() {
        var description = (String) data.remove("description");
        return isBlank(description) ? null : description;
    }

    String comment(AttachmentStorageSupport owner, @Nullable String previous, int maxLength) {
        var content = commentMarkdown(owner, previous, maxLength);
        if (isBlank(content))
            throw new BadRequestException("Comment must not be empty");
        if (content.length() > maxLength)
            throw new BadRequestException("Comment exceeds " + maxLength + " characters");
        return content;
    }

    @Nullable
    String commentMarkdown(AttachmentStorageSupport owner, @Nullable String previous, int maxLength) {
        var submitted = data.get("content");
        if (submitted != null && !(submitted instanceof String))
            throw new BadRequestException("Comment must be Markdown text");
        return markdown(owner, (String) submitted, previous, maxLength);
    }

    @Nullable
    String markdown(AttachmentStorageSupport owner, @Nullable String submitted, @Nullable String previous, int maxLength) {
        var destinations = new ArrayList<BasedSequence>();
        if (submitted != null && submitted.contains("attachment:")) {
            var document = OneDev.getInstance(MarkdownService.class).parse(submitted);
            for (var node : document.getDescendants()) {
                if (node instanceof LinkNodeBase link && link.getUrl().startsWith("attachment:"))
                    destinations.add(link.getUrl());
            }
        }
        if (!hasUploads() && destinations.isEmpty())
            return submitted != null ? submitted : previous;

        var attachments = saveAttachments(owner);
        return placeAttachments(submitted != null ? submitted : previous, destinations, attachments, maxLength);
    }

    private record Attachment(String name, String url) {}

    private static String placeAttachments(@Nullable String markdown, List<BasedSequence> destinations,
                                   Map<String, Attachment> attachments, int maxLength) {
        var result = new StringBuilder(markdown != null ? markdown : "");
        var placed = new HashSet<String>();
        // Replace from the end so offsets still refer to the original Markdown.
        destinations.sort((left, right) -> Integer.compare(right.getStartOffset(), left.getStartOffset()));
        for (var destination : destinations) {
            var path = destination.toString().substring("attachment:".length());
            var attachment = attachments.get(path);
            if (attachment == null)
                throw new BadRequestException("Missing --attach for Markdown destination: " + path);
            result.replace(destination.getStartOffset(), destination.getEndOffset(), attachment.url());
            placed.add(path);
        }
        attachments.forEach((path, attachment) -> {
            if (!placed.contains(path)) {
                if (!result.isEmpty())
                    result.append("\n\n");
                if (FilenameUtils.isWebSafeImage(attachment.name()))
                    result.append('!');
                var label = attachment.name().replaceAll("([\\\\\\[\\]\\*_`~<&])", "\\\\$1");
                result.append('[').append(label).append("](").append(attachment.url()).append(')');
            }
        });
        if (result.length() > maxLength)
            throw new BadRequestException("Markdown with attachments exceeds " + maxLength + " characters");
        return result.toString();
    }

    private Map<String, Attachment> saveAttachments(AttachmentStorageSupport owner) {
        var attachments = new LinkedHashMap<String, Attachment>();
        if (parts == null)
            return attachments;
        var project = owner.getAttachmentProject();
        var projectId = project.getId();
        var group = owner.getAttachmentGroup();
        var storage = OneDev.getInstance(AttachmentService.class);
        var urlService = OneDev.getInstance(UrlService.class);
        var savedNames = new ArrayList<String>();
        OneDev.getInstance(TransactionService.class).getTransaction().runAfterCompletion(status -> {
            if (status == TransactionStatus.ROLLED_BACK)
                savedNames.forEach(name -> storage.deleteAttachment(projectId, group, name));
        });
        parts.setFileSizeMax(ProjectAttachmentSupport.MAX_FILE_SIZE);
        try {
            while (parts.hasNext()) {
                var part = parts.next();
                if (!part.getFieldName().equals("attachment") || part.isFormField())
                    throw new BadRequestException("Expected an attachment file part");
                String path;
                try {
                    path = URLDecoder.decode(part.getName(), UTF_8);
                } catch (IllegalArgumentException e) {
                    throw new BadRequestException("Invalid encoded attachment path", e);
                }
                var basename = path.substring(Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\')) + 1);
                var name = FilenameUtils.sanitizeFileName(basename);
                if (name.isBlank() || name.equals(".") || name.equals(SiteSyncUtils.FILE_VERSION)
                        || name.chars().anyMatch(Character::isISOControl) || attachments.containsKey(path))
                    throw new BadRequestException("Invalid or duplicate attachment: " + path);
                try (var stream = part.getInputStream()) {
                    name = storage.saveAttachment(projectId, group, name, stream);
                    savedNames.add(name);
                }
                var url = urlService.urlForAttachment(project, group, name, false)
                        .replace("(", "%28").replace(")", "%29").replace("&", "%26");
                attachments.put(path, new Attachment(name, url));
            }
        } catch (IOException | RuntimeException e) {
            var sizeError = ExceptionUtils.find(e, FileUploadByteCountLimitException.class);
            if (sizeError != null)
                throw new AttachmentTooLargeException(sizeError.getMessage());
            if (e instanceof IOException)
                throw new BadRequestException("Incomplete multipart upload", e);
            throw ExceptionUtils.unchecked(e);
        }
        if (attachments.isEmpty())
            throw new BadRequestException("Expected at least one attachment");
        return attachments;
    }
}
