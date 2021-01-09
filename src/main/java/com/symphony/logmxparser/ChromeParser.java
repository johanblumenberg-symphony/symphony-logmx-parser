package com.symphony.logmxparser;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeParser extends Parser {
	private Chrome chrome = new Chrome();
	private Mana mana = new Mana(this);
	protected StringBuilder entry = null;

	@Override
	public String getParserName() {
		return "Chrome log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Chrome log files";
	}

	@Override
	protected void parseLine(String line) throws Exception {
		if (line == null) {
			if (entry != null) {
				proceed(entry.toString());
			}
		} else {
			if (chrome.isStartLine(line)) {
				if (entry != null) {
					proceed(entry.toString());
				}
				entry = new StringBuilder(line);
			} else {
				if (entry == null) {
					proceed(line);
				} else {
					entry.append("\n").append(line);
				}
			}
		}
	}

	private void proceed(String line) throws Exception {
		ParsedEntry entry = prepareNewEntry();
		entry.setMessage(line);
		entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

		chrome.refineEntry(entry);
		mana.refineEntry(entry);

		addParsedEntry(entry);
	}
}
