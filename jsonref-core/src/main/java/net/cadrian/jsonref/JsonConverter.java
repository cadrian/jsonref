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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * Customizable JSON/R converter for "atomic" types
 */
public interface JsonConverter {

	public static interface Context {
		// empty by default
	}

	/**
	 * Get a new context to be passed around during the serialization
	 *
	 * @return the new context
	 */
	Context getNewContext();

	/**
	 * Convert the atomic value to a valid JSON string
	 *
	 * @param value
	 *            the value
	 * @return the converted value
	 */
	String toJson(Object value);

	/**
	 * Convert the JSON string to an atomic value
	 *
	 * @param string
	 *            the JSON string
	 * @param propertyType
	 *            the expected class
	 * @param <T>
	 *            the returned object type
	 * @return the object
	 */
	<T> T fromJson(String string, Class<? extends T> propertyType);

	/**
	 * Is the type considered a type of atomic values?
	 *
	 * @param propertyType
	 *            the type to check
	 * @return <code>true</code> if the type is of atomic values,
	 *         <code>false</code> otherwise
	 */
	boolean isAtomicValue(Class<?> propertyType);

	/**
	 * Create a new collection of the most appropriate type.
	 *
	 * @param wantedType
	 *            the collection's required type
	 * @return the new collection
	 */
	Collection<?> newCollection(
			@SuppressWarnings("rawtypes") Class<Collection> wantedType);

	/**
	 * Create a new map of the most appropriate type.
	 *
	 * @param wantedType
	 *            the map's required type
	 * @return the new map
	 */
	Map<?, ?> newMap(@SuppressWarnings("rawtypes") Class<Map> wantedType);

	/**
	 * Transient fields are not serialized.
	 *
	 * @param pd
	 *            the property descriptor
	 * @param propertyField
	 *            the field to check
	 * @param context
	 *            the serialization context
	 * @return <code>true</code> if the field is transient (i.e. not to be
	 *         serialized), <code>false</code> otherwise
	 */
	boolean isTransient(PropertyDescriptor pd, Field propertyField,
			Context context);

	/**
	 * Get the value of the property
	 *
	 * @param pd
	 *            the property descriptor
	 * @param object
	 *            the object to get the value from
	 * @param context
	 *            the serialization context
	 * @return the value
	 */
	Object getPropertyValue(PropertyDescriptor pd, Object object,
			Context context);

	/**
	 * Set the value of the property
	 *
	 * @param pd
	 *            the property descriptor
	 * @param object
	 *            the object to set the value to
	 * @param value
	 *            the value to set
	 * @param context
	 *            the serialization context
	 */
	void setPropertyValue(PropertyDescriptor pd, Object object, Object value,
			Context context);

	/**
	 * Get the type of the value of the property
	 *
	 * @param pd
	 *            the property descriptor
	 * @param propertyField
	 *            the property actual field
	 * @param context
	 *            the serialization context
	 * @return the type of the property
	 */
	Class<?> getPropertyType(PropertyDescriptor pd, Field propertyField,
			Context context);

	/**
	 * Start de/serializing given property
	 *
	 * @param pd
	 *            the property descriptor
	 * @param propertyField
	 *            the property actual field
	 * @param object
	 *            the property holder
	 * @param value
	 *            the valued returned by
	 *            {@link #getPropertyValue(PropertyDescriptor, Object, Context)}
	 *            (for serialization), or <code>null</code> (for
	 *            deserialization)
	 * @param context
	 *            the serialization context
	 */
	void nestIn(PropertyDescriptor pd, Field propertyField, Object object,
			Object value, net.cadrian.jsonref.JsonConverter.Context context);

	/**
	 * Finishes de/serializing given property
	 *
	 * @param pd
	 *            the property descriptor
	 * @param propertyField
	 *            the property actual field
	 * @param object
	 *            the property holder
	 * @param value
	 *            the valued returned by
	 *            {@link #getPropertyValue(PropertyDescriptor, Object, Context)}
	 *            (for deserialization), or build using
	 *            {@link #fromJson(String, Class)} (for deserialization)
	 * @param context
	 *            the serialization context
	 */
	void nestOut(PropertyDescriptor pd, Field propertyField, Object object,
			Object value, net.cadrian.jsonref.JsonConverter.Context context);

}
