package com.symphony.logmxparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeParser extends ManaParser {
	private final static Pattern LOG_PATTERN = Pattern
			.compile("^\\[\\d+:\\d+:\\d+/(\\d+\\.\\d+):([A-Za-z0-9]+):(.*)\\(\\d+\\)\\] (.*)$");

	private final static Pattern MSG_PATTERN = Pattern.compile("^\"(.*)\", source: (.*)$", Pattern.DOTALL);

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
			recordPreviousEntryIfExists();
			return;
		}

		Matcher matcher = LOG_PATTERN.matcher(line);

		if (matcher.matches()) {
			String emitter = matcher.group(3);
			String message = matcher.group(4);

			prepareNewEntry();

			entry.setDate(matcher.group(1));
			entry.setLevel(matcher.group(2));
			entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

			if ("CONSOLE".equals(emitter)) {
				entry.setEmitter(emitter);
			} else {
				entry.setEmitter("chrome." + emitter);
			}

			entryMsgBuffer.append(message);
		} else if (entry != null) {
			entryMsgBuffer.append('\n').append(line);
		}
	}

	@Override
	public String getEntryStringRepresentation(ParsedEntry entry) {
		if (super.isManaLog(entry)) {
			return super.getEntryStringRepresentation(entry);
		} else {
			return null;
		}
	}
	
	@Override
	protected void prepareEntry(ParsedEntry entry) throws Exception {
		String message = entry.getMessage();
		Matcher matcher = MSG_PATTERN.matcher(message);
		
		if(matcher.matches()) {
			message = matcher.group(1);
			entry.setMessage(message);
			
			if (parseEntry(message)) {
				recordPreviousEntryIfExists();
			} else {
				super.prepareEntry(entry);
			}
		} else {
			super.prepareEntry(entry);
		}
	}
}
