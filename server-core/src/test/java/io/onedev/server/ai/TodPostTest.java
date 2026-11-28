package io.onedev.server.ai;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.transaction.Status;
import jakarta.transaction.Synchronization;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.subject.Subject;
import org.hibernate.Transaction;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Guice;

import io.onedev.server.OneDev;
import io.onedev.server.attachment.AttachmentService;
import io.onedev.server.attachment.AttachmentTooLargeException;
import io.onedev.server.markdown.DefaultMarkdownService;
import io.onedev.server.markdown.MarkdownService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Issue;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.persistence.TransactionService;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.UrlService;

class TodPostTest {

    private final Issue issue = new Issue();
    private final ObjectMapper mapper = new ObjectMapper();
    private final TodPostReader reader = Guice.createInjector(binder ->
            binder.bind(ObjectMapper.class).toInstance(mapper)).getInstance(TodPostReader.class);
    private final AttachmentService storage = mock(AttachmentService.class);
    private final Transaction transaction = mock(Transaction.class, CALLS_REAL_METHODS);
    private final List<String> saved = new ArrayList<>();
    private MockedStatic<OneDev> oneDev;
    private MockedStatic<SecurityUtils> security;

    @BeforeEach
    void setUp() {
        security = mockStatic(SecurityUtils.class);
        security.when(SecurityUtils::getUser).thenReturn(new User());
        var project = new Project();
        project.setId(1L);
        issue.setProject(project);
        oneDev = mockStatic(OneDev.class);
        var transactions = mock(TransactionService.class);
        when(transactions.getTransaction()).thenReturn(transaction);
        oneDev.when(() -> OneDev.getInstance(TransactionService.class)).thenReturn(transactions);
        oneDev.when(() -> OneDev.getInstance(AttachmentService.class)).thenReturn(storage);
        var markdown = new DefaultMarkdownService(null, Set.of(), Set.of());
        oneDev.when(() -> OneDev.getInstance(MarkdownService.class)).thenReturn(markdown);
        var urls = mock(UrlService.class);
        oneDev.when(() -> OneDev.getInstance(UrlService.class)).thenReturn(urls);
        when(urls.urlForAttachment(eq(project), eq(issue.getUUID()), anyString(), eq(false)))
                .thenAnswer(call -> "/files/" + call.getArgument(2, String.class).replace(" ", "%20"));
        when(storage.saveAttachment(eq(1L), eq(issue.getUUID()), anyString(), any())).thenAnswer(call -> {
            var name = call.getArgument(2, String.class);
            var bytes = call.getArgument(3, InputStream.class).readAllBytes();
            assertArrayEquals("test\u0000bytes".getBytes(UTF_8), bytes);
            if (name.equals("oversized"))
                throw new AttachmentTooLargeException("Upload too large");
            var stored = saved.contains(name) ? "second-" + name : name;
            saved.add(stored);
            return stored;
        });
    }

    @AfterEach
    void tearDown() {
        oneDev.close();
        security.close();
    }

    private TodPost upload(Map<String, String> data, String... paths) throws Exception {
        var body = new StringBuilder("--test\r\nContent-Disposition: form-data; name=\"data\"\r\n\r\n")
                .append(mapper.writeValueAsString(data));
        for (var path : paths) {
            body.append("\r\n--test\r\nContent-Disposition: form-data; name=\"attachment\"; filename=\"")
                    .append(URLEncoder.encode(path, UTF_8)).append("\"\r\n\r\ntest\u0000bytes");
        }
        body.append("\r\n--test--\r\n");
        return reader.readFrom(TodPost.class, TodPost.class, null,
                MediaType.valueOf("multipart/form-data; boundary=test"), new MultivaluedHashMap<>(),
                new ByteArrayInputStream(body.toString().getBytes(UTF_8)));
    }

    @Test
    void rejectsAnonymousRequestsBeforeReadingTheBody() {
        security.when(SecurityUtils::getUser).thenReturn(null);
        var body = mock(InputStream.class);
        for (var type : List.of(MediaType.TEXT_PLAIN_TYPE, MediaType.APPLICATION_JSON_TYPE,
                MediaType.MULTIPART_FORM_DATA_TYPE)) {
            assertThrows(UnauthenticatedException.class, () -> reader.readFrom(TodPost.class, TodPost.class,
                    null, type, new MultivaluedHashMap<>(), body));
        }
        verifyNoInteractions(body);
    }

