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

import net.cadrian.jsonref.JsonAtomicValues;
import net.cadrian.jsonref.SerializationData;

public class SerializationObject extends AbstractSerializationData {
	private final Class<?> type;
	private final int ref;
	private final Map<String, SerializationData> properties = new HashMap<>();

	public SerializationObject(final Class<?> type, final int id) {
		this.type = type;
		this.ref = id;
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
	 * Getter id
	 *
	 * @return the id
	 */
	public int getRef() {
		return ref;
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
	public void toJson(final StringBuilder result, final JsonAtomicValues converter) {
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
	<T> T fromJson(final SerializationHeap heap, final JsonAtomicValues converter,
			final Class<? extends T> clazz) {
		Object result = null;
		if (heap != null) {
			result = heap.getDeser(ref);
		}
		if (result == null) {
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
				final PropertyDescriptor[] pds = beanInfo
						.getPropertyDescriptors();

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
				throw new RuntimeException("fromJson", e);
			}
		}
		return (T) result;
	}

}