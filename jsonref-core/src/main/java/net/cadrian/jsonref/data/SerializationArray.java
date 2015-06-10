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
import java.util.List;

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
			final JsonAtomicValues converter, final Class<? extends T> clazz) {
		final T result;
		if (clazz.isArray()) {
			result = fromJsonArray(heap, converter, clazz);
		} else if (Collection.class.isAssignableFrom(clazz)) {
			result = fromJsonCollection(heap, converter, clazz);
		} else {
			throw new SerializationException("not array compatible");
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonArray(final SerializationHeap heap,
			final JsonAtomicValues converter, final Class<? extends T> clazz) {
		assert clazz.isArray() : "not an array";

		final Class<?> componentType = clazz.getComponentType();
		final int n = array.size();
		final Object result = Array.newInstance(componentType, n);
		for (int i = 0; i < n; i++) {
			final AbstractSerializationData data = (AbstractSerializationData) array
					.get(i);
			Array.set(result, i, data.fromJson(heap, converter, componentType));
		}
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonCollection(final SerializationHeap heap,
			final JsonAtomicValues converter, final Class<? extends T> clazz) {
		assert Collection.class.isAssignableFrom(clazz) : "not a collection";

		final Collection<Object> result;

		if (clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
			result = (Collection<Object>) FactoryUtils
					.instantiateFactory(clazz).create();
		} else {
			try {
				result = (Collection<Object>) clazz.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		}

		for (final SerializationData data : array) {
			result.add(((AbstractSerializationData) data).fromJson(heap,
					converter, Object.class));
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
