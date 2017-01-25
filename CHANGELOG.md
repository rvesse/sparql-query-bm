# Change Log

## Version 2.3.0 (*Unreleased*)

- Bug fixes
    - Better error handling for blank or invalid lines (#4)

## Version 2.2.0 (16th September 2016)

- Upgrade dependencies
    - Jena 3.1.0
- Fix possible bug with hangs when long running queries time out or are otherwise terminated that both hangs the client and causes the server to waste resources
- **BREAKING** Refactored how the built-in mix runners get their operation ordering to support other new features - this involves some slight changes to the `OperationMixRunner` API
- New `IntelligentMixRunner` primarily designed for benchmarking
    - Automatically tunes the mix being run during the run
    - Operations that time out during the warmups are excluded from subsequent runs
    - Operations that reach a configurable failure threshold are excluded from subsequent runs
    - Operation timeout is automatically tuned based on the observed maximum runtime during warmups
- New local limit option (`--local-limit`)
    - The local limit applies only to how many results are counted and not to the queries themselves
    - This provides a compromise between enforcing a limit on queries (`--limit`) and not counting results at all (`--no-count`) both of which can make results less realistic.  Enforcing a limit asks the system being tested to do less work and may cause it to optimise and process the query differently.  On the other hand not counting results can skew results because often calculating the first result can be very quick but calculating all the results takes much longer.
    - Therefore being able to count some portion of the results allows you to put an upper bound on the amount of work a system does for a given query
- New summarize feature (`--summarize`)
    - Runs queries in summarise form instead of the original. Essentially this rewrites queries so that they are sub-queries of a `SELECT (COUNT(*) AS ?var)` outer query.
    - Effectively this means that the system being tested is forced to compute all possible solutions in order to count them but need only materialise and transmit a single result row. Thus allowing a time to compute the complete query results to be benchmarked without including results serialisation in the timings.

## Version 2.1.1 (5th November 2015)

- **BREAKING** - Now Requires Java 8
- Upgrade dependencies
    - Airline 2.0.0
    - Jena 3.0.0
    - ARQ 3.0.0
- Upgrade Maven plugin versions for builds
- Fix bug with XML results producing an error during final reporting
- Remove deprecated references to Sonatype OSS Parent

## Version 2.1.0 (17th February 2015)
- Add suites directory to distribution package
- Upgrade dependencies
    - JUnit 4.12
    - Airline 0.9.1
- Use latest Maven plugin versions for builds    

## Version 2.0.1 (July 28th 2014)
- Support option for making relative URIs absolute prior to actually executing them
- Fix OperationStats.getVariance() being reported in seconds instead of nanoseconds
- Change Core API to only depend on slf4j and exclude log4j so it can more easily be used with other logging frameworks
- Add additional logging of errors direct to logging framework
- Fix GitHub Issue #1 - Variance is incorrectly converted and presented

## Version 2.0.0 (June 9th 2014)
- Support arbitrary operations e.g. updates, sleeps, parameterized queries etc.
- Support running more than just benchmarks e.g. soak testing
- Support for plugging in arbitrary custom operations
- Refactor CLI to use Airline to make it much more user friendly and easy to extend
- Add operations, soak, smoke and stress commands to CLI
- Support for testing against in-memory datasets
- Bumped dependencies to latest Jena and ARQ (Jena 2.11.2 and ARQ 2.11.2)

## Version 1.1.0 (October 8th 2013)
- Bumped dependencies to latest Jena and ARQ (Jena 2.11.0 and ARQ 2.11.0)
- Added support for wider range of authentication methods
- Removed --insecure option since it didn't function as documented anyway
- Added --debug option to be used in conjunction with --logging option to get detailed HTTP traces

## Version 1.0.2
- Bumped dependencies to latest Jena and ARQ (Jena 2.10.0-SNAPSHOT and ARQ 2.10.0-SNAPSHOT)

## Version 1.0.1
- Bumped dependencies to stable Jena 2.7.3 release (Jena 2.7.3 and ARQ 2.9.3)

## Version 1.0.0
- Updated License notices to Cray Legal standards
- Changed -f/--filename option to more accurate -c/--csv option
- Finalized documentation for first external release
- Bumped dependencies to stable Jena 2.7.1 release (Jena 2.7.1 and ARQ 2.9.1)

## Version 0.9.0 
- Changed so errors/timeouts record the time till error/timeout by default rather than Long.MAX_VALUE so they don't render stats unusable

## Version 0.8.0 (March 1st 2012)
- Support for SPARQL CSV
- New options for using HTTP Basic Authentication
- Fixed a bug in the query randomizer which prevented it from randomizing the last query (last query always happened last)
- Added the query execution order to the XML output

## Version 0.7.0 (February 13th 2012)
- Reworked how multi-threaded benchmarks are run to create a more realistic run environment

## Version 0.6.0 (January 23rd 2012)
- Added additional response time statistics
- Added --nocount option for disabling result counting
- Added --nocsv and --noxml options to disable CSV and XML outputs
- Change so that existing result files will not be overwritten and benchmarking won't run unless --overwrite option is specified
- Added -l/--limit option for enforcing a LIMIT on all queries, enforced limit is minimum of specified limit or existing query limit

## Version 0.5.0 (January 16th 2012)
- Added new statistics for multi-threaded benchmarks
    - Actual Runtime
    - Actual Average Runtime (Arithmetic)
    - Queries per Second
    - Actual Queries per Second
    - Actual Queries per Hour
    - Actual Query Mixes per Hour
- Revised CSV and Console output so that multi-threaded statistics are only included for multi-threaded runs
- Added API document
- Improved comments
- Added FileProgressListener and ConsoleErrProgressListener
- Error handling in the case where a progress listener throws an error including respecting benchmark halting options

## Version 0.4.0 (January 11th 2012)
- Added new statistics
    - Geometric Mean
    - Queries per Hour
    - Query Mixes per Hour
- Changed use of terminology - query set is now query mix in line with other SPARQL benchmarks
- Added note to README about multi-threaded benchmark stats

## Version 0.3.0 (January 10th 2012)
- Added support for multi-threaded benchmarking with configurable level of parallel threads
- Bug Fix to allow text/boolean for ASK results

## Version 0.2.0 (January 6th 2012)
- Added pluggable ProgressListener API and refactored code to use this rather than writing directly to System.out and CSV results file
- Added quiet mode
- Added long format equivalents for all short format arguments e.g. --runs and -r
- Better error reporting and more consistent halting behaviour

## Version 0.1.0 (December 2011)
- First Version
