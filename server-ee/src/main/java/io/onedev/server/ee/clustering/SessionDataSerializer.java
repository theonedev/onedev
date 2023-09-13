//
//  ========================================================================
//  Copyright (c) 1995-2022 Mort Bay Consulting Pty Ltd and others.
//  ------------------------------------------------------------------------
//  All rights reserved. This program and the accompanying materials
//  are made available under the terms of the Eclipse Public License v1.0
//  and Apache License v2.0 which accompanies this distribution.
//
//      The Eclipse Public License is available at
//      http://www.eclipse.org/legal/epl-v10.html
//
//      The Apache License v2.0 is available at
//      http://www.opensource.org/licenses/apache2.0.php
//
//  You may elect to redistribute this code under either of these licenses.
//  ========================================================================
//

package io.onedev.server.ee.clustering;

import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.StreamSerializer;
import org.eclipse.jetty.server.session.SessionData;
import org.eclipse.jetty.util.ClassLoadingObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;

/**
 * SessionDataSerializer
 *
 * Handles serialization on behalf of the SessionData object, and
 * ensures that we use jetty's classloading knowledge.
 */
public class SessionDataSerializer implements StreamSerializer<SessionData>
{
    public static final int __TYPEID = 99;

    @Override
    public int getTypeId()
    {
        return __TYPEID;
    }

    @Override
    public void destroy()
    {
    }

    @Override
    public void write(ObjectDataOutput out, SessionData data) throws IOException
    {
        out.writeUTF(data.getId());
        out.writeUTF(data.getContextPath());
        out.writeUTF(data.getVhost());

        out.writeLong(data.getAccessed());
        out.writeLong(data.getLastAccessed());
        out.writeLong(data.getCreated());
        out.writeLong(data.getCookieSet());
        out.writeUTF(data.getLastNode());

        out.writeLong(data.getExpiry());
        out.writeLong(data.getMaxInactiveMs());

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ObjectOutputStream oos = new ObjectOutputStream(baos))
        {
            SessionData.serializeAttributes(data, oos);
            out.writeByteArray(baos.toByteArray());
        }
    }

    @Override
    public SessionData read(ObjectDataInput in) throws IOException
    {
        String id = in.readUTF();
        String contextPath = in.readUTF();
        String vhost = in.readUTF();

        long accessed = in.readLong();
        long lastAccessed = in.readLong();
        long created = in.readLong();
        long cookieSet = in.readLong();
        String lastNode = in.readUTF();
        long expiry = in.readLong();
        long maxInactiveMs = in.readLong();

        SessionData sd = new SessionData(id, contextPath, vhost, created, accessed, lastAccessed, maxInactiveMs);

        ByteArrayInputStream bais = new ByteArrayInputStream(in.readByteArray());
        try (ClassLoadingObjectInputStream ois = new ClassLoadingObjectInputStream(bais))
        {
            SessionData.deserializeAttributes(sd, ois);
        }
        catch (ClassNotFoundException e)
        {
            throw new IOException(e);
        }
        sd.setCookieSet(cookieSet);
        sd.setLastNode(lastNode);
        sd.setExpiry(expiry);
        return sd;
    }
}
