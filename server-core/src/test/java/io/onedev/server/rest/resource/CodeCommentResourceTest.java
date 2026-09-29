package io.onedev.server.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import jakarta.ws.rs.NotAcceptableException;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.apache.shiro.subject.Subject;
import org.eclipse.jgit.lib.FileMode;
import org.eclipse.jgit.revwalk.RevCommit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import io.onedev.commons.loader.ImplementationRegistry;
import io.onedev.commons.utils.PlanarRange;
import io.onedev.server.data.migration.VersionedXmlDoc;
import io.onedev.server.git.Blob;
import io.onedev.server.git.BlobIdent;
import io.onedev.server.git.service.GitService;
import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.CodeCommentStatusChange;
import io.onedev.server.model.Project;
import io.onedev.server.model.PullRequest;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.model.support.Mark;
import io.onedev.server.rest.resource.CodeCommentResource.CodeCommentCreateData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.AuditService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.service.CodeCommentStatusChangeService;
import io.onedev.server.service.ProjectService;
import io.onedev.server.service.PullRequestService;
import io.onedev.server.rest.resource.support.MarkData;
import io.onedev.server.rest.resource.support.CompareContextData;
import io.onedev.server.rest.resource.support.PlanarRangeData;
import io.onedev.server.util.jackson.ObjectMapperProvider;
import io.onedev.server.util.diff.WhitespaceOption;
import io.onedev.server.validation.HibernateValidationTestSupport;

class CodeCommentResourceTest extends HibernateValidationTestSupport {

	private static final String COMMIT = "1234567890123456789012345678901234567890";

	private CodeCommentService comments;
	private CodeCommentStatusChangeService statusChanges;
	private AuditService audit;
	private ProjectService projects;
	private PullRequestService pullRequests;
	private CodeCommentResource resource;
	private Project project;
	private User user;
	private Subject subject;
	private MockedStatic<SecurityUtils> security;
	private MockedStatic<VersionedXmlDoc> xml;

	@BeforeEach
	void setUp() {
		comments = mock(CodeCommentService.class);
		statusChanges = mock(CodeCommentStatusChangeService.class);
		audit = mock(AuditService.class);
		projects = mock(ProjectService.class);
		pullRequests = mock(PullRequestService.class);
		resource = new CodeCommentResource(comments, audit, projects, statusChanges, mock(GitService.class), pullRequests);
		project = mock(Project.class);
		user = new User();
		user.setId(2L);
		when(projects.load(1L)).thenReturn(project);
		when(project.getRevCommit(COMMIT, false)).thenReturn(mock(RevCommit.class));
		var blob = mock(Blob.class);
		when(blob.getText()).thenReturn(new Blob.Text(StandardCharsets.UTF_8, "hello\nworld"));
		when(project.getBlob(any(BlobIdent.class), eq(false))).thenReturn(blob);
		security = mockStatic(SecurityUtils.class);
		subject = mock(Subject.class);
		security.when(SecurityUtils::getSubject).thenReturn(subject);
		security.when(() -> SecurityUtils.getUser(subject)).thenReturn(user);
		security.when(SecurityUtils::getUser).thenReturn(user);
		security.when(SecurityUtils::getAuthUser).thenReturn(user);
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(true);
		security.when(() -> SecurityUtils.canReadCode(subject, project))
				.thenAnswer(invocation -> SecurityUtils.canReadCode(project));
		xml = mockStatic(VersionedXmlDoc.class);
		var document = mock(VersionedXmlDoc.class);
		when(document.toXML()).thenReturn("<comment/>");
		xml.when(() -> VersionedXmlDoc.fromBean(any())).thenReturn(document);
	}

	@AfterEach
	void tearDown() {
		if (xml != null)
			xml.close();
		if (security != null)
			security.close();
	}

