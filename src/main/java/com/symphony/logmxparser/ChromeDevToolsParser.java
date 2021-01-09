package com.symphony.logmxparser;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeDevToolsParser extends Parser {
	private ChromeDevTools chromeDevTools = new ChromeDevTools();
	private Mana mana = new Mana(this);
	protected StringBuilder entry = null;
	
	@Override
	public String getParserName() {
		return "Chrome DevTools log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Chrome DevTools log files";
	}

	@Override
	protected void parseLine(String line) throws Exception {
		if (line != null) {
			if (entry == null) {
				if (line.endsWith("{")) {
					entry = new StringBuilder(line);
				} else {
					proceed(line);
				}
			} else {
				entry.append("\n").append(line);
				
				if ("}".equals(line)) {
					proceed(entry.toString());
					entry = null;					
				}
			}
		}
	}

	private void proceed(String line) throws Exception {
		ParsedEntry entry = prepareNewEntry();
		entry.setMessage(line);
		entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

		chromeDevTools.refineEntry(entry);
		mana.refineEntry(entry);
		
		addParsedEntry(entry);
	}
}
