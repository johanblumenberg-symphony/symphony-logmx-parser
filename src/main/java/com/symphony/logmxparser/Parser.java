package com.symphony.logmxparser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public abstract class Parser extends LogFileParser {
	public static final String EXTRA_SEQ_FIELD_KEY = "seq";
	public static final String EXTRA_HIDDEN_ORG_FIELD_KEY = "org";

	protected static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList(EXTRA_SEQ_FIELD_KEY);

	protected ParsedEntry entry = null;
	protected StringBuilder entryMsgBuffer = null;

	@Override
	public List<String> getUserDefinedFields() {
		return EXTRA_FIELDS_KEYS;
	}

	protected void localAddEntry(ParsedEntry entry) throws Exception {
		addEntry(entry);
	}

	protected void prepareEntry(ParsedEntry entry) throws Exception {
		localAddEntry(entry);
	}
	
	protected void recordPreviousEntryIfExists() throws Exception {
		if (entry != null) {
			entry.setMessage(entryMsgBuffer.toString());

			ParsedEntry e = entry;
			entry = null;
			prepareEntry(e);
		}
	}

	protected void prepareNewEntry() throws Exception {
		recordPreviousEntryIfExists();
		entry = createNewEntry();
		entryMsgBuffer = new StringBuilder(80);
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
	}
}
