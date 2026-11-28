package io.onedev.server.ai;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Map;

import jakarta.inject.Inject;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.Provider;

import org.apache.commons.fileupload2.core.AbstractRequestContext;
import org.apache.commons.fileupload2.jakarta.servlet6.JakartaServletDiskFileUpload;
import org.apache.shiro.authz.UnauthenticatedException;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.onedev.server.security.SecurityUtils;

@Provider
@Consumes({MediaType.APPLICATION_JSON, MediaType.TEXT_PLAIN, MediaType.MULTIPART_FORM_DATA})
public class TodPostReader implements MessageBodyReader<TodPost> {

    static final int MAX_METADATA_BYTES = 1024 * 1024;

    @Inject
    private ObjectMapper mapper;

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return type == TodPost.class;
    }

    @Override
    public TodPost readFrom(Class<TodPost> type, Type genericType, Annotation[] annotations, MediaType mediaType,
                            MultivaluedMap<String, String> headers, InputStream body) throws IOException {
        if (SecurityUtils.getUser() == null)
            throw new UnauthenticatedException();
        if (MediaType.TEXT_PLAIN_TYPE.isCompatible(mediaType))
            return new TodPost(Map.of("content", new String(body.readAllBytes(), UTF_8)), null);
        if (MediaType.APPLICATION_JSON_TYPE.isCompatible(mediaType))
            return new TodPost(readData(body), null);

        var context = new AbstractRequestContext<>(headers::getFirst, () -> -1, body) {
            public String getCharacterEncoding() { return UTF_8.name(); }
            public String getContentType() { return mediaType.toString(); }
            public InputStream getInputStream() { return body; }
        };
        var parts = new JakartaServletDiskFileUpload().getItemIterator(context);
        if (!parts.hasNext())
            throw new BadRequestException("Missing multipart data");
        var first = parts.next();
        if (!first.isFormField() || !first.getFieldName().equals("data"))
            throw new BadRequestException("Expected JSON data as first multipart field");
        var metadata = first.getInputStream().readNBytes(MAX_METADATA_BYTES + 1);
        if (metadata.length > MAX_METADATA_BYTES)
            throw new ClientErrorException("Multipart data exceeds 1 MiB", Status.REQUEST_ENTITY_TOO_LARGE);
        return new TodPost(readData(new ByteArrayInputStream(metadata)), parts);
    }

    private Map<String, Serializable> readData(InputStream stream) throws IOException {
        Map<String, Serializable> data = mapper.readValue(stream, new TypeReference<>() {});
        if (data == null)
            throw new BadRequestException("Expected a JSON object");
        return data;
    }
}
