package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ChromeDevToolsParserTest {
	private TestEntries entries = new TestEntries();
	private ChromeDevToolsParser parser = new ChromeDevToolsParser();

	@Before
	public void createLogParser() {
		parser.init();
		parser.setConsumer(entries);
	}

	@Test
	public void testDevToolsLog() throws Exception {
		parser.parseLine("[1610104509.861][INFO]: Test");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("Test", e.getMessage());
		assertEquals("1610104509.861", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("CONSOLE", e.getEmitter());
		assertEquals("[1610104509.861][INFO]: Test",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testDevToolsNavigationLog() throws Exception {
		parser.parseLine("[1610104509.861][INFO]: Waiting for pending navigations...");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("Waiting for pending navigations...", e.getMessage());
		assertEquals("1610104509.861", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("CONSOLE.navigations", e.getEmitter());
		assertEquals("[1610104509.861][INFO]: Waiting for pending navigations...",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testDevToolsWebSocketLog() throws Exception {
		parser.parseLine("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.evaluate (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {");
		parser.parseLine("   \"expression\": \"1\"");
		parser.parseLine("}");
		parser.parseLine(null);

		var e = entries.getEntry();
		assertEquals("(id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {\n   \"expression\": \"1\"\n}", e.getMessage());
		assertEquals("1610104509.861", e.getDate());
		assertEquals("DEBUG", e.getLevel());
		assertEquals("Runtime.evaluate", e.getEmitter());
		assertEquals("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.evaluate (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {\n   \"expression\": \"1\"\n}",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}
}
