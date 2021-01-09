package com.symphony.logmxparser;

import com.lightysoft.logmx.business.ParsedEntry;

public class ManaParser extends Parser {
	private Mana mana = new Mana(this);

	@Override
	public String getParserName() {
		return "Symphony Client 2.0 log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Symphony Client 2.0 log files";
	}

	public void parseLine(String line) throws Exception {
		if (line != null) {
			if (mana.isStartLine(line)) {
				proceed(line);
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
