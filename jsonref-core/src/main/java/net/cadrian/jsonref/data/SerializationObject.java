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
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.Prettiness.Context;
import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

/**
 * A JSON/R object
 */
public class SerializationObject extends AbstractSerializationObject {

	private final Map<String, AbstractSerializationData> properties = new LinkedHashMap<>();

	/**
	 * @param type
	 *            the type of the object
	 * @param ref
	 *            the reference of the object in the heap
	 */
	public SerializationObject(final Class<?> type, final int ref) {
		super(type, ref);
	}

	/**
	 * Add a property to the object
	 *
	 * @param property
	 *            the property name
	 * @param value
	 *            the property value
	 */
	public void add(final String property, final SerializationData value) {
		assert !contains(property);

		properties.put(property, (AbstractSerializationData) value);
	}

	/**
	 * @param property
	 *            the property to look for
	 * @return <code>true</code> if the object has the given property,
	 *         <code>false</code> otherwise
	 */
	public boolean contains(final String property) {
		return properties.containsKey(property);
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
		out.append('{');
		String sep = "";
		for (final Map.Entry<String, AbstractSerializationData> value : properties
				.entrySet()) {
			out.append(sep);
			out.append(converter.toJson(value.getKey()));
			out.append(':');
			value.getValue().toJson(out, converter, context);
			sep = ",";
		}
		out.append('}');
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
		T result = null;
		if (heap != null) {
			@SuppressWarnings("unchecked")
			final T deser = (T) heap.getDeser(ref);
			result = deser;
		}
		if (result == null) {
			if (propertyType != null
					&& Map.class.isAssignableFrom(propertyType)) {
				result = fromJsonMap(heap, propertyType, converter,
						converterContext);
			} else {
				result = fromJsonObject(heap, propertyType, converter,
						converterContext);
			}
		}
		return result;
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

		for (final Map.Entry<String, AbstractSerializationData> entry : properties
				.entrySet()) {
			final String key = entry.getKey();
			final Object value = entry.getValue().fromJson(heap, null,
					converter, converterContext);
			result.put(key, value);
		}
		return (T) result;
	}

	@SuppressWarnings("unchecked")
	private <T> T fromJsonObject(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter,
			final JsonConverter.Context converterContext) {
		final T result;

		try {
			final Class<?> actualType;
			if (propertyType != null) {
				actualType = propertyType;
			} else {
				final AbstractSerializationData classProperty = properties
						.get("class");
				final String className = classProperty.fromJson(heap,
						String.class, converter, converterContext);
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
					final Field propertyField = getField(propertyName,
							propertyType);
					if (!converter.isTransient(pd, propertyField,
							converterContext)) {
						converter.nestIn(pd, propertyField, result, null,
								converterContext);
						final Object value = properties.get(propertyName)
								.fromJson(heap, pd.getPropertyType(),
										converter, converterContext);
						converter.setPropertyValue(pd, propertyField, result,
								value, converterContext);
						converter.nestOut(pd, propertyField, result, value,
								converterContext);
					}
				}
			}
		} catch (final ClassNotFoundException | InstantiationException
				| IllegalAccessException | IntrospectionException e) {
			throw new SerializationException(e);
		}

		return result;
	}

}