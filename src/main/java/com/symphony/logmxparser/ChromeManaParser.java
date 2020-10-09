package com.symphony.logmxparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeManaParser extends ManaParser {
	private final static Pattern LOG_PATTERN = Pattern
			.compile("^\\[\\d+:\\d+:\\d+/(\\d+\\.\\d+):([A-Za-z0-9]+):(.*)\\(\\d+\\)\\] (.*)$");

	private final static Pattern MSG_PATTERN = Pattern.compile("^\"(.*)\", source: (.*)$");

	@Override
	public String getParserName() {
		return "Symphony Client 2.0 chrome log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Symphony Client 2.0 chrome log files";
	}

	@Override
	protected void parseLine(String line) throws Exception {
		if (line == null) {
			recordPreviousEntryIfExists();
			return;
		}

		Matcher matcher = LOG_PATTERN.matcher(line);
		if (matcher.matches()) {
			if ("CONSOLE".equals(matcher.group(3))) {
				Matcher msg = MSG_PATTERN.matcher(matcher.group(4));

				if (msg.matches()) {
					if (!parseEntry(msg.group(1))) {
						prepareNewEntry();

						entry.setDate(matcher.group(1));
						entry.setLevel(matcher.group(2));
						entry.setEmitter(matcher.group(3));
						
						entryMsgBuffer.append(msg.group(1));
					}
				}
			} else {
				prepareNewEntry();

				entry.setDate(matcher.group(1));
				entry.setLevel(matcher.group(2));
				entry.setEmitter("chrome." + matcher.group(3));
				
				entryMsgBuffer.append(matcher.group(4));
			}
		} else if (entry != null) {
			entryMsgBuffer.append('\n').append(line);
		}
	}

	@Override
	public String getEntryStringRepresentation(ParsedEntry entry) {
		if ("CONSOLE".equals(entry.getEmitter())) {
			return super.getEntryStringRepresentation(entry);
		} else {
			return null;
		}
	}
}
