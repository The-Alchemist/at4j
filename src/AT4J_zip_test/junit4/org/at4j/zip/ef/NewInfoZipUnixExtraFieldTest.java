package org.at4j.zip.ef;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class NewInfoZipUnixExtraFieldTest
{
	@Test
	public void testCreateAndParse()
	{
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();

		NewInfoZipUnixExtraField zeef = new NewInfoZipUnixExtraFieldFactory(1234, 5678).create(false, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertNull(zeef.getUid());
		assertNull(zeef.getGid());
		assertSame(zeef, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);

		zeef = NewInfoZipUnixExtraFieldParser.INSTANCE.parse(zeef.encode(null), false);
		assertNull(zeef.getUid());
		assertNull(zeef.getGid());
		assertSame(zeef, NewInfoZipUnixExtraField.CENTRAL_HEADER_VERSION);
	}

	@Test
	public void testCreateAndParseInLocalHeader()
	{
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();

		NewInfoZipUnixExtraField zeef = new NewInfoZipUnixExtraFieldFactory(1234, 5678).create(true, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, null);
		assertEquals(1234, zeef.getUid().intValue());
		assertEquals(5678, zeef.getGid().intValue());

		zeef = NewInfoZipUnixExtraFieldParser.INSTANCE.parse(zeef.encode(null), true);
		assertEquals(1234, zeef.getUid().intValue());
		assertEquals(5678, zeef.getGid().intValue());
	}
}
