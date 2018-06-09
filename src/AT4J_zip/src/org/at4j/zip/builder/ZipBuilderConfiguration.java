package org.at4j.zip.builder;

import java.nio.charset.Charset;

/**
 * This interface defines an API that can be used to query a Zip builder about
 * its configuration.
 * @author Karl Gustafsson
 * @since 1.0
 */
public interface ZipBuilderConfiguration
{
	/**
	 * Get the charset used for encoding text such as comments in the Zip file.
	 * @return The charset used for encoding text in the Zip file.
	 * @see #getFileNameEncodingCharset()
	 */
	Charset getTextEncodingCharset();

	/**
	 * Get the charset used for encoding file names in the Zip file.
	 * @return The charset used for encoding file names in the Zip file.
	 * @see #getTextEncodingCharset()
	 */
	Charset getFileNameEncodingCharset();
}
