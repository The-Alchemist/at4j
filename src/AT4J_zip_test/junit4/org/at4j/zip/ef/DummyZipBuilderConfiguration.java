package org.at4j.zip.ef;

import java.nio.charset.Charset;

import org.at4j.zip.builder.ZipBuilderConfiguration;
import org.entityfs.support.nio.Charsets;

class DummyZipBuilderConfiguration implements ZipBuilderConfiguration
{
	public Charset getFileNameEncodingCharset()
	{
		return Charsets.UTF8;
	}

	public Charset getTextEncodingCharset()
	{
		return Charsets.UTF8;
	}
}
