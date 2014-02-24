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

package net.sf.sparql.query.benchmarking.cmd;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.inject.Inject;

import net.sf.sparql.benchmarking.loader.OperationLoader;
import net.sf.sparql.benchmarking.loader.OperationLoaderArgument;
import net.sf.sparql.benchmarking.loader.OperationLoaderRegistry;
import net.sf.sparql.query.benchmarking.cmd.util.PrintHelper;

import org.apache.commons.lang.ArrayUtils;

import io.airlift.command.Arguments;
import io.airlift.command.Command;
import io.airlift.command.HelpOption;
import io.airlift.command.Option;
import io.airlift.command.ParseArgumentsMissingException;
import io.airlift.command.ParseArgumentsUnexpectedException;
import io.airlift.command.ParseOptionMissingException;
import io.airlift.command.ParseOptionMissingValueException;
import io.airlift.command.SingleCommand;

/**
 * A command which provides help about available operations
 * 
 * @author rvesse
 * 
 */
@Command(name = "operations", description = "Lists operations that the API supports and provides help on configuring each operation in your mix files")
public class OperationsCommand {

    /**
     * Help option
     */
    @Inject
    public HelpOption helpOption;

    /**
     * Operation option
     */
    @Option(name = { "-o", "--op", "--operation" }, arity = 1, title = "Operation Name", description = "Requests that help for a specific operation be shown")
    public String op;

    /**
     * Verbose option
     */
    @Option(name = { "-v", "--verbose" }, description = "Enables verbose help")
    public boolean verbose = false;

    /**
     * Width option
     */
    @Option(name = { "-w", "--width" }, description = "Sets the desired column width which can be used to wrap the help output for restricted viewports.  This defaults to 100 when not set and a minimum value of 40 is enforced.  Some portions of the output will not wrap because doing so would make them difficult to understand.")
    public int width = 100;

    /**
     * Classes argument
     */
    @Arguments(description = "Provides additional classes that should be loaded thus allowing help for custom operations provided by additional JARs on the CLASSPATH to be shown.  Classes may either be classes that implement the OperationLoader interface in which case they will be registered directly or they may be classes which use static initializer blocks to intialize register multiple custom operation loaders.")
    public List<String> classes;

    /**
     * Entry point for the operations command
     * 
     * @param args
     *            Arguments
     */
    public static void main(String[] args) {
        int exitCode = 0;
        try {
            // Parse options
            OperationsCommand cmd = SingleCommand.singleCommand(OperationsCommand.class).parse(args);

            // Show help if requested
            if (cmd.helpOption.showHelpIfRequested()) {
                return;
            }

            // Run command
            cmd.loadClasses();
            cmd.run();

            // Successful exit
            exitCode = 0;
        } catch (ParseOptionMissingException e) {
            if (!ArrayUtils.contains(args, "--help")) {
                System.err.println(AbstractCommand.ANSI_RED + e.getMessage());
                System.err.println();
            }
            AbstractCommand.showUsage(OperationsCommand.class);
            exitCode = 1;
        } catch (ParseOptionMissingValueException e) {
            AbstractCommand.showUsage(OperationsCommand.class);
            exitCode = 2;
        } catch (ParseArgumentsMissingException e) {
            System.err.println(AbstractCommand.ANSI_RED + e.getMessage());
            System.err.println();
            exitCode = 3;
        } catch (ParseArgumentsUnexpectedException e) {
            System.err.println(AbstractCommand.ANSI_RED + e.getMessage());
            System.err.println();
            exitCode = 4;
        } catch (Throwable e) {
            System.err.println(AbstractCommand.ANSI_RED + e.getMessage());
            e.printStackTrace(System.err);
            exitCode = 10;
        } finally {
            System.err.println(AbstractCommand.ANSI_RESET);
            System.exit(exitCode);
        }
    }

    private void loadClasses() {
        if (this.classes == null)
            return;
        try {
            for (String className : this.classes) {
                try {
                    Class<?> cls = Class.forName(className);
                    Object instance = cls.newInstance();
                    if (instance instanceof OperationLoader) {
                        // Register it
                        OperationLoader loader = (OperationLoader) instance;
                        System.out.println("Successfully registered custom operation loader class " + className
                                + " which provides operation " + loader.getPreferredName());
                        OperationLoaderRegistry.addLoader(loader);
                    } else {
                        System.out.println("Successfully loaded class " + className
                                + " and any static initializer blocks will have been invoked");
                    }
                } catch (ClassNotFoundException e) {
                    System.err.println(AbstractCommand.ANSI_RED + "Class " + className + " not found on your CLASSPATH");
                } catch (InstantiationException e) {
                    System.err.println(AbstractCommand.ANSI_RED + "Class " + className + " could not be instantiated");
                } catch (IllegalAccessException e) {
                    System.err.println(AbstractCommand.ANSI_RED + "Class " + className + " is not accessible");
                }
            }
            System.out.println();
        } finally {
            System.err.println(AbstractCommand.ANSI_RESET);
        }
    }

