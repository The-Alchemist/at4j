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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.entityfs.DataSource;
import org.entityfs.RandomAccess;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is the Tar entry handler delegate used by {@link TarFile} to build its
 * tree of the Tar file contents.
 * @author Karl Gustafsson
 * @since 1.0
 */
class TarFileContentsBuildingEntryHandlerDelegate implements TarEntryHandlerDelegate
{
	private static final String USTAR_MAGIC = "ustar";

	private static class TarEntryNode
	{
		private TarEntryHeaderData m_headerData;
		private Map<String, TarEntryNode> m_childNodes = new HashMap<String, TarEntryNode>();
		private long m_startPosOfFileData;
	}

	private final TarEntryCollaborator m_collaborator;
	private final TarEntryNode m_rootNode = new TarEntryNode();
	private int m_numberOfEntries;

	TarFileContentsBuildingEntryHandlerDelegate(TarEntryCollaborator collaborator)
	{
		m_collaborator = collaborator;
	}

	private void putInTree(TarEntryNode n, TarEntryHeaderData headerData, LinkedList<String> pathSegmentStack, long startPosOfFileData)
	{
		String nextPathSegment = pathSegmentStack.remove(0);
		TarEntryNode childNode = n.m_childNodes.get(nextPathSegment);
		if (childNode == null)
		{
			childNode = new TarEntryNode();
			n.m_childNodes.put(nextPathSegment, childNode);
		}

		if (pathSegmentStack.isEmpty())
		{
			childNode.m_headerData = headerData;
			childNode.m_startPosOfFileData = startPosOfFileData;
		}
		else
		{
			putInTree(childNode, headerData, pathSegmentStack, startPosOfFileData);
		}
	}

	public long handle(TarEntryHeaderData ehd, DataSource src)
	{
		m_numberOfEntries++;
		if (src instanceof RandomAccess)
		{
			putInTree(m_rootNode, ehd, ehd.getLocation().getPathSegmentStack(), ((RandomAccess) src).getFilePointer());
		}
		else
		{
			throw new RuntimeException("Bug: expected a RandomAccess source");
		}

		// Skip past the entry's data to the next header
		return ehd.getFileSize();
	}

	private TarDirectoryEntry createDirectoryEntry(TarEntryHeaderData hd, AbsoluteLocation location, Map<String, TarEntry> childEntries, TarEntryCollaborator collaborator)
	{
		if ((hd != null) && USTAR_MAGIC.equals(hd.getMagic()))
		{
			// Ustar format. Do we have PAX variables?
			if (hd.getVariables() != null)
			{
				return new PaxDirectoryEntry(hd, location, collaborator, childEntries);
			}
			else
			{
				return new UstarDirectoryEntry(hd, location, collaborator, childEntries);
			}
		}
		else
		{
			// Old Tar format
			return new TarDirectoryEntry(hd, location, collaborator, childEntries);
		}
	}

	private TarFileEntry createFileEntry(TarEntryHeaderData hd, long startPosOfFileData, TarEntryCollaborator collaborator)
	{
		if (USTAR_MAGIC.equals(hd.getMagic()))
		{
			// Ustar format. Do we have PAX variables?
			if (hd.getVariables() != null)
			{
				return new PaxFileEntry(hd, startPosOfFileData, collaborator);
			}
			else
			{
				return new UstarFileEntry(hd, startPosOfFileData, collaborator);
			}
		}
		else
		{
			// Old Tar format
			return new TarFileEntry(hd, startPosOfFileData, collaborator);
		}
	}

	private TarSymbolicLinkEntry createSymbolicLinkEntry(TarEntryHeaderData hd, TarEntryCollaborator collaborator)
	{
		if (USTAR_MAGIC.equals(hd.getMagic()))
		{
			// Ustar format. Do we have PAX variables
			if (hd.getVariables() != null)
			{
				return new PaxSymbolicLinkEntry(hd, collaborator);
			}
			else
			{
				return new UstarSymbolicLinkEntry(hd, collaborator);
			}
		}
		else
		{
			// Old Tar format
			return new TarSymbolicLinkEntry(hd, collaborator);
		}
	}

	@SuppressWarnings("unchecked")
	private TarEntry createEntries(TarEntryNode n, AbsoluteLocation nodeLocation, Map<AbsoluteLocation, TarEntry> entries, TarEntryCollaborator collaborator)
	{
		TarEntryHeaderData hd = n.m_headerData;
		if (n.m_childNodes.size() > 0)
		{
			if ((hd != null) && (!hd.isDirectory()))
			{
				throw new TarFileParseException("The entry " + hd.getLocation() + " has child entries but is not a directory");
			}

			Map<String, TarEntry> childEntries = new HashMap<String, TarEntry>(n.m_childNodes.size());
			for (Map.Entry<String, TarEntryNode> childNodes : n.m_childNodes.entrySet())
			{
				AbsoluteLocation childLocation = nodeLocation.getChildLocation(childNodes.getKey());
				TarEntry childEntry = createEntries(childNodes.getValue(), childLocation, entries, collaborator);
				entries.put(childLocation, childEntry);
				childEntries.put(childNodes.getKey(), childEntry);
				// System.out.println(nodeLocation + "/" + childNodes.getKey());
			}

			return createDirectoryEntry(hd, nodeLocation, childEntries, collaborator);
		}
		else
		{
			// No child nodes
			if ((hd == null) || hd.isDirectory())
			{
				assert (hd == null) || ((hd.getTypeFlag() == TarConstants.DIRECTORY_TYPE_FLAG) || (!"ustar".equals(hd.getMagic())));
				return createDirectoryEntry(hd, nodeLocation, Collections.EMPTY_MAP, collaborator);
			}
			else if ((hd.getTypeFlag() == TarConstants.FILE_TYPE_FLAG) || (hd.getTypeFlag() == TarConstants.ALT_FILE_TYPE_FLAG))
			{
				// Regular file
				return createFileEntry(hd, n.m_startPosOfFileData, collaborator);
			}
			else if (hd.getTypeFlag() == TarConstants.SYMBOLIC_LINK_TYPE_FLAG)
			{
				// Symbolic link
				return createSymbolicLinkEntry(hd, collaborator);
			}
			else
			{
				// Don't understand this. Print a warning and treat it as a
				// regular file
				System.err.println("Don't understand the entry type flag " + hd.getTypeFlag() + ". This entry will be treated as a regular file.");
				return createFileEntry(hd, n.m_startPosOfFileData, collaborator);
			}
		}
	}

	/**
	 * This is called by {@link TarFile} after the file has been parsed.
	 * @return
	 */
	TarFileContents getContents()
	{
		Map<AbsoluteLocation, TarEntry> entries = new HashMap<AbsoluteLocation, TarEntry>(m_numberOfEntries);
		TarDirectoryEntry rootEntry = (TarDirectoryEntry) createEntries(m_rootNode, AbsoluteLocation.ROOT_DIR, entries, m_collaborator);
		entries.put(AbsoluteLocation.ROOT_DIR, rootEntry);
		return new TarFileContents(rootEntry, entries);
	}
}
