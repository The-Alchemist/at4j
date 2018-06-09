package org.at4j.zip.extattrs;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.at4j.zip.ZipVersionMadeBy;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class MsDosExternalFileAttributesTest
{
	@Test
	public void testEncodeAndDecodeAttributes()
	{
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		MsDosExternalFileAttributes efa = new MsDosExternalFileAttributesFactory(new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE, MsDosFileAttributes.READ_ONLY), new MsDosExternalFileAttributes(MsDosFileAttributes.ARCHIVE,
				MsDosFileAttributes.READ_ONLY, MsDosFileAttributes.SUB_DIRECTORY)).create(UnixEntityType.DIRECTORY, new AbsoluteLocation("/foo"), root);

		Set<MsDosFileAttributes> attrs = new HashSet<MsDosFileAttributes>();
		attrs.add(MsDosFileAttributes.ARCHIVE);
		attrs.add(MsDosFileAttributes.READ_ONLY);
		attrs.add(MsDosFileAttributes.SUB_DIRECTORY);
		assertEquals(attrs, efa.getAttributes());
		assertEquals(ZipVersionMadeBy.MSDOS, efa.getVersionMadeBy());

		assertEquals(efa, MsDosExternalFileAttributesParser.INSTANCE.parse(ZipVersionMadeBy.MSDOS, efa.getEncodedValue().getBigEndianByteArray()));
	}
}
