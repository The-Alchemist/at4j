package org.at4j.tar.builder;

import org.entityfs.entityattrs.unix.UnixEntityMode;

final class TarBuilderConstants
{
	static final TarEntrySettings DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS;
	static final TarEntrySettings DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS;
	static
	{
		String user = System.getProperty("user.name");
		user = user != null ? user : "root";
		DEFAULT_DEFAULT_TAR_FILE_ENTRY_SETTINGS = new TarEntrySettings().setEntityMode(UnixEntityMode.forCode(0644)).setOwnerUid(Integer.valueOf(0)).setOwnerGid(Integer.valueOf(0)).setOwnerUserName(user).setOwnerGroupName("users")
				.setReadOnly();
		DEFAULT_DEFAULT_TAR_DIRECTORY_ENTRY_SETTINGS = new TarEntrySettings().setEntityMode(UnixEntityMode.forCode(0755)).setOwnerUid(Integer.valueOf(0)).setOwnerGid(Integer.valueOf(0)).setOwnerUserName(user).setOwnerGroupName("users")
				.setReadOnly();
	}

	private TarBuilderConstants()
	{
		// Nothing
	}
}
