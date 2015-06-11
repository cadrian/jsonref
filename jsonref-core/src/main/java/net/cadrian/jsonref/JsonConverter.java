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

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

/**
 * Basic JSON converter, for "atomic" types
 */
public interface JsonConverter {

	/**
	 * Convert the value to a valid JSON string
	 *
	 * @param value
	 *            the value
	 * @return the converted value
	 */
	String toJson(Object value);

	/**
	 * Convert the JSON string to an object
	 *
	 * @param string
	 *            the JSON string
	 * @param propertyType
	 *            the expected class
	 * @return the object
	 */
	<T> T fromJson(String string, Class<? extends T> propertyType);

	/**
	 * Is the type an atomic value?
	 *
	 * @param propertyType
	 *            the type to check
	 * @return <code>true</code> if the type is an atomic value,
	 *         <code>false</code> otherwise
	 */
	boolean isAtomicValue(Class<?> propertyType);

	/**
	 * Create a new collection of the most appropriate type.
	 *
	 * @param wantedType
	 * @return
	 */
	Collection<?> newCollection(
			@SuppressWarnings("rawtypes") Class<Collection> wantedType);

	/**
	 * Create a new map of the most appropriate type.
	 *
	 * @param wantedType
	 * @return
	 */
	Map<?, ?> newMap(@SuppressWarnings("rawtypes") Class<Map> wantedType);

	/**
	 * Transient fields are not serialized.
	 *
	 * @param field
	 * @return
	 */
	boolean isTransient(Field field);

}
