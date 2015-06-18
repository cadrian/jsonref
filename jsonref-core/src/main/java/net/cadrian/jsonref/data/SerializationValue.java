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
package net.cadrian.jsonref.data;

import java.io.IOException;
import java.io.Writer;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.Prettiness.Context;

/**
 * An atomic JSON/R value
 */
public class SerializationValue extends AbstractSerializationData {

	private final Class<?> type;
	private final Object value;
	private final String string;

	/**
	 * Constructor for serialization
	 *
	 * @param type
	 *            the value type
	 * @param value
	 *            the value
	 */
	public SerializationValue(final Class<?> type, final Object value) {
		this.type = type;
		this.value = value;
		this.string = null;
	}

	/**
	 * Constructor for deserialization: the value is always a string, type is
	 * not yet known
	 *
	 * @param value
	 *            the value
	 */
	public SerializationValue(final String value) {
		this.type = null;
		this.value = this.string = value;
	}

	/**
	 * Getter type
	 *
	 * @return the type
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Getter value
	 *
	 * @return the value
	 */
	public Object getValue() {
		return value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.SerializationData#toJson(java.io.Writer,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.Prettiness.Context)
	 */
	@Override
	public void toJson(final Writer out, final JsonConverter converter,
			final Context context) throws IOException {
		out.append(converter.toJson(value));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.cadrian.jsonref.data.AbstractSerializationData#fromJson(java.lang
	 * .Class, net.cadrian.jsonref.JsonConverter)
	 */
	@Override
	public <T> T fromJson(final Class<? extends T> wantedType,
			final JsonConverter converter) {
		return converter.fromJson(string, wantedType);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.cadrian.jsonref.data.AbstractSerializationData#fromJson(net.cadrian
	 * .jsonref.data.SerializationHeap, java.lang.Class,
	 * net.cadrian.jsonref.JsonConverter)
	 */
	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType, final JsonConverter converter) {
		return fromJson(propertyType, converter);
	}

}