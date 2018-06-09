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
package org.at4j.test.support;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.channels.ScatteringByteChannel;

import org.entityfs.ReadableFile;
import org.entityfs.exception.LockTimeoutException;
import org.entityfs.lock.DummyLock;
import org.entityfs.lock.EntityLock;
import org.entityfs.lock.ReadLockRequiredException;
import org.entityfs.security.AccessDeniedException;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
public class FaultInjectionReadableFile implements ReadableFile
{
	public ScatteringByteChannel openChannelForRead() throws ReadLockRequiredException, AccessDeniedException
	{
		return null;
	}

	public InputStream openForRead() throws ReadLockRequiredException, AccessDeniedException
	{
		FaultInjectionInputStream res = new FaultInjectionInputStream(new ByteArrayInputStream(new byte[0]));
		res.injectFault();
		return res;
	}

	public EntityLock getReadLock()
	{
		return DummyLock.INSTANCE;
	}

	public boolean isReadLockedByCurrentThread() throws IllegalStateException
	{
		return true;
	}

	public EntityLock lockForReading() throws LockTimeoutException
	{
		return DummyLock.INSTANCE;
	}

	public long getSize()
	{
		return -1L;
	}

	public long getDataSize()
	{
		return -1L;
	}
}
