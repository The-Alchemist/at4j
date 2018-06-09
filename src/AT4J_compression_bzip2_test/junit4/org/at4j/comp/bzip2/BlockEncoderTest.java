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
package org.at4j.comp.bzip2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import org.at4j.support.io.LittleEndianBitInputStream;
import org.at4j.support.io.LittleEndianBitOutputStream;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BlockEncoderTest
{
	@Test
	public void testCategoryPerNumberOfTreesAndPercentage()
	{
		for (int i = 0; i < BlockEncoder.CATEGORY_PER_NO_OF_TREES_AND_PERCENTAGE.length; i++)
		{
			assertEquals(101, BlockEncoder.CATEGORY_PER_NO_OF_TREES_AND_PERCENTAGE[i].length);
		}
	}

	@Test
	public void testAddRunaAndRunb() throws IOException
	{
		int[] res = new int[1];
		res[0] = -1;
		assertEquals(1, BlockEncoder.addRunaAndRunb(res, 0, 1));
		assertEquals(0, res[0]);

		res = new int[1];
		assertEquals(1, BlockEncoder.addRunaAndRunb(res, 0, 2));
		assertEquals(1, res[0]);

		res = new int[2];
		Arrays.fill(res, -1);
		assertEquals(2, BlockEncoder.addRunaAndRunb(res, 0, 3));
		assertEquals(0, res[0]);
		assertEquals(0, res[1]);

		res = new int[2];
		Arrays.fill(res, -1);
		assertEquals(2, BlockEncoder.addRunaAndRunb(res, 0, 4));
		assertEquals(1, res[0]);
		assertEquals(0, res[1]);

		res = new int[2];
		Arrays.fill(res, -1);
		assertEquals(2, BlockEncoder.addRunaAndRunb(res, 0, 5));
		assertEquals(0, res[0]);
		assertEquals(1, res[1]);

		res = new int[2];
		Arrays.fill(res, -1);
		assertEquals(2, BlockEncoder.addRunaAndRunb(res, 0, 6));
		assertEquals(1, res[0]);
		assertEquals(1, res[1]);

		res = new int[3];
		Arrays.fill(res, -1);
		assertEquals(3, BlockEncoder.addRunaAndRunb(res, 0, 7));
		assertEquals(0, res[0]);
		assertEquals(0, res[1]);
		assertEquals(0, res[2]);

		res = new int[3];
		Arrays.fill(res, -1);
		assertEquals(3, BlockEncoder.addRunaAndRunb(res, 0, 8));
		assertEquals(1, res[0]);
		assertEquals(0, res[1]);
		assertEquals(0, res[2]);

		// Example from Wikipedia's article on bzip2
		res = new int[5];
		assertEquals(5, BlockEncoder.addRunaAndRunb(res, 0, 49));
		// RUNA == 0, RUNB == 1
		assertTrue(Arrays.equals(new int[] { 0, 1, 0, 0, 1 }, res));
	}

	private int[] getMinAndMaxLengths(int[] codeLengths)
	{
		int minLength = codeLengths[0];
		int maxLength = codeLengths[0];
		for (int i = 1; i < codeLengths.length; i++)
		{
			if (codeLengths[i] < minLength)
			{
				minLength = codeLengths[i];
			}
			else if (codeLengths[i] > maxLength)
			{
				maxLength = codeLengths[i];
			}
		}
		return new int[] { minLength, maxLength };
	}

	@Test
	public void testEncodeAndDecodeHuffmanTree() throws IOException
	{
		int[] frequencies = new int[] { 1, 1, 1, 0, 3, 3, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 17, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		assertEquals(2, t.getMinLength());
		assertEquals(4, t.getMaxLength());
		assertEquals(3, t.m_numberOfLengths);
		assertTrue(Arrays.equals(new int[] { 0, 2, 5 }, t.m_symbolOffsetPerLength));
		assertTrue(Arrays.equals(new int[] { 4, 5, 2, 3, 6, 0, 1 }, t.m_symbolSequenceNos));
		assertTrue(Arrays.equals(new int[] { 1, 6 }, t.m_limitsPerLength));
		assertTrue(Arrays.equals(new int[] { 0, 4, 14 }, t.m_baseValuesPerLength));

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream bos = new LittleEndianBitOutputStream(baos);
		BlockEncoder.encodeHuffmanTree(t, (short) 7, bos);
		bos.padToByteBoundary();
		bos.close();

		LittleEndianBitInputStream bis = new LittleEndianBitInputStream(new ByteArrayInputStream(baos.toByteArray()));
		t = BlockDecoder.decodeHuffmanTree(7, bis);
		assertEquals(2, t.getMinLength());
		assertEquals(4, t.getMaxLength());
		assertEquals(3, t.m_numberOfLengths);
		assertTrue(Arrays.equals(new int[] { 0, 2, 5 }, t.m_symbolOffsetPerLength));
		assertTrue(Arrays.equals(new int[] { 4, 5, 2, 3, 6, 0, 1 }, t.m_symbolSequenceNos));
		assertTrue(Arrays.equals(new int[] { 1, 6 }, t.m_limitsPerLength));
		assertTrue(Arrays.equals(new int[] { 0, 4, 14 }, t.m_baseValuesPerLength));
	}
}
