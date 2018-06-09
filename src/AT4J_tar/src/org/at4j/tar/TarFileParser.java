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
package org.at4j.tar;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.at4j.support.nio.charset.Charsets;
import org.entityfs.DataSource;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.entityattrs.unix.UnixEntityMode;
import org.entityfs.support.exception.WrappedIOException;

/**
 * The Tar file parser is used to parse a Tar file. It reads through data it
 * gets from a {@link DataSource}, parses Tar entry headers, and then uses a
 * delegate object of the type {@link TarEntryHandlerDelegate} to handle the
 * entry in some way. {@link TarFile} uses this to build a map of the Tar file
 * structure that it keeps in memory, and {@link TarExtractor} uses this to
 * extract the Tar entries.
 * <p>
 * The Tar file parser object is stateless. Instead of instantiating it, the
 * singleton instance {@link #INSTANCE} may be used.
 * @author Karl Gustafsson
 * @since 1.0
 */
public final class TarFileParser
{
	public static final TarFileParser INSTANCE = new TarFileParser();

	private static final byte[] EMPTY_BLOCK = new byte[TarConstants.BLOCK_SIZE];

	static
	{
		Arrays.fill(EMPTY_BLOCK, (byte) 0);
	}

	private TarFileParser()
	{
		// Nothing.
	}

	private UnixEntityMode parseEntityMode(String s)
	{
		return UnixEntityMode.forCode(Integer.parseInt(s, 8));
	}

	private byte[] readEofOk(DataSource src, int no)
	{
		byte[] res = new byte[no];
		int noRead = src.read(res);
		if (noRead == -1)
		{
			// EOF
			return null;
		}
		else if (noRead != no)
		{
			throw new TarFileParseException("Wanted to read " + no + " bytes. Got " + noRead);
		}
		return res;
	}

	private byte[] read(DataSource src, int no)
	{
		byte[] res = readEofOk(src, no);
		if (res == null)
		{
			throw new TarFileParseException("Wanted to read " + no + " bytes. Got EOF");
		}
		return res;
	}

	/**
	 * Skip forward at least {@code minNoToSkip} until we reach the start of the
	 * next block after the block starting at the position we reach after
	 * skipping {@code distanceToNextBlockStart} bytes.
	 */
	private void skipForward(DataSource src, long minNoToSkip, int distanceToNextBlockStart)
	{
		long noToSkip = (minNoToSkip == 0 ? 0 : ((minNoToSkip - 1) / TarConstants.BLOCK_SIZE + 1) * TarConstants.BLOCK_SIZE) + distanceToNextBlockStart;
		long noSkipped = src.skipBytes(noToSkip);
		if (noSkipped != noToSkip)
		{
			throw new TarFileParseException("Wanted to skip " + noToSkip + " bytes. Only skipped " + noSkipped);
		}
	}

	private GnuExtendedInformationHeader parseGnuLongFileNameHeader(DataSource src, Charset fileNameCharset, int fileSize, GnuExtendedInformationHeader headerInfo)
	{
		// Read the file name
		byte[] fileName = read(src, fileSize);
		String fn = fileNameCharset.decode(ByteBuffer.wrap(fileName)).toString().trim();
		if (fn.endsWith("/"))
		{
			do
			{
				fn = fn.substring(0, fn.length() - 1);
			}
			while (fn.endsWith("/"));
			headerInfo.setDirectory(Boolean.TRUE);
		}
		else
		{
			headerInfo.setDirectory(Boolean.FALSE);
		}
		headerInfo.setFileName(new AbsoluteLocation("/" + fn));

		// Skip to next block
		skipForward(src, 0, TarConstants.BLOCK_SIZE - (fileSize % TarConstants.BLOCK_SIZE));

		return headerInfo;
	}

	private GnuExtendedInformationHeader parseGnuLongLinkTargetHeader(DataSource src, Charset fileNameCharset, int fileSize, GnuExtendedInformationHeader headerInfo)
	{
		// Read the link target
		byte[] linkTarget = read(src, fileSize);
		String lt = fileNameCharset.decode(ByteBuffer.wrap(linkTarget)).toString().trim();
		while (lt.endsWith("/"))
		{
			lt = lt.substring(0, lt.length() - 1);
		}
		headerInfo.setLinkName(lt);

		// Skip to next block
		skipForward(src, 0, TarConstants.BLOCK_SIZE - (fileSize % TarConstants.BLOCK_SIZE));

		return headerInfo;
	}

