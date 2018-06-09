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
package org.at4j.zip.builder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.at4j.zip.ZipInternalFileAttributes;
import org.at4j.zip.extattrs.UnixEntityType;
import org.entityfs.el.AbsoluteLocation;

/**
 * This is the {@link InternalFileAttributesStrategy} implementation that is
 * used by default by the {@link ZipBuilder}.
 * <p>
 * It has a set of file extensions for files that it thinks are text files. This
 * set can be modified at will by clients. The default list was created by
 * taking all file name extensions for file types that could be suspected to
 * contain character data from the {@code mime.types} configuration file in the
 * Apache web server version 2.0.63.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class DefaultInternalFileAttributesStrategy implements InternalFileAttributesStrategy
{
	// This array contains all file name extensions that implies that the file
	// contains character data. The file name extensions comes from Apache's
	// mime.types configuration file as of Apache 2.0.63
	private static final String[] DEFAULT_TEXT_FILE_NAME_EXTENSIONS = new String[] { "atom", "atomcat", "atomsvc", "ccxml", "davmount", "ecma", "js", "json", "mathml", "mscml", "pls", "ai", "eps", "ps", "rdf", "rif", "rnc", "rl", "rs",
			"rsd", "rss", "sbml", "sdp", "shf", "smi", "smil", "srx", "grxml", "ssml", "xdp", "mpkg", "cdxml", "wbs", "pml", "ppd", "es3", "et3", "kml", "vcg", "zmm", "irp", "lbe", "dd2", "mxl", "sdkm", "sdkd", "xsm", "bdm", "xdm", "uoml",
			"wbxml", "zaz", "vxml", "wsdl", "wspolicy", "csh", "latex", "crd", "sh", "tcl", "tex", "texinfo", "texi", "src", "xenc", "xhtml", "xht", "xml", "xsl", "dtd", "xop", "xslt", "xspf", "mxml", "xhvml", "xvml", "xvm", "svg", "eml",
			"mime", "ics", "ifb", "css", "csv", "html", "htm", "txt", "text", "conf", "def", "list", "log", "in", "dsc", "rtx", "sgml", "sgm", "tsv", "t", "tr", "roff", "man", "me", "ms", "uri", "uris", "urls", "fly", "flx", "3dml",
			"spot", "jad", "wml", "wmls", "s", "asm", "c", "cc", "cxx", "cpp", "h", "hh", "dic", "f", "for", "f77", "f90", "p", "pas", "java", "etx", "uu", "vcs", "vcf" };

	private final Set<String> m_textFileNameExtensions;

	/**
	 * Create a default internal file attributes strategy instance using the
	 * default list of text file name extensions.
	 */
	public DefaultInternalFileAttributesStrategy()
	{
		m_textFileNameExtensions = new HashSet<String>(Arrays.asList(DEFAULT_TEXT_FILE_NAME_EXTENSIONS));
	}

	/**
	 * Get the set containing the file name extensions for files that this
	 * strategy object thinks are text files. The set returned is the actual set
	 * used by this object, so updates to that set will be reflected in this
	 * object's behavior.
	 * <p>
	 * Extensions are added to the set without the leading dot.
	 * @return The set containing the file name extensions for files that this
	 * object thinks are text files.
	 */
	public Set<String> getTextFileNameExtensionSet()
	{
		return m_textFileNameExtensions;
	}

	public ZipInternalFileAttributes createInternalFileAttributes(UnixEntityType type, AbsoluteLocation loc)
	{
		ZipInternalFileAttributes res = new ZipInternalFileAttributes();
		res.setTextFile(m_textFileNameExtensions.contains(loc.getExtension()));
		return res;
	}
}
