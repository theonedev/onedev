/*
 * Copyright (c) 2010, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.message.internal;

import java.text.ParseException;
import java.util.Map;

import javax.ws.rs.core.MediaType;

import javax.inject.Singleton;

import org.glassfish.jersey.internal.LocalizationMessages;
import org.glassfish.jersey.spi.HeaderDelegateProvider;
import static org.glassfish.jersey.message.internal.Utils.throwIllegalArgumentExceptionIfNull;

/**
 * Header delegate provider for MediaType.
 *
 * @author Marc Hadley
 * @author Marek Potociar
 * @author Martin Matula
 */
@Singleton
public class MediaTypeProvider implements HeaderDelegateProvider<MediaType> {

    private static final String MEDIA_TYPE_IS_NULL = LocalizationMessages.MEDIA_TYPE_IS_NULL();

    @Override
    public boolean supports(Class<?> type) {
        return MediaType.class.isAssignableFrom(type);
    }

    @Override
    public String toString(MediaType header) {

        throwIllegalArgumentExceptionIfNull(header, MEDIA_TYPE_IS_NULL);

        StringBuilder b = new StringBuilder();
        b.append(header.getType()).append('/').append(header.getSubtype());
        for (Map.Entry<String, String> e : header.getParameters().entrySet()) {
            b.append(";").append(e.getKey()).append('=');
            StringBuilderUtils.appendQuotedIfNonToken(b, e.getValue());
        }
        return b.toString();
    }

    @Override
    public MediaType fromString(String header) {

        throwIllegalArgumentExceptionIfNull(header, MEDIA_TYPE_IS_NULL);

		var commaIndex = header.indexOf(',');
		if (commaIndex != -1) {
			var colonIndex = header.indexOf(';', commaIndex);
			if (colonIndex != -1)
				header = header.substring(0, commaIndex) + header.substring(colonIndex);
			else 
				header = header.substring(0, commaIndex);
		}
        try {
            return valueOf(HttpHeaderReader.newInstance(header));
        } catch (ParseException ex) {
            throw new IllegalArgumentException(
                    "Error parsing media type '" + header + "'", ex);
        }
    }

    /**
     * Create a new {@link javax.ws.rs.core.MediaType} instance from a header reader.
     *
     * @param reader header reader.
     * @return new {@code MediaType} instance.
     *
     * @throws ParseException in case of a header parsing error.
     */
    public static MediaType valueOf(HttpHeaderReader reader) throws ParseException {
        // Skip any white space
        reader.hasNext();

        // Get the type
        final String type = reader.nextToken().toString();
        reader.nextSeparator('/');
        // Get the subtype
        final String subType = reader.nextToken().toString();

        Map<String, String> params = null;

        if (reader.hasNext()) {
            params = HttpHeaderReader.readParameters(reader);
        }

        return new MediaType(type, subType, params);
    }
}
