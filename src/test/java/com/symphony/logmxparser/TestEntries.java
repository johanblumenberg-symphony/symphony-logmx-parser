package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.lightysoft.logmx.business.ParsedEntry;
import com.symphony.logmxparser.base.EntryConsumer;

public class TestEntries implements EntryConsumer {
	private List<ParsedEntry> entries = new ArrayList<>();

	public List<ParsedEntry> getEntries() {
		return entries;
	}

	public ParsedEntry getEntry() {
		assertEquals(1, entries.size());
		return entries.get(0);
	}

	@Override
	public void addParsedEntry(ParsedEntry entry) {
		entries.add(entry);
	}
}
