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
package org.at4j.tar.builder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.at4j.archive.builder.ArchiveEntrySettings;
import org.entityfs.entityattrs.unix.UnixEntityMode;

/**
 * This object contains settings for a Tar entry that is to be added to a tar
 * archive by a {@link TarBuilder}.
 * <p>
 * The properties and methods of this object works as is described in the
 * {@link ArchiveEntrySettings} documentation.
 * <p>
 * In addition to the regular properties of this object, it also has a {@code
 * Map<String, String>} for storing named properties in. Those properties are
 * currently not used for anything, but they may be used by clients to pass data
 * to, for instance, custom {@link PaxVariableProvider} implementations. When
 * this settings object is combined with another settings object, the properties
 * of this object is copied to the new object, and then the properties of the
 * other object is copied to the new object. If both this and the other object
 * contains properties with the same names, the properties of the other object
 * takes precedence.
 * @author Karl Gustafsson
 * @since 1.0
 */
public class TarEntrySettings implements ArchiveEntrySettings<TarEntrySettings>
{
	// The maximum uid or gid (8^7 - 1)
	private static final int MAX_ID = 2097151;

	private UnixEntityMode m_entityMode;
	private Integer m_ownerUid;
	private Integer m_ownerGid;
	private String m_ownerUserName;
	private String m_ownerGroupName;
	private Map<String, String> m_properties;
	private boolean m_readOnly;

	public TarEntrySettings()
	{
		m_properties = new HashMap<String, String>();
	}

	private void assertNotReadOnly() throws IllegalStateException
	{
		if (m_readOnly)
		{
			throw new IllegalStateException("This configuration object is read only");
		}
	}

	public TarEntrySettings setReadOnly()
	{
		m_readOnly = true;
		return this;
	}

	/**
	 * Set the entry's Unix entity permission mode.
	 * @param mode The permission mode, or {@code null} if this property should
	 * not be set.
	 * @return {@code this}
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings setEntityMode(UnixEntityMode mode) throws IllegalStateException
	{
		assertNotReadOnly();

		// Null is allowed
		m_entityMode = mode;
		return this;
	}

	/**
	 * Get the Unix entity permissions mode.
	 * @return The Unix entity permissions mode, or {@code null} if this
	 * property is not set.
	 * @see #setEntityMode(UnixEntityMode)
	 */
	public UnixEntityMode getEntityMode()
	{
		return m_entityMode;
	}

	private void checkOwnerIdRange(Integer i) throws IllegalArgumentException
	{
		if (i != null)
		{
			int v = i.intValue();
			if (v < 0 || v > MAX_ID)
			{
				throw new IllegalArgumentException("Illegal UID or GID " + v + ". It must be between 0 and " + MAX_ID + " (inclusive)");
			}
		}
	}

	/**
	 * Set the owner user id.
	 * @param uid The owner user id. This must be a value between {@code 0} and
	 * {@code 2097151} (inclusive), or {@code null} if this property should not
	 * be set.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the UID is not in the permitted
	 * range.
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings setOwnerUid(Integer uid) throws IllegalArgumentException, IllegalStateException
	{
		checkOwnerIdRange(uid);
		assertNotReadOnly();

		// Null is allowed
		m_ownerUid = uid;
		return this;
	}

	/**
	 * Get the owner user id.
	 * @return The owner user id, or {@code null} if this property is not set.
	 * @see #setOwnerUid(Integer)
	 */
	public Integer getOwnerUid()
	{
		return m_ownerUid;
	}

	/**
	 * Set the owner group id.
	 * @param gid The owner group id. This must be a value between {@code 0} and
	 * {@code 2097151} (inclusive), or {@code null} if this property should not
	 * be set.
	 * @return {@code this}
	 * @throws IllegalArgumentException If the GID is not in the permitted
	 * range.
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings setOwnerGid(Integer gid) throws IllegalArgumentException, IllegalStateException
	{
		checkOwnerIdRange(gid);
		assertNotReadOnly();

		// Null is allowed
		m_ownerGid = gid;
		return this;
	}

	/**
	 * Get the owner group id.
	 * @return The owner group id, or {@code null} if this property is not set.
	 * @see #setOwnerGid(Integer)
	 */
	public Integer getOwnerGid()
	{
		return m_ownerGid;
	}

	/**
	 * Set the owner user name.
	 * @param n The owner user name, or {@code null} if this property should not
	 * be set.
	 * @return {@code this}
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings setOwnerUserName(String n) throws IllegalStateException
	{
		assertNotReadOnly();

		// Null is allowed
		m_ownerUserName = n;
		return this;
	}

	/**
	 * Get the owner user name.
	 * @return The owner user name, or {@code null} if this property is not set.
	 * @see #setOwnerUserName(String)
	 */
	public String getOwnerUserName()
	{
		return m_ownerUserName;
	}

	/**
	 * Set the owner group name.
	 * @param n The owner group name, or {@code null} if this property should
	 * not be set.
	 * @return {@code this}
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings setOwnerGroupName(String n) throws IllegalStateException
	{
		assertNotReadOnly();

		// Null is allowed
		m_ownerGroupName = n;
		return this;
	}

	/**
	 * Get the owner group name.
	 * @return The owner group name, or {@code null} if this property is not
	 * set.
	 * @see #setOwnerGroupName(String)
	 */
	public String getOwnerGroupName()
	{
		return m_ownerGroupName;
	}

	/**
	 * Add a custom, named property.
	 * @param name The name of the property.
	 * @param value The property value.
	 * @return {@code this}
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public TarEntrySettings addProperty(String name, String value) throws IllegalStateException
	{
		assertNotReadOnly();

		// Null checks
		name.getClass();
		value.getClass();

		m_properties.put(name, value);
		return this;
	}

	/**
	 * Remove a named property.
	 * @param name The name of the property to remove.
	 * @return The property's value, or {@code null} if there was no property
	 * with the supplied name (or if it had the value {@code null}).
	 * @throws IllegalStateException If this object has been set read only.
	 */
	public String removeProperty(String name) throws IllegalStateException
	{
		assertNotReadOnly();

		return m_properties.remove(name);
	}

	/**
	 * Get the properties for this settings object. The returned map is read
	 * only.
	 * @return Properties for this settings object, read only.
	 */
	public Map<String, String> getProperties()
	{
		return Collections.unmodifiableMap(m_properties);
	}

	public TarEntrySettings combineWith(TarEntrySettings settings)
	{
		TarEntrySettings res = clone();
		if (settings.getEntityMode() != null)
		{
			res.setEntityMode(settings.getEntityMode());
		}
		if (settings.getOwnerUid() != null)
		{
			res.setOwnerUid(settings.getOwnerUid());
		}
		if (settings.getOwnerGid() != null)
		{
			res.setOwnerGid(settings.getOwnerGid());
		}
		if (settings.getOwnerUserName() != null)
		{
			res.setOwnerUserName(settings.getOwnerUserName());
		}
		if (settings.getOwnerGroupName() != null)
		{
			res.setOwnerGroupName((settings.getOwnerGroupName()));
		}
		res.m_properties.putAll(settings.m_properties);
		return res;
	}

	@Override
	public TarEntrySettings clone()
	{
		try
		{
			TarEntrySettings res = (TarEntrySettings) super.clone();
			res.m_readOnly = false;
			res.m_properties = new HashMap<String, String>();
			res.m_properties.putAll(m_properties);
			return res;
		}
		catch (CloneNotSupportedException e)
		{
			// Should not happen
			throw new RuntimeException("Bug", e);
		}
	}
}
