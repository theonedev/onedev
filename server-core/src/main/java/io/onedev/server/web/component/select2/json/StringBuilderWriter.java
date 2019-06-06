/*
 * Copyright 2012 Igor Vaynberg
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this work except in compliance with
 * the License. You may obtain a copy of the License in the LICENSE file, or at:
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.onedev.server.web.component.select2.json;

import java.io.IOException;
import java.io.Writer;

/**
 * A {@link Writer} that writes into a {@link StringBuilder}.
 * 
 * @author igor
 * 
 */
public class StringBuilderWriter extends Writer {
    private final StringBuilder builder;

    public StringBuilderWriter() {
	this(new StringBuilder());
    }

    public StringBuilderWriter(StringBuilder builder) {
	this.builder = builder;
    }

    public StringBuilder getBuilder() {
	return builder;
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
	if ((off < 0) || (off > cbuf.length) || (len < 0) || ((off + len) > cbuf.length) || ((off + len) < 0)) {
	    throw new IndexOutOfBoundsException();
	} else if (len == 0) {
	    return;
	}
	builder.append(cbuf, off, len);
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

}
