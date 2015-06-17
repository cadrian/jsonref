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
	 * @see
	 * net.cadrian.jsonref.SerializationData#toJson(java.lang.StringBuilder,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.Prettiness.Context)
	 */
	@Override
	public void toJson(final StringBuilder result,
			final JsonConverter converter, final Context context) {
		if (isMapOfStrings) {
			toJsonMap(result, converter, context);
		} else {
			toJsonArray(result, converter, context);
		}
	}

	private void toJsonMap(final StringBuilder result,
			final JsonConverter converter, final Context context) {
		result.append('{');
		context.toJson(
				result,
				map.entrySet(),
				new Serializer<Map.Entry<SerializationData, SerializationData>>() {
					@Override
					public void toJson(
							final StringBuilder result,
							final Entry<SerializationData, SerializationData> value,
							final Prettiness level) {
						value.getKey().toJson(result, converter, context);
						result.append(':');
						if (context.getPrettiness() != Prettiness.COMPACT) {
							result.append(' ');
						}
						value.getValue().toJson(result, converter, context);
					}
				});
		result.append('}');
	}

	private void toJsonArray(final StringBuilder result,
			final JsonConverter converter, final Context context) {
		result.append('[');
		context.toJson(
				result,
				map.entrySet(),
				new Serializer<Map.Entry<SerializationData, SerializationData>>() {
					@Override
					public void toJson(
							final StringBuilder result,
							final Entry<SerializationData, SerializationData> value,
							final Prettiness level) {
						result.append('[');
						context.toJson(
								result,
								Arrays.asList(value.getKey(), value.getValue()),
								new Serializer<SerializationData>() {
									@Override
									public void toJson(
											final StringBuilder result,
											final SerializationData value,
											final Prettiness level) {
										value.toJson(result, converter, context);
									}
								});
						result.append(']');
					}
				});
		result.append(']');
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
