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

import java.util.ArrayList;
import java.util.List;

import net.cadrian.jsonref.JsonConverter;

public class SerializationHeap extends AbstractSerializationData {
	private final List<AbstractSerializationObject> heap = new ArrayList<>();
	private List<Object> deser;

	public AbstractSerializationObject get(final int ref) {
		return heap.get(ref);
	}

	public void add(final AbstractSerializationObject object) {
		assert object.getRef() == nextRef() : "wrong ref " + object.getRef()
				+ " != " + nextRef();
		heap.add(object);
	}

	public int nextRef() {
		return heap.size();
	}

	@Override
	public void toJson(final StringBuilder result,
			final JsonConverter converter) {
		final int n = heap.size();
		assert n > 0 : "empty heap?!";
		if (n == 1) {
			heap.get(0).toJson(result, converter);
		} else {
			result.append('<');
			for (int i = 0; i < n; i++) {
				final AbstractSerializationObject ref = heap.get(i);
				assert ref.getRef() == i : "wrong ref " + ref.getRef() + " != "
						+ i;
				if (i > 0) {
					result.append(",");
				}
				ref.toJson(result, converter);
			}
			result.append('>');
		}
	}

	@Override
	public <T> T fromJson(final Class<? extends T> wantedType,
			final JsonConverter converter) {
		return heap.get(0).fromJson(this, wantedType, converter);
	}

	@Override
	<T> T fromJson(final SerializationHeap heap,
			final Class<? extends T> propertyType,
			final JsonConverter converter) {
		return fromJson(propertyType, converter);
	}

	void setDeser(final int ref, final Object d) {
		if (deser == null) {
			deser = new ArrayList<Object>(ref + 1);
		} else {
			((ArrayList<Object>) deser).ensureCapacity(ref + 1);
		}
		while (deser.size() <= ref) {
			deser.add(null);
		}
		deser.set(ref, d);
	}

	Object getDeser(final int ref) {
		Object result = null;
		if (deser != null && ref < deser.size()) {
			result = deser.get(ref);
		}
		return result;
	}

}