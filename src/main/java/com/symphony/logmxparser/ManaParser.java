package com.symphony.logmxparser;

import com.lightysoft.logmx.business.ParsedEntry;
import com.symphony.logmxparser.base.Parser;
import com.symphony.logmxparser.format.Mana;

public class ManaParser extends Parser {
	private Mana mana = new Mana(this);
	protected StringBuilder entry = null;

	@Override
	public String getParserName() {
		return "Symphony Client 2.0 log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Symphony Client 2.0 log files";
	}

	public void parseLine(String line) throws Exception {
		if (line == null) {
			if (entry != null) {
				proceed(entry.toString());
			}
		} else if (mana.isStartLine(line)) {
			if (entry != null) {
				proceed(entry.toString());
			}
			entry = new StringBuilder(line);
		} else {
			if (entry != null) {
				entry.append("\n").append(line);
			}
		}
	}

	private void proceed(String line) throws Exception {
		ParsedEntry entry = prepareNewEntry();
		entry.setMessage(line);
		entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

		mana.refineEntry(entry);

		addParsedEntry(entry);

		mana.addStatisticsEvents(entry);
	}
}
