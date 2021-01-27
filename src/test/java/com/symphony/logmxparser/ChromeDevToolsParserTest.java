package com.symphony.logmxparser;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import com.lightysoft.logmx.business.ParsedEntry;

public class ChromeDevToolsParserTest {
	private TestEntries entries = new TestEntries();
	private ChromeDevToolsParser parser = new ChromeDevToolsParser();

	@Before
	public void createLogParser() {
		parser.init();
		parser.setConsumer(entries);
		parser.setParserLocale(Locale.US);
	}

	@Test
	public void testDevToolsLog() throws Exception {
		parser.parseLine("[1610104509.861][INFO]: Test");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
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

		ParsedEntry e = entries.getEntry();
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

		ParsedEntry e = entries.getEntry();
		assertEquals("(id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {\n   \"expression\": \"1\"\n}", e.getMessage());
		assertEquals("1610104509.861", e.getDate());
		assertEquals("DEBUG", e.getLevel());
		assertEquals("Runtime.evaluate", e.getEmitter());
		assertEquals("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.evaluate (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {\n   \"expression\": \"1\"\n}",
				e.getUserDefinedFields().get(Parser.EXTRA_HIDDEN_ORG_FIELD_KEY));
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testDevToolsConsoleApiLog() throws Exception {
		parser.parseLine("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.consoleAPICalled (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {");
		parser.parseLine("   \"args\": [ {");
		parser.parseLine("      \"type\": \"string\",");
		parser.parseLine("      \"value\": \"hello world\"");
		parser.parseLine("   } ]");
		parser.parseLine("}");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("hello world", e.getMessage());
		assertEquals("1610104509.861", e.getDate());
		assertEquals("DEBUG", e.getLevel());
		assertEquals("Runtime.consoleAPICalled", e.getEmitter());
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testDevToolsMana15Log() throws Exception {
		parser.parseLine("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.consoleAPICalled (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {");
		parser.parseLine("   \"args\": [ {");
		parser.parseLine("      \"type\": \"string\",");
		parser.parseLine("      \"value\": \"2021-01-08T11:15:15.981Z | INFO(3) | RtcLogImpl | RtcLog initiated\"");
		parser.parseLine("   } ]");
		parser.parseLine("}");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("RtcLog initiated", e.getMessage());
		assertEquals("2021-01-08T11:15:15.981Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("RtcLogImpl", e.getEmitter());
		assertEquals(null, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}

	@Test
	public void testDevToolsMana20Log() throws Exception {
		parser.parseLine("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.consoleAPICalled (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {");
		parser.parseLine("   \"args\": [ {");
		parser.parseLine("      \"type\": \"string\",");
		parser.parseLine("      \"value\": \"1|2021-01-08T11:15:15.981Z|INFO(3)|RtcLogImpl: RtcLog initiated\"");
		parser.parseLine("   } ]");
		parser.parseLine("}");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("RtcLog initiated", e.getMessage());
		assertEquals("2021-01-08T11:15:15.981Z", e.getDate());
		assertEquals("INFO", e.getLevel());
		assertEquals("RtcLogImpl", e.getEmitter());
		assertEquals(1, e.getUserDefinedFields().get(Parser.EXTRA_SEQ_FIELD_KEY));
	}
	
	@Test
	public void testCopyColumnMana20Log() throws Exception {
		parser.parseLine("[1610104509.861][DEBUG]: DevTools WebSocket Command: Runtime.consoleAPICalled (id=550) 8BB6563E7B1D68B5CA9DB401D4ED5410 {");
		parser.parseLine("   \"args\": [ {");
		parser.parseLine("      \"type\": \"string\",");
		parser.parseLine("      \"value\": \"1|2021-01-08T11:15:15.981Z|INFO(3)|RtcLogImpl: RtcLog initiated\"");
		parser.parseLine("   } ]");
		parser.parseLine("}");
		parser.parseLine(null);

		ParsedEntry e = entries.getEntry();
		assertEquals("1|2021-01-08T11:15:15.981Z|INFO(3)|RtcLogImpl: RtcLog initiated", e.getUserDefinedFields().get(Parser.EXTRA_COPY_FIELD_KEY));
	}
}
