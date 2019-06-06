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

import org.json.JSONException;
import org.json.JSONWriter;

/**
 * Json utilities
 */
public class Json {
    private Json() {
    };

    /**
     * Writes a key/value pair into the {@code writer} if the value is not {@code null}
     * 
     * @param writer
     *            json writer
     * @param key
     *            key
     * @param value
     *            value
     * @throws JSONException
     */
    public static void writeObject(JSONWriter writer, String key, Object value) throws JSONException {
	if (value != null) {
	    writer.key(key);
	    writer.value(value);
	}
    }

    /**
     * Writes a key/value pair into the {@code writer} where {@code value} represents a javascript function and should
     * be written out unencoded if the value is not {@code null}
     * 
     * @param writer
     *            json writer
     * @param key
     *            key
     * @param value
     *            value
     * @throws JSONException
     */
    public static void writeFunction(JSONWriter writer, String key, String value) throws JSONException {
	if (value != null) {
	    writer.key(key).value(new JsonFunction(value));
	}
    }

}
