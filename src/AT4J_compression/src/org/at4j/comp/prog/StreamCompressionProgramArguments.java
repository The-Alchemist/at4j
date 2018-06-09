package org.at4j.comp.prog;

/**
 * Abstract base class for compression program arguments.
 * @author Karl Gustafsson
 * @since 1.0
 */
public abstract class StreamCompressionProgramArguments
{
	private boolean m_timing;
	private int m_numberOfThreads = 1;

	public abstract int getNumberOfSources();

	public void setNumberOfThreads(int no)
	{
		m_numberOfThreads = no;
	}

	public int getNumberOfThreads()
	{
		return m_numberOfThreads;
	}

	public void setTiming()
	{
		m_timing = true;
	}

	public boolean isTiming()
	{
		return m_timing;
	}
}
