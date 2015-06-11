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

import net.cadrian.jsonref.atomic.DefaultJsonAtomicValues;

/**
 * JSON/R serialization
 */
public class JsonSerializer {

	private static final SerializationProcessor SERIALIZATION_PROCESSOR = new SerializationProcessor();
	private static final DeserializationProcessor DESERIALIZATION_PROCESSOR = new DeserializationProcessor();

	private final JsonAtomicValues converter;

	/**
	 * Default constructor with default converter
	 */
	public JsonSerializer() {
		this(null);
	}

	/**
	 * Constructor with converter
	 *
	 * @param converter
	 *            the converter
	 */
	public JsonSerializer(final JsonAtomicValues converter) {
		if (converter == null) {
			this.converter = new DefaultJsonAtomicValues();
		} else {
			this.converter = converter;
		}
	}

	/**
	 * Serialize to JSON/R
	 *
	 * @param object
	 *            the object to serialize
	 * @return the JSON/R string
	 */
	public String toJson(final Object object) {
		return SERIALIZATION_PROCESSOR.serialize(object, converter);
	}

	/**
	 * Deserialize from JSON/R
	 *
	 * @param jsonR
	 *            the JSON/R string
	 * @return the object
	 */
	public Object fromJson(final String jsonR) {
		return DESERIALIZATION_PROCESSOR.deserialize(jsonR, converter);
	}

	/**
	 * Clone an object using JSON/R
	 *
	 * @param object
	 *            the object to clone
	 * @return the cloned object
	 */
	@SuppressWarnings("unchecked")
	public <T> T clone(final T object) {
		return (T) fromJson(toJson(object));
	}

}
