/* AT4J -- Archive file tools for Java -- http://www.at4j.org
 * Copyright (C) 2009 Karl Gustafsson
 *
 * This file is a part of AT4J
 *
 * AT4J is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option)
 * any later version.
 *
 * AT4J is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.at4j.tar.builder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.at4j.support.nio.charset.Charsets;
import org.entityfs.DataSink;
import org.entityfs.ETFile;
import org.entityfs.RandomAccess;
import org.entityfs.ReadableFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.support.exception.WrappedIOException;

/**
 * This strategy extends the {@link UstarEntryStrategy} creates a special Pax
 * header before the regular ustar header. The Pax header contains metadata
 * variables. Pax variables are always encoded in UTF-8 in the Tar file, which
 * should make them (the only part of the Tar file that is) portable across
 * different platforms.
 * <p>
 * If the Tar entry path is too long to fit in the regular ustar header, the
 * full path is set in the Pax header variable "path" and a truncated path is
 * used in the ustar header.
 * <p>
 * If it is detected that the path name of the entry contains non-ASCII
 * characters, the "path" variable is added even if the path name is short
 * enough to fit in the ustar header.
 * <p>
 * By default, this strategy adds the "mtime" (last entity modification time)
 * and perhaps the "path" Pax variables to the headers that it creates. By
 * adding {@link PaxVariableProvider} objects to the strategy, more variables
 * can be set.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class PaxTarEntryStrategy extends UstarEntryStrategy
{
	// There does not seem to be any platform-independent way of getting the
	// Java process' PID
	private static final int DEFAULT_PID = 1000;

	private final List<PaxVariableProvider> m_variableProviders = new ArrayList<PaxVariableProvider>(2);

	private int m_processId = DEFAULT_PID;

	/**
	 * Create a new strategy object that will use the platform's default
	 * character encoding to encode text values in the Tar entries.
	 * @see #PaxTarEntryStrategy(Charset)
	 */
	public PaxTarEntryStrategy()
	{
		// Nothing
	}

	/**
	 * Create a new strategy object that will use the supplied charset for
	 * encoding test values in the Tar entries.
	 * @param cs The charset.
	 * @see #PaxTarEntryStrategy()
	 */
	public PaxTarEntryStrategy(Charset cs)
	{
		super(cs);
	}

	/**
	 * Set the process id of the current process (optional). The process id is
	 * used when creating the name of the Pax header. If this is not set, a
	 * default process id of {@code 1000} is used. (There are no good
	 * platform-independent ways of discovering a Java process' process id.)
	 * @param processId The process id.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the process id is zero or negative.
	 */
	public PaxTarEntryStrategy setProcessId(int processId) throws IllegalArgumentException
	{
		if (processId < 1)
		{
			throw new IllegalArgumentException("Invalid process id " + processId + ". The process id must be >= 1");
		}
		m_processId = processId;
		return this;
	}

	/**
	 * Get the process id.
	 * @return The process id.
	 * @see #setProcessId(int)
	 */
	public int getProcessId()
	{
		return m_processId;
	}

	/**
	 * Add one Pax variable provider.
	 * @param p A Pax variable provider.
	 * @return {@code this}
	 */
	public PaxTarEntryStrategy addPaxVariableProvider(PaxVariableProvider p)
	{
		// Null check
		p.getClass();

		m_variableProviders.add(p);
		return this;
	}

	private AbsoluteLocation truncateLocation(AbsoluteLocation location)
	{
		byte[] locationBytes = Charsets.getBytes(location.getLocation(), getTextEncodingCharset());
		if (locationBytes.length > 100)
		{
			// Truncate the name
			String newLocation = getTextEncodingCharset().decode(ByteBuffer.wrap(locationBytes, 0, 100)).toString();
			while (newLocation.endsWith("/"))
			{
				newLocation = newLocation.substring(0, newLocation.length() - 1);
			}
			location = new AbsoluteLocation(newLocation);
		}
		return location;
	}

	private byte[] createVariablesData(Object entity, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		Map<String, String> variables = new HashMap<String, String>(8);
		long lastModifiedStamp = lastModified.getTime();
		variables.put("mtime", "" + (lastModifiedStamp / 1000) + "." + (lastModifiedStamp % 1000) + "000000");

		String pathStr = location.getLocation().substring(1);
		byte[] pathBytes = Charsets.getBytes(pathStr, Charsets.UTF8);
		if (pathBytes.length > 99)
		{
			variables.put("path", pathStr);
		}
		else if (pathBytes.length != pathStr.length())
		{
			// Non-ASCII characters in the path
			variables.put("path", pathStr);
		}
		for (PaxVariableProvider prov : m_variableProviders)
		{
			prov.addVariables(variables, entity, location, effectiveSettings, lastModified);
		}

		try
		{
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			for (Map.Entry<String, String> variable : variables.entrySet())
			{
				byte[] varln = Charsets.getBytes(" " + variable.getKey() + "=" + variable.getValue() + "\n", Charsets.UTF8);
				// The length of the variable line
				int varlnLen = varln.length;
				// The length of the variable line length header
				int varlnLenLen = ("" + varlnLen).length();
				// The total length of the variable line (header + line)
				int totlnLen = varlnLen + varlnLenLen;
				// The length of total length
				int totlnLenLen = ("" + totlnLen).length();
				// Longer than the length of just the variable line?
				if (totlnLenLen > varlnLenLen)
				{
					// Add one to the total length
					totlnLen++;
				}
				baos.write(("" + totlnLen).getBytes());
				baos.write(varln);
			}
			baos.close();
			return baos.toByteArray();
		}
		catch (IOException e)
		{
			throw new WrappedIOException(e);
		}
	}

	private void writePaxHeader(DataSink out, Object entity, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		AbsoluteLocation parentLocation = location.getParentLocation();
		String truncatedLocation = truncateLocation(parentLocation.getChildLocation("PaxHeader." + m_processId).getChildLocation(location.getName())).getLocation().substring(1);

		byte[] variablesData = createVariablesData(entity, location, effectiveSettings, lastModified);

		TarEntrySettings paxHeaderSettings = new TarEntrySettings();
		paxHeaderSettings.setEntityMode(UnixEntityMode.forCode(0644));
		paxHeaderSettings.setOwnerUid(effectiveSettings.getOwnerUid());
		paxHeaderSettings.setOwnerGid(effectiveSettings.getOwnerGid());
		paxHeaderSettings.setOwnerUserName(effectiveSettings.getOwnerUserName());
		paxHeaderSettings.setOwnerGroupName(effectiveSettings.getOwnerGroupName());
		byte[] paxHeader = getHeader(null, truncatedLocation, paxHeaderSettings, variablesData.length, ETFile.TYPE, lastModified);

		// Set the extended PAX header flag
		paxHeader[156] = (byte) 'x';

		addChecksumToHeader(paxHeader);

		out.write(paxHeader);
		out.write(variablesData);
		goToNextBlockBoundary(out, paxHeader.length + variablesData.length);
	}

	/**
	 * Override the inherited implementation to add a Pax header before the
	 * ustar header.
	 */
	@Override
	public void writeFile(DataSink out, ReadableFile f, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		writePaxHeader(out, f, location, effectiveSettings, lastModified);
		location = truncateLocation(location);
		super.writeFile(out, f, location, effectiveSettings, lastModified);
	}

	/**
	 * Override the inherited implementation to add a Pax header before the
	 * ustar header.
	 */
	@Override
	public void writeFileFromStream(RandomAccess out, InputStream is, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		writePaxHeader(out, is, location, effectiveSettings, lastModified);
		location = truncateLocation(location);
		super.writeFileFromStream(out, is, location, effectiveSettings, lastModified);
	}

	/**
	 * Override the inherited implementation to add a Pax header before the
	 * ustar header.
	 */
	@Override
	public void writeDirectory(DataSink out, DirectoryAdapter<?> da, AbsoluteLocation location, TarEntrySettings effectiveSettings, Date lastModified)
	{
		writePaxHeader(out, da, location, effectiveSettings, lastModified);
		location = truncateLocation(location);
		super.writeDirectory(out, da, location, effectiveSettings, lastModified);
	}
}