	@Test
	void mapsCommentRangeLikePullRequestUiWithoutChangingOriginalMark() {
		var comment = existingComment();
		comment.setMark(new Mark(COMMIT, "file.txt", new PlanarRange(0, 1, 2, 3)));
		var originalMark = comment.getMark();
		var targetCommit = "abcdefabcdefabcdefabcdefabcdefabcdefabcd";
		when(project.getRevCommit(targetCommit, false)).thenReturn(mock(RevCommit.class));
		when(project.readLines(any(BlobIdent.class), any(WhitespaceOption.class), anyBoolean())).thenCallRealMethod();
		var oldBlob = mock(Blob.class);
		when(oldBlob.getText()).thenReturn(new Blob.Text(StandardCharsets.UTF_8, "first  \nmiddle\nlast"));
		when(project.getBlob(new BlobIdent(COMMIT, "file.txt", FileMode.REGULAR_FILE.getBits()), true)).thenReturn(oldBlob);
		var newBlob = mock(Blob.class);
		when(newBlob.getText()).thenReturn(new Blob.Text(StandardCharsets.UTF_8, "inserted\nfirst\nmiddle \nlast"));
		when(project.getBlob(new BlobIdent(targetCommit, "file.txt", FileMode.REGULAR_FILE.getBits()), false)).thenReturn(newBlob);

		assertEquals(new Mark(targetCommit, "file.txt", new PlanarRange(1, 1, 3, 3)), resource.getMark(4L, targetCommit));
		assertSame(originalMark, comment.getMark());

		// The UI rejects a range with a changed interior line even if both endpoints still match.
		when(newBlob.getText()).thenReturn(new Blob.Text(StandardCharsets.UTF_8, "inserted\nfirst\nchanged\nlast"));
		assertNull(resource.getMark(4L, targetCommit));
		when(newBlob.getText()).thenReturn(null);
		assertNull(resource.getMark(4L, targetCommit));
		when(project.getBlob(new BlobIdent(targetCommit, "file.txt", FileMode.REGULAR_FILE.getBits()), false)).thenReturn(null);
		assertNull(resource.getMark(4L, targetCommit));
		verifyNoInteractions(audit);
	}

