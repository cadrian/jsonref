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
import net.cadrian.jsonref.Prettiness;
import net.cadrian.jsonref.Prettiness.Context;
import net.cadrian.jsonref.Prettiness.Serializer;

/**
 * The heap represents a list of objects known by their reference (which is
 * their index into the list)
 */
public class SerializationHeap extends AbstractSerializationData {

	private final List<AbstractSerializationObject> heap = new ArrayList<>();
	private List<Object> deser;

	/**
	 * Get an object by its reference
	 *
	 * @param ref
	 *            the object reference
	 * @return the object
	 */
	public AbstractSerializationObject get(final int ref) {
		return heap.get(ref);
	}

	/**
	 * Add an object into the heap. Its reference must match the expected
	 * {@linkplain #nextRef() next reference}.
	 *
	 * @param object
	 *            the object to add
	 */
	public void add(final AbstractSerializationObject object) {
		assert object.getRef() == nextRef() : "wrong ref " + object.getRef()
				+ " != " + nextRef();
		heap.add(object);
	}

	/**
	 * Get the reference of the next object to
	 * {@linkplain #add(AbstractSerializationObject) add}
	 *
	 * @return the expected next reference
	 */
	public int nextRef() {
		return heap.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.cadrian.jsonref.SerializationData#toJson(java.lang.StringBuilder,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.Prettiness.Context)
	 */
	@Override
	public void toJson(final StringBuilder result,
			final JsonConverter converter, final Context context) {
		final int n = heap.size();
		assert n > 0 : "empty heap?!";
		if (n == 1) {
			heap.get(0).toJson(result, converter, context);
		} else {
			result.append('<');
			context.toJson(result, heap,
					new Serializer<AbstractSerializationObject>() {
				@Override
				public void toJson(final StringBuilder result,
						final AbstractSerializationObject value,
						final Prettiness level) {
					value.toJson(result, converter, context);
				}
			});
			result.append('>');
		}
	}

	@Override
	public <T> T fromJson(final Class<? extends T> wantedType,
			final JsonConverter converter) {
		return heap.get(0).fromJson(this, wantedType, converter);
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