/*
 * Copyright 2014 YarcData LLC All Rights Reserved.
 */

package net.sf.sparql.query.benchmarking.runners;

import net.sf.sparql.query.benchmarking.monitoring.ProgressListener;
import net.sf.sparql.query.benchmarking.operations.BenchmarkOperation;
import net.sf.sparql.query.benchmarking.options.Options;
import net.sf.sparql.query.benchmarking.stats.OperationMixRun;
import net.sf.sparql.query.benchmarking.stats.OperationRun;

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
    public void reportProgress(T options, BenchmarkOperation operation, OperationRun run) {
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