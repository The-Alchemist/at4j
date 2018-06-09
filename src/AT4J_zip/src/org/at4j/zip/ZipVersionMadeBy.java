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
package org.at4j.zip;

import java.util.HashMap;
import java.util.Map;

import org.at4j.support.lang.UnsignedByte;

/**
 * This is an enumeration over the different Zip implementations that can be
 * used to create Zip files. It uses Info-Zip's code assignment rather than
 * PKWare's.
 * @author Karl Gustafsson
 * @since 1.0
 */
public enum ZipVersionMadeBy
{
	MSDOS("MS-DOS, OS/2", 0), AMIGA("amiga", 1), OPENVMS("OpenVMS", 2), UNIX("Unix", 3), VMCMS("VM/CMS", 4), ATARI_ST("Atari ST", 5), OS2_HPFS("OS/2 HPFS", 6), MACINTOSH("Macintosh", 7), Z_SYSTEM("Z-System", 8), CPM("CP/M", 9), WINDOWS_NTFS(
			"Windows NTFS", 11), SMS_QDOS("SMS/QDOS", 12), ACORN_RISC("Acorn RISC", 13), VFAT("VFAT", 14), ALT_MVS("Alternate MVS", 15), BEOS("BeOS", 16), TANDEM("Tandem", 17), THEOS("THEOS", 18), OSX("OS X", 19), ATHEOS("AtheOS/Syllable",
			30);

	private final static Map<UnsignedByte, ZipVersionMadeBy> CODE_MAP = new HashMap<UnsignedByte, ZipVersionMadeBy>(19);

	static
	{
		CODE_MAP.put(MSDOS.getCode(), MSDOS);
		CODE_MAP.put(AMIGA.getCode(), AMIGA);
		CODE_MAP.put(OPENVMS.getCode(), OPENVMS);
		CODE_MAP.put(UNIX.getCode(), UNIX);
		CODE_MAP.put(VMCMS.getCode(), VMCMS);
		CODE_MAP.put(ATARI_ST.getCode(), ATARI_ST);
		CODE_MAP.put(OS2_HPFS.getCode(), OS2_HPFS);
		CODE_MAP.put(MACINTOSH.getCode(), MACINTOSH);
		CODE_MAP.put(Z_SYSTEM.getCode(), Z_SYSTEM);
		CODE_MAP.put(CPM.getCode(), CPM);
		CODE_MAP.put(WINDOWS_NTFS.getCode(), WINDOWS_NTFS);
		CODE_MAP.put(SMS_QDOS.getCode(), SMS_QDOS);
		CODE_MAP.put(ACORN_RISC.getCode(), ACORN_RISC);
		CODE_MAP.put(VFAT.getCode(), VFAT);
		CODE_MAP.put(ALT_MVS.getCode(), ALT_MVS);
		CODE_MAP.put(BEOS.getCode(), BEOS);
		CODE_MAP.put(TANDEM.getCode(), TANDEM);
		CODE_MAP.put(THEOS.getCode(), THEOS);
		CODE_MAP.put(OSX.getCode(), OSX);
		CODE_MAP.put(ATHEOS.getCode(), ATHEOS);
	}

	private final String m_tag;
	private final UnsignedByte m_code;

	private ZipVersionMadeBy(String tag, int code)
	{
		m_tag = tag;
		m_code = UnsignedByte.valueOf(code);
	}

	/**
	 * Get the code associated with this Zip version made by enum constant. The
	 * code is used to represent this version in the Zip file header.
	 * @return The code associated with this Zip version made by enum constant.
	 */
	public UnsignedByte getCode()
	{
		return m_code;
	}

	/**
	 * Get the Zip version made by enum constant that corresponds to the code.
	 * @param code The version made by code.
	 * @return The Zip version made by enum constant that corresponds to the
	 * code.
	 * @throws IllegalArgumentException If there is no Zip version made by enum
	 * constant associated with the supplied code.
	 */
	public static ZipVersionMadeBy valueOf(UnsignedByte code) throws IllegalArgumentException
	{
		ZipVersionMadeBy res = CODE_MAP.get(code);
		if (res == null)
		{
			throw new IllegalArgumentException("Unknown Zip version made by code " + code);
		}
		return res;
	}

	@Override
	public String toString()
	{
		return m_tag;
	}
}
