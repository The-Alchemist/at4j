package org.at4j.zip.ef;

import static org.junit.Assert.assertEquals;

import org.at4j.zip.builder.ZipEntrySettings;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class UnicodeCommentExtraFieldTest
{
	@Test
	public void testCreateAndParse()
	{
		ZipEntrySettings zes = new ZipEntrySettings().setComment("Comment, ju!");
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();

		UnicodeCommentExtraField zeef = UnicodeCommentExtraFieldFactory.INSTANCE.create(true, AbsoluteLocation.ROOT_DIR, UnixEntityType.DIRECTORY, root, zes);
		assertEquals("Comment, ju!", zeef.getComment());

		zeef = UnicodeCommentExtraFieldParser.INSTANCE.parse(zeef.encode(new DummyZipBuilderConfiguration()), true);
		assertEquals("Comment, ju!", zeef.getComment());
	}
}
