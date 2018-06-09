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
package org.at4j.zip;

import org.at4j.support.lang.UnsignedByte;
import org.at4j.support.lang.UnsignedShort;
import org.at4j.zip.comp.Deflate64CompressionMethod;
import org.at4j.zip.comp.DeflatedCompressionMethod;
import org.at4j.zip.comp.ImplodedCompressionMethod;
import org.at4j.zip.comp.LzmaCompressionMethod;
import org.at4j.zip.comp.ZipEntryCompressionMethod;

/**
 * This object contains boolean properties that represent the general purpose
 * bit flags of a Zip entry. Some of the properties are compression
 * method-specific.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class ZipGeneralPurposeBitFlags
{
	private boolean m_encrypted;
	// This is significant if imploding compression is used
	private boolean m_8kSlidingDictionary;
	// This is significant if imploding compression is used
	private boolean m_3ShannonFanoTrees;
	// This is significant if deflating compression is used
	private boolean m_maximumCompression;
	// This is significant if deflating compression is used
	private boolean m_fastCompression;
	// This is significant if deflating compression is used
	private boolean m_superFastCompression;
	// This is significant if LZMA compression is used
	private boolean m_eosMarkerUsed;
	private boolean m_localHeaderFieldsZeroed;
	private boolean m_compressedPatchedData;
	private boolean m_strongEncryption;
	private boolean m_utf8Encoding;
	private boolean m_localHeaderValuesMasked;

	/**
	 * Create a new general purpose bit flags object with all properties set to
	 * {@code false}.
	 */
	public ZipGeneralPurposeBitFlags()
	{
		// Nothing
	}

	/**
	 * Create a new general purpose bit flags object that is initialized with
	 * the contents of the supplied short object read from a Zip entry header.
	 * @param s The short object containing data read from a Zip entry header.
	 */
	public ZipGeneralPurposeBitFlags(UnsignedShort s)
	{
		byte[] barr = s.getBigEndianByteArray();
		UnsignedByte b1 = UnsignedByte.valueOf(barr[0]);
		UnsignedByte b2 = UnsignedByte.valueOf(barr[1]);
		m_encrypted = b1.isBitSet(0);
		m_8kSlidingDictionary = b1.isBitSet(1);
		m_3ShannonFanoTrees = b1.isBitSet(2);
		m_maximumCompression = (!b1.isBitSet(2)) && b1.isBitSet(1);
		m_fastCompression = b1.isBitSet(2) && (!b1.isBitSet(1));
		m_superFastCompression = b1.isBitSet(2) && b1.isBitSet(1);
		m_eosMarkerUsed = b1.isBitSet(1);
		m_localHeaderFieldsZeroed = b1.isBitSet(3);
		m_compressedPatchedData = b1.isBitSet(5);
		m_strongEncryption = b1.isBitSet(6);
		m_utf8Encoding = b2.isBitSet(3);
		m_localHeaderValuesMasked = b2.isBitSet(5);
	}

	/**
	 * <pre>
	 * Bit 0: If set, indicates that the file is encrypted.
	 * </pre>
	 * @return {@code true} if the file is encrypted.
	 */
	public boolean isEncrypted()
	{
		return m_encrypted;
	}

	public void setEncrypted(boolean encrypted)
	{
		m_encrypted = encrypted;
	}

	/**
	 * <pre>
	 * Bit 1: If the compression method used was type 6,
	 *        Imploding, then this bit, if set, indicates
	 *        an 8K sliding dictionary was used.  If clear,
	 *        then a 4K sliding dictionary was used.
	 * </pre>
	 * @return {@code true} if an 8k sliding dictionary was used.
	 */
	public boolean is8kSlidingDictionary()
	{
		return m_8kSlidingDictionary;
	}

	public void set8kSlidingDictionary(boolean b)
	{
		m_8kSlidingDictionary = b;
	}

	/**
	 * <pre>
	 * Bit 2: If the compression method used was type 6,
	 *        Imploding, then this bit, if set, indicates
	 *        3 Shannon-Fano trees were used to encode the
	 *        sliding dictionary output.  If clear, then 2
	 *        Shannon-Fano trees were used.
	 * </pre>
	 * @return {@code true} if 3 Shannon-Fano trees were used.
	 */
	public boolean is3ShannonFanoTrees()
	{
		return m_3ShannonFanoTrees;
	}

	public void set3ShannonFanoTrees(boolean b)
	{
		m_3ShannonFanoTrees = b;
	}

	/**
	 * <pre>
	 * (For Methods 8 and 9 - Deflating)
	 * Bit 2  Bit 1
	 *   0      1    Maximum (-exx/-ex) compression option was used.
	 * </pre>
	 * @return {@code true} if the maximum compression option was used.
	 */
	public boolean isMaximumCompression()
	{
		return m_maximumCompression;
	}

	public void setMaximumCompression(boolean b)
	{
		m_maximumCompression = b;
	}

	/**
	 * <pre>
	 * (For Methods 8 and 9 - Deflating)
	 * Bit 2  Bit 1
	 *   1      0    Fast (-ef) compression option was used.
	 * </pre>
	 * @return {@code true} if the fast compression option was used.
	 */
	public boolean isFastCompression()
	{
		return m_fastCompression;
	}

	public void setFastCompression(boolean b)
	{
		m_fastCompression = b;
	}

	/**
	 * <pre>
	 * (For Methods 8 and 9 - Deflating)
	 * Bit 2  Bit 1
	 *   1      1    Super Fast (-es) compression option was used.
	 * </pre>
	 * @return {@code true} if the super fast compression option was used.
	 */
	public boolean isSuperFastCompression()
	{
		return m_superFastCompression;
	}

	public void setSuperFastCompression(boolean b)
	{
		m_superFastCompression = b;
	}

	/**
	 * <pre>
	 * (For Method 14 - LZMA)
	 * Bit 1: If the compression method used was type 14,
	 *        LZMA, then this bit, if set, indicates
	 *        an end-of-stream (EOS) marker is used to
	 *        mark the end of the compressed data stream.
	 *        If clear, then an EOS marker is not present
	 *        and the compressed data size must be known
	 *        to extract.
	 * </pre>
	 * @return {@code true} if an EOS marker is used.
	 */
	public boolean isEosMarkerUsed()
	{
		return m_eosMarkerUsed;
	}

	public void setEosMarkerUsed(boolean b)
	{
		m_eosMarkerUsed = b;
	}

	/**
	 * <pre>
	 * Bit 3: If this bit is set, the fields crc-32, compressed 
	 *        size and uncompressed size are set to zero in the 
	 *        local header.  The correct values are put in the 
	 *        data descriptor immediately following the compressed
	 *        data.  (Note: PKZIP version 2.04g for DOS only 
	 *        recognizes this bit for method 8 compression, newer 
	 *        versions of PKZIP recognize this bit for any 
	 *        compression method.)
	 * </pre>
	 * @return {@code true} if the local header fields are zeroed.
	 */
	public boolean isLocalHeaderFieldZeroed()
	{
		return m_localHeaderFieldsZeroed;
	}

	public void setLocalHeaderFieldsZeroed(boolean b)
	{
		m_localHeaderFieldsZeroed = b;
	}

	/**
	 * <pre>
	 * Bit 5: If this bit is set, this indicates that the file is 
	 * 	      compressed patched data.  (Note: Requires PKZIP 
	 * 	      version 2.70 or greater)
	 * </pre>
	 * @return {@code true} if the file is compressed patched data.
	 */
	public boolean isCompressedPatchedData()
	{
		return m_compressedPatchedData;
	}

	public void setCompressedPatchedData(boolean b)
	{
		m_compressedPatchedData = b;
	}

	/**
	 * <pre>
	 * Bit 6: Strong encryption.  If this bit is set, you should
	 *        set the version needed to extract value to at least
	 *        50 and you must also set bit 0.  If AES encryption
	 *        is used, the version needed to extract value must 
	 *        be at least 51.
	 * </pre>
	 * @return {@code true} if strong encryption is used.
	 */
	public boolean isStrongEncryption()
	{
		return m_strongEncryption;
	}

	public void setStrongEncryption(boolean b)
	{
		m_strongEncryption = b;
	}

	/**
	 * <pre>
	 * Bit 11: Language encoding flag (EFS).  If this bit is set,
	 *         the filename and comment fields for this file
	 *         must be encoded using UTF-8. (see APPENDIX D)
	 * </pre>
	 * @return {@code true} if UTF-8 encoding must be used.
	 */
	public boolean isUtf8Encoding()
	{
		return m_utf8Encoding;
	}

	public void setUtf8Encoding(boolean b)
	{
		m_utf8Encoding = b;
	}

	/**
	 * <pre>
	 * Bit 13: Used when encrypting the Central Directory to indicate 
	 *         selected data values in the Local Header are masked to
	 *         hide their actual values.  See the section describing 
	 *         the Strong Encryption Specification for details.
	 * </pre>
	 * @return {@code true} if selected local header data values are masked.
	 */
	public boolean isLocalHeaderValuesMasked()
	{
		return m_localHeaderValuesMasked;
	}

	public void setLocalHeaderValuesMasked(boolean b)
	{
		m_localHeaderValuesMasked = b;
	}

	/**
	 * Encode the bit flag values for storing them in a Zip file header.
	 * @param cm The compression method used for the Zip file.
	 * @return The general purpose bit flags, encoded in a two bytes long byte
	 * array.
	 */
	public byte[] toBytes(ZipEntryCompressionMethod cm)
	{
		// Initialized with zeroes
		byte[] res = new byte[2];
		if (m_encrypted)
		{
			res[0] = 1;
		}
		if (cm instanceof ImplodedCompressionMethod)
		{
			if (m_8kSlidingDictionary)
			{
				res[0] += 2;
			}
			if (m_3ShannonFanoTrees)
			{
				res[0] += 4;
			}
		}
		else if ((cm instanceof DeflatedCompressionMethod) || (cm instanceof Deflate64CompressionMethod))
		{
			if (m_maximumCompression)
			{
				res[0] += 2;
			}
			else if (m_fastCompression)
			{
				res[0] += 4;
			}
			else if (m_superFastCompression)
			{
				res[0] += 6;
			}
		}
		else if (cm instanceof LzmaCompressionMethod)
		{
			if (m_eosMarkerUsed)
			{
				res[0] += 2;
			}
		}

		if (m_localHeaderFieldsZeroed)
		{
			res[0] += 8;
		}

		if (m_compressedPatchedData)
		{
			res[0] += 32;
		}

		if (m_strongEncryption)
		{
			res[0] += 64;
		}

		if (m_utf8Encoding)
		{
			res[1] += 8;
		}

		if (m_localHeaderValuesMasked)
		{
			res[1] += 32;
		}

		return res;
	}
}
