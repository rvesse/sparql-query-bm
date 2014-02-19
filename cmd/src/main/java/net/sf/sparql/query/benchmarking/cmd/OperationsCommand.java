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
    @Option(name = { "-o", "--operation" }, arity = 1, title = "Operation Name", description = "Requests that help for a specific operation be shown")
    public String op;

    /**
     * Verbose option
     */
    @Option(name = { "-v", "--verbose" }, description = "Enables verbose help")
    public boolean verbose = false;

    /**
     * Classes argument
     */
    @Arguments(description = "Provides additional classes that should be loaded thus allowing help for custom operations provided by additional JARs on the CLASSPATH to be shown")
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
            int columnWidth = 100 - maxKeyLength;
            if (columnWidth <= 0)
                columnWidth = 60;
            for (String key : loaders.keySet()) {
                // Print operation name plus padding
                System.out.print(key);
                printIndent(maxKeyLength - key.length());

                // Print operation description
                String description = loaders.get(key).getDescription();
                printDescription(description, maxKeyLength, columnWidth);
            }

            // When verbose print argument summaries for every operation
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
            }

            System.out.println(this.op);
            printChars('=', this.op.length());
            System.out.println();
            System.out.println();
            printDescription(loader.getDescription(), 0, 100);
            System.out.println();

            System.out.println("Operation Arguments");
            System.out.println("-------------------");
            System.out.println();
            System.out.println("Example usage in a TSV mix file (note spaces are used in places of tabs for clarity):");
            System.out.println();

            // Show example usage
            OperationLoaderArgument[] args = loader.getArguments();
            int closeArgs = 0;
            printIndent(4);
            System.out.print(this.op);
            printIndent(4);
            for (int i = 0; i < args.length; i++) {
                OperationLoaderArgument arg = args[i];
                if (arg.isOptional()) {
                    System.out.print('[');
                    closeArgs++;
                }
                System.out.print(arg.getName());
                if (i < args.length - 1)
                    printIndent(4);
            }
            if (closeArgs > 0)
                printChars(']', closeArgs);
            System.out.println();
            System.out.println();

            // Show argument descriptions
            System.out.println("Argument details:");
            System.out.println();
            for (int i = 0; i < args.length; i++) {
                OperationLoaderArgument arg = args[i];
                printIndent(4);
                System.out.println(arg.getName());
                printIndent(8);
                printDescription(arg.getDescription(), 8, 92);
                System.out.println();

                // TODO When verbose add additional general information about
                // the argument type
            }
        }
    }

    private void printIndent(int length) {
        printChars(' ', length);
    }

    private void printChars(char c, int length) {
        for (int i = 0; i < length; i++) {
            System.out.print(c);
        }
    }

    private void printDescription(String description, int indent, int columnWidth) {
        int newLineIndex = description.indexOf('\n');
        if (newLineIndex == -1) {
            printDescriptionWithoutNewLines(description, indent, columnWidth);
        } else {
            int start = 0;
            while (newLineIndex > -1) {
                String partialDescription = description.substring(0, newLineIndex - 1);
                printDescriptionWithoutNewLines(partialDescription, indent, columnWidth);
                printIndent(indent);
                start = newLineIndex + 1;
                newLineIndex = description.indexOf('\n', start);
            }
            printDescriptionWithoutNewLines(description.substring(start), indent, columnWidth);
        }
    }

    private void printDescriptionWithoutNewLines(String description, int indent, int columnWidth) {
        if (description.length() <= columnWidth) {
            System.out.println(description);
        } else {
            int start = 0;
            while (true) {
                if (description.length() - start <= columnWidth) {
                    printDescriptionWithoutNewLines(description.substring(start), indent, columnWidth);
                    break;
                }
                int spaceIndex = findPrecedingSpace(description, start + columnWidth);
                printDescriptionWithoutNewLines(description.substring(start, spaceIndex), indent, columnWidth);
                printIndent(indent);
                start = spaceIndex + 1;
            }
        }
    }

    private int findPrecedingSpace(String value, int index) {
        char c = value.charAt(index);
        while (!Character.isWhitespace(c) && index >= 0) {
            index--;
            c = value.charAt(index);
        }
        return index;
    }
}
