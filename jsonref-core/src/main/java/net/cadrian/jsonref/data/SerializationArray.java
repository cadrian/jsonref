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

import java.lang.reflect.Array;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.cadrian.jsonref.JsonAtomicValues;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

import org.apache.commons.collections4.FactoryUtils;

public class SerializationArray extends AbstractSerializationObject {

	private final List<SerializationData> array;

	public SerializationArray(final int capacity, final Class<?> type,
			final int ref) {
		super(type, ref);
		this.array = new ArrayList<SerializationData>(capacity);
	}

	@Override
	public void toJson(final StringBuilder result,
			final JsonAtomicValues converter) {
		result.append('[');
		String sep = "";
		for (final SerializationData data : array) {
			result.append(sep);
			data.toJson(result, converter);
			sep = ",";
		}
		result.append(']');
	}

	@Override
	<T> T fromJson(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		final T result;
		if (propertyType.isArray()) {
			result = fromJsonArray(heap, converter, propertyType);
		} else if (Collection.class.isAssignableFrom(propertyType)) {
			result = fromJsonCollection(heap, converter, propertyType);
		} else if (Map.class.isAssignableFrom(propertyType)) {
			result = fromJsonMap(heap, converter, propertyType);
		} else {
			throw new SerializationException("not array compatible");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonArray(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
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
			Array.set(result, i, data.fromJson(heap, converter, componentType));
		}

		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonCollection(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		assert Collection.class.isAssignableFrom(propertyType) : "not a collection";

		final Collection<Object> result;

		if (propertyType.isInterface()
				|| Modifier.isAbstract(propertyType.getModifiers())) {
			result = (Collection<Object>) FactoryUtils.instantiateFactory(
					propertyType).create();
		} else {
			try {
				result = (Collection<Object>) propertyType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		}
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (final SerializationData data : array) {
			result.add(((AbstractSerializationData) data).fromJson(heap,
					converter, null));
		}

		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonMap(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		assert Collection.class.isAssignableFrom(propertyType) : "not a collection";

		final Map<Object, Object> result;

		if (propertyType.isInterface()
				|| Modifier.isAbstract(propertyType.getModifiers())) {
			result = (Map<Object, Object>) FactoryUtils.instantiateFactory(
					propertyType).create();
		} else {
			try {
				result = (Map<Object, Object>) propertyType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		}
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (final SerializationData data : array) {
			final Object entry = ((AbstractSerializationData) data).fromJson(
					heap, converter, null);
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

	/**
	 * @return the type
	 */
	@Override
	public Class<?> getType() {
		return type;
	}

	public void add(final SerializationData data) {
		array.add(data);
	}

}
