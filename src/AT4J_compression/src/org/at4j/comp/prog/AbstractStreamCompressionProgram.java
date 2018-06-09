package org.at4j.comp.prog;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.at4j.support.prog.AbstractProgram;
import org.at4j.support.prog.CommandErrorException;
import org.entityfs.support.io.StreamUtil;

/**
 * An abstract base class for compression programs that use streams to compress
 * and decompress data.
 * @author Karl Gustafsson
 * @since 1.0
 * @param <T> The class containing the parsed command line arguments for the
 * command.
 */
public abstract class AbstractStreamCompressionProgram<T extends StreamCompressionProgramArguments> extends AbstractProgram
{
	private static class ErrorState
	{
		private Throwable m_exception;

		private synchronized boolean hasException()
		{
			return m_exception != null;
		}

		private synchronized Throwable getException()
		{
			return m_exception;
		}

		private synchronized void setException(Throwable t)
		{
			m_exception = t;
		}
	}

	private class ProcessRunnable implements Runnable
	{
		private final int m_sourceNo;
		private final T m_settings;
		private final ErrorState m_errState;

		private ProcessRunnable(int sourceNo, T settings, ErrorState errState)
		{
			m_sourceNo = sourceNo;
			m_settings = settings;
			m_errState = errState;
		}

		public void run()
		{
			if (m_errState.hasException())
			{
				// Don't run
				return;
			}

			try
			{
				processFile(m_sourceNo, m_settings);
			}
			catch (IgnoreFileException e)
			{
				System.err.println(e.getMessage());
			}
			catch (IOException e)
			{
				m_errState.setException(e);
			}
			catch (RuntimeException e)
			{
				m_errState.setException(e);
			}
		}
	}

	/**
	 * Subclasses implement this to parse their command line.
	 * @param args The command line arguments, unparsed.
	 * @return The command line arguments, parsed.
	 * @throws CommandErrorException If the commands were invalid.
	 */
	protected abstract T parseCommandLine(String[] args) throws CommandErrorException;

	/**
	 * Create an input stream on the source entity for the given command line
	 * arguments.
	 * @param sourceNo The index number for the source entity. The first source
	 * has index == 0.
	 * @param settings The command line arguments.
	 * @return An input stream on the source.
	 * @throws IOException On I/O errors.
	 * @throws IgnoreFileException If the file should be ignored for some
	 * reason.
	 */
	protected abstract InputStream createInputStream(int sourceNo, T settings) throws IOException, IgnoreFileException;

	/**
	 * Create an output stream on the target entity for the given command line
	 * arguments.
	 * @param targetNo The index number for the target entity. The first target
	 * has index == 0.
	 * @param settings The command line arguments.
	 * @return An output stream on the target.
	 * @throws IOException On I/O errors.
	 * @throws IgnoreFileException If the file should be ignored for some
	 * reason.
	 */
	protected abstract OutputStream createOutputStream(int targetNo, T settings) throws IOException, IgnoreFileException;

	/**
	 * Subclasses may override this for postprocessing the source and/or target
	 * entity. This implementation does not do anything.
	 * @param sourceNo The index number for the source and target entities.
	 * @param settings The command line arguments.
	 * @throws IOException On I/O errors.
	 */
	protected void postProcess(int sourceNo, T settings) throws IOException
	{
		// Nothing
	}

	private void processFile(int sourceNo, T settings) throws IOException, IgnoreFileException
	{
		InputStream is = createInputStream(sourceNo, settings);
		try
		{
			OutputStream os = createOutputStream(sourceNo, settings);
			try
			{
				StreamUtil.copyStreams(is, os, 8192);
			}
			finally
			{
				os.close();
			}
		}
		finally
		{
			is.close();
		}

		postProcess(sourceNo, settings);
	}

	private void runSingleThread(T cla) throws IOException
	{
		for (int i = 0; i < cla.getNumberOfSources(); i++)
		{
			try
			{
				processFile(i, cla);
			}
			catch (IgnoreFileException e)
			{
				System.err.println(e.getMessage());
			}
		}
	}

	private void runMultipleThreads(T cla) throws IOException
	{
		ErrorState errState = new ErrorState();

		final int numberOfSources = cla.getNumberOfSources();
		ExecutorService exec = Executors.newFixedThreadPool(Math.min(numberOfSources, cla.getNumberOfThreads()));
		try
		{
			for (int i = 0; i < numberOfSources; i++)
			{
				exec.submit(new ProcessRunnable(i, cla, errState));
			}
		}
		finally
		{
			exec.shutdown();
		}
		try
		{
			exec.awaitTermination(1000000000000L, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			throw new RuntimeException(e);
		}

		Throwable t = errState.getException();
		if (t != null)
		{
			throw new RuntimeException(t);
		}
	}

	protected void run(String[] args) throws CommandErrorException, IOException
	{
		T cla = parseCommandLine(args);

		long start = System.currentTimeMillis();

		if (cla.getNumberOfThreads() == 1)
		{
			runSingleThread(cla);
		}
		else
		{
			runMultipleThreads(cla);
		}

		long end = System.currentTimeMillis();
		if (cla.isTiming())
		{
			System.out.println("The operation took " + (end - start) + " ms.");
			System.out.println("The time it took to load Java and start the program is not included.");
		}
	}
}
