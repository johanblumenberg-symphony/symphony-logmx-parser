package com.symphony.logmxparser;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public abstract class Parser extends LogFileParser {
	public static final String EXTRA_SEQ_FIELD_KEY = "seq";
	public static final String EXTRA_HIDDEN_ORG_FIELD_KEY = "org";
	public static final String EXTRA_HIDDEN_DATE_FIELD_KEY = "date";

	protected static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList(EXTRA_SEQ_FIELD_KEY);

	private EntryConsumer consumer;

	public void setConsumer(EntryConsumer consumer) {
		this.consumer = consumer;
	}
	
	@Override
	public List<String> getUserDefinedFields() {
		return EXTRA_FIELDS_KEYS;
	}

	@Override
	public Date getRelativeEntryDate(ParsedEntry pEntry) throws Exception {
		return null;
	}

	@Override
	public Date getAbsoluteEntryDate(ParsedEntry entry) throws Exception {
		return (Date) entry.getUserDefinedFields().get(EXTRA_HIDDEN_DATE_FIELD_KEY);
	}

	@Override
	public String getEntryStringRepresentation(ParsedEntry entry) {
		return (String) entry.getUserDefinedFields().get(EXTRA_HIDDEN_ORG_FIELD_KEY);
	}

	public void addParsedEntry(ParsedEntry entry) throws Exception {
		if (consumer != null) {
			consumer.addParsedEntry(entry);
		} else {
			addEntry(entry);
		}
	}
	
	protected ParsedEntry prepareNewEntry() {
		var entry = createNewEntry();
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
		return entry;
	}
}
