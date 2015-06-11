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

import net.cadrian.jsonref.JsonConverter;

public class SerializationRef extends AbstractSerializationData {
	private final int ref;

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

	@Override
	public void toJson(final StringBuilder result,
			final JsonConverter converter) {
		result.append('$').append(ref);
	}

	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType, JsonConverter converter) {
		assert heap != null : "no heap for reference?!";

		return heap.get(ref).fromJson(heap, propertyType, converter);
	}

}