	private PaxHeader parsePaxHeader(DataSource src, int fileSize)
	{
		byte[] variables = read(src, fileSize);
		Map<String, String> vm = new HashMap<String, String>(4);
		int pos = 0;
		while (pos < fileSize)
		{
			// Parse variable size
			int varSize = 0;
			int varSizeSize = 0;
			char curChar = (char) variables[pos];
			while ((curChar != ' ') && (curChar != '\0'))
			{
				// 0 is ASCII #48
				varSize = 10 * varSize + (curChar - 48);
				pos++;
				varSizeSize++;
				curChar = (char) variables[pos];
			}
			pos++;
			varSizeSize++;

			// The variable
			String varStr = Charsets.UTF8.decode(ByteBuffer.wrap(variables, pos, varSize - varSizeSize)).toString().trim();
			pos += varSize - varSizeSize;

			// Split the key - value pair
			int equalsIndex = varStr.indexOf('=');
			vm.put(varStr.substring(0, equalsIndex), varStr.substring(equalsIndex + 1));
		}

		// Skip to next block
		skipForward(src, 0, TarConstants.BLOCK_SIZE - (fileSize % TarConstants.BLOCK_SIZE));

		return new PaxHeader(vm);
	}

	/**
	 * Read the Tar file entry header at the {@code DataSource}'s current
	 * position. After reading the entry, the data source is positioned at the
	 * start of the next entry's header.
	 * @return The current Tar entry header or {@code null} if at EOF.
	 */
	private TarEntryHeader readEntryHeader(DataSource src, Charset fileNameCharset, TarEntryHeader previousHeader) throws TarFileParseException
	{
		byte[] header = readEofOk(src, TarConstants.BLOCK_SIZE);
		if (header == null)
		{
			// EOF
			return null;
		}

		// Empty block (meaning end of archive)?
		if (Arrays.equals(header, EMPTY_BLOCK))
		{
			return null;
		}

		TarEntryHeaderData res = new TarEntryHeaderData();
		long fileSize = Long.parseLong(fileNameCharset.decode(ByteBuffer.wrap(header, 124, 12)).toString().trim(), 8);
		res.setFileSize(fileSize);
		res.setTypeFlag(fileNameCharset.decode(ByteBuffer.wrap(header, 156, 1)).toString().charAt(0));
		if ('L' == res.getTypeFlag())
		{
			// This is a Gnu Tar long file name header. It contains
			// information on the file name. This header's following file 
			// contains the file name. The next header is the file's real header
			assert (previousHeader == null) || (previousHeader instanceof GnuExtendedInformationHeader);
			return parseGnuLongFileNameHeader(src, fileNameCharset, (int) fileSize, previousHeader != null ? (GnuExtendedInformationHeader) previousHeader : new GnuExtendedInformationHeader());
		}
		else if ('K' == res.getTypeFlag())
		{
			// This is a Gnu Tar long link target header. It contains
			// information on the file name and the link target. This header's following file 
			// contains the file name. The next header is the file's real header
			assert (previousHeader == null) || (previousHeader instanceof GnuExtendedInformationHeader);
			return parseGnuLongLinkTargetHeader(src, fileNameCharset, (int) fileSize, previousHeader != null ? (GnuExtendedInformationHeader) previousHeader : new GnuExtendedInformationHeader());
		}
		else if ('x' == res.getTypeFlag() || 'X' == res.getTypeFlag())
		{
			// This is a pax header. The following file contains variables with
			// information on the next file in the archive
			assert previousHeader == null;
			return parsePaxHeader(src, (int) fileSize);
		}

		String fileName = fileNameCharset.decode(ByteBuffer.wrap(header, 0, 100)).toString().trim();
		if (fileName.endsWith("/"))
		{
			res.setDirectory(true);
			fileName = fileName.substring(0, fileName.length() - 1);
		}
		else
		{
			res.setDirectory('5' == res.getTypeFlag());
		}
		res.setMode(parseEntityMode(fileNameCharset.decode(ByteBuffer.wrap(header, 100, 8)).toString().trim()));
		res.setOwnerUid(Integer.parseInt(fileNameCharset.decode(ByteBuffer.wrap(header, 108, 8)).toString().trim(), 8));
		res.setOwnerGid(Integer.parseInt(fileNameCharset.decode(ByteBuffer.wrap(header, 116, 8)).toString().trim(), 8));
		res.setLastModificationTime(new Date(Long.parseLong(fileNameCharset.decode(ByteBuffer.wrap(header, 136, 12)).toString().trim(), 8) * 1000));
		res.setChecksum(Integer.parseInt(fileNameCharset.decode(ByteBuffer.wrap(header, 148, 8)).toString().trim(), 8));
		res.setLinkName(fileNameCharset.decode(ByteBuffer.wrap(header, 157, 100)).toString().trim());
		res.setMagic(fileNameCharset.decode(ByteBuffer.wrap(header, 257, 6)).toString().trim().intern());
		if ("ustar".equals(res.getMagic()))
		{
			// USTAR (POSIX (IEEE P1003.1)) format
			res.setUstarVersion(fileNameCharset.decode(ByteBuffer.wrap(header, 263, 2)).toString().trim().intern());
			res.setOwnerName(fileNameCharset.decode(ByteBuffer.wrap(header, 265, 32)).toString().trim().intern());
			res.setGroupName(fileNameCharset.decode(ByteBuffer.wrap(header, 297, 32)).toString().trim().intern());
			String devMajorStr = fileNameCharset.decode(ByteBuffer.wrap(header, 329, 8)).toString().trim();
			res.setDevMajor(devMajorStr.length() > 0 ? Integer.parseInt(devMajorStr, 8) : 0);
			String devMinorStr = fileNameCharset.decode(ByteBuffer.wrap(header, 337, 8)).toString().trim();
			res.setDevMinor(devMinorStr.length() > 0 ? Integer.parseInt(devMinorStr, 8) : 0);
			String fileNamePrefix = fileNameCharset.decode(ByteBuffer.wrap(header, 345, 155)).toString().trim();
			if (fileNamePrefix.length() > 0)
			{
				fileName = fileNamePrefix + "/" + fileName;
			}
			res.setLocation(new AbsoluteLocation("/" + fileName));
		}
		else
		{
			// Old Tar format
			res.setLocation(new AbsoluteLocation("/" + fileName));
		}

		// Was this header preceded with a header containing metadata?
		if (previousHeader != null)
		{
			// Was the previous header a Gnu extended information header?
			if (previousHeader instanceof GnuExtendedInformationHeader)
			{
				GnuExtendedInformationHeader geih = (GnuExtendedInformationHeader) previousHeader;
				if (geih.getFileName() != null)
				{
					res.setLocation(geih.getFileName());
					res.setDirectory(geih.isDirectory().booleanValue());
				}
				if (geih.getLinkName() != null)
				{
					res.setLinkName(geih.getLinkName());
				}
			}
			// A PAX header? In that case, set PAX variables
			else if (previousHeader instanceof PaxHeader)
			{
				Map<String, String> variables = ((PaxHeader) previousHeader).getVariables();
				res.setVariables(variables);
				// Do we have a path variable?
				String path = variables.get("path");
				if (path != null)
				{
					if (path.endsWith("/"))
					{
						res.setLocation(new AbsoluteLocation("/" + path.substring(0, path.length() - 1)));
						res.setDirectory(true);
					}
					else
					{
						res.setLocation(new AbsoluteLocation("/" + path));
					}
				}
				String linkPath = variables.get("linkpath");
				if (linkPath != null)
				{
					res.setLinkName(linkPath);
				}
			}
		}
		return res;
	}

