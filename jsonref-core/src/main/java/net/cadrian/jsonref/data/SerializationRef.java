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

import java.io.IOException;
import java.io.Writer;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.Prettiness.Context;

/**
 * A reference in the heap
 */
public class SerializationRef extends AbstractSerializationData {

	private final int ref;

	/**
	 * @param ref
	 *            the reference of the object
	 */
	public SerializationRef(final int ref) {
		this.ref = ref;
	}

	/**
	 * Getter ref
	 *
	 * @return the ref
	 */
	public int getRef() {
		return ref;
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
		out.append('$').append(Integer.toString(ref));
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see
	 * net.cadrian.jsonref.data.AbstractSerializationData#fromJson(net.cadrian
	 * .jsonref.data.SerializationHeap, java.lang.Class,
	 * net.cadrian.jsonref.JsonConverter)
	 */
	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType, final JsonConverter converter) {
		assert heap != null : "no heap for reference?!";

		return heap.get(ref).fromJson(heap, propertyType, converter);
	}

}