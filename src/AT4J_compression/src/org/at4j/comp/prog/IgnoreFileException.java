package org.at4j.comp.prog;

/**
 * This exception is thrown by a compression program when an input file should
 * be ignored for some reason.
 * <p>
 * The message of the exception is printed to stderr by the method catching it.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class IgnoreFileException extends Exception
{
	private static final long serialVersionUID = 1L;

	public IgnoreFileException(String msg)
	{
		super(msg);
	}
}
