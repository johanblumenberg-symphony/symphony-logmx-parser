package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeParserTest {
	private TestEntries entries = new TestEntries();
	private ChromeParser parser = new ChromeParser();

	@Before
	public void createLogParser() {
		parser.init();
		parser.setConsumer(entries);
		parser.setParserLocale(Locale.US);
	}

	@Test
	public void testChromeLog() throws Exception {
		parser.parseLine("[89:89:1203/094800.745154:INFO:cpu_info(53)] Available number of cores: 4");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("Available number of cores: 4", e.getMessage());
		assertEquals("094800.745154", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("chrome.cpu_info", e.getEmitter());
		assertEquals("[89:89:1203/094800.745154:INFO:cpu_info(53)] Available number of cores: 4",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testChromeLogFromCcFile() throws Exception {
		parser.parseLine("[89:89:1203/094800.745154:INFO:cpu_info.cc(53)] Available number of cores: 4");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("Available number of cores: 4", e.getMessage());
		assertEquals("094800.745154", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("chrome.cpu_info", e.getEmitter());
		assertEquals("[89:89:1203/094800.745154:INFO:cpu_info.cc(53)] Available number of cores: 4",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMultiLineChromeLog() throws Exception {
		parser.parseLine("[89:89:1203/094800.745154:INFO:cpu_info.cc(53)] hello");
		parser.parseLine("world");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("hello\nworld", e.getMessage());
		assertEquals("094800.745154", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("chrome.cpu_info", e.getEmitter());
		assertEquals("[89:89:1203/094800.745154:INFO:cpu_info.cc(53)] hello\nworld",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testConsoleLog() throws Exception {
		parser.parseLine(
				"[81:81:1203/094806.902929:INFO:CONSOLE(2)] \"Hello World!\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("Hello World!", e.getMessage());
		assertEquals("094806.902929", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("CONSOLE", e.getEmitter());
		assertEquals(
				"[81:81:1203/094806.902929:INFO:CONSOLE(2)] \"Hello World!\", source: https://test.com/bundle.js (2)",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMultiLineConsoleLog() throws Exception {
		parser.parseLine("[81:81:1203/094806.902929:INFO:CONSOLE(2)] \"Hello");
		parser.parseLine("World!\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("Hello\nWorld!", e.getMessage());
		assertEquals("094806.902929", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("CONSOLE", e.getEmitter());
		assertEquals(
				"[81:81:1203/094806.902929:INFO:CONSOLE(2)] \"Hello\nWorld!\", source: https://test.com/bundle.js (2)",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMana20Log() throws Exception {
		parser.parseLine(
				"[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: created\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("created", e.getMessage());
		assertEquals("2020-12-03T09:48:07.759Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("core.streamstore", e.getEmitter());
		assertEquals("[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: created\", source: https://test.com/bundle.js (2)",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(1, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMana15Log() throws Exception {
		parser.parseLine(
				"[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"2020-12-03T09:48:07.759Z | INFO(3) | core.streamstore | created\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("created", e.getMessage());
		assertEquals("2020-12-03T09:48:07.759Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("core.streamstore", e.getEmitter());
		assertEquals("[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"2020-12-03T09:48:07.759Z | INFO(3) | core.streamstore | created\", source: https://test.com/bundle.js (2)",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMultiLineMana20Log() throws Exception {
		parser.parseLine(
				"[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: hello");
		parser.parseLine("world\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("hello\nworld", e.getMessage());
		assertEquals("2020-12-03T09:48:07.759Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("core.streamstore", e.getEmitter());
		assertEquals("[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: hello\nworld\", source: https://test.com/bundle.js (2)",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(1, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testCopyColumnMana20Log() throws Exception {
		parser.parseLine(
				"[81:81:1203/094807.791698:INFO:CONSOLE(2)] \"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: created\", source: https://test.com/bundle.js (2)");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: created",
				e.getUserDefinedFields().get(Parser.EXTRA_COPY_FIELD_KEY));
	}
}
