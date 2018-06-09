package org.at4j.tar.builder;

/**
 * This object contains the settings for a Tar archive builder. When a new
 * instance is created, it is initialized with the default settings.
 * <p>
 * The default file entry settings are:
 * <table border="1">
 * <tr>
 * <th>Property</th>
 * <th>Value</th>
 * </tr>
 * <tr>
 * <td>Unix entity mode</td>
 * <td>0644</td>
 * </tr>
 * <tr>
 * <td>Owner UID</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>Owner GID</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>Owner user name</td>
 * <td><i>The name of the user owning the Java process</i></td>
 * </tr>
 * <tr>
 * <td>Owner group name</td>
 * <td>users</td>
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
 * <td>Unix entity mode</td>
 * <td>0755</td>
 * </tr>
 * <tr>
 * <td>Owner UID</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>Owner GID</td>
 * <td>0</td>
 * </tr>
 * <tr>
 * <td>Owner user name</td>
 * <td><i>The name of the user owning the Java process</i></td>
 * </tr>
 * <tr>
 * <td>Owner group name</td>
 * <td>users</td>
 * </tr>
 * </table>
 * <p>
 * The default entry strategy is the default {@link GnuTarEntryStrategy}, which
 * means that the Tar builder will create Gnu Tar compatible archives by
 * default.
 * <p>
 * The setter methods of this object return the object instance so that setter
 * calls can be chained together.
 * @author Karl Gustafsson
 * @since 1.0
 * @see TarBuilder
 * @see TarStreamBuilder
 */
public class TarBuilderSettings
{
	static final TarEntryStrategy DEFAULT_ENTRY_STRATEGY = new GnuTarEntryStrategy();

	private TarEntrySettings m_defaultFileEntrySettings = TarBuilderConstants.DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS;
	private TarEntrySettings m_defaultDirectoryEntrySettings = TarBuilderConstants.DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS;
	private TarEntryStrategy m_entryStrategy = DEFAULT_ENTRY_STRATEGY;

	/**
	 * Get the default file entry settings.
	 * @return The default file entry settings.
	 */
	public TarEntrySettings getDefaultFileEntrySettings()
	{
		return m_defaultFileEntrySettings;
	}

	/**
	 * Set new default file entry settings.
	 * <p>
	 * This will be combined with the default default file entry settings, so it
	 * only has to contain the configuration delta compared with the default
	 * settings.
	 * @param defaultFileSettings The new default file entry settings.
	 * @return {@code this}
	 */
	public TarBuilderSettings setDefaultFileEntrySettings(TarEntrySettings defaultFileSettings)
	{
		// Null check
		defaultFileSettings.getClass();

		m_defaultFileEntrySettings = defaultFileSettings;
		return this;
	}

	/**
	 * Get the default directory entry settings.
	 * @return The default directory entry settings.
	 */
	public TarEntrySettings getDefaultDirectoryEntrySettings()
	{
		return m_defaultDirectoryEntrySettings;
	}

	/**
	 * Set new default directory entry settings.
	 * <p>
	 * This will be combined with the default default directory entry settings,
	 * so it only has to contain the configuration delta compared with the
	 * default settings.
	 * @param defaultDirectorySettings The new default directory entry settings.
	 * @return {@code this}
	 */
	public TarBuilderSettings setDefaultDirectoryEntrySettings(TarEntrySettings defaultDirectorySettings)
	{
		// Null check
		defaultDirectorySettings.getClass();

		m_defaultDirectoryEntrySettings = defaultDirectorySettings;
		return this;
	}

	/**
	 * Get the entry strategy.
	 * @return The entry strategy.
	 */
	public TarEntryStrategy getEntryStrategy()
	{
		return m_entryStrategy;
	}

	/**
	 * Set a new entry strategy.
	 * @param entryStrategy The new entry strategy.
	 * @return {@code this}
	 */
	public TarBuilderSettings setEntryStrategy(TarEntryStrategy entryStrategy)
	{
		// Null check
		entryStrategy.getClass();

		m_entryStrategy = entryStrategy;
		return this;
	}
}
