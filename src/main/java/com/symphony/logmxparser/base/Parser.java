package com.symphony.logmxparser.base;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public abstract class Parser extends LogFileParser {
	public static final String EXTRA_SEQ_FIELD_KEY = "seq";
	public static final String EXTRA_COPY_FIELD_KEY = "copy";
	public static final String EXTRA_HIDDEN_ORG_FIELD_KEY = "org";
	private static final String EXTRA_HIDDEN_DATE_FIELD_KEY = "date";

	protected static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList(EXTRA_SEQ_FIELD_KEY, EXTRA_COPY_FIELD_KEY);

	private EntryConsumer consumer;
	private Locale locale;
	private static SimpleDateFormat dateFormat;

	public void setConsumer(EntryConsumer consumer) {
		this.consumer = consumer;
	}

	public Locale getParserLocale() {
		if (locale != null) {
			return locale;
		} else {
			return getLocale();
		}
	}

	public void setParserLocale(Locale locale) {
		this.locale = locale;
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
		ParsedEntry entry = createNewEntry();
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
		return entry;
	}

	public ParsedEntry prepareNewEntryFrom(ParsedEntry src) {
		ParsedEntry entry = createNewEntry();
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
		entry.setDate(src.getDate());
		entry.getUserDefinedFields().putAll(src.getUserDefinedFields());
		entry.setEmitter(src.getEmitter());
		entry.setLevel(src.getEmitter());
		entry.setMessage(src.getMessage());
		return entry;
	}

	public static void setDate(ParsedEntry entry, Date value) {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}
		synchronized (dateFormat) {
			entry.setDate(dateFormat.format(value));
			entry.getUserDefinedFields().put(EXTRA_HIDDEN_DATE_FIELD_KEY, value);
		}
	}
}
