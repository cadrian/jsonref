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
package net.cadrian.jsonref.atomic;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import net.cadrian.jsonref.JsonConverter;
import net.cadrian.jsonref.SerializationException;

/**
 * The default JSON/R converter
 */
@SuppressWarnings("rawtypes")
public class DefaultJsonConverter implements JsonConverter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.JsonConverter#toJson(java.lang.Object)
	 */
	@Override
	public String toJson(final Object value) {
		if (value == null) {
			return "null";
		}
		assert isAtomicValue(value.getClass());
		return AtomicValue.get(value.getClass()).toJson(value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.JsonConverter#fromJson(java.lang.String,
	 * java.lang.Class)
	 */
	@Override
	public <T> T fromJson(final String value,
			final Class<? extends T> propertyType) {
		assert isAtomicValue(propertyType);
		return AtomicValue.get(propertyType).fromJson(value, propertyType);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.JsonConverter#isValue(java.lang.Class)
	 */
	@Override
	public boolean isAtomicValue(final Class<?> propertyType) {
		return AtomicValue.get(propertyType) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.JsonConverter#newCollection(java.lang.Class)
	 */
	@Override
	public Collection<?> newCollection(final Class<Collection> wantedType) {
		final Collection<?> result;

		if (wantedType.isInterface()
				|| Modifier.isAbstract(wantedType.getModifiers())) {
			try {
				result = (Collection<?>) chooseMostSuitableCollection(
						wantedType).newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		} else {
			try {
				result = wantedType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.cadrian.jsonref.JsonConverter#newMap(java.lang.Class)
	 */
	@Override
	public Map<?, ?> newMap(final Class<Map> wantedType) {
		Map<?, ?> result = null;

		if (wantedType.isInterface()
				|| Modifier.isAbstract(wantedType.getModifiers())) {
			try {
				result = (Map<?, ?>) chooseMostSuitableMap(wantedType)
						.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		} else {
			try {
				result = wantedType.newInstance();
			} catch (InstantiationException | IllegalAccessException e) {
				throw new SerializationException(e);
			}
		}
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.cadrian.jsonref.JsonConverter#isTransient(java.lang.reflect.Field)
	 */
	@Override
	public boolean isTransient(final Field field) {
		// By default, all the actual fields are serialized
		return field == null;
	}

	private static final Map<Class, Class> MOST_SUITABLE_COLLECTIONS = new HashMap<>();
	private static final Map<Class, Class> MOST_SUITABLE_MAPS = new HashMap<>();
	static {
		MOST_SUITABLE_COLLECTIONS.put(Collection.class, ArrayList.class);
		MOST_SUITABLE_COLLECTIONS.put(List.class, ArrayList.class);
		MOST_SUITABLE_COLLECTIONS.put(Set.class, HashSet.class);
		MOST_SUITABLE_COLLECTIONS.put(Deque.class, ArrayDeque.class);
		MOST_SUITABLE_COLLECTIONS.put(NavigableSet.class, TreeSet.class);

		MOST_SUITABLE_MAPS.put(Map.class, HashMap.class);
		MOST_SUITABLE_MAPS.put(NavigableMap.class, TreeMap.class);
	}

	@SuppressWarnings("unchecked")
	private static Class chooseMostSuitable(final Class wantedType,
			final Map<Class, Class> candidates) {
		Class result = candidates.get(wantedType);
		if (result == null) {
			for (final Map.Entry<Class, Class> candidate : candidates
					.entrySet()) {
				if (wantedType.isAssignableFrom(candidate.getKey())) {
					result = candidate.getValue();
				}
			}
		}
		return result;
	}

	private static Class chooseMostSuitableCollection(final Class wantedType) {
		return chooseMostSuitable(wantedType, MOST_SUITABLE_COLLECTIONS);
	}

	private static Class chooseMostSuitableMap(final Class wantedType) {
		return chooseMostSuitable(wantedType, MOST_SUITABLE_MAPS);
	}

}
