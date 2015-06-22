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
/**
 * @author E451177
 *
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
	 * @see net.cadrian.jsonref.SerializationData#toJson(java.io.Writer,
	 * net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.Prettiness.Context)
	 */
	@Override
	public void toJson(final Writer out, final JsonConverter converter,
			final Context context) throws IOException {
		final int n = heap.size();
		assert n > 0 : "empty heap?!";
		if (n == 1) {
			heap.get(0).toJson(out, converter, context);
		} else {
			out.append('<');
			context.toJson(out, heap,
					new Serializer<AbstractSerializationObject>() {
				@Override
				public void toJson(final Writer out,
						final AbstractSerializationObject value,
						final Prettiness level) throws IOException {
					value.toJson(out, converter, context);
				}
			});
			out.append('>');
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.cadrian.jsonref.data.AbstractSerializationData#fromJson(java.lang
	 * .Class, net.cadrian.jsonref.JsonConverter,
	 * net.cadrian.jsonref.JsonConverter.Context)
	 */
	@Override
	public <T> T fromJson(final Class<? extends T> wantedType,
			final JsonConverter converter,
			final net.cadrian.jsonref.JsonConverter.Context converterContext) {
		return heap.get(0).fromJson(this, wantedType, converter,
				converterContext);
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
		return fromJson(propertyType, converter, converterContext);
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