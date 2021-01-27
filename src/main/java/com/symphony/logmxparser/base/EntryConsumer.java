package com.symphony.logmxparser.base;

import com.lightysoft.logmx.business.ParsedEntry;

public interface EntryConsumer {
	void addParsedEntry(ParsedEntry entry) throws Exception;
}
