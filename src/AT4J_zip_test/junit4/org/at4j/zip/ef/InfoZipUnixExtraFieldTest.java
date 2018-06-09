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

public class InfoZipUnixExtraFieldTest extends At4JTestCase
{
	@Test
	public void testCreateAndParse()
	{
		Date d = getUtcDate("20090405083912");
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		root.setLastModified(d.getTime());

		InfoZipUnixExtraField zeef = new InfoZipUnixExtraFieldFactory(1234, 5678).create(false, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertEquals(d, zeef.getLastModificationTime());
		assertNull(zeef.getUid());
		assertNull(zeef.getGid());

		zeef = InfoZipUnixExtraFieldParser.INSTANCE.parse(zeef.encode(null), false);
		assertEquals(d, zeef.getLastModificationTime());
		assertNull(zeef.getUid());
		assertNull(zeef.getGid());
	}

	@Test
	public void testCreateAndParseInLocalHeader()
	{
		Date d = getUtcDate("20090405083912");
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		root.setLastModified(d.getTime());

		InfoZipUnixExtraField zeef = new InfoZipUnixExtraFieldFactory(1234, 5678).create(true, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertEquals(d, zeef.getLastModificationTime());
		assertEquals(1234, zeef.getUid().intValue());
		assertEquals(5678, zeef.getGid().intValue());

		zeef = InfoZipUnixExtraFieldParser.INSTANCE.parse(zeef.encode(null), true);
		assertEquals(d, zeef.getLastModificationTime());
		assertEquals(1234, zeef.getUid().intValue());
		assertEquals(5678, zeef.getGid().intValue());
	}
}