    private void run() {
        if (this.width < 40)
            this.width = 40;

        if (this.op == null) {
            // List available operations
            System.out.println("Available Operations");
            System.out.println("--------------------");
            System.out.println();
            System.out.println("The following operations are registered with the API:");
            System.out.println();

            Map<String, OperationLoader> loaders = new TreeMap<String, OperationLoader>(OperationLoaderRegistry.getLoaders());
            int maxKeyLength = 0;
            for (String key : loaders.keySet()) {
                if (key.length() > maxKeyLength)
                    maxKeyLength = key.length();
            }

            maxKeyLength += 4;
            boolean fixedIndent = false;
            int columnWidth = this.width - maxKeyLength;
            if (columnWidth <= 20) {
                columnWidth = this.width;
                fixedIndent = true;
            }
            for (String key : loaders.keySet()) {
                // Print operation name plus padding
                System.out.print(key);
                if (fixedIndent) {
                    System.out.println();
                    PrintHelper.printIndent(4);
                } else {
                    PrintHelper.printIndent(maxKeyLength - key.length());
                }

                // Print operation description
                String description = loaders.get(key).getDescription();
                PrintHelper.print(description, fixedIndent ? 4 : maxKeyLength, columnWidth);
            }

            System.out.println();
            PrintHelper.print(
                    "For operation specific help run with the --op option and provide the name of the operation you wish to see help for e.g.\n"
                            + "    ./operations --op query", 0, this.width);

            // When verbose PrintHelper.print argument summaries for every
            // operation
            if (this.verbose) {
                System.out.println();
                for (String key : loaders.keySet()) {
                    this.op = key;
                    this.run();
                }
            }
        } else {
            // Print operation specific help
            OperationLoader loader = OperationLoaderRegistry.getLoader(this.op);
            if (loader == null) {
                System.err.println(AbstractCommand.ANSI_RED + "Cannot show help for unknown operation " + this.op);
                return;
            }

            System.out.println(this.op);
            PrintHelper.printChars('=', this.op.length());
            System.out.println();
            System.out.println();
            PrintHelper.print(loader.getDescription(), 0, 100);
            System.out.println();

            System.out.println("Operation Arguments");
            System.out.println("-------------------");
            System.out.println();
            PrintHelper.print("Example usage in a TSV mix file (note spaces are used in places of tabs for clarity):", 0,
                    this.width);
            System.out.println();

            // Show example usage
            OperationLoaderArgument[] args = loader.getArguments();
            int closeArgs = 0;
            PrintHelper.printIndent(4);
            System.out.print(this.op);
            PrintHelper.printIndent(4);
            for (int i = 0; i < args.length; i++) {
                OperationLoaderArgument arg = args[i];
                if (arg.isOptional()) {
                    System.out.print('[');
                    closeArgs++;
                }
                System.out.print(arg.getName());
                if (i < args.length - 1)
                    PrintHelper.printIndent(4);
            }
            if (closeArgs > 0)
                PrintHelper.printChars(']', closeArgs);
            System.out.println();
            System.out.println();

            // Show argument descriptions
            System.out.println("Argument details:");
            System.out.println();
            for (int i = 0; i < args.length; i++) {
                // Argument description
                OperationLoaderArgument arg = args[i];
                PrintHelper.printIndent(4);
                System.out.print(arg.getName());
                if (arg.isOptional())
                    System.out.print(" (Optional)");
                System.out.println();
                PrintHelper.printIndent(8);
                PrintHelper.print(arg.getDescription(), 8, this.width - 8);
                System.out.println();

                // When verbose add additional general information about the
                // argument type
                if (this.verbose) {
                    switch (arg.getType()) {
                    case OperationLoaderArgument.TYPE_FILE:
                        PrintHelper.printIndent(8);
                        PrintHelper
                                .print("File type arguments expect to receive a path to the file and this path may be absolute or relative.  In the case of relative paths they are resolved relative to the directory in which the mix file specifying the operation is located.\nFile type arguments can also be used to refer to classpath resources, in the case that the path specifies both a file and a classpath resource the file is used.",
                                        8, this.width - 8);
                        System.out.println();
                        break;
                    case OperationLoaderArgument.TYPE_LONG:
                        PrintHelper.printIndent(8);
                        PrintHelper.print(
                                "Long type arguments expect to receive an integer value which is a valid long integer.", 8,
                                this.width - 8);
                        System.out.println();
                        break;
                    case OperationLoaderArgument.TYPE_STRING:
                        // No special argument information
                        break;
                    case OperationLoaderArgument.TYPE_BOOLEAN:
                        PrintHelper.printIndent(8);
                        PrintHelper.print("Boolean type arguments expect to receive a boolean value of true or false", 8,
                                this.width - 8);
                        break;
                    default:
                        PrintHelper.printIndent(8);
                        PrintHelper.print("This argument has an unknown type, the expected values for arguments are unknown", 8,
                                this.width - 8);
                        break;
                    }
                }
            }

            // Inform users they can use verbose to see additional argument
            // information
            if (!this.verbose) {
                PrintHelper.print("To see additional information about argument types run with the -v/--verbose option", 0,
                        this.width);
            }
        }
    }
}
