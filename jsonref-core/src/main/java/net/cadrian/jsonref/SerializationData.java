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

import java.io.IOException;
import java.io.Writer;

import net.cadrian.jsonref.Prettiness.Context;

/**
 * Serialization data is used as an intermediate object graph for both
 * serialization and deserialization. It exposes helper methods for both
 * serialization and deserializatin.
 */
public interface SerializationData {

	/**
	 * Serialization
	 *
	 * @param out
	 *            the JSON/R stream to append to
	 * @param converter
	 *            the converter
	 * @param context
	 *            the prettiness context
	 * @throws IOException
	 *             on I/O exception
	 */
	void toJson(Writer out, JsonConverter converter, Context context)
			throws IOException;

	/**
	 * Deserialization
	 *
	 * @param wantedType
	 *            the type of the object to deserialize
	 * @param converter
	 *            the converter
	 * @param <T>
	 *            the type of the object to return
	 * @return the actual deserialized object
	 */
	<T> T fromJson(Class<? extends T> wantedType, JsonConverter converter);
}