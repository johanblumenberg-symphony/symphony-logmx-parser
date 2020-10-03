package com.symphony.logmxparser;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public class ManaParser extends LogFileParser {
	private ParsedEntry entry = null;
	private StringBuilder entryMsgBuffer = null;

	private SimpleDateFormat dateFormat = null;

	private final static Pattern ENTRY_BEGIN_PATTERN = Pattern
			.compile("^(\\d*)\\|(.*)\\|(.*)\\(\\d*\\)\\|([^:]*): (.*)$");

	private static final String EXTRA_SEQ_FIELD_KEY = "seq";
	private static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList(EXTRA_SEQ_FIELD_KEY);

	@Override
	public String getParserName() {
		return "Symphony Client 2.0 log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Symphony Client 2.0 log files";
	}

	@Override
	protected void parseLine(String line) throws Exception {
		if (line == null) {
			recordPreviousEntryIfExists();
			return;
		}

		Matcher matcher = ENTRY_BEGIN_PATTERN.matcher(line);
		if (matcher.matches()) {
			prepareNewEntry();

			Integer seq = Integer.parseInt(matcher.group(1));
			entry.getUserDefinedFields().put(EXTRA_SEQ_FIELD_KEY, seq);
			entry.setDate(matcher.group(2));
			entry.setLevel(matcher.group(3));
			entry.setEmitter(matcher.group(4));

			entryMsgBuffer.append(matcher.group(5));
		} else if (entry != null) {
			entryMsgBuffer.append('\n').append(line);
		}
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
	public Date getAbsoluteEntryDate(ParsedEntry pEntry) throws Exception {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss.SSSZ", getLocale());
		}
		synchronized (dateFormat) {
			return dateFormat.parse(pEntry.getDate());
		}
	}

	private void recordPreviousEntryIfExists() throws Exception {
		if (entry != null) {
			entry.setMessage(entryMsgBuffer.toString());
			addEntry(entry);
			entry = null;
		}
	}

	private void prepareNewEntry() throws Exception {
		recordPreviousEntryIfExists();
		entry = createNewEntry();
		entryMsgBuffer = new StringBuilder(80);
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
	}
}
