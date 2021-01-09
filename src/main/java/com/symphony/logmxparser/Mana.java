package com.symphony.logmxparser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public class Mana {
	private static ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private final static Pattern CLIENT20_BEGIN_PATTERN = Pattern
			.compile("^(\\d*)\\|(.*)\\|(.*)\\(\\d*\\)\\|([^:]*): (.*)$", Pattern.DOTALL);
	private final static Pattern CLIENT15_BEGIN_PATTERN = Pattern
			.compile("^([^\\s]*)\\s*\\|\\s*([^\\s]*)\\(\\d*\\)\\s*\\|\\s*([^\\s]*)\\s*\\|\\s*(.*)$", Pattern.DOTALL);

	private Parser parser;
	private SimpleDateFormat dateFormat;

	public Mana(Parser parser) {
		this.parser = parser;
		
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		jsonMapper.setDefaultPrettyPrinter(prettyPrinter);
	}

	protected boolean isManaLog(ParsedEntry entry) {
		return entry.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY) != null;
	}
	
	public void refineEntry(ParsedEntry entry) throws Exception {
		String line = entry.getMessage();
		Matcher matcher1 = CLIENT20_BEGIN_PATTERN.matcher(line);
		Matcher matcher2 = CLIENT15_BEGIN_PATTERN.matcher(line);

		if (matcher1.matches()) {
			Integer seq = Integer.parseInt(matcher1.group(1));
			entry.getUserDefinedFields().put(Parser.EXTRA_SEQ_FIELD_KEY, seq);
			Parser.setDate(entry, matcher1.group(2), parseDate(matcher1.group(2)));
			entry.setLevel(matcher1.group(3));
			entry.setEmitter(matcher1.group(4));
			entry.setMessage(matcher1.group(5));
			
			refineStringRepresentation(entry);
		} else if (matcher2.matches()) {
			Parser.setDate(entry, matcher2.group(1), parseDate(matcher2.group(1)));
			entry.setLevel(matcher2.group(2));
			entry.setEmitter(matcher2.group(3));
			entry.setMessage(matcher2.group(4));

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
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", parser.getParserLocale());			
		}
		synchronized (dateFormat) {
			return dateFormat.parse(value);
		}
	}
}
