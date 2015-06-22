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

import java.lang.reflect.Field;

import net.cadrian.jsonref.SerializationData;
import net.cadrian.jsonref.SerializationException;

/**
 * Any kind of {@link SerializationData} that can be referenced by a
 * {@linkplain SerializationHeap heap}
 */
public abstract class AbstractSerializationObject extends
		AbstractSerializationData {

	final Class<?> type;
	final int ref;

	AbstractSerializationObject(final Class<?> type, final int ref) {
		this.type = type;
		this.ref = ref;
	}

	/**
	 * Find the field with the given name and type
	 *
	 * @param name
	 *            the name of the field
	 * @param type
	 *            the type of the field
	 *
	 * @return the field, or <code>null</code> if not found
	 */
	public static Field getField(final String name, final Class<?> type) {
		Class<?> actualType = type;
		do {
			try {
				return actualType.getDeclaredField(name);
			} catch (final NoSuchFieldException e) {
				actualType = actualType.getSuperclass();
			} catch (final SecurityException e) {
				throw new SerializationException(
						"Security error while looking for field "
								+ actualType.getName() + "." + name, e);
			}
		} while (actualType != Object.class);
		return null;
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
	 * Getter ref
	 *
	 * @return the ref
	 */
	public int getRef() {
		return ref;
	}

}