/**
 * Copyright 2012 Robert Vesse
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * you may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.sf.sparql.query.benchmarking;

/**
 * Possible behaviours when a halt condition is encountered
 * @author rvesse
 *
 */
public enum HaltBehaviour {
	/**
	 * Indicates that the program should exit when a halt condition is reached
	 */
	EXIT,
	/**
	 * Indicates that the program should throw an exception when a halt condition is reached
	 */
	THROW_EXCEPTION
}