	/**
	 * Parse the Tar file data read from the data source.
	 * @param src The data source.
	 * @param fileNameCharset The charset to use for decoding entry names.
	 * @param entryHandler The delegate object that handles each parsed entry.
	 * @throws TarFileParseException On parse errors.
	 * @throws WrappedIOException On I/O errors.
	 */
	public void parse(DataSource src, Charset fileNameCharset, TarEntryHandlerDelegate entryHandler) throws TarFileParseException, WrappedIOException
	{
		try
		{
			// Read all entry headers

			// This variable is set if the previous header read contained meta
			// data about the next header to read.
			TarEntryHeader previousHeader = null;
			TarEntryHeader entryHeader = readEntryHeader(src, fileNameCharset, null);
			while (entryHeader != null)
			{
				if (entryHeader instanceof TarEntryHeaderData)
				{
					TarEntryHeaderData ehd = (TarEntryHeaderData) entryHeader;
					long noToSkip = entryHandler.handle(ehd, src);
					if (noToSkip > 0)
					{
						skipForward(src, noToSkip, 0);
					}
					// System.out.println(ehd.getLocation());
					previousHeader = null;
				}
				else
				{
					// The entry contained meta information about the next
					// entry.
					previousHeader = entryHeader;
				}
				entryHeader = readEntryHeader(src, fileNameCharset, previousHeader);
			}
		}
		catch (WrappedIOException e)
		{
			throw e;
		}
		catch (RuntimeException e)
		{
			throw new TarFileParseException(e);
		}
	}
}
