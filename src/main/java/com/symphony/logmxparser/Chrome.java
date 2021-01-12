package com.symphony.logmxparser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.lightysoft.logmx.business.ParsedEntry;

public class Chrome {
	private final static Pattern LOG_PATTERN = Pattern
			.compile("^\\[\\d+:\\d+:\\d+/(\\d+\\.\\d+):([A-Za-z0-9]+):(.*)\\(\\d+\\)\\] (.*)$", Pattern.DOTALL);

	private final static Pattern MSG_PATTERN = Pattern.compile("^\"(.*)\", source: (.*)$", Pattern.DOTALL);

	public boolean isStartLine(String line) {
		return LOG_PATTERN.matcher(line).matches();
	}

	public void refineEntry(ParsedEntry entry) throws Exception {
		String line = entry.getMessage();
		Matcher matcher = LOG_PATTERN.matcher(line);

		if (matcher.matches()) {
			String emitter = matcher.group(3);
			String message = matcher.group(4);

			entry.setDate(matcher.group(1));
			entry.setLevel(matcher.group(2));
			entry.setMessage(message);

			if ("CONSOLE".equals(emitter)) {
				entry.setEmitter(emitter);

				Matcher matcher2 = MSG_PATTERN.matcher(message);
				
				if(matcher2.matches()) {
					message = matcher2.group(1);
					entry.setMessage(message);
				}
			} else if (emitter.endsWith(".cc")) {
				entry.setEmitter("chrome." + emitter.substring(0, emitter.length() - 3));
			} else {
				entry.setEmitter("chrome." + emitter);
			}
		}
	}
}
