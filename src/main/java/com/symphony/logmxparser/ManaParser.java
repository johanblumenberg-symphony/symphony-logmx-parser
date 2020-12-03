package com.symphony.logmxparser;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.util.DefaultIndenter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.lightysoft.logmx.business.ParsedEntry;
import com.lightysoft.logmx.mgr.LogFileParser;

public class ManaParser extends Parser {
	private SimpleDateFormat dateFormat = null;
	private static ObjectMapper jsonMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

	private final static Pattern CLIENT20_BEGIN_PATTERN = Pattern
			.compile("^(\\d*)\\|(.*)\\|(.*)\\(\\d*\\)\\|([^:]*): (.*)$", Pattern.DOTALL);
	private final static Pattern CLIENT15_BEGIN_PATTERN = Pattern
			.compile("^([^\\s]*)\\s*\\|\\s*([^\\s]*)\\(\\d*\\)\\s*\\|\\s*([^\\s]*)\\s*\\|\\s*(.*)$", Pattern.DOTALL);

	public ManaParser() {
		DefaultPrettyPrinter prettyPrinter = new DefaultPrettyPrinter();
		prettyPrinter.indentArraysWith(DefaultIndenter.SYSTEM_LINEFEED_INSTANCE);
		jsonMapper.setDefaultPrettyPrinter(prettyPrinter);
	}

	@Override
	public String getParserName() {
		return "Symphony Client 2.0 log file parser";
	}

	@Override
	public String getSupportedFileType() {
		return "Symphony Client 2.0 log files";
	}

	protected boolean isManaLog(ParsedEntry entry) {
		return entry.getUserDefinedFields().get(EXTRA_SEQ_FIELD_KEY) != null;
	}
	
	protected boolean parseEntry(String line) throws Exception {
		Matcher matcher1 = CLIENT20_BEGIN_PATTERN.matcher(line);
		Matcher matcher2 = CLIENT15_BEGIN_PATTERN.matcher(line);

		if (matcher1.matches()) {
			prepareNewEntry();

			Integer seq = Integer.parseInt(matcher1.group(1));
			entry.getUserDefinedFields().put(EXTRA_SEQ_FIELD_KEY, seq);
			entry.setDate(matcher1.group(2));
			entry.setLevel(matcher1.group(3));
			entry.setEmitter(matcher1.group(4));
			entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

			entryMsgBuffer.append(matcher1.group(5));

			return true;
		} else if (matcher2.matches()) {
			prepareNewEntry();

			entry.setDate(matcher2.group(1));
			entry.setLevel(matcher2.group(2));
			entry.setEmitter(matcher2.group(3));
			entry.getUserDefinedFields().put(EXTRA_HIDDEN_ORG_FIELD_KEY, line);

			entryMsgBuffer.append(matcher2.group(4));

			return true;

		} else {
			return false;
		}
	}

	@Override
	protected void parseLine(String line) throws Exception {
		if (line == null) {
			recordPreviousEntryIfExists();
			return;
		}

		if (!parseEntry(line)) {
			if (entry != null) {
				entryMsgBuffer.append('\n').append(line);
			}
		}
	}

	@Override
	public Date getRelativeEntryDate(ParsedEntry pEntry) throws Exception {
		return null;
	}

	@Override
	public Date getAbsoluteEntryDate(ParsedEntry pEntry) throws Exception {
		if (dateFormat == null) {
			dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", getLocale());
		}
		synchronized (dateFormat) {
			return dateFormat.parse(pEntry.getDate());
		}
	}

	@Override
	public String getEntryStringRepresentation(ParsedEntry entry) {
		String message = entry.getMessage();

		for (int i = message.indexOf(','); i >= 0; i = message.indexOf(',', i + 1)) {
			try {
				List<Object> parsed = jsonMapper.readValue("[" + message.substring(i + 1) + "]", List.class);

				StringBuilder result = new StringBuilder();

				result.append(entry.getUserDefinedFields().get(EXTRA_HIDDEN_ORG_FIELD_KEY)).append("\n\n");

				result.append(message.substring(0, i));
				for (Object arg : parsed) {
					result.append('\n');
					result.append(jsonMapper.writeValueAsString(arg));
				}
				return result.toString();
			} catch (IOException e) {
				// try next
			}
		}

		StringBuilder result = new StringBuilder();
		result.append(entry.getUserDefinedFields().get(EXTRA_HIDDEN_ORG_FIELD_KEY)).append("\n\n");
		result.append(message);
		return result.toString();
	}
}
