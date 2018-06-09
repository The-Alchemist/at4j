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

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

import org.at4j.comp.bzip2.BurrowsWheelerEncoder.BurrowsWheelerEncodingResult;
import org.entityfs.support.io.StreamUtil;
import org.junit.Test;

/**
 * @author Karl Gustafsson
 * @since 1.1
 */
public class BurrowsWheelerEncoderTest extends AbstractBurrowsWheelerTest
{
	private byte[] addOvershoot(byte[] data)
	{
		byte[] res = new byte[data.length + ThreeWayRadixQuicksort.DATA_OVERSHOOT];
		System.arraycopy(data, 0, res, 0, data.length);
		int noCopied = 0;
		while (noCopied < ThreeWayRadixQuicksort.DATA_OVERSHOOT)
		{
			int noToCopy = Math.min(ThreeWayRadixQuicksort.DATA_OVERSHOOT - noCopied, data.length);
			System.arraycopy(data, 0, res, data.length + noCopied, noToCopy);
			noCopied += noToCopy;
		}
		return res;
	}

	private byte[] truncateArray(byte[] data, int length)
	{
		byte[] res = new byte[length];
		System.arraycopy(data, 0, res, 0, length);
		return res;
	}

	@Test
	public void testEncode1()
	{
		// Example from the Burrows Wheeler Wikipedia page
		byte[] data = createData("^BANANAx");
		BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), data.length, new EncodingScratchpad()).encode();
		assertTrue(Arrays.equals(truncateArray(er.m_lastColumn, data.length), new byte[] { 'B', 'N', 'N', '^', 'A', 'A', 'x', 'A' }));
		assertEquals(6, er.m_firstPointer);
	}

	@Test
	public void testEncode2()
	{
		// Example from Data Compression, The Complete Reference, Fourth Edition
		byte[] data = createData("swiss miss");
		BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), data.length, new EncodingScratchpad()).encode();
		assertTrue(Arrays.equals(truncateArray(er.m_lastColumn, data.length), createData("swm siisss")));
		assertEquals(8, er.m_firstPointer);
	}

	@Test
	public void testEncode3() throws IOException
	{
		byte[] data = new byte[46];
		boolean lower = true;
		for (int j = 0; j < 46; j++)
		{
			data[j] = (byte) (lower ? 0 : 1);
			lower = j % 3 == 0 ? lower : !lower;
		}

		// Data:
		// AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA

		// Matrix
		//  0: AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA
		//  1: ABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA A
		//  2: BAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AA
		//  3: AABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AAB
		//  4: ABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABA
		//  5: BAABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAA
		//  6: AABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAAB
		//  7: ABA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAABA
		//  8: BA ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAA
		//  9: A ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAAB
		// 10: ABAABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA
		// 11: BAABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA A
		// 12: AABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA AB
		// 13: ABAABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABA
		// 14: BAABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAA
		// 15: AABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAAB
		// 16: ABAA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAABA
		// 17: BAA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAA
		// 18: AA BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAAB
		// 19: A BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAABA
		// 20: BAABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA
		// 21: AABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA B
		// 22: ABAABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BA
		// 23: BAABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAA
		// 24: AABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAAB
		// 25: ABAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABA
		// 26: BAAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAA
		// 27: AAB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAAB
		// 28: AB AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAABA
		// 29: B AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAA
		// 30: AABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB
		// 31: ABAABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB A
		// 32: BAABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AA
		// 33: AABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AAB
		// 34: ABAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABA
		// 35: BAABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAA
		// 36: AABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAAB
		// 37: ABA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAABA
		// 38: BA ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAA
		// 39: A ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAAB
		// 40: ABAABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA
		// 41: BAABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA A
		// 42: AABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA AB
		// 43: ABA AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABA
		// 44: BA AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAA
		// 45: A AABAABAABA ABAABAABAA BAABAABAAB AABAABAABA ABAAB

		// Matrix, sorted
		// 45: AAABAABAABAABAABAABAABAABAABAABAABAABAABAABAAB
		// 42: AABAAABAABAABAABAABAABAABAABAABAABAABAABAABAAB
		// 39: AABAABAAABAABAABAABAABAABAABAABAABAABAABAABAAB
		// 36: AABAABAABAAABAABAABAABAABAABAABAABAABAABAABAAB
		// 12: AABAABAABAABAABAABAABAABAABAABAABAAABAABAABAAB
		// 15: AABAABAABAABAABAABAABAABAABAABAAABAABAABAABAAB
		// 18: AABAABAABAABAABAABAABAABAABAAABAABAABAABAABAAB
		// 21: AABAABAABAABAABAABAABAABAAABAABAABAABAABAABAAB
		// 24: AABAABAABAABAABAABAABAAABAABAABAABAABAABAABAAB
		// 27: AABAABAABAABAABAABAAABAABAABAABAABAABAABAABAAB
		// 30: AABAABAABAABAABAAABAABAABAABAABAABAABAABAABAAB
		// 33: AABAABAABAABAAABAABAABAABAABAABAABAABAABAABAAB
		//  0: AABAABAABAABAABAABAABAABAABAABAABAABAABAABAABA
		//  3: AABAABAABAABAABAABAABAABAABAABAABAABAABAABAAAB
		//  6: AABAABAABAABAABAABAABAABAABAABAABAABAABAAABAAB
		//  9: AABAABAABAABAABAABAABAABAABAABAABAABAAABAABAAB
		// 43: ABAAABAABAABAABAABAABAABAABAABAABAABAABAABAABA
		// 40: ABAABAAABAABAABAABAABAABAABAABAABAABAABAABAABA
		// 37: ABAABAABAAABAABAABAABAABAABAABAABAABAABAABAABA
		// 10: ABAABAABAABAABAABAABAABAABAABAABAABAAABAABAABA
		// 13: ABAABAABAABAABAABAABAABAABAABAABAAABAABAABAABA
		// 16: ABAABAABAABAABAABAABAABAABAABAAABAABAABAABAABA
		// 19: ABAABAABAABAABAABAABAABAABAAABAABAABAABAABAABA
		// 22: ABAABAABAABAABAABAABAABAAABAABAABAABAABAABAABA
		// 25: ABAABAABAABAABAABAABAAABAABAABAABAABAABAABAABA
		// 28: ABAABAABAABAABAABAAABAABAABAABAABAABAABAABAABA
		// 31: ABAABAABAABAABAAABAABAABAABAABAABAABAABAABAABA
		// 34: ABAABAABAABAAABAABAABAABAABAABAABAABAABAABAABA
		//  1: ABAABAABAABAABAABAABAABAABAABAABAABAABAABAABAA
		//  4: ABAABAABAABAABAABAABAABAABAABAABAABAABAABAAABA
		//  7: ABAABAABAABAABAABAABAABAABAABAABAABAABAAABAABA
		// 44: BAAABAABAABAABAABAABAABAABAABAABAABAABAABAABAA
		// 41: BAABAAABAABAABAABAABAABAABAABAABAABAABAABAABAA
		// 38: BAABAABAAABAABAABAABAABAABAABAABAABAABAABAABAA
		// 35: BAABAABAABAAABAABAABAABAABAABAABAABAABAABAABAA
		// 32: BAABAABAABAABAAABAABAABAABAABAABAABAABAABAABAA
		// 29: BAABAABAABAABAABAAABAABAABAABAABAABAABAABAABAA
		// 26: BAABAABAABAABAABAABAAABAABAABAABAABAABAABAABAA
		// 23: BAABAABAABAABAABAABAABAAABAABAABAABAABAABAABAA
		// 20: BAABAABAABAABAABAABAABAABAAABAABAABAABAABAABAA
		// 17: BAABAABAABAABAABAABAABAABAABAAABAABAABAABAABAA
		// 14: BAABAABAABAABAABAABAABAABAABAABAAABAABAABAABAA
		// 11: BAABAABAABAABAABAABAABAABAABAABAABAAABAABAABAA
		//  8: BAABAABAABAABAABAABAABAABAABAABAABAABAAABAABAA
		//  5: BAABAABAABAABAABAABAABAABAABAABAABAABAABAAABAA
		//  2: BAABAABAABAABAABAABAABAABAABAABAABAABAABAABAAA

		BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), 46, new EncodingScratchpad()).encode();

		byte[] dataRead = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(truncateArray(er.m_lastColumn, data.length), data.length, calcByteFrequencies(data), er.m_firstPointer).decode(), 60);
		assertTrue(Arrays.equals(data, dataRead));
	}

	@Test
	public void testEncodeAndDecode1() throws IOException
	{
		byte[] data = createData("swiss miss");
		BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), data.length, new EncodingScratchpad()).encode();
		byte[] dataRead = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(truncateArray(er.m_lastColumn, data.length), data.length, calcByteFrequencies(data), er.m_firstPointer).decode(), 32);
		assertTrue(Arrays.equals(data, dataRead));
	}

	@Test
	public void testEncodeAndDecode2() throws IOException
	{
		// This will always return the same number sequence, ensuring that this
		// test method will always be run with the same data.
		Random r = new Random(34834);
		for (int len = 1000; len < 900000; len += 15000)
		{
			byte[] data = createThreeCharAlphabetData(len, r);
			BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), data.length, new EncodingScratchpad()).encode();
			byte[] dataRead = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(truncateArray(er.m_lastColumn, data.length), data.length, calcByteFrequencies(data), er.m_firstPointer).decode(), 16384);
			assertTrue(Arrays.equals(data, dataRead));
		}
	}

	@Test
	public void testEncodeAndDecode3() throws IOException
	{
		// This will always return the same number sequence, ensuring that this
		// test method will always be run with the same data.
		Random r = new Random(40191283);
		for (int len = 1000; len < 900000; len += 15000)
		{
			byte[] data = createData(len, r);
			BurrowsWheelerEncodingResult er = new BurrowsWheelerEncoder(addOvershoot(data), data.length, new EncodingScratchpad()).encode();
			byte[] dataRead = StreamUtil.readStreamFully(new BurrowsWheelerDecoder(er.m_lastColumn, data.length, calcByteFrequencies(data), er.m_firstPointer).decode(), 16384);
			assertTrue(Arrays.equals(data, dataRead));
		}
	}
}
