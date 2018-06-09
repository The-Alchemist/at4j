package org.at4j.zip.builder;

import java.nio.charset.Charset;

import org.at4j.comp.CompressionLevel;

/**
 * This object contains configuration for a {@link ZipBuilder}. When a new
 * instance is created, it is initialized with the default settings.
 * <p>
 * The default file entry settings are:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>Compression method</td>
 * <td>Deflate compression with default settings (
 * {@link org.at4j.zip.comp.DeflatedCompressionMethod})</td>
 * </tr>
 * <tr>
 * <td>External file attributes</td>
 * <td>MS DOS external file attributes (
 * {@link org.at4j.zip.extattrs.MsDosExternalFileAttributes}) with default file
 * attributes (
 * {@link org.at4j.zip.extattrs.MsDosExternalFileAttributes#DEFAULT_FILE_ATTRIBUTES}
 * &ndash; archive)</td>
 * </tr>
 * <tr>
 * <td>Extra fields</td>
 * <td><i>No extra fields</i></td>
 * </tr>
 * </table>
 * <p>
 * The default directory entry settings are:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>External file attributes</td>
 * <td>MS DOS external file attributes (
 * {@link org.at4j.zip.extattrs.MsDosExternalFileAttributes}) with default
 * directory attributes (
 * {@link org.at4j.zip.extattrs.MsDosExternalFileAttributes#DEFAULT_DIRECTORY_ATTRIBUTES}
 * &ndash; directory)</td>
 * </tr>
 * <tr>
 * <td>Extra fields</td>
 * <td><i>No extra fields</i></td>
 * </tr>
 * </table>
 * <p>
 * The platform's default {@link Charset} is used for encoding text.
 * <p>
 * The setter methods of this object return the object instance so that setter
 * calls can be chained together.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipBuilder
 */
public class ZipBuilderSettings
{
	static final Charset DEFAULT_FILE_NAME_ENCODING_CHARSET = Charset.defaultCharset();
	static final Charset DEFAULT_TEXT_ENCODING_CHARSET = Charset.defaultCharset();

	/**
	 * The default file comment.
	 */
	public static final String DEFAULT_FILE_COMMENT = "*** Created by AT4J -- www.at4j.org ***";

	private ZipEntrySettings m_defaultFileEntrySettings = new ZipEntrySettings(ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_FILE_ENTRY_SETTINGS);
	private ZipEntrySettings m_defaultDirectoryEntrySettings = new ZipEntrySettings(ZipBuilderConstants.DEFAULT_DEFAULT_ZIP_DIRECTORY_ENTRY_SETTINGS);
	private Charset m_fileNameEncodingCharset = DEFAULT_FILE_NAME_ENCODING_CHARSET;
	private Charset m_textEncodingCharset = DEFAULT_TEXT_ENCODING_CHARSET;
	private InternalFileAttributesStrategy m_internalFileAttributesStrategy = new DefaultInternalFileAttributesStrategy();
	private String m_fileComment = DEFAULT_FILE_COMMENT;

	/**
	 * Get the default settings for file entries.
	 * @return The default settings for file entries.
	 */
	public ZipEntrySettings getDefaultFileEntrySettings()
	{
		return m_defaultFileEntrySettings;
	}

	/**
	 * Set the default settings for file entries.
	 * <p>
	 * This will be combined with the default default file entry settings, so it
	 * only has to contain the configuration delta compared with the default
	 * settings.
	 * @param defaultFileEntrySettings The new default settings for file
	 * entries.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setDefaultFileEntrySettings(ZipEntrySettings defaultFileEntrySettings)
	{
		// Null check
		defaultFileEntrySettings.getClass();

		m_defaultFileEntrySettings = defaultFileEntrySettings;
		return this;
	}

	/**
	 * Set the compression level for the default Zip file entry settings. This
	 * is a shortcut that may be used instead of setting the compression level
	 * on the default file {@link ZipEntrySettings} object's compression method
	 * property.
	 * @param level The compression level.
	 * @return {@code this}
	 * @since 1.0.2
	 */
	public ZipBuilderSettings setCompressionLevel(CompressionLevel level)
	{
		m_defaultFileEntrySettings.setCompressionMethod(m_defaultFileEntrySettings.getCompressionMethod().createWithCompressionLevel(level));
		return this;
	}

	/**
	 * Get the default settings for directory entries.
	 * @return The default settings for directory entries.
	 */
	public ZipEntrySettings getDefaultDirectoryEntrySettings()
	{
		return m_defaultDirectoryEntrySettings;
	}

	/**
	 * Set the default settings for directory entries.
	 * <p>
	 * This will be combined with the default default directory entry settings,
	 * so it only has to contain the configuration delta compared with the
	 * default settings.
	 * @param defaultDirectoryEntrySettings The new default settings for
	 * directory entries.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setDefaultDirectoryEntrySettings(ZipEntrySettings defaultDirectoryEntrySettings)
	{
		// Null check
		defaultDirectoryEntrySettings.getClass();

		m_defaultDirectoryEntrySettings = defaultDirectoryEntrySettings;
		return this;
	}

	/**
	 * Get the charset to use for encoding file names in the archive.
	 * @return The charset to use for encoding file names in the archive.
	 */
	public Charset getFileNameEncodingCharset()
	{
		return m_fileNameEncodingCharset;
	}

	/**
	 * Set the charset to use for encoding file names in the archive.
	 * @param fileNameEncodingCharset The charset to use for encoding file names
	 * in the archive.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setFileNameEncodingCharset(Charset fileNameEncodingCharset)
	{
		// Null check
		fileNameEncodingCharset.getClass();

		m_fileNameEncodingCharset = fileNameEncodingCharset;
		return this;
	}

	/**
	 * Get the charset to use for encoding text other than file names in the
	 * archive.
	 * @return The charset to use for encoding text other than file names in the
	 * archive.
	 */
	public Charset getTextEncodingCharset()
	{
		return m_textEncodingCharset;
	}

	/**
	 * Set the charset to use for encoding text other than file names in the
	 * archive.
	 * @param textEncodingCharset The charset to use for encoding text other
	 * than file names in the archive.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setTextEncodingCharset(Charset textEncodingCharset)
	{
		// Null check
		textEncodingCharset.getClass();

		m_textEncodingCharset = textEncodingCharset;
		return this;
	}

	/**
	 * Get the internal file attributes strategy for creating the internal file
	 * attributes for the entries that are added to the archive.
	 * @return The internal file attributes strategy.
	 */
	public InternalFileAttributesStrategy getInternalFileAttributesStrategy()
	{
		return m_internalFileAttributesStrategy;
	}

	/**
	 * Set the internal file attributes strategy used for creating the internal
	 * file attributes for the entries that are added to the archive.
	 * @param strat The internal file attributes strategy.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setInternalFileAttributesStrategy(InternalFileAttributesStrategy strat)
	{
		// Null check
		strat.getClass();

		m_internalFileAttributesStrategy = strat;
		return this;
	}

	/**
	 * Get the Zip file's comment.
	 * @return The Zip file's comment.
	 */
	public String getFileComment()
	{
		return m_fileComment;
	}

	/**
	 * Set the Zip file's comment.
	 * @param s The Zip files comment, or {@code null} if the file should not
	 * have a comment.
	 * @return {@code this}
	 */
	public ZipBuilderSettings setFileComment(String s)
	{
		m_fileComment = s;
		return this;
	}
}
