package com.symphony.logmxparser.format;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightysoft.logmx.business.ParsedEntry;
import com.symphony.logmxparser.base.Parser;

public class Mana {
	private static ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
	private static ObjectMapper jsonMapperShort = new ObjectMapper();

	private final static Pattern CLIENT20_BEGIN_PATTERN = Pattern
			.compile("^(\\d*)\\|(.*)\\|(.*)\\(\\d*\\)\\|([^:]*): (.*)$", Pattern.DOTALL);
	private final static Pattern CLIENT15_BEGIN_PATTERN = Pattern
			.compile("^([^\\s]*)\\s*\\|\\s*([^\\s]*)\\(\\d*\\)\\s*\\|\\s*([^\\s]*)\\s*\\|\\s*(.*)$", Pattern.DOTALL);
	private final static Pattern CLIENT20_STATS_EMITTER_PATTERN = Pattern.compile("^rtc\\.stats-(\\d+)$");
	private final static Pattern CLIENT20_STATS_MESSAGE_PATTERN = Pattern.compile("^stats, (.*)$");
	private final static Pattern CLIENT15_STATS_EMITTER_PATTERN = Pattern.compile("^stats-(\\d+)$");
	private final static Pattern CLIENT15_STATS_MESSAGE_PATTERN = Pattern
			.compile("^stats, '\\[object Object\\]': (.*)$");

	private Parser parser;
	private SimpleDateFormat dateFormat;

	public Mana(Parser parser) {
		this.parser = parser;

		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		jsonMapper.setDefaultPrettyPrinter(prettyPrinter);
	}

	public boolean isStartLine(String line) {
		return CLIENT20_BEGIN_PATTERN.matcher(line).matches() || CLIENT15_BEGIN_PATTERN.matcher(line).matches();
	}

	public void refineEntry(ParsedEntry entry) throws Exception {
		String line = entry.getMessage();
		Matcher matcher1 = CLIENT20_BEGIN_PATTERN.matcher(line);
		Matcher matcher2 = CLIENT15_BEGIN_PATTERN.matcher(line);

		if (matcher1.matches()) {
			Integer seq = Integer.parseInt(matcher1.group(1));
			entry.getUserDefinedFields().put(Parser.EXTRA_SEQ_FIELD_KEY, seq);
			Parser.setDate(entry, parseDate(matcher1.group(2)));
			entry.setLevel(matcher1.group(3));
			entry.setEmitter(matcher1.group(4));
			entry.setMessage(matcher1.group(5));
			entry.getUserDefinedFields().put(Parser.EXTRA_COPY_FIELD_KEY, line);

			refineStringRepresentation(entry);
		} else if (matcher2.matches()) {
			Parser.setDate(entry, parseDate(matcher2.group(1)));
			entry.setLevel(matcher2.group(2));
			entry.setEmitter(matcher2.group(3));
			entry.setMessage(matcher2.group(4));
			entry.getUserDefinedFields().put(Parser.EXTRA_COPY_FIELD_KEY, line);

			refineStringRepresentation(entry);
		}
	}

	private void refineStringRepresentation(ParsedEntry entry) {
		String message = entry.getMessage();

		for (int i = message.indexOf(','); i >= 0; i = message.indexOf(',', i + 1)) {
			try {
				List<Object> parsed = jsonMapper.readValue("[" + message.substring(i + 1) + "]", List.class);

				StringBuilder result = new StringBuilder();

				result.append(entry.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY)).append("\n\n");

				result.append(message.substring(0, i));
				for (Object arg : parsed) {
					result.append('\n');
					result.append(jsonMapper.writeValueAsString(arg));
				}
				entry.getUserDefinedFields().put(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY, result.toString());
				break;
			} catch (IOException e) {
				// try next
			}
		}
	}

	private Date parseDate(String value) throws Exception {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			dateFormat.setTimeZone(TimeZone.getTimeZone("Etc/UTC"));
		}
		synchronized (dateFormat) {
			return dateFormat.parse(value);
		}
	}

	public void addStatisticsEvents(ParsedEntry entry) throws Exception {
		Matcher emitter20 = CLIENT20_STATS_EMITTER_PATTERN.matcher(entry.getEmitter());
		Matcher message20 = CLIENT20_STATS_MESSAGE_PATTERN.matcher(entry.getMessage());
		Matcher emitter15 = CLIENT15_STATS_EMITTER_PATTERN.matcher(entry.getEmitter());
		Matcher message15 = CLIENT15_STATS_MESSAGE_PATTERN.matcher(entry.getMessage());

		System.out.println("Matches " + emitter15.matches() + " " + message15.matches() + " " + entry.getMessage());
		if (emitter20.matches() && message20.matches()) {
			Map<String, Object> parsed = parseValue(message20.group(1));

			if (parsed != null) {
				for (Map.Entry<String, Object> value : parsed.entrySet()) {
					ParsedEntry e = parser.prepareNewEntryFrom(entry);
					e.setEmitter(entry.getEmitter() + "." + value.getKey());
					e.setLevel("TRACE");
					e.setMessage(jsonMapperShort.writeValueAsString(value.getValue()));
					e.getUserDefinedFields().put(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY, jsonMapper.writeValueAsString(value.getValue()));
					e.getUserDefinedFields().put(Parser.EXTRA_COPY_FIELD_KEY, jsonMapperShort.writeValueAsString(value.getValue()));

					parser.addParsedEntry(e);
				}
			}
		} else if (emitter15.matches() && message15.matches()) {
			Map<String, Object> parsed = parseValue(message15.group(1));

			if (parsed != null) {
				for (Map.Entry<String, Object> value : parsed.entrySet()) {
					ParsedEntry e = parser.prepareNewEntryFrom(entry);
					e.setEmitter(entry.getEmitter() + "." + value.getKey());
					e.setLevel("TRACE");
					e.setMessage(jsonMapperShort.writeValueAsString(value.getValue()));
					e.getUserDefinedFields().put(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY, jsonMapper.writeValueAsString(value.getValue()));
					e.getUserDefinedFields().put(Parser.EXTRA_COPY_FIELD_KEY, jsonMapperShort.writeValueAsString(value.getValue()));
					parser.addParsedEntry(e);
				}
			}
		}
	}

	private Map<String, Object> parseValue(String value) {
		try {
			return jsonMapper.readValue(value, Map.class);
		} catch (IOException e) {
			return null;
		}
	}
}

// TODO: Include orignal line in stats messages
// TODO: Separate original message and text representation
// TODO: Add "gone" messages to stats
