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
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.Prettiness;
import net.cadrian.jsonref.Prettiness.Context;
import net.cadrian.jsonref.Prettiness.Serializer;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

/**
 * The representation of a JSON/R array. Used to serialize arrays and
 * collections; and to deserialize arrays, collections, and maps with non-string
 * keys
 */
public class SerializationArray extends AbstractSerializationObject {

	private final List<SerializationData> array;

	/**
	 * @param capacity
	 *            default capacity of the array
	 * @param type
	 *            type of the array or collection; <code>null</code> if not
	 *            known
	 * @param ref
	 *            reference of the array in the heap
	 */
	public SerializationArray(final int capacity, final Class<?> type,
			final int ref) {
		super(type, ref);
		this.array = new ArrayList<SerializationData>(capacity);
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
		out.append('[');
		context.toJson(out, array, new Serializer<SerializationData>() {
			@Override
			public void toJson(final Writer out, final SerializationData value,
					final Prettiness level) throws IOException {
				value.toJson(out, converter, context);
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
			final JsonConverter.Context converterContext) {
		final T result;
		if (propertyType == null
				|| Collection.class.isAssignableFrom(propertyType)) {
			result = fromJsonCollection(heap, propertyType, converter,
					converterContext);
		} else if (propertyType.isArray()) {
			result = fromJsonArray(heap, propertyType, converter,
					converterContext);
		} else if (Map.class.isAssignableFrom(propertyType)) {
			result = fromJsonMap(heap, propertyType, converter,
					converterContext);
		} else {
			throw new SerializationException("not array compatible");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonArray(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter,
			final JsonConverter.Context converterContext) {
		assert propertyType.isArray() : "not an array";

		final Class<?> componentType = propertyType.getComponentType();
		final int n = array.size();
		final Object result = Array.newInstance(componentType, n);
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (int i = 0; i < n; i++) {
			final AbstractSerializationData data = (AbstractSerializationData) array
					.get(i);
			Array.set(result, i, data.fromJson(heap, componentType, converter,
					converterContext));
		}

		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonCollection(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter,
			final JsonConverter.Context converterContext) {
		assert propertyType == null
				|| Collection.class.isAssignableFrom(propertyType) : "not a collection";

		@SuppressWarnings("rawtypes")
		final Collection<Object> result = (Collection<Object>) converter
				.newCollection((Class<Collection>) propertyType);
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (final SerializationData data : array) {
			result.add(((AbstractSerializationData) data).fromJson(heap, null,
					converter, converterContext));
		}

		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonMap(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter,
			final JsonConverter.Context converterContext) {
		assert Map.class.isAssignableFrom(propertyType) : "not a map";

		@SuppressWarnings("rawtypes")
		final Map<Object, Object> result = (Map<Object, Object>) converter
				.newMap((Class<Map>) propertyType);
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (final SerializationData data : array) {
			final Object entry = ((AbstractSerializationData) data).fromJson(
					heap, null, converter, converterContext);
			if (entry instanceof Collection<?>) {
				final Collection<Object> entrycoll = (Collection<Object>) entry;
				if (entrycoll.size() != 2) {
					throw new SerializationException("Not a map");
				}
				final Iterator<Object> it = entrycoll.iterator();
				result.put(it.next(), it.next());
			} else if (entry.getClass().isArray()) {
				if (Array.getLength(entry) != 2) {
					throw new SerializationException("Not a map");
				}
				result.put(Array.get(entry, 0), Array.get(entry, 1));
			} else {
				throw new SerializationException("Not a map");
			}
		}

		return (T) result;
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
	 * Add an object in the array
	 *
	 * @param data
	 *            the object to add
	 */
	public void add(final SerializationData data) {
		array.add(data);
	}

}
