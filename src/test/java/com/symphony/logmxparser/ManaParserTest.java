package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ManaParserTest {
	private TestEntries entries = new TestEntries();
	private ManaParser parser = new ManaParser();

	@Before
	public void createLogParser() {
		parser.init();
		parser.setConsumer(entries);
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
}
