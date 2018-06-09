package org.at4j.support.prog;

import java.io.File;

import org.entityfs.el.AbsoluteLocation;

/**
 * This is an abstract base class for command-line programs such as {@code zip},
 * {@code unzip} and {@code tar}. It contains common utility functions.
 * @author Karl Gustafsson
 * @since 1.0
 */
public abstract class AbstractProgram
{
	/**
	 * Get the argument from the specified position. If the position is beyond
	 * the end of the argument array, throw a {@link CommandErrorException}
	 * having the supplied error message.
	 * @param args The argument array.
	 * @param pos The position in the array of the argument.
	 * @param errMsg The error message to use for the exception if the position
	 * is beyond the end of the array.
	 * @return The argument value.
	 * @throws CommandErrorException If the position is beyond the end of the
	 * array.
	 */
	protected String getArg(String[] args, int pos, String errMsg) throws CommandErrorException
	{
		if (pos >= args.length)
		{
			throw new CommandErrorException(errMsg);
		}
		return args[pos];
	}

	/**
	 * Is the argument a flag argument, i.e. does it start with a {@code -}?
	 * @param arg The argument.
	 * @return {@code true} if the argument is a flag argument.
	 */
	protected boolean isFlagArgument(String arg)
	{
		return arg.charAt(0) == '-';
	}

	/**
	 * Get the absolute location of the file's parent directory.
	 * @param f The file
	 * @return The location of the file's parent directory.
	 */
	protected AbsoluteLocation getParentLocation(File f)
	{
		String parentDir = f.getParent();
		return parentDir != null ? new AbsoluteLocation("/" + parentDir) : AbsoluteLocation.ROOT_DIR;
	}
}
