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

package net.sf.sparql.query.benchmarking.queries;

import java.util.concurrent.Callable;

import net.sf.sparql.query.benchmarking.Benchmarker;
import net.sf.sparql.query.benchmarking.stats.QueryMixRun;


/**
 * A callable for Query Mixes so we can execute them in parallel to do multi-threaded benchmarks
 * @author rvesse
 *
 */
public class QueryMixRunner implements Callable<QueryMixRun> {
	
	private Benchmarker b;
	
	/**
	 * Creates a new Query Mix Runner
	 * @param b Benchmarker
	 */
	public QueryMixRunner(Benchmarker b)
	{
		this.b = b;
	}

	/**
	 * Runs the Query Mix returning the results of the run
	 */
	@Override
	public QueryMixRun call()
	{
		return this.b.getQueryMix().run(this.b);
	}

}
