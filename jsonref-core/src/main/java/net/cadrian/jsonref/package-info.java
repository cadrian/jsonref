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

/**
 * JSON/R is an extended JSON notation that handles "references" to objects, thus allowing one to serialize / deserialize whole object graphs, even with cycles (objects referencing each other).
 *
 *<p>
 * The class {@link net.cadrian.jsonref.JsonSerializer} is the main class. It provides methods to serialize object graphs to strings, deserialize strings back to object graphs,
 * and useful methods to clone object graphs and "transtype" them (i.e. a cheap kind of dozer).
 * </p>
 */
package net.cadrian.jsonref;

