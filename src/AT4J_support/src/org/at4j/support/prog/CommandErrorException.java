package org.at4j.support.prog;

/**
 * This checked exception can be thrown by a command if there is an error while
 * it is executing.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class CommandErrorException extends Exception
{
	private static final long serialVersionUID = 1L;

	public CommandErrorException(String msg)
	{
		super(msg);
	}
}
