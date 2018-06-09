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

import org.at4j.comp.bzip2.prog.ProgSuite;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * @author Karl Gustafsson
 * @since 1.0
 */
@RunWith(Suite.class)
@SuiteClasses( { BlockEncoderTest.class, BurrowsWheelerDecoderTest.class, BurrowsWheelerEncoderTest.class, BZip2CompressorInputStreamTest.class, BZip2InputStreamTest.class, BZip2OutputStreamTest.class,
		BZip2ReadableAndWritableFileTest.class, HighValueBranchHuffmanTreeTest.class, RLEDecodingInputStreamTest.class, ThreeWayRadixQuicksortTest.class,

		ProgSuite.class })
public class BZip2Suite
{
	// Nothing
}
