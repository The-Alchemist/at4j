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
import java.util.Comparator;

import org.at4j.support.io.LittleEndianBitInputStream;
import org.at4j.support.io.LittleEndianBitOutputStream;
import org.at4j.test.support.At4JTestCase;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class HighValueBranchHuffmanTreeTest extends At4JTestCase
{
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
	public void testCreateCanonicalTree1() throws IOException
	{
		int[] frequencies = new int[] { 100, 100, 100, 100, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 9, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Gives the following tree
		//  0: 00
		//  1: 01
		//  2: 10
		//  3: 110
		//  4: 11100
		//  5: 11101
		//  6: 111100
		//  7: 111101
		//  8: 111110
		//  9: 11111100
		// 10: 111111010
		// 11: 111111011
		// 12: 111111100
		// 13: 111111101
		// 14: 111111110
		// 15: 111111111

		assertEquals(2, t.getMinLength());
		assertEquals(9, t.getMaxLength());
		assertEquals(8, t.m_numberOfLengths);
		assertTrue(Arrays.equals(t.m_baseValuesPerLength, new int[] { 0, 6, 14, 28, 60, 126, 252, 506 }));
		assertTrue(Arrays.equals(t.m_limitsPerLength, new int[] { 2, 6, 13, 29, 62, 125, 252 }));
		assertTrue(Arrays.equals(t.m_symbolOffsetPerLength, new int[] { 0, 3, 4, 4, 6, 9, 9, 10 }));
	}

	@Test
	public void testCreateCanonicalTree3() throws IOException
	{
		int[] frequencies = new int[] { 100, 100, 100, 100, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 9, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Gives (roughly) the following tree
		//  0: 00
		//  1: 01
		//  2: 10
		//  3: 110
		//  4: 11100
		//  5: 11101
		//  6: 111100
		//  7: 111101
		//  8: 111110
		//  9: 11111100
		// 10: 111111010
		// 11: 111111011
		// 12: 111111100
		// 13: 111111101
		// 14: 111111110
		// 15: 111111111

		assertEquals(2, t.getMinLength());
		assertEquals(9, t.getMaxLength());
		assertEquals(8, t.m_numberOfLengths);
		assertTrue(Arrays.equals(t.m_baseValuesPerLength, new int[] { 0, 6, 14, 28, 60, 126, 252, 506 }));
		assertTrue(Arrays.equals(t.m_limitsPerLength, new int[] { 2, 6, 13, 29, 62, 125, 252 }));
		assertTrue(Arrays.equals(t.m_symbolOffsetPerLength, new int[] { 0, 3, 4, 4, 6, 9, 9, 10 }));
	}

	@Test
	public void testCreateCanonicalTreeWithDifferentFrequenciesGettingTheSameTreeDepth() throws IOException
	{
		int[] frequencies = new int[] { 2, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 9, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Gives the following tree
		//  0: 0
		//  1: 1

		assertEquals(1, t.getMinLength());
		assertEquals(1, t.getMaxLength());
		assertEquals(1, t.m_numberOfLengths);
		assertTrue(Arrays.equals(t.m_baseValuesPerLength, new int[] { 0 }));
		assertTrue(Arrays.equals(t.m_limitsPerLength, new int[0]));
		assertTrue(Arrays.equals(t.m_symbolOffsetPerLength, new int[] { 0 }));
	}

	@Test
	public void testCreateCanonicalTreeWithLimitedHeight1() throws IOException
	{
		int[] frequencies = new int[] { 100, 100, 100, 100, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 7, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		assertEquals(2, t.getMinLength());
		assertEquals(7, t.getMaxLength());
		assertEquals(6, t.m_numberOfLengths);
	}

	@Test
	public void testCreateCanonicalTreeWithLimitedHeight2() throws IOException
	{
		// Minimal max height
		int[] frequencies = new int[] { 100, 100, 100, 100, 10, 10, 10, 10, 10, 1, 1, 1, 1, 1, 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 4, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Gives the following tree
		//  1: 0000
		//  2: 0001
		//  3: 0010
		//  4: 0011
		//  5: 0100
		//  6: 0101
		//  7: 0110
		//  8: 0111
		//  9: 1000
		// 10: 1001
		// 11: 1010
		// 12: 1011
		// 13: 1100
		// 14: 1101
		// 15: 1110
		// 16: 1111

		assertEquals(4, t.getMinLength());
		assertEquals(4, t.getMaxLength());
		assertEquals(1, t.m_numberOfLengths);
		assertTrue(Arrays.equals(t.m_baseValuesPerLength, new int[] { 0 }));
		assertTrue(Arrays.equals(t.m_limitsPerLength, new int[0]));
		assertTrue(Arrays.equals(t.m_symbolOffsetPerLength, new int[] { 0 }));
	}

	private static class SymbolComparator implements Comparator<int[]>
	{
		private static final SymbolComparator INSTANCE = new SymbolComparator();

		@Override
		public int compare(int[] o1, int[] o2)
		{
			return o1[0] - o2[0];
		}
	}

	private int[] getSymbolLengths(int[][] symbolsAndLengths)
	{
		int[][] tmp = new int[symbolsAndLengths.length][];
		System.arraycopy(symbolsAndLengths, 0, tmp, 0, symbolsAndLengths.length);
		Arrays.sort(tmp, SymbolComparator.INSTANCE);
		int[] res = new int[tmp.length];
		for (int i = 0; i < tmp.length; i++)
		{
			res[i] = tmp[i][1];
		}
		return res;
	}

	@Test
	public void testEncodeAndDecode1() throws IOException
	{
		int[] frequencies = new int[] { 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 4, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t1 = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Use the tree to encode data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream bos = new LittleEndianBitOutputStream(baos);
		t1.write(bos, 0);
		t1.write(bos, 1);
		t1.write(bos, 1);
		t1.write(bos, 0);
		bos.padToByteBoundary();
		bos.close();
		byte[] encoded = baos.toByteArray();

		// Use the information from the created tree to create a new tree for
		// reading data.
		int[][] symbolsAndLengths = t1.getSortedSymbolSequenceNosAndCodeLengths();
		HighValueBranchHuffmanTree t2 = new HighValueBranchHuffmanTree(getSymbolLengths(symbolsAndLengths), t1.getMinLength(), t1.getMaxLength(), false);
		ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
		LittleEndianBitInputStream bis = new LittleEndianBitInputStream(bais);
		assertEquals(0, t2.readNext(bis));
		assertEquals(1, t2.readNext(bis));
		assertEquals(1, t2.readNext(bis));
		assertEquals(0, t2.readNext(bis));
		bis.close();
	}

	@Test
	public void testEncodeAndDecode2() throws IOException
	{
		// swiss miss
		int[] frequencies = new int[] { 5, 2, 1, 1, 1 };
		int[] codeLengths = HighValueBranchHuffmanTree.createCodeLengths(frequencies, frequencies.length, 4, new EncodingScratchpad());
		int[] minAndMaxLength = getMinAndMaxLengths(codeLengths);
		HighValueBranchHuffmanTree t1 = new HighValueBranchHuffmanTree(codeLengths, minAndMaxLength[0], minAndMaxLength[1], true);

		// Use the tree to encode data
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		LittleEndianBitOutputStream bos = new LittleEndianBitOutputStream(baos);
		t1.write(bos, (short) 0);
		t1.write(bos, (short) 4);
		t1.write(bos, (short) 1);
		t1.write(bos, (short) 0);
		t1.write(bos, (short) 0);
		t1.write(bos, (short) 2);
		t1.write(bos, (short) 3);
		t1.write(bos, (short) 1);
		t1.write(bos, (short) 0);
		t1.write(bos, (short) 0);
		bos.padToByteBoundary();
		bos.close();
		byte[] encoded = baos.toByteArray();

		// Use the information from the created tree to create a new tree for
		// reading data.
		int[][] symbolsAndLengths = t1.getSortedSymbolSequenceNosAndCodeLengths();
		HighValueBranchHuffmanTree t2 = new HighValueBranchHuffmanTree(getSymbolLengths(symbolsAndLengths), t1.getMinLength(), t1.getMaxLength(), false);
		ByteArrayInputStream bais = new ByteArrayInputStream(encoded);
		LittleEndianBitInputStream bis = new LittleEndianBitInputStream(bais);
		assertEquals(0, t2.readNext(bis));
		assertEquals(4, t2.readNext(bis));
		assertEquals(1, t2.readNext(bis));
		assertEquals(0, t2.readNext(bis));
		assertEquals(0, t2.readNext(bis));
		assertEquals(2, t2.readNext(bis));
		assertEquals(3, t2.readNext(bis));
		assertEquals(1, t2.readNext(bis));
		assertEquals(0, t2.readNext(bis));
		assertEquals(0, t2.readNext(bis));
		bis.close();
	}
}
