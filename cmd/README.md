# Introduction

SPARQL Benchmarker is a set of simple command line tools that can be used to test
SPARQL systems accessible via HTTP

# General Script Options

All scripts will respect a `JAVA_OPTIONS` environment variable if set which can be 
used to pass custom options to the JVM.

The \*nix scripts will automatically attempt to locate the JAR file relative to where
they are run, the Windows scripts currently do not have this behaviour.

## Benchmarking

The `benchmark` command is used to benchmark the performance of a SPARQL system.

On \*nix systems you can invoke the benchmarker like so:

    ./benchmark [options]

On Windows systems you can invoke the benchmarker like so:

    benchmark.bat [options]

Or you can invoke the jar directly using Java as follows:

    java -cp sparql-query-bm-cli.jar net.sf.sparql.query.benchmarking.cmd.BenchmarkCommand [options]

To see full usage summary run with the `-h` or `--help` option

## Soak Testing

The `soak` command is used to stress test a SPARQL system by continually running tests against it
for some user defined period of time.

On \*nix systems you can invoke the soak tester like so:

    ./soak [options]
  
On Windows systems you can invoke the soak tester like so:

    soak.bat [options]
  
Or you can invoke the jar directly using Java as follows:

    java -cp sparql-query-bm-cli.jar net.sf.sparql.query.benchmarking.cmd.SoakCommand [options]
  
To see full usage summary run with the `-h` or `--help` option

# Documentation

For documentation on the CLI please see the [wiki](https://sourceforge.net/p/sparql-query-bm/wiki/CLI/)

# License

Copyright 2011-2014 Cray Inc. All Rights Reserved

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are
met:

* Redistributions of source code must retain the above copyright
  notice, this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright
  notice, this list of conditions and the following disclaimer in the
  documentation and/or other materials provided with the distribution.

* Neither the name Cray Inc. nor the names of its contributors may be
  used to endorse or promote products derived from this software
  without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Acknowledgements

SPARQL Query Benchmarker uses the the Apache Jena ARQ query engine for issuing queries 
and parsing the results - http://jena.apache.org

Uses SP2B queries under the BSD license from http://dbis.informatik.uni-freiburg.de/forschung/projekte/SP2B/

Uses LUBM queries from academic paper:

GUO, Y., PAN, Z., HEFLIN, J.. LUBM: A Benchmark for OWL Knowledge Base Systems. Web Semantics: Science, Services
and Agents on the World Wide Web, North America, 3, mar. 2011. 
Available at: <http://www.websemanticsjournal.org/index.php/ps/article/view/70/68>. Date accessed: 01 Jun. 2012.
