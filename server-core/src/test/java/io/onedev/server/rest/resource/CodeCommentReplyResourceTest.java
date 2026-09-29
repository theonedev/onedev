package io.onedev.server.rest.resource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.apache.shiro.authz.UnauthenticatedException;
import org.apache.shiro.authz.UnauthorizedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;

import io.onedev.server.model.CodeComment;
import io.onedev.server.model.CodeCommentReply;
import io.onedev.server.model.Project;
import io.onedev.server.model.User;
import io.onedev.server.model.support.CompareContext;
import io.onedev.server.rest.resource.CodeCommentReplyResource.CodeCommentReplyCreateData;
import io.onedev.server.security.SecurityUtils;
import io.onedev.server.service.CodeCommentReplyService;
import io.onedev.server.service.CodeCommentService;
import io.onedev.server.validation.HibernateValidationTestSupport;

class CodeCommentReplyResourceTest extends HibernateValidationTestSupport {

	private CodeCommentReplyService replies;
	private CodeCommentService comments;
	private CodeCommentReplyResource resource;
	private CodeComment comment;
	private User user;
	private MockedStatic<SecurityUtils> security;

	@BeforeEach
	void setUp() {
		replies = mock(CodeCommentReplyService.class);
		comments = mock(CodeCommentService.class);
		resource = new CodeCommentReplyResource(replies, comments);
		comment = new CodeComment();
		comment.setId(1L);
		comment.setProject(new Project());
		comment.setCompareContext(new CompareContext());
		user = new User();
		user.setId(2L);
		when(comments.load(1L)).thenReturn(comment);
		security = mockStatic(SecurityUtils.class);
		security.when(SecurityUtils::getUser).thenReturn(user);
		security.when(SecurityUtils::getAuthUser).thenReturn(user);
		security.when(() -> SecurityUtils.canReadCode(comment.getProject())).thenReturn(true);
	}

	@AfterEach
	void tearDown() {
		if (security != null)
			security.close();
	}

	@ParameterizedTest
	@ValueSource(booleans = {true, false})
	void createsReplyWithAuthenticatedAuthorAndParentContext(boolean scopedToken) {
		if (scopedToken)
			security.when(SecurityUtils::getAuthUser).thenReturn(null);
		var data = createData();
		doAnswer(invocation -> {
			CodeCommentReply reply = invocation.getArgument(0);
			assertSame(comment, reply.getComment());
			assertSame(user, reply.getUser());
			assertSame(comment.getCompareContext(), reply.getCompareContext());
			assertEquals(data.getContent(), reply.getContent());
			assertNotNull(reply.getDate());
			reply.setId(3L);
			return null;
		}).when(replies).create(any());
		assertEquals(3L, resource.createReply(data));
	}

	@Test
	void requiresAuthenticationAndCodeReadPermissionToCreate() {
		security.when(SecurityUtils::getUser).thenReturn(null);
		assertThrows(UnauthenticatedException.class, () -> resource.createReply(createData()));
		verifyNoInteractions(comments, replies);
		security.when(SecurityUtils::getUser).thenReturn(user);
		security.when(SecurityUtils::getAuthUser).thenReturn(null);
		security.when(() -> SecurityUtils.canReadCode(comment.getProject())).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.createReply(createData()));
		verifyNoInteractions(replies);
	}

	@Test
	void readsAndUpdatesReplyWithoutChangingAuthorOrContext() {
		var reply = existingReply();
		assertSame(reply, resource.getReply(3L));
		security.when(() -> SecurityUtils.canModifyOrDelete(reply)).thenReturn(true);
		var date = reply.getDate();
		try (var response = resource.updateReply(3L, "Updated")) {
			assertEquals(200, response.getStatus());
		}
		assertEquals("Updated", reply.getContent());
		assertSame(comment, reply.getComment());
		assertSame(user, reply.getUser());
		assertSame(comment.getCompareContext(), reply.getCompareContext());
		assertEquals(date, reply.getDate());
		try (var response = resource.updateReply(3L, "Updated")) {
			assertEquals(200, response.getStatus());
		}
		verify(replies, times(1)).update(reply);
	}

	@Test
	void enforcesReadAndModificationPermissions() {
		var reply = existingReply();
		assertThrows(UnauthorizedException.class, () -> resource.updateReply(3L, "Updated"));
		assertThrows(UnauthorizedException.class, () -> resource.deleteReply(3L));
		security.when(() -> SecurityUtils.canModifyOrDelete(reply)).thenReturn(true);
		security.when(() -> SecurityUtils.canReadCode(comment.getProject())).thenReturn(false);
		assertThrows(UnauthorizedException.class, () -> resource.getReply(3L));
		assertThrows(UnauthorizedException.class, () -> resource.updateReply(3L, "Updated"));
		assertThrows(UnauthorizedException.class, () -> resource.deleteReply(3L));
		assertEquals("Original", reply.getContent());
		verify(replies, never()).update(any());
		verify(replies, never()).delete(any(CodeCommentReply.class));
	}

	@Test
	void deletesThroughReplyService() {
		var reply = existingReply();
		security.when(() -> SecurityUtils.canModifyOrDelete(reply)).thenReturn(true);
		try (var response = resource.deleteReply(3L)) {
			assertEquals(200, response.getStatus());
		}
		verify(replies).delete(reply);
	}

	@Test
	void validatesCreateAndUpdateContent() throws Exception {
		assertPaths(validator.validate(new CodeCommentReplyCreateData()), "commentId", "content");
		var data = createData();
		assertTrue(validator.validate(data).isEmpty());
		var create = CodeCommentReplyResource.class.getMethod("createReply", CodeCommentReplyCreateData.class);
		var update = CodeCommentReplyResource.class.getMethod("updateReply", Long.class, String.class);
		for (String content : new String[] {null, " ", "x".repeat(CodeCommentReply.MAX_CONTENT_LEN + 1)}) {
			data.setContent(content);
			assertFalse(validator.forExecutables().validateParameters(resource, create, new Object[] {data}).isEmpty());
			assertFalse(validator.forExecutables().validateParameters(resource, update, new Object[] {3L, content}).isEmpty());
		}
	}

	private CodeCommentReplyCreateData createData() {
		var data = new CodeCommentReplyCreateData();
		data.setCommentId(1L);
		data.setContent("Original");
		return data;
	}

	private CodeCommentReply existingReply() {
		var reply = new CodeCommentReply();
		reply.setId(3L);
		reply.setComment(comment);
		reply.setUser(user);
		reply.setContent("Original");
		reply.setCompareContext(comment.getCompareContext());
		when(replies.load(3L)).thenReturn(reply);
		return reply;
	}
}