    @Test
    void boundsMultipartMetadataWithoutContentLength() throws Exception {
        var emptyMetadata = mapper.writeValueAsBytes(Map.of("content", ""));
        var content = "a".repeat(TodPostReader.MAX_METADATA_BYTES - emptyMetadata.length);
        assertEquals(content, upload(Map.of("content", content)).data.get("content"));
        var error = assertThrows(ClientErrorException.class, () -> upload(Map.of("content", content + "a")));
        assertEquals(413, error.getResponse().getStatus());
    }

    @Test
    void placesActualDestinationsAndAppendsUnusedFilesInOrder() throws Exception {
        var content = "See ![before](<attachment:before/my image.png> \"old\") and [again][image].\r\n"
                + "\r\n[image]: <attachment:before/my image.png>\r\n"
                + "\r\n`[example](attachment:literal)`\r\n\r\n```md\r\n[a](attachment:literal)\r\n```\r\n"
                + "\r\n![after](<attachment:after/my image.png>)";
        var post = upload(Map.of("content", content), "before/my image.png", "after/my image.png",
                "report (1).log", "detail.PNG");
        assertEquals(content.replace("attachment:before/my image.png", "/files/my%20image.png")
                        .replace("attachment:after/my image.png", "/files/second-my%20image.png")
                        + "\n\n[report (1).log](/files/report%20%281%29.log)\n\n![detail.PNG](/files/detail.PNG)",
                post.comment(issue, null, Issue.MAX_DESCRIPTION_LEN));
        assertEquals(List.of("my image.png", "second-my image.png", "report (1).log", "detail.PNG"), saved);
    }

    @Test
    void preservesIndentationInSubmittedDescription() throws Exception {
        var description = "    [example](attachment:example)\n\n[actual](attachment:note.txt)\n";
        var post = upload(Map.of("description", description), "note.txt");
        assertEquals(description.replace("attachment:note.txt", "/files/note.txt"),
                post.markdown(issue, post.removeDescription(), null, Issue.MAX_DESCRIPTION_LEN));
    }

    @Test
    void attachmentOnlyEditPreservesExistingMarkdownLiterally() throws Exception {
        var old = "[old](attachment:historical)";
        assertEquals(old + "\n\n[note.txt](/files/note.txt)", upload(Map.of(), "note.txt")
                .comment(issue, old, Issue.MAX_DESCRIPTION_LEN));
    }

    @Test
    void missingAttachmentFailsEvenWhenNoFilesWerePassed() {
        var post = new TodPost(Map.of("content", "[missing](attachment:file.txt)"), null);
        assertTrue(assertThrows(BadRequestException.class, () -> post.comment(issue, null, 1000))
                .getMessage().contains("Missing --attach"));
        verifyNoInteractions(storage);
    }

    @Test
    void uploadsResolutionNoteOnlyWhenStatusChanges() throws Exception {
        var comment = new CodeComment();
        comment.setId(3L);
        comment.setProject(issue.getProject());
        comment.setUUID(issue.getUUID());
        var subject = mock(Subject.class);
        var changes = mock(CodeCommentStatusChangeService.class);
        oneDev.when(() -> OneDev.getInstance(CodeCommentStatusChangeService.class)).thenReturn(changes);
        when(OneDev.getInstance(UrlService.class).urlFor(comment, true)).thenReturn("/comment");
        security.when(() -> SecurityUtils.canChangeStatus(subject, comment)).thenReturn(true);
        var note = upload(Map.of("content", "Fixed in [this trace](attachment:trace.log)."), "trace.log");
        CodeCommentHelper.changeStatus(subject, comment, true, note);
        verify(changes).create(argThat((CodeCommentStatusChange change) ->
                        change.getComment() == comment && change.isResolved()),
                eq("Fixed in [this trace](/files/trace.log)."));
        comment.setResolved(true);
        var result = CodeCommentHelper.changeStatus(subject, comment, true, upload(Map.of(), "skipped.log"));
        assertTrue(result.containsKey("warning"));
        assertEquals(List.of("trace.log"), saved);
        verifyNoMoreInteractions(changes);
    }

    @Test
    void rollbackAfterUploadFailureRemovesOnlyNewFiles() throws Exception {
        var post = upload(Map.of(), "ok.txt", "oversized");
        assertThrows(AttachmentTooLargeException.class, () -> post.comment(issue, null, 1000));
        var callback = ArgumentCaptor.forClass(Synchronization.class);
        verify(transaction).registerSynchronization(callback.capture());
        callback.getValue().afterCompletion(Status.STATUS_ROLLEDBACK);
        verify(storage).deleteAttachment(1L, issue.getUUID(), "ok.txt");
    }
}
