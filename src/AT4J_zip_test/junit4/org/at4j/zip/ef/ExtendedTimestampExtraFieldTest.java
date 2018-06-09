package org.at4j.zip.ef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Date;

import org.at4j.test.support.At4JTestCase;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class ExtendedTimestampExtraFieldTest extends At4JTestCase
{
	@Test
	public void testCreateAndParse()
	{
		Date d = getUtcDate("20090405083912");
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		root.setLastModified(d.getTime());

		ExtendedTimestampExtraField zeef = ExtendedTimestampExtraFieldFactory.INSTANCE.create(true, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertEquals(d, zeef.getLastModified());
		assertNull(zeef.getLastAccessed());
		assertNull(zeef.getOriginalCreationTime());

		zeef = ExtendedTimestampExtraFieldParser.INSTANCE.parse(zeef.encode(null), true);
		assertEquals(d, zeef.getLastModified());
		assertNull(zeef.getLastAccessed());
		assertNull(zeef.getOriginalCreationTime());
	}
}
