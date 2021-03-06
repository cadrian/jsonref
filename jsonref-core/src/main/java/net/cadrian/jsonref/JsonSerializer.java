/*
   Copyright 2015 Cyril Adrian <cyril.adrian@gmail.com>

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package net.cadrian.jsonref;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import net.cadrian.jsonref.atomic.DefaultJsonConverter;

/**
 * Main entry point of the JSON/R de/serialization
 */
public class JsonSerializer {

	private static final SerializationProcessor SERIALIZATION_PROCESSOR = new SerializationProcessor();
	private static final DeserializationProcessor DESERIALIZATION_PROCESSOR = new DeserializationProcessor();

	private final JsonConverter converter;

	/**
	 * Default constructor with a {@link DefaultJsonConverter default converter}
	 */
	public JsonSerializer() {
		this(null);
	}

	/**
	 * Constructor with a given converter
	 *
	 * @param converter
	 *            the converter; if <code>null</code> a
	 *            {@link DefaultJsonConverter default converter} will be used
	 *            instead
	 */
	public JsonSerializer(final JsonConverter converter) {
		if (converter == null) {
			this.converter = new DefaultJsonConverter();
		} else {
			this.converter = converter;
		}
	}

	/**
	 * Serialize to JSON/R
	 *
	 * @param object
	 *            the object to serialize
	 * @param level
	 *            the prettiness level (can be <code>null</code>, in that case a
	 *            {@link Prettiness#COMPACT compact} string will be produced)
	 * @return the JSON/R string
	 */
	public String toJson(final Object object, final Prettiness level) {
		return SERIALIZATION_PROCESSOR.serialize(object, converter,
				level == null ? null : level.newContext());
	}

	/**
	 * Serialize to JSON/R ({@link Prettiness#COMPACT compact} string)
	 *
	 * @param object
	 *            the object to serialize
	 * @return the JSON/R string
	 */
	public String toJson(final Object object) {
		return toJson(object, (Prettiness) null);
	}

	/**
	 * Serialize to JSON/R
	 *
	 * @param object
	 *            the object to serialize
	 * @param out
	 *            the stream to write to
	 * @param level
	 *            the prettiness level (can be <code>null</code>, in that case a
	 *            {@link Prettiness#COMPACT compact} string will be produced)
	 * @throws IOException
	 *             on I/O exception
	 */
	public void toJson(final Object object, final Writer out,
			final Prettiness level) throws IOException {
		SERIALIZATION_PROCESSOR.serializeTo(object, out, converter,
				level == null ? null : level.newContext());
	}

	/**
	 * Serialize to JSON/R ({@link Prettiness#COMPACT compact} string)
	 *
	 * @param object
	 *            the object to serialize
	 * @param out
	 *            the stream to write to
	 * @throws IOException
	 *             on I/O exception
	 */
	public void toJson(final Object object, final Writer out)
			throws IOException {
		toJson(object, out, null);
	}

	/**
	 * Deserialize from JSON/R to the most appropriate type
	 *
	 * @param jsonR
	 *            the JSON/R string
	 * @return the object
	 */
	public Object fromJson(final String jsonR) {
		return DESERIALIZATION_PROCESSOR.deserialize(jsonR, converter, null);
	}

	/**
	 * Deserialize from JSON/R to the given type
	 *
	 * @param jsonR
	 *            the JSON/R string
	 * @param wantedType
	 *            the wanted type
	 * @param <T>
	 *            the type of the object to return
	 * @return the object
	 */
	public <T> T fromJson(final String jsonR,
			final Class<? extends T> wantedType) {
		return DESERIALIZATION_PROCESSOR.deserialize(jsonR, converter,
				wantedType);
	}

	/**
	 * Deserialize from JSON/R to the most appropriate type
	 *
	 * @param jsonR
	 *            the JSON/R string
	 * @return the object
	 * @throws IOException
	 *             on I/O exception
	 */
	public Object fromJson(final Reader jsonR) throws IOException {
		return DESERIALIZATION_PROCESSOR.deserialize(jsonR, converter, null);
	}

	/**
	 * Deserialize from JSON/R to the given type
	 *
	 * @param jsonR
	 *            the JSON/R string
	 * @param wantedType
	 *            the wanted type
	 * @param <T>
	 *            the type of the object to return
	 * @return the object
	 * @throws IOException
	 *             on I/O exception
	 */
	public <T> T fromJson(final Reader jsonR,
			final Class<? extends T> wantedType) throws IOException {
		return DESERIALIZATION_PROCESSOR.deserialize(jsonR, converter,
				wantedType);
	}

	/**
	 * Clone an object using JSON/R
	 *
	 * @param object
	 *            the object to clone
	 * @param <T>
	 *            the type of the object to return
	 * @return the cloned object
	 */
	public <T> T clone(final T object) {
		@SuppressWarnings("unchecked")
		final Class<? extends T> wantedType = (Class<? extends T>) object
		.getClass();
		return fromJson(toJson(object), wantedType);
	}

	/**
	 * Transtype an object using JSON/R
	 *
	 * @param object
	 *            the object to transtype
	 * @param wantedType
	 *            the wanted type
	 * @param <T>
	 *            the type of the object to return
	 * @return the cloned object
	 */
	public <T> T transtype(final Object object,
			final Class<? extends T> wantedType) {
		return fromJson(toJson(object), wantedType);
	}

}
