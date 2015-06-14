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
package net.cadrian.jsonref;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.cadrian.jsonref.Prettiness.Context;
import net.cadrian.jsonref.data.SerializationArray;
import net.cadrian.jsonref.data.SerializationHeap;
import net.cadrian.jsonref.data.SerializationMap;
import net.cadrian.jsonref.data.SerializationObject;
import net.cadrian.jsonref.data.SerializationRef;
import net.cadrian.jsonref.data.SerializationValue;

class SerializationProcessor {

	private static class ObjectReference {
		private final Object object;
		private final int id;

		/**
		 * @param object
		 * @param generator
		 */
		public ObjectReference(final Object object, final int id) {
			this.object = object;
			this.id = id;
		}

		/**
		 * Getter object
		 *
		 * @return the object
		 */
		public Object getObject() {
			return object;
		}

		/**
		 * Getter id
		 *
		 * @return the id
		 */
		public int getId() {
			return id;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			boolean result = false;
			if (obj instanceof ObjectReference) {
				result = object == ((ObjectReference) obj).object;
			}
			return result;
		}

		/**
		 * {@inheritDoc}
		 *
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			return object.hashCode();
		}
	}

	/**
	 * Serialize an object graph to JSON/R
	 *
	 * @param value
	 *            the object to serialize
	 * @param converter
	 *            the converter
	 * @param context
	 *            the prettiness level
	 * @return the serialized object
	 */
	public String serialize(final Object value, final JsonConverter converter,
			Context context) {
		if (value == null) {
			return "null";
		}
		if (context == null) {
			context = Prettiness.COMPACT.newContext();
		}

		final StringBuilder result = new StringBuilder();
		final Map<ObjectReference, ObjectReference> refs = new HashMap<>();

		final SerializationData data = getData(null, refs, value,
				value.getClass(), converter);
		if (data != null) {
			data.toJson(result, converter, context);
		} else {
			final SerializationHeap heap = new SerializationHeap();
			getData(heap, refs, value, value.getClass(), converter);
			heap.toJson(result, converter, context);
		}
		return result.toString();
	}

	private SerializationData getData(final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final Object value, final Class<?> propertyType,
			final JsonConverter converter) {
		final SerializationData data;
		if (value == null) {
			data = new SerializationValue(propertyType, null);
		} else if (converter.isAtomicValue(value.getClass())) {
			data = new SerializationValue(value.getClass(), value);
		} else if (propertyType == Class.class) {
			data = new SerializationValue(propertyType,
					((Class<?>) value).getName());
		} else if (heap != null) {
			data = getHeapData(value, propertyType, heap, refs, converter);
		} else {
			data = null;
		}
		return data;
	}

	private SerializationData getHeapData(final Object value,
			final Class<?> propertyType, final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final JsonConverter converter) {
		final SerializationData result;
		final ObjectReference ref = refs.get(new ObjectReference(value, 0));
		if (ref != null) {
			result = new SerializationRef(ref.getId());
		} else if (propertyType.isArray()) {
			result = serializeArray(new ObjectReference(value, heap.nextRef()),
					propertyType, heap, refs, converter);
		} else if (Collection.class.isAssignableFrom(propertyType)) {
			result = serializeCollection(
					new ObjectReference(value, heap.nextRef()), propertyType,
					heap, refs, converter);
		} else if (Map.class.isAssignableFrom(propertyType)) {
			result = serializeMap(new ObjectReference(value, heap.nextRef()),
					propertyType, heap, refs, converter);
		} else {
			final int objectId = serializeObject(new ObjectReference(value,
					heap.nextRef()), heap, refs, converter);
			result = new SerializationRef(objectId);
		}
		return result;
	}

	private SerializationArray serializeArray(final ObjectReference ref,
			final Class<?> propertyType, final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final JsonConverter converter) {
		final Object array = ref.getObject();
		final int n = Array.getLength(array);
		final Class<?> componentType = propertyType.getComponentType();
		final SerializationArray result = new SerializationArray(n,
				propertyType, ref.getId());
		heap.add(result);
		refs.put(ref, ref);

		for (int i = 0; i < n; i++) {
			result.add(getData(heap, refs, Array.get(array, i), componentType,
					converter));
		}
		return result;
	}

	private SerializationArray serializeCollection(final ObjectReference ref,
			final Class<?> propertyType, final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final JsonConverter converter) {

		@SuppressWarnings("unchecked")
		final Collection<Object> array = (Collection<Object>) ref.getObject();
		final SerializationArray result = new SerializationArray(array.size(),
				propertyType, ref.getId());
		heap.add(result);
		refs.put(ref, ref);

		for (final Object object : array) {
			result.add(getData(heap, refs, object, Object.class, converter));
		}
		return result;
	}

	private SerializationMap serializeMap(final ObjectReference ref,
			final Class<?> propertyType, final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final JsonConverter converter) {

		@SuppressWarnings("unchecked")
		final Map<Object, Object> map = (Map<Object, Object>) ref.getObject();
		final SerializationMap result = new SerializationMap(map.size(),
				propertyType, ref.getId());
		heap.add(result);
		refs.put(ref, ref);

		for (final Map.Entry<Object, Object> entry : map.entrySet()) {
			final Object key = entry.getKey();
			final Object value = entry.getValue();
			result.add(getData(heap, refs, key, Object.class, converter),
					getData(heap, refs, value, Object.class, converter));
		}
		return result;
	}

	private int serializeObject(final ObjectReference ref,
			final SerializationHeap heap,
			final Map<ObjectReference, ObjectReference> refs,
			final JsonConverter converter) {
		assert ref.getObject() != null : "null object?!";
		assert !refs.containsKey(ref) : "duplicated ref " + ref.getId();

		final Object object = ref.getObject();
		final Class<?> type = object.getClass();

		final SerializationObject result = new SerializationObject(type,
				ref.getId());
		heap.add(result);
		refs.put(ref, ref);

		result.add("class", getData(heap, refs, type, Class.class, converter));
		try {
			final BeanInfo beanInfo = Introspector.getBeanInfo(type);
			final PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();

			for (final PropertyDescriptor pd : pds) {
				if (!converter.isTransient(getField(type, pd.getName()))) {
					final Method reader = pd.getReadMethod();
					if (reader != null) {
						final SerializationData data;

						final Object value = reader.invoke(object);
						final Class<?> propertyType = reader.getReturnType();
						data = getData(heap, refs, value, propertyType,
								converter);

						if (data != null) {
							result.add(pd.getName(), data);
						}
					}
				}
			}
		} catch (final IntrospectionException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new SerializationException(e);
		}

		return result.getRef();
	}

	private Field getField(Class<?> type, final String name) {
		do {
			try {
				return type.getDeclaredField(name);
			} catch (final NoSuchFieldException e) {
				type = type.getSuperclass();
			} catch (final SecurityException e) {
				throw new SerializationException(
						"Security error while looking for field "
								+ type.getName() + "." + name, e);
			}
		} while (type != Object.class);
		return null;
	}

}
