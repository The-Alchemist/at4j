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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.at4j.archive.builder.ArchiveEntrySettings;
import org.at4j.zip.comp.ZipEntryCompressionMethod;
import org.at4j.zip.ef.ZipEntryExtraFieldFactory;
import org.at4j.zip.extattrs.ZipExternalFileAttributesFactory;

/**
 * This object contains the settings for Zip entries. It is used by the
 * {@link ZipBuilder}.
 * <p>
 * The properties and methods of this object works as is described in the
 * {@link ArchiveEntrySettings} documentation.
 * @author Karl Gustafsson
 * @since 1.0
 * @see ZipBuilder
 */
public class ZipEntrySettings implements ArchiveEntrySettings<ZipEntrySettings>
{
	private ZipEntryCompressionMethod m_compressionMethod;
	private ZipExternalFileAttributesFactory m_externalFileAttributesFactory;
	private String m_comment;
	private List<ZipEntryExtraFieldFactory> m_extraFieldFactories;
	private boolean m_readOnly;

	/**
	 * Create a new read/write Zip entry settings object.
	 */
	public ZipEntrySettings()
	{
		m_extraFieldFactories = new ArrayList<ZipEntryExtraFieldFactory>(4);
	}

	/**
	 * Create a new read/write Zip entry settings object using the supplied
	 * object as a template.
	 * <p>
	 * The properties from the template object is copied to this object.
	 * @param template The template object.
	 * @since 1.0.2
	 */
	public ZipEntrySettings(ZipEntrySettings template)
	{
		m_compressionMethod = template.m_compressionMethod;
		m_externalFileAttributesFactory = template.m_externalFileAttributesFactory;
		m_comment = template.m_comment;
		m_extraFieldFactories = new ArrayList<ZipEntryExtraFieldFactory>(template.m_extraFieldFactories);
		m_readOnly = false;
	}

	private void assertNotReadOnly() throws IllegalStateException
	{
		if (m_readOnly)
		{
			throw new IllegalStateException("This configuration object is read only");
		}
	}

	public ZipEntrySettings setReadOnly()
	{
		m_readOnly = true;
		return this;
	}

	/**
	 * Set the compression method to use.
	 * @param cm The compression method to use, or {@code null} if this property
	 * should not be set in this settings object.
	 * @return {@code this}
	 * @throws IllegalStateException If this configuration object is read only.
	 */
	public ZipEntrySettings setCompressionMethod(ZipEntryCompressionMethod cm) throws IllegalStateException
	{
		assertNotReadOnly();

		// Nulls are permitted
		m_compressionMethod = cm;
		return this;
	}

	/**
	 * Get the Zip entry compression method.
	 * @return The Zip entry compression method, or {@code null} if it is not
	 * set.
	 * @see #setCompressionMethod(ZipEntryCompressionMethod)
	 */
	public ZipEntryCompressionMethod getCompressionMethod()
	{
		return m_compressionMethod;
	}

	/**
	 * Set a factory object for creating the external file attributes.
	 * @param f The external file attributes factory, or {@code null} if that
	 * should not be set.
	 * @return {@code this}
	 * @throws IllegalStateException If this configuration object is read only.
	 */
	public ZipEntrySettings setExternalFileAttributesFactory(ZipExternalFileAttributesFactory f) throws IllegalStateException
	{
		assertNotReadOnly();

		// Nulls are permitted
		m_externalFileAttributesFactory = f;
		return this;
	}

	/**
	 * Get the external file attributes factory for this Zip entry.
	 * @return The external file attributes factory for this Zip entry, or
	 * {@code null} if it is not set.
	 * @see #setExternalFileAttributesFactory(ZipExternalFileAttributesFactory)
	 */
	public ZipExternalFileAttributesFactory getExternalFileAttributesFactory()
	{
		return m_externalFileAttributesFactory;
	}

	/**
	 * Set the Zip entry comment.
	 * @param comment The Zip entry comment or {@code null} if that should not
	 * be set.
	 * @return {@code this}
	 * @throws IllegalStateException If this configuration object is read only.
	 */
	public ZipEntrySettings setComment(String comment) throws IllegalStateException
	{
		assertNotReadOnly();

		// Nulls are permitted
		m_comment = comment;
		return this;
	}

	/**
	 * Get the Zip entry comment.
	 * @return The Zip entry comment, or {@code null} if that is not set.
	 * @see #setComment(String)
	 */
	public String getComment()
	{
		return m_comment;
	}

	/**
	 * Add a {@link ZipEntryExtraFieldFactory} that creates a Zip entry extra
	 * field for each Zip entry configured by this settings object.
	 * <p>
	 * If this object is combined with another settings object, that settings
	 * object's list of extra field factories is appended to this object's list.
	 * @param f The factory.
	 * @return {@code this}.
	 * @throws IllegalStateException If this configuration object is read only.
	 */
	public ZipEntrySettings addExtraFieldFactory(ZipEntryExtraFieldFactory f) throws IllegalStateException
	{
		assertNotReadOnly();

		// Null check
		f.getClass();
		m_extraFieldFactories.add(f);
		return this;
	}

	/**
	 * Get the factories for creating extra fields for Zip entries.
	 * @return The returned list is read only.
	 * @see #addExtraFieldFactory(ZipEntryExtraFieldFactory)
	 */
	public List<ZipEntryExtraFieldFactory> getExtraFieldFactories()
	{
		return Collections.unmodifiableList(m_extraFieldFactories);
	}

	/**
	 * Combine this settings object with the supplied object. The two objects
	 * are combined by first cloning this object and then overwriting the
	 * properties in the new object with the properties that are set in the
	 * supplied settings object. In other words, the properties that are set in
	 * the supplied object take precedence over the properties in this object.
	 * <p>
	 * If a property is not set in either of this or the supplied object, it
	 * will be set to {@code null} in the new object.
	 * <p>
	 * The created object will have a list of extra field factories that first
	 * contains the factories of this object and then the factories of the
	 * supplied object.
	 * <p>
	 * None of this or the supplied settings objects are modified by this
	 * method.
	 * @param zes The settings object to combine this object's properties with.
	 * @return A new settings object containing the combined properties of this
	 * object and the supplied object.
	 */
	public ZipEntrySettings combineWith(ZipEntrySettings zes)
	{
		ZipEntrySettings res = clone();
		if (zes.getCompressionMethod() != null)
		{
			res.setCompressionMethod(zes.getCompressionMethod());
		}
		if (zes.getExternalFileAttributesFactory() != null)
		{
			res.setExternalFileAttributesFactory(zes.getExternalFileAttributesFactory());
		}
		if (zes.getComment() != null)
		{
			res.setComment(zes.getComment());
		}
		res.m_extraFieldFactories.addAll(m_extraFieldFactories);
		res.m_extraFieldFactories.addAll(zes.m_extraFieldFactories);
		return res;
	}

	@Override
	public ZipEntrySettings clone()
	{
		try
		{
			ZipEntrySettings res = (ZipEntrySettings) super.clone();
			res.m_readOnly = false;
			res.m_extraFieldFactories = new ArrayList<ZipEntryExtraFieldFactory>(4);
			res.m_extraFieldFactories.addAll(m_extraFieldFactories);
			return res;
		}
		catch (CloneNotSupportedException e)
		{
			throw new RuntimeException("Bug: ", e);
		}
	}
}
