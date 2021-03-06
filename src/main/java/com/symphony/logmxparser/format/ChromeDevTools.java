package com.symphony.logmxparser.format;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightysoft.logmx.business.ParsedEntry;
import com.symphony.logmxparser.base.Parser;

public class ChromeDevTools {
	private static ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private final static Pattern ENTRY_BEGIN_PATTERN = Pattern.compile("^\\[(\\d*\\.\\d*)\\]\\[([A-Z]*)\\]: (.*)$",
			Pattern.DOTALL);
	private final static Pattern DEVTOOLS_BEGIN_PATTERN = Pattern.compile("^DevTools WebSocket [^:]+: ([^ ]+) (.*)$",
			Pattern.DOTALL);

	public ChromeDevTools() {
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		jsonMapper.setDefaultPrettyPrinter(prettyPrinter);
	}

	public boolean isStartLine(String line) {
		return ENTRY_BEGIN_PATTERN.matcher(line).matches();
	}

	public void refineEntry(ParsedEntry entry) throws Exception {
		Matcher matcher = ENTRY_BEGIN_PATTERN.matcher(entry.getMessage());

		if (matcher.matches()) {
			Parser.setDate(entry, parseDate(matcher.group(1)));
			entry.setLevel(matcher.group(2));
			entry.setMessage(matcher.group(3));
			String msg = matcher.group(3);

			Matcher devtools = DEVTOOLS_BEGIN_PATTERN.matcher(msg);
			if (devtools.matches()) {
				String emitter = devtools.group(1);
				entry.setEmitter(emitter);
				entry.setMessage(devtools.group(2));
			} else if ("Done waiting for pending navigations. Status: ok".equals(msg)) {
				entry.setEmitter("CONSOLE.navigations");
			} else if ("Waiting for pending navigations...".equals(msg)) {
				entry.setEmitter("CONSOLE.navigations");
			} else {
				entry.setEmitter("CONSOLE");
			}

			String orig = entry.getMessage();
			if ("Runtime.consoleAPICalled".equals(entry.getEmitter())) {
				Map<String, Object> parsed = parseArg(orig);
				Object args = parsed.get("args");

				if (args instanceof List) {
					List<Map<String, Object>> args2 = (List<Map<String, Object>>) args;

					if (!args2.isEmpty() && args2.get(0) instanceof Map) {
						String consoleMsg = "";
						for (Map<String, Object> arg : args2) {
							if ("string".equals(arg.get("type")) && arg.get("value") instanceof String) {
								consoleMsg += " " + arg.get("value");
							} else {
								break;
							}
						}
						if (consoleMsg.length() > 0) {
							entry.setMessage(consoleMsg.substring(1));
						}
					}
				}
			} else if ("Log.entryAdded".equals(entry.getEmitter())) {
				Map<String,Object> parsed = parseArg(orig);
				Object logEntry = parsed.get("entry");

				if (logEntry instanceof Map) {
					Map<String,Object> logEntry2 = (Map<String, Object>) logEntry;

					if (logEntry2.get("text") instanceof String) {
						entry.setMessage((String) logEntry2.get("text"));
					}
				}
			} else if ("Input.dispatchKeyEvent".equals(entry.getEmitter())) {
				Map<String,Object> parsed = parseArg(orig);
				Object type = parsed.get("type");
				Object code = parsed.get("code");

				if (type instanceof String && code instanceof String) {
					entry.setMessage(type + " " + code);
				}
			}
		}
	}

	private Map<String, Object> parseArg(String value) {
		int i = value.indexOf('{');
		try {
			return jsonMapper.readValue(value.substring(i), Map.class);
		} catch (IOException e) {
			return null;
		}
	}

	private Date parseDate(String value) {
		return new Date(Long.parseLong(value.replace(".", "")));
	}
}
