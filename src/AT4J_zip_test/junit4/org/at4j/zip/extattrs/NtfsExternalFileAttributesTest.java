package org.at4j.zip.extattrs;

import static org.junit.Assert.assertEquals;

import java.util.HashSet;
import java.util.Set;

import org.at4j.zip.ZipVersionMadeBy;
import org.entityfs.Directory;
import org.entityfs.el.AbsoluteLocation;
import org.entityfs.ram.RamFileSystemBuilder;
import org.junit.Test;

public class NtfsExternalFileAttributesTest
{
	@Test
	public void testEncodeAndDecodeAttributes()
	{
		Directory root = new RamFileSystemBuilder().create().getRootDirectory();
		NtfsExternalFileAttributes efa = new NtfsExternalFileAttributesFactory(new NtfsExternalFileAttributes(NtfsFileAttributes.ARCHIVE, NtfsFileAttributes.READ_ONLY), new NtfsExternalFileAttributes(NtfsFileAttributes.ARCHIVE,
				NtfsFileAttributes.READ_ONLY, NtfsFileAttributes.DIRECTORY)).create(UnixEntityType.DIRECTORY, new AbsoluteLocation("/foo"), root);

		Set<NtfsFileAttributes> attrs = new HashSet<NtfsFileAttributes>();
		attrs.add(NtfsFileAttributes.ARCHIVE);
		attrs.add(NtfsFileAttributes.READ_ONLY);
		attrs.add(NtfsFileAttributes.DIRECTORY);
		assertEquals(attrs, efa.getAttributes());
		assertEquals(ZipVersionMadeBy.WINDOWS_NTFS, efa.getVersionMadeBy());

		assertEquals(efa, NtfsExternalFileAttributesParser.INSTANCE.parse(ZipVersionMadeBy.WINDOWS_NTFS, efa.getEncodedValue().getBigEndianByteArray()));
	}
}
