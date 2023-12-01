package pax.tecs.psconfig.web.record;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.Date;
import org.junit.jupiter.api.Test;

class PeriodTest {

	@Test
	void testBuildMap() {
		Map<String, String> m = Period.buildMap(); 
		assertTrue(m.containsKey(Period.h1.name()));
		assertTrue(m.containsValue(Period.h1.getDisplayName()));
	}
	
	@Test
	void testGetDate() {
		LocalDateTime ldt = LocalDateTime.now();
		Date now = Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
		Date m30 = Period.m30.getDate(ldt);
		Date h1 = Period.h1.getDate(ldt);
		Date h2 = Period.h2.getDate(ldt);
		Date h4 = Period.h4.getDate(ldt);
		
		long mins30 = (now.getTime() - m30.getTime())/1000/60;
		long minsH1 = (now.getTime() - h1.getTime())/1000/60;
		long minsH2 = (now.getTime() - h2.getTime())/1000/60;
		long minsH4 = (now.getTime() - h4.getTime())/1000/60;
		
		assertEquals(30l, mins30);
		assertEquals(60l, minsH1);
		assertEquals(120l, minsH2);
		assertEquals(240l, minsH4);
	}

}
