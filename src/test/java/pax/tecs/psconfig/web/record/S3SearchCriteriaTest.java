package pax.tecs.psconfig.web.record;

import static org.junit.jupiter.api.Assertions.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

class S3SearchCriteriaTest {

	@Test
	void testExtractLogDate() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		Date ExDate = s.extractLogDate(
				"{\"@timestamp\":\"2023-08-09 11:50:00.001\",\"thread\":\"taskScheduler-3\",\"logLevel\":\"INFO\",\"logClass\":\"pax.tecs.domain.util.NcicContext\",\"origLogEntry\":\"2023-08-09 11:50:00.001 [taskScheduler-3] INFO pax.tecs.domain.util.NcicContext ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\"}");

		Date expected = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").parse("2023-08-09 11:50:00.001");
		assertTrue(ExDate.compareTo(expected) == 0);
	}

	@Test
	void testExtractSearchDays() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		s.setStartDateTime("2023-11-01T13:50:00");
		s.setEndDateTime("2023-11-03T13:50:01");
		List<String> sDays = s.extractSearchDays();
		assertTrue(sDays.contains("20231101"));
		assertTrue(sDays.contains("20231102"));
		assertTrue(sDays.contains("20231103"));
	}

	@Test
	void testAcceptBasedOnRelative() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		S3ObjectSummary s3Object = new S3ObjectSummary();
		LocalDateTime ldt = LocalDateTime.now();
		ldt = ldt.minusMinutes(35l);
		s3Object.setLastModified(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
		s.setPeriod(Period.m30);
		assertFalse(s.accept(s3Object));
	}
	
	@Test
	void testAcceptBasedOnRelativeWhenInsideTimeFrame() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		S3ObjectSummary s3Object = new S3ObjectSummary();
		LocalDateTime ldt = LocalDateTime.now();
		ldt = ldt.minusMinutes(29l);
		s3Object.setLastModified(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
		s.setPeriod(Period.m30);
		assertTrue(s.accept(s3Object));
	}
	

	@Test
	void testAcceptBasedOnRange() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		S3ObjectSummary s3Object = new S3ObjectSummary();
		LocalDateTime ldt = LocalDateTime.now();
		LocalDateTime lstModified = ldt.minusMinutes(35l);
		s3Object.setLastModified(Date.from(lstModified.atZone(ZoneId.systemDefault()).toInstant()));

		LocalDateTime sLdt = ldt.minusMinutes(40l);
		LocalDateTime eLdt = ldt.minusMinutes(30l);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		s.setStartDateTime(dateTimeFormat.format(sLdt));
		s.setEndDateTime(dateTimeFormat.format(eLdt));

		assertTrue(s.accept(s3Object));

	}
	
	@Test
	void testAcceptBasedOnRangeWhenOutOfUpperBound() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		S3ObjectSummary s3Object = new S3ObjectSummary();
		LocalDateTime ldt = LocalDateTime.now();
		LocalDateTime lstModified = ldt.minusMinutes(35l);
		s3Object.setLastModified(Date.from(lstModified.atZone(ZoneId.systemDefault()).toInstant()));

		LocalDateTime sLdt = ldt.minusMinutes(40l);
		LocalDateTime eLdt = ldt.minusMinutes(36l);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		s.setStartDateTime(dateTimeFormat.format(sLdt));
		s.setEndDateTime(dateTimeFormat.format(eLdt));

		assertFalse(s.accept(s3Object));

	}
	
	@Test
	void testAcceptBasedOnRangeWhenOutOfLowerBound() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		S3ObjectSummary s3Object = new S3ObjectSummary();
		LocalDateTime ldt = LocalDateTime.now();
		LocalDateTime lstModified = ldt.minusMinutes(35l);
		s3Object.setLastModified(Date.from(lstModified.atZone(ZoneId.systemDefault()).toInstant()));

		LocalDateTime sLdt = ldt.minusMinutes(34l);
		LocalDateTime eLdt = ldt.minusMinutes(30l);
		DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
		s.setStartDateTime(dateTimeFormat.format(sLdt));
		s.setEndDateTime(dateTimeFormat.format(eLdt));

		assertFalse(s.accept(s3Object));

	}

	@Test
	void testSaveAsFileName() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		List<Domain> types = Arrays.asList(new Domain[] { Domain.pi_airsea, Domain.pi_land });
		s.setEntryType(types);
		s.setSearchText1("1165526083350691");
		s.setStartDateTime("2023-09-06T07:25");
		s.setEndDateTime("2023-09-06T07:40");

		String fileName = s.saveAsFileName();
		assertEquals( "pi_airsea_pi_land_11655_2023-09-06T07:252023-09-06T07:40.txt", fileName);

	}
	
	@Test
	void testSaveAsFileNamePeriod() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		List<Domain> types = Arrays.asList(new Domain[] { Domain.pi_airsea, Domain.pi_land });
		s.setEntryType(types);
		s.setSearchText1("1165526083350691");
		s.setPeriod(Period.h1);

		String fileName = s.saveAsFileName();
		assertEquals("pi_airsea_pi_land_11655_h1.txt", fileName);

	}

	@Test
	void testMatch() throws ParseException {
		S3SearchCriteria s = new S3SearchCriteria();
		s.setSearchText1("ALLOW");
		s.setSearchText1("NCIC");
		s.setSearchText1("WLAR02A011");
		s.setStartDateTime("2023-10-30T06:00:01");
		s.setEndDateTime("2023-10-30T07:00:02");

		String line = "{\"@timestamp\":\"2023-10-30 06:10:37.143\",\"thread\":\"queryExec-1\",\"logLevel\":\"INFO\",\"logClass\":\"pax.tecs.domain.workflow.impl.RunQueryTypeFilter\",\"origLogEntry\":\"[180]RunQry filter for RUN_NCIC_QRY >>> ALLOW >>> traceInfo= [SAVN, siteCode=L236, pkgId=5093166554, msgId=WLAR02A011:59c09f84540, transId=WLAR02A011 2023103005103697] payload class=pax.tecs.domain.workflow.msg.WfPersonQueryRequest\"}";
		assertNotNull(s.match(line));

	}

}
