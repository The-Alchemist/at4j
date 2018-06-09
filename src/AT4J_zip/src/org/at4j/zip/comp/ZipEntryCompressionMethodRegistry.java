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
package org.at4j.zip.comp;

import java.util.HashMap;
import java.util.Map;

import org.at4j.support.lang.UnsignedShort;

/**
 * This is a registry where factory objects for the different
 * {@link ZipEntryCompressionMethod}:s that may occur in a Zip file are
 * registered.
 * <p>
 * By default, the registry knows about only the compression methods that are
 * currently supported.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipEntryCompressionMethodFactory
 */
public class ZipEntryCompressionMethodRegistry
{
	private final Map<UnsignedShort, ZipEntryCompressionMethodFactory> m_registry;

	public ZipEntryCompressionMethodRegistry()
	{
		// Register all known codes
		m_registry = new HashMap<UnsignedShort, ZipEntryCompressionMethodFactory>(16);
		m_registry.put(StoredCompressionMethod.CODE, StoredCompressionMethod.INSTANCE);
		m_registry.put(DeflatedCompressionMethod.CODE, DeflatedCompressionMethodFactory.INSTANCE);
		m_registry.put(BZip2CompressionMethod.CODE, BZip2CompressionMethod.INSTANCE);
		m_registry.put(LzmaCompressionMethod.CODE, LzmaCompressionMethod.DEFAULT_INSTANCE);
		m_registry.put(Deflate64CompressionMethod.CODE, Deflate64CompressionMethod.INSTANCE);
		m_registry.put(IbmLz77CompressionMethod.CODE, IbmLz77CompressionMethod.INSTANCE);
		m_registry.put(IbmTerseCompressionMethod.CODE, IbmTerseCompressionMethod.INSTANCE);
		m_registry.put(ImplodedCompressionMethod.CODE, ImplodedCompressionMethod.INSTANCE);
		m_registry.put(PKWareImplodingCompressionMethod.CODE, PKWareImplodingCompressionMethod.INSTANCE);
		m_registry.put(PpmdCompressionMethod.CODE, PpmdCompressionMethod.INSTANCE);
		m_registry.put(Reduced1CompressionMethod.CODE, Reduced1CompressionMethod.INSTANCE);
		m_registry.put(Reduced2CompressionMethod.CODE, Reduced2CompressionMethod.INSTANCE);
		m_registry.put(Reduced3CompressionMethod.CODE, Reduced3CompressionMethod.INSTANCE);
		m_registry.put(Reduced4CompressionMethod.CODE, Reduced4CompressionMethod.INSTANCE);
		m_registry.put(ShrunkCompressionMethod.CODE, ShrunkCompressionMethod.INSTANCE);
		m_registry.put(WavPackCompressionMethod.CODE, WavPackCompressionMethod.INSTANCE);
	}

	/**
	 * Register a new compression method.
	 * @param f The compression method factory.
	 */
	public void registerCompressionMethodFactory(ZipEntryCompressionMethodFactory f)
	{
		m_registry.put(f.getCode(), f);
	}

	/**
	 * Get the {@link ZipEntryCompressionMethodFactory} that creates compression
	 * method objects corresponding to the supplied code. The code is the code
	 * identifying the compression method in a Zip entry's header.
	 * @param code The code for the compression method.
	 * @return The compression method factory.
	 * @throws UnknownZipEntryCompressionMethodException If no compression
	 * method is registered for the supplied code.
	 */
	public ZipEntryCompressionMethodFactory forCode(UnsignedShort code) throws UnknownZipEntryCompressionMethodException
	{
		ZipEntryCompressionMethodFactory res = m_registry.get(code);
		if (res == null)
		{
			throw new UnknownZipEntryCompressionMethodException("No Zip entry compression method factory is registered for the code " + code);
		}
		return res;
	}
}
