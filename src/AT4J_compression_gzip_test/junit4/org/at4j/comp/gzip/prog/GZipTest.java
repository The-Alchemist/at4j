package org.at4j.comp.gzip.prog;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.at4j.test.support.At4JTestCase;
import org.entityfs.Directory;
import org.entityfs.EFile;
import org.entityfs.ReadableFile;
import org.entityfs.util.Directories;
import org.entityfs.util.Entities;
import org.entityfs.util.Files;
import org.entityfs.util.cap.entity.ECFileResolvableUtil;
import org.junit.Test;

public class GZipTest extends At4JTestCase
{
	@Test
	public void testDefaultCompressAndDecompress()
	{
		Directory root = createTempDirectory();
		try
		{
			EFile f1 = Directories.newFile(root, "cheese.txt");
			ReadableFile tf = getTestDataFile("the_complete_book_on_cheese.txt");
			Files.copyContents(tf, f1);
			long sizeUncompressed = Files.getSize(f1);

			GZip.main(new String[] { ECFileResolvableUtil.getFileObject(f1).getAbsolutePath() });

			assertFalse(Entities.isValid(f1));

			EFile f1c = Directories.getFile(root, "cheese.txt.gz");
			assertTrue(Files.getSize(f1c) < sizeUncompressed);

			GUnzip.main(new String[] { ECFileResolvableUtil.getFileObject(f1c).getAbsolutePath() });
			assertFalse(Entities.isValid(f1));

			f1 = Directories.getFile(root, "cheese.txt");
			assertEquals(Files.readTextFile(tf), Files.readTextFile(f1));
		}
		finally
		{
			deleteTempDirectory(root);
		}
	}
}