	@Test
	void checksPermissionAndTargetCommitBeforeMappingComment() throws Exception {
		existingComment();
		var method = CodeCommentResource.class.getMethod("getMark", Long.class, String.class);
		assertFalse(validator.forExecutables().validateParameters(resource, method, new Object[] {4L, null}).isEmpty());
		assertThrows(NotAcceptableException.class, () -> resource.getMark(4L, "invalid"));
		assertThrows(NotAcceptableException.class, () -> resource.getMark(4L, "abcdefabcdefabcdefabcdefabcdefabcdefabcd"));
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.getMark(4L, COMMIT));
		verify(project, never()).readLines(any(), any(), anyBoolean());
	}

	@Test
	void listsOnlyRepliesOfTheRequestedCommentWithCodeReadPermission() {
		var comment = existingComment();
		assertTrue(resource.getReplies(4L).isEmpty());
		var reply = new CodeCommentReply();
		reply.setComment(comment);
		comment.getReplies().add(reply);
		assertSame(comment.getReplies(), resource.getReplies(4L));
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.getReplies(4L));
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void createsCommentFromJsonWithPullRequestContext(boolean scopedToken) throws Exception {
		if (scopedToken)
			security.when(SecurityUtils::getAuthUser).thenReturn(null);
		var request = new PullRequest();
		request.setId(3L);
		request.setTargetProject(project);
		when(pullRequests.load(3L)).thenReturn(request);
		var mapper = new ObjectMapperProvider(Set.of(), Set.of(), mock(ImplementationRegistry.class), Set.of()).get();
		var data = mapper.readValue("{\"projectId\":1,\"content\":\"Review this\","
				+ "\"mark\":{\"commitHash\":\"" + COMMIT + "\",\"path\":\"file.txt\","
				+ "\"range\":{\"fromRow\":0,\"fromColumn\":0,\"toRow\":0,\"toColumn\":5}},"
				+ "\"compareContext\":{\"oldCommitHash\":\"" + COMMIT + "\",\"newCommitHash\":\"" + COMMIT
				+ "\",\"pullRequestId\":3}}", CodeCommentCreateData.class);
		assertTrue(validator.validate(data).isEmpty());
		var range = new PlanarRange(0, 0, 1, 4, 4);
		assertEquals(range, mapper.readValue(mapper.writeValueAsString(range), PlanarRangeData.class).toPlanarRange());
		doAnswer(invocation -> {
			CodeComment comment = invocation.getArgument(0);
			assertSame(project, comment.getProject());
			assertSame(user, comment.getUser());
			assertSame(request, comment.getCompareContext().getPullRequest());
			assertNull(comment.getCompareContext().getPathFilter());
			assertEquals(WhitespaceOption.IGNORE_TRAILING, comment.getCompareContext().getWhitespaceOption());
			assertEquals(new PlanarRange(0, 0, 0, 5), comment.getMark().getRange());
			assertEquals("Review this", comment.getContent());
			comment.setId(4L);
			return null;
		}).when(comments).create(any());
		assertEquals(4L, resource.createComment(data));
		verifyNoInteractions(audit);
	}

	@Test
	void requiresAuthenticationAndCodeReadPermissionToCreate() {
		security.when(SecurityUtils::getUser).thenReturn(null);
		assertThrows(UnauthenticatedException.class, () -> resource.createComment(createData()));
		verifyNoInteractions(projects, comments);
		security.when(SecurityUtils::getUser).thenReturn(user);
		security.when(SecurityUtils::getAuthUser).thenReturn(null);
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.createComment(createData()));
		verifyNoInteractions(comments, audit);
	}

	@Test
	void rejectsCrossProjectPullRequestAndInvalidLocations() {
		var data = createData();
		var request = new PullRequest();
		request.setTargetProject(new Project());
		when(pullRequests.load(3L)).thenReturn(request);
		data.getCompareContext().setPullRequestId(3L);
		assertThrows(NotAcceptableException.class, () -> resource.createComment(data));
		data.getCompareContext().setPullRequestId(null);
		data.getMark().setCommitHash("invalid");
		assertThrows(NotAcceptableException.class, () -> resource.createComment(data));
		data.getMark().setCommitHash(COMMIT);
		data.getMark().setRange(rangeData(0, 0, 2, 0));
		assertThrows(NotAcceptableException.class, () -> resource.createComment(data));
		data.getMark().setRange(rangeData(0, 0, 0, 5));
		when(project.getRevCommit(COMMIT, false)).thenReturn(null);
		assertThrows(NotAcceptableException.class, () -> resource.createComment(data));
		verifyNoInteractions(comments, audit);
	}

	@Test
	void updatesOnlyContentAndSkipsUnchangedContent() {
		var comment = existingComment();
		security.when(() -> SecurityUtils.canModifyOrDelete(comment)).thenReturn(true);
		var mark = comment.getMark();
		var context = comment.getCompareContext();
		try (var response = resource.updateComment(4L, "Updated")) {
			assertEquals(200, response.getStatus());
		}
		assertEquals("Updated", comment.getContent());
		assertSame(user, comment.getUser());
		assertSame(mark, comment.getMark());
		assertSame(context, comment.getCompareContext());
		try (var response = resource.updateComment(4L, "Updated")) {
			assertEquals(200, response.getStatus());
		}
		verify(comments, times(1)).update(comment);
		verifyNoInteractions(audit);
	}

	@Test
	void auditsDeletionOfCodeCommentThread() {
		var comment = existingComment();
		security.when(() -> SecurityUtils.canModifyOrDelete(comment)).thenReturn(true);
		try (var response = resource.deleteComment(4L)) {
			assertEquals(200, response.getStatus());
		}
		verify(comments).delete(comment);
		verify(audit).audit(project, "deleted code comment on file \"file.txt\" via RESTful API", "<comment/>", null);
	}

	@Test
	void rejectsEditingWithoutPermission() {
		var comment = existingComment();
		assertThrows(UnauthorizedException.class, () -> resource.updateComment(4L, "Updated"));
		security.when(() -> SecurityUtils.canModifyOrDelete(comment)).thenReturn(true);
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.updateComment(4L, "Updated"));
		assertEquals("Original", comment.getContent());
		verify(comments, never()).update(any());
		verifyNoInteractions(audit);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void changesStatusWithOptionalNoteAndPreservesContext(boolean resolved) {
		var comment = existingComment();
		comment.setResolved(!resolved);
		security.when(() -> SecurityUtils.canChangeStatus(subject, comment)).thenReturn(true);
		String note = resolved ? "Fixed in the latest revision" : null;
		try (var response = resolved ? resource.resolveComment(4L, note) : resource.unresolveComment(4L, note)) {
			assertEquals(200, response.getStatus());
		}
		var change = ArgumentCaptor.forClass(CodeCommentStatusChange.class);
		verify(statusChanges).create(change.capture(), eq(note));
		assertSame(comment, change.getValue().getComment());
		assertSame(user, change.getValue().getUser());
		assertEquals(resolved, change.getValue().isResolved());
		assertSame(comment.getCompareContext(), change.getValue().getCompareContext());
		verify(comments, never()).update(any());
		verifyNoInteractions(audit);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void rejectsStatusChangesAfterCodeReadAccessIsRevoked(boolean resolved) {
		var comment = existingComment();
		comment.setResolved(resolved);
		security.when(() -> SecurityUtils.canChangeStatus(subject, comment)).thenReturn(true);
		security.when(() -> SecurityUtils.canReadCode(project)).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.resolveComment(4L, "Note"));
		assertThrows(UnauthorizedException.class, () -> resource.unresolveComment(4L, "Note"));
		assertEquals(resolved, comment.isResolved());
		verifyNoInteractions(statusChanges, audit);
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void checksStatusPermissionEvenWhenAlreadyInRequestedState(boolean resolved) {
		var comment = existingComment();
		comment.setResolved(resolved);
		assertThrows(UnauthorizedException.class, () -> {
			if (resolved)
				resource.resolveComment(4L, "Note");
			else
				resource.unresolveComment(4L, "Note");
		});
		security.when(() -> SecurityUtils.canChangeStatus(subject, comment)).thenReturn(true);
		try (var response = resolved ? resource.resolveComment(4L, "Note") : resource.unresolveComment(4L, "Note")) {
			assertEquals(200, response.getStatus());
		}
		verifyNoInteractions(statusChanges);
	}

	@Test
	void validatesOptionalNoteLength() throws Exception {
		for (String methodName : new String[] {"resolveComment", "unresolveComment"}) {
			var method = CodeCommentResource.class.getMethod(methodName, Long.class, String.class);
			assertTrue(validator.forExecutables().validateParameters(resource, method,
					new Object[] {4L, null}).isEmpty());
			assertFalse(validator.forExecutables().validateParameters(resource, method,
					new Object[] {4L, "x".repeat(CodeCommentReply.MAX_CONTENT_LEN + 1)}).isEmpty());
		}
	}

	@Test
	void validatesRequiredFieldsAndContentLength() throws Exception {
		assertPaths(validator.validate(new CodeCommentCreateData()), "projectId", "content", "mark", "compareContext");
		var data = createData();
		data.setMark(new MarkData());
		data.setCompareContext(new CompareContextData());
		assertPaths(validator.validate(data), "mark.commitHash", "mark.path", "mark.range",
				"compareContext.oldCommitHash", "compareContext.newCommitHash");
		data = createData();
		data.getMark().setRange(new PlanarRangeData());
		assertPaths(validator.validate(data), "mark.range.fromRow", "mark.range.fromColumn",
				"mark.range.toRow", "mark.range.toColumn");
		var method = CodeCommentResource.class.getMethod("updateComment", Long.class, String.class);
		for (String content : new String[] {null, " ", "x".repeat(CodeComment.MAX_CONTENT_LEN + 1)}) {
			assertFalse(validator.forExecutables().validateParameters(resource, method,
					new Object[] {4L, content}).isEmpty());
		}
		assertTrue(validator.forExecutables().validateParameters(resource, method,
				new Object[] {4L, "Updated"}).isEmpty());
	}

	private PlanarRangeData rangeData(int fromRow, int fromColumn, int toRow, int toColumn) {
		var range = new PlanarRangeData();
		range.setFromRow(fromRow);
		range.setFromColumn(fromColumn);
		range.setToRow(toRow);
		range.setToColumn(toColumn);
		return range;
	}

	private CodeCommentCreateData createData() {
		var data = new CodeCommentCreateData();
		data.setProjectId(1L);
		data.setContent("Original");
		var mark = new MarkData();
		mark.setCommitHash(COMMIT);
		mark.setPath("file.txt");
		mark.setRange(rangeData(0, 0, 0, 5));
		data.setMark(mark);
		var context = new CompareContextData();
		context.setOldCommitHash(COMMIT);
		context.setNewCommitHash(COMMIT);
		data.setCompareContext(context);
		return data;
	}

	private CodeComment existingComment() {
		var data = createData();
		var comment = new CodeComment();
		comment.setId(4L);
		comment.setProject(project);
		comment.setUser(user);
		comment.setContent(data.getContent());
		comment.setMark(data.getMark().toMark());
		var context = new CompareContext();
		context.setOldCommitHash(COMMIT);
		context.setNewCommitHash(COMMIT);
		comment.setCompareContext(context);
		when(comments.load(4L)).thenReturn(comment);
		return comment;
	}
}
