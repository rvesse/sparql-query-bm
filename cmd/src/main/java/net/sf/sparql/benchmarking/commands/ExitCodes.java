package net.sf.sparql.benchmarking.commands;

/**
 * Standard exit codes for the CLI
 * 
 * @author rvesse
 * 
 */
public class ExitCodes {

	/**
	 * Success
	 */
	public static final int SUCCESS = 0;
	/**
	 * Failure
	 */
	public static final int FAILURE = 1;
	/**
	 * A required option was missing
	 */
	public static final int REQUIRED_OPTION_MISSING = 10;
	/**
	 * An option requires a value and the value was missing
	 */
	public static final int REQUIRED_OPTION_VALUE_MISSING = 11;
	/**
	 * The command requires arguments that were missing
	 */
	public static final int REQUIRED_ARGUMENTS_MISSING = 12;
	/**
	 * The command received an unexpected argument
	 */
	public static final int UNEXPECTED_ARGUMENT = 13;
	/**
	 * An IO error occurred
	 */
	public static final int IO_ERROR = 20;
	/**
	 * An unexpected error occurred
	 */
	public static final int UNEXPECTED_ERROR = 100;

}
