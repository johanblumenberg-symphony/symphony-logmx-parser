package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class ManaParserTest {
	private TestEntries entries = new TestEntries();
	private ManaParser parser = new ManaParser();

	@Before
	public void createLogParser() {
		parser.init();
		parser.setConsumer(entries);
		parser.setParserLocale(Locale.US);
	}

	@Test
	public void testClient20Log() throws Exception {
		parser.parseLine("2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("hello", e.getMessage());
		assertEquals("2020-11-19T13:00:14.153Z", e.getDate());
		assertEquals("SYSTEM_INFO", e.getLevel());
		assertEquals("rtc.info", e.getEmitter());
		assertEquals("2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(2, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testClient15Log() throws Exception {
		parser.parseLine("2020-11-05T22:00:48.635Z | DEBUG(4) | rtc.info | hello");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("hello", e.getMessage());
		assertEquals("2020-11-05T22:00:48.635Z", e.getDate());
		assertEquals("DEBUG", e.getLevel());
		assertEquals("rtc.info", e.getEmitter());
		assertEquals("2020-11-05T22:00:48.635Z | DEBUG(4) | rtc.info | hello",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testMultiLineMana20Log() throws Exception {
		parser.parseLine(
				"1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: hello");
		parser.parseLine("world");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("hello\nworld", e.getMessage());
		assertEquals("2020-12-03T09:48:07.759Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("core.streamstore", e.getEmitter());
		assertEquals("1|2020-12-03T09:48:07.759Z|INFO(3)|core.streamstore: hello\nworld",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(1, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testJsonArgs() throws Exception {
		parser.parseLine("2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello, 1, {\"key\":\"value\"}");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("hello, 1, {\"key\":\"value\"}", e.getMessage());
		assertEquals("2020-11-19T13:00:14.153Z", e.getDate());
		assertEquals("SYSTEM_INFO", e.getLevel());
		assertEquals("rtc.info", e.getEmitter());
		assertEquals(
				"2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello, 1, {\"key\":\"value\"}\n\nhello\n1\n{\n  \"key\" : \"value\"\n}",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(2, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testStatistics() throws Exception {
		parser.parseLine("1|2020-11-25T16:35:59.323Z|DEBUG(4)|rtc.stats-4: stats, {\"name\":{\"key1\":\"value1\",\"key2\":\"value2\"}}");
		parser.parseLine(null);

		var e = entries.getEntries().get(0);
		assertEquals("stats, {\"name\":{\"key1\":\"value1\",\"key2\":\"value2\"}}", e.getMessage());
		assertEquals("rtc.stats-4", e.getEmitter());
		assertEquals("DEBUG", e.getLevel());
		assertEquals("2020-11-25T16:35:59.323Z", e.getDate());

		var s = entries.getEntries().get(1);
		assertEquals("{\"key1\":\"value1\",\"key2\":\"value2\"}", s.getMessage());
		assertEquals("rtc.stats-4.name", s.getEmitter());
		assertEquals("TRACE", s.getLevel());
		assertEquals("2020-11-25T16:35:59.323Z", s.getDate());
	}
	
	@Test
	public void testCopyColumnMana20Log() throws Exception {
		parser.parseLine("2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello, 1, {\"key\":\"value\"}");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals(
				"2|2020-11-19T13:00:14.153Z|SYSTEM_INFO(0)|rtc.info: hello, 1, {\"key\":\"value\"}",
				e.getUserDefinedFields().get(Parser.EXTRA_COPY_FIELD_KEY));
	}

	@Test
	public void testCopyColumnClient15Log() throws Exception {
		parser.parseLine("2020-11-05T22:00:48.635Z | DEBUG(4) | rtc.info | hello");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("2020-11-05T22:00:48.635Z | DEBUG(4) | rtc.info | hello",
				e.getUserDefinedFields().get(Parser.EXTRA_COPY_FIELD_KEY));
	}
}
