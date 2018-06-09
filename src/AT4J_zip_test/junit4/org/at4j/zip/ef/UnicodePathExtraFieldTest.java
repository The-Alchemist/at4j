package org.at4j.zip.ef;

import static org.junit.Assert.assertEquals;

import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.Directory;
import org.entityfs.EFile;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.entityfs.util.Directories;
import org.junit.Test;

public class UnicodePathExtraFieldTest
{
	@Test
	public void testCreateAndParse()
	{
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		EFile f1 = Directories.newFile(root, "räksmörgås.txt");

		UnicodePathExtraField zeef = UnicodePathExtraFieldFactory.INSTANCE.create(true, new AbsoluteLocation("/räksmörgås.txt"), UnixEntityType.DIRECTORY, f1, null);
		assertEquals(new AbsoluteLocation("/räksmörgås.txt"), zeef.getAbsolutePath());

		zeef = UnicodePathExtraFieldParser.INSTANCE.parse(zeef.encode(new DummyZipBuilderConfiguration()), true);
		assertEquals(new AbsoluteLocation("/räksmörgås.txt"), zeef.getAbsolutePath());
	}
}
