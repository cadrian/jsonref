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
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.jsonref.JsonAtomicValues;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

import org.apache.commons.collections4.FactoryUtils;

public class SerializationObject extends AbstractSerializationObject {
	private final Map<String, SerializationData> properties = new HashMap<>();

	public SerializationObject(final Class<?> type, final int ref) {
		super(type, ref);
	}

	/**
	 * Getter values
	 *
	 * @return the values
	 */
	public Map<String, SerializationData> getProperties() {
		return properties;
	}

	/**
	 * @param property
	 * @param value
	 */
	public void add(final String property, final SerializationData value) {
		assert !contains(property);

		properties.put(property, value);
	}

	/**
	 * @param property
	 * @return
	 */
	public boolean contains(final String property) {
		return properties.containsKey(property);
	}

	@Override
	public void toJson(final StringBuilder result,
			final JsonAtomicValues converter) {
		result.append('{');
		String sep = "";
		for (final Map.Entry<String, SerializationData> value : properties
				.entrySet()) {
			result.append(sep);
			result.append(converter.toJson(value.getKey()));
			result.append(':');
			value.getValue().toJson(result, converter);
			sep = ",";
		}
		result.append('}');
	}

	@SuppressWarnings("unchecked")
	@Override
	<T> T fromJson(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		Object result = null;
		if (heap != null) {
			result = heap.getDeser(ref);
		}
		if (result == null) {
			if (Map.class.isAssignableFrom(propertyType)) {
				result = fromJsonMap(heap, converter, propertyType);
			} else {
				result = fromJsonObject(heap, converter, result);
			}
		}
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonMap(final SerializationHeap heap,
			final JsonAtomicValues converter,
			final Class<? extends T> propertyType) {
		assert Map.class.isAssignableFrom(propertyType) : "not a map";

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

		for (final Map.Entry<String, SerializationData> entry : properties
				.entrySet()) {
			final String key = entry.getKey();
			final Object value = ((AbstractSerializationData) entry.getValue())
					.fromJson(heap, converter, null);
			result.put(key, value);
		}
		return (T) result;
	}

	private Object fromJsonObject(final SerializationHeap heap,
			final JsonAtomicValues converter, Object result) {
		try {
			final Class<?> actualType = Class
					.forName(((AbstractSerializationData) properties
							.get("class")).fromJson(heap, converter,
							String.class));
			result = actualType.newInstance();
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
						writer.invoke(result,
								((AbstractSerializationData) properties
										.get(propertyName)).fromJson(heap,
										converter, pd.getPropertyType()));
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