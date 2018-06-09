package org.at4j.zip.ef;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.at4j.test.support.At4JTestCase;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class NtfsExtraFieldTest extends At4JTestCase
{
	@Test
	public void testCreateAndParse()
	{
		Date d = getUtcDate("20090405083912");
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		root.setLastModified(d.getTime());

		NtfsExtraField zeef = NtfsExtraFieldFactory.INSTANCE.create(true, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertEquals(d, zeef.getLastModified().getDate());
		assertEquals(d, zeef.getCreationTime().getDate());

		zeef = NtfsExtraFieldParser.INSTANCE.parse(zeef.encode(null), true);
		assertEquals(d, zeef.getLastModified().getDate());
		assertEquals(d, zeef.getCreationTime().getDate());
	}
}
