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
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.Prettiness;
import net.cadrian.jsonref.Prettiness.Context;
import net.cadrian.jsonref.Prettiness.Serializer;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

/**
 * A JSON/R map
 */
public class SerializationMap extends AbstractSerializationObject {

	private final Map<SerializationData, SerializationData> map;
	private boolean isMapOfStrings = true;

	/**
	 * @param capacity
	 *            the default capacity of the map
	 * @param type
	 *            the map's type
	 * @param ref
	 *            reference of the map in the heap
	 */
	public SerializationMap(final int capacity, final Class<?> type,
			final int ref) {
		super(type, ref);
		this.map = new HashMap<SerializationData, SerializationData>(capacity);
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
		if (isMapOfStrings) {
			toJsonMap(out, converter, context);
		} else {
			toJsonArray(out, converter, context);
		}
	}

	private void toJsonMap(final Writer out, final JsonConverter converter,
			final Context context) throws IOException {
		out.append('{');
		context.toJson(
				out,
				map.entrySet(),
				new Serializer<Map.Entry<SerializationData, SerializationData>>() {
					@Override
					public void toJson(
							final Writer out,
							final Entry<SerializationData, SerializationData> value,
							final Prettiness level) throws IOException {
						value.getKey().toJson(out, converter, context);
						out.append(':');
						if (context.getPrettiness() != Prettiness.COMPACT) {
							out.append(' ');
						}
						value.getValue().toJson(out, converter, context);
					}
				});
		out.append('}');
	}

	private void toJsonArray(final Writer out, final JsonConverter converter,
			final Context context) throws IOException {
		out.append('[');
		context.toJson(
				out,
				map.entrySet(),
				new Serializer<Map.Entry<SerializationData, SerializationData>>() {
					@Override
					public void toJson(
							final Writer out,
							final Entry<SerializationData, SerializationData> value,
							final Prettiness level) throws IOException {
						out.append('[');
						context.toJson(
								out,
								Arrays.asList(value.getKey(), value.getValue()),
								new Serializer<SerializationData>() {
									@Override
									public void toJson(final Writer out,
											final SerializationData value,
											final Prettiness level)
											throws IOException {
										value.toJson(out, converter, context);
									}
								});
						out.append(']');
					}
				});
		out.append(']');
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.cadrian.jsonref.data.AbstractSerializationData#fromJson(net.cadrian
	 * .jsonref.data.SerializationHeap, java.lang.Class,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.JsonConverter.Context)
	 */
	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter,
			final net.cadrian.jsonref.JsonConverter.Context converterContext) {
		throw new SerializationException(
				"SerializationMap not used for deserialization");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.data.AbstractSerializationObject#getType()
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	/**
	 * Add an object into the map
	 *
	 * @param key
	 *            the object key
	 * @param value
	 *            the object value
	 */
	public void add(final SerializationData key, final SerializationData value) {
		map.put(key, value);
		if (key instanceof SerializationValue) {
			final Object keyval = ((SerializationValue) key).getValue();
			if (keyval != null && keyval.getClass() != String.class) {
				isMapOfStrings = false;
			}
		} else {
			isMapOfStrings = false;
		}
	}

}
