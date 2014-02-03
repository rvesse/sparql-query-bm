/*
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
 
*/

package net.sf.sparql.benchmarking.runners;

import net.sf.sparql.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.benchmarking.operations.Operation;
import net.sf.sparql.benchmarking.options.Options;
import net.sf.sparql.benchmarking.stats.OperationMixRun;
import net.sf.sparql.benchmarking.stats.OperationRun;

/**
 * Abstract implementation of a runner providing common halting and progress
 * reporting functionality
 * 
 * @author rvesse
 * 
 * @param <T>
 */
public abstract class AbstractRunner<T extends Options> implements Runner<T> {

    private boolean halted = false;

    @Override
    public void halt(T options, String message) {
        System.err.println("Benchmarking Aborted - Halting due to " + message);
        if (!halted) {
            // Make sure we only reallyHalt once, otherwise, we infinite loop
            // with bad behavior from a listener.
            halted = true;
            reallyHalt(options, message);
        }
    }

    /**
     * Helper method that ensures we really halt without going into an infinite
     * loop
     * 
     * @param options
     *            Options
     * @param message
     *            Message
     */
    private void reallyHalt(T options, String message) {
        // Inform Listeners that Benchmarking Finished with a halt condition
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleFinished(this, options, false);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleFinish() - " + e.getMessage());
                if (options.getHaltOnError() || options.getHaltAny()) {
                    halt(options, l.getClass().getName() + " encountering an error during finish");
                }
            }
        }

        // Then perform actual halting depending on configured behaviour
        switch (options.getHaltBehaviour()) {
        case EXIT:
            System.exit(2);
        case THROW_EXCEPTION:
            throw new RuntimeException("Benchmarking Aborted - Halting due to " + message);
        }
    }

    @Override
    public void halt(T options, Exception e) {
        halt(options, e.getMessage());
    }

    @Override
    public void reportProgress(T options) {
        this.reportPartialProgress(options, "\n");
    }

    @Override
    public void reportPartialProgress(T options, String message) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, message);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnError()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    @Override
    public void reportProgress(T options, String message) {
        this.reportPartialProgress(options, message + '\n');
    }

    @Override
    public void reportProgress(T options, Operation operation, OperationRun run) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, operation, run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnTimeout()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

    @Override
    public void reportProgress(T options, OperationMixRun run) {
        for (ProgressListener l : options.getListeners()) {
            try {
                l.handleProgress(this, options, run);
            } catch (Exception e) {
                System.err.println(l.getClass().getName() + " encountered an error during handleProgress() - " + e.getMessage());
                if (options.getHaltAny() || options.getHaltOnError()) {
                    halt(options, l.getClass().getName() + " encountering an error in progress reporting");
                }
            }
        }
    }

}