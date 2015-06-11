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

public abstract class AbstractSerializationObject extends
		AbstractSerializationData {
	final Class<?> type;
	final int ref;

	AbstractSerializationObject(final Class<?> type, final int ref) {
		this.type = type;
		this.ref = ref;
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
	 * Getter id
	 *
	 * @return the id
	 */
	public int getRef() {
		return ref;
	}

}