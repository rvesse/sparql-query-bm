# Introduction

SPARQL Query Benchmarker started life purely as a SPARQL over HTTP benchmarking tool.
Over time it has evolved into a general purpose API that while designed primarily for
testing SPARQL servers (including queries, updates and GSP requests) can in principal
be extended to test much more than that.

The API is described by javadoc available at TBD and in the documentation at TBD

The command line currently comprises three main tools:

- `benchmark` - Runs a benchmark of operations against a SPARQL server.
- `soak` - Runs a soak test of operations against a SPARQL server.
- `operations` - Provides command line help with available operations

See the [CLI documentation](https://sourceforge.net/p/sparql-query-bm/wiki/CLI) for examples
or running the various commands.

# Build

See the BUILD.md document in this directory

# Documentation

Documentation for the project may be found at https://sourceforge.net/p/sparql-query-bm/wiki/

# License

SPARQL Query Benchmarker is licensed under a 3 Clause BSD License

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
 
# Acknowledgments

This tool was originally developed at Cray Inc (http://www.cray.com) and its development
continues to be in part supported by Cray

SPARQL Query Benchmarker uses the the Apache Jena ARQ query engine for issuing queries 
and parsing the results - http://jena.apache.org

Uses SP2B queries under the BSD license from http://dbis.informatik.uni-freiburg.de/forschung/projekte/SP2B/

Uses LUBM queries from academic paper:

GUO, Y., PAN, Z., HEFLIN, J.. LUBM: A Benchmark for OWL Knowledge Base Systems. Web Semantics: Science, Services
and Agents on the World Wide Web, North America, 3, mar. 2011. 
Available at: <http://www.websemanticsjournal.org/index.php/ps/article/view/70/68>. Date accessed: 01 Jun. 2012.