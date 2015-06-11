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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

public class SerializationObject extends AbstractSerializationObject {
	private final Map<String, AbstractSerializationData> properties = new HashMap<>();

	public SerializationObject(final Class<?> type, final int ref) {
		super(type, ref);
	}

	/**
	 * @param property
	 * @param value
	 */
	public void add(final String property, final SerializationData value) {
		assert !contains(property);

		properties.put(property, (AbstractSerializationData) value);
	}

	/**
	 * @param property
	 * @return
	 */
	public boolean contains(final String property) {
		return properties.containsKey(property);
	}

	@Override
	public void toJson(final StringBuilder result, final JsonConverter converter) {
		result.append('{');
		String sep = "";
		for (final Map.Entry<String, AbstractSerializationData> value : properties
				.entrySet()) {
			result.append(sep);
			result.append(converter.toJson(value.getKey()));
			result.append(':');
			value.getValue().toJson(result, converter);
			sep = ",";
		}
		result.append('}');
	}

	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType, final JsonConverter converter) {
		T result = null;
		if (heap != null) {
			@SuppressWarnings("unchecked")
			final T deser = (T) heap.getDeser(ref);
			result = deser;
		}
		if (result == null) {
			if (propertyType != null
					&& Map.class.isAssignableFrom(propertyType)) {
				result = fromJsonMap(heap, propertyType, converter);
			} else {
				result = fromJsonObject(heap, propertyType, converter);
			}
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonMap(final SerializationHeap heap,
			final Class<? extends T> propertyType, final JsonConverter converter) {
		assert Map.class.isAssignableFrom(propertyType) : "not a map";

		final Map<Object, Object> result;

		result = converter
				.newMap((Class<? extends Map<Object, Object>>) propertyType);
		if (heap != null) {
			heap.setDeser(ref, result);
		}

		for (final Map.Entry<String, AbstractSerializationData> entry : properties
				.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue().fromJson(heap, null,
					converter);
			result.put(key, value);
		}
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonObject(final SerializationHeap heap,
			final Class<? extends T> propertyType, final JsonConverter converter) {
		final T result;

		try {
			final Class<?> actualType;
			if (propertyType != null) {
				actualType = propertyType;
			} else {
				final AbstractSerializationData classProperty = properties
						.get("class");
				final String className = classProperty.fromJson(heap,
						String.class, converter);
				actualType = Class.forName(className);
			}

			result = (T) actualType.newInstance();
			if (heap != null) {
				heap.setDeser(ref, result);
			}

			final BeanInfo beanInfo = Introspector.getBeanInfo(actualType);
			final PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

			for (final PropertyDescriptor pd : pds) {
				final String propertyName = pd.getName();
				if (properties.containsKey(propertyName)) {
					final Method writer = pd.getWriteMethod();
					if (writer != null) {
						writer.invoke(
								result,
								properties.get(propertyName).fromJson(heap,
										pd.getPropertyType(), converter));
					}
				}
			}
		} catch (final ClassNotFoundException | InstantiationException
				| IllegalAccessException | IntrospectionException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new SerializationException(e);
		}

		return result;
	}

}