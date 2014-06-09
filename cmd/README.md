# Introduction

SPARQL Benchmarker is a set of simple command line tools that can be used to test
SPARQL systems.  This includes both remote services accessed via HTTP and those
accessed via an ARQ in-memory dataset.

# General Script Options

All scripts will respect a `JAVA_OPTIONS` environment variable if set which can be 
used to pass custom options to the JVM.

The \*nix scripts will automatically attempt to locate the JAR file relative to where
they are run and will respect the `CLASSPATH` environment variable if present.  Note that
the Windows scripts currently does not have the former behaviour but it will honour the `CLASSPATH`
environment variable.

## Benchmarking

The `benchmark` command is used to benchmark the performance of a SPARQL system.

On \*nix systems you can invoke the benchmarker like so:

    ./benchmark [options]

On Windows systems you can invoke the benchmarker like so:

    benchmark.bat [options]

To see full usage summary run with the `-h` or `--help` option

## Soak Testing

The `soak` command is used to stress test a SPARQL system by continually running tests against it
for some user defined period of time.

On \*nix systems you can invoke the soak tester like so:

    ./soak [options]
  
On Windows systems you can invoke the soak tester like so:

    soak.bat [options]
  
To see full usage summary run with the `-h` or `--help` option

## Smoke Testing

The `smoke` command is used to run a set of tests once against a SPARQL system to see whether they
pass or fail

On \*nix systems you can invoke the smoke tester like so:

    ./smoke [options]
  
On Windows systems you can invoke the smoke tester like so:

    smoke.bat [options]
  
To see full usage summary run with the `-h` or `--help` option

## Smoke Testing

The `stress` command is used to run a set of tests repeatedly against a SPARQL system under progressively
increasing load to determine how a system responds under high load.

On \*nix systems you can invoke the stress tester like so:

    ./stress [options]
  
On Windows systems you can invoke the stress tester like so:

    stress.bat [options]
  
To see full usage summary run with the `-h` or `--help` option

## Operations

The `operations` command provides information about the supported operations.

On \*nix systems you can invoke the command like so:

    ./operations [options]
    
On Windows system you can invoke the command like so:

    operations.bat [options]
    
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
