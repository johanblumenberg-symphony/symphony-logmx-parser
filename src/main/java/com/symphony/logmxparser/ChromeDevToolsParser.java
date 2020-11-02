package com.symphony.logmxparser;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public class ChromeDevToolsParser extends LogFileParser {
	protected ParsedEntry entry = null;
	protected StringBuilder entryMsgBuffer = null;

	private static ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private final static Pattern ENTRY_BEGIN_PATTERN = Pattern.compile("^\\[(\\d*\\.\\d*)\\]\\[([A-Z]*)\\]: (.*)$");
	private final static Pattern DEVTOOLS_BEGIN_PATTERN = Pattern.compile("^DevTools WebSocket [^:]+: ([^ ]+) .*$");

	private static final String EXTRA_HIDDEN_ORG_FIELD_KEY = "org";
	private static final List<String> EXTRA_FIELDS_KEYS = Arrays.asList();

	public ChromeDevToolsParser() {
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		jsonMapper.setDefaultPrettyPrinter(prettyPrinter);
	}

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
		if (line == null) {
			recordPreviousEntryIfExists();
			return;
		}

		Matcher matcher = ENTRY_BEGIN_PATTERN.matcher(line);

		if (matcher.matches()) {
			prepareNewEntry();

			entry.setDate(matcher.group(1));
			entry.setLevel(matcher.group(2));
			entry.setMessage(matcher.group(3));
			String msg = matcher.group(3);

			Matcher devtools = DEVTOOLS_BEGIN_PATTERN.matcher(msg);
			if (devtools.matches()) {
				String emitter = devtools.group(1);
				entry.setEmitter(emitter);
			} else if ("Done waiting for pending navigations. Status: ok".equals(msg)) {
				entry.setEmitter("CONSOLE.navigations");
			} else if ("Waiting for pending navigations...".equals(msg)) {
				entry.setEmitter("CONSOLE.navigations");
			} else {
				entry.setEmitter("CONSOLE");
			}
			entryMsgBuffer.append(line);
		} else {
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
		return new Date(Long.parseLong(pEntry.getDate().replace(".", "")));
	}

	@Override
	public String getEntryStringRepresentation(ParsedEntry entry) {
		return (String) entry.getUserDefinedFields().get(EXTRA_HIDDEN_ORG_FIELD_KEY);
	}

	private Map<String, Object> parseArg(String value) {
		int i = value.indexOf('{');
		try {
			return jsonMapper.readValue(value.substring(i), Map.class);
		} catch (IOException e) {
			return null;
		}
	}

	protected void recordPreviousEntryIfExists() throws Exception {
		if (entry != null) {
			String orig = entryMsgBuffer.toString();
			entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, orig);

			if ("Runtime.consoleAPICalled".equals(entry.getEmitter())) {
				var parsed = parseArg(orig);

				if (parsed.get("args") != null && parsed.get("args") instanceof List) {
					var args = (List<Map<String, Object>>) parsed.get("args");

					if (args != null && !args.isEmpty() && args.get(0) instanceof Map) {
						String msg = "";
						for (Map<String, Object> arg : args) {
							if ("string".equals(arg.get("type")) && arg.get("value") instanceof String) {
								msg += arg.get("value") + " ";
							} else {
								break;
							}
						}
						if (msg.length() > 0) {
							entry.setMessage(msg);
						}
					}
				}
			} else if ("Log.entryAdded".equals(entry.getEmitter())) {
				var parsed = parseArg(orig);
				var logEntry = parsed.get("entry");

				if (logEntry instanceof Map) {
					var logEntry2 = (Map<String, Object>) logEntry;

					if (logEntry2.get("text") instanceof String) {
						entry.setMessage((String) logEntry2.get("text"));
					}
				}
			} else if ("Input.dispatchKeyEvent".equals(entry.getEmitter())) {
				var parsed = parseArg(orig);
				var type = parsed.get("type");
				var code = parsed.get("code");

				if (type instanceof String && code instanceof String) {
					entry.setMessage(type + " " + code);
				}
			}

			addEntry(entry);
			entry = null;
		}
	}

	protected void prepareNewEntry() throws Exception {
		recordPreviousEntryIfExists();
		entry = createNewEntry();
		entryMsgBuffer = new StringBuilder(80);
		entry.setUserDefinedFields(new HashMap<String, Object>(1));
	}
}
