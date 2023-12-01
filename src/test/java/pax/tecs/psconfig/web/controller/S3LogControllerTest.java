package pax.tecs.psconfig.web.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import org.mockito.Spy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.StringEscapeUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.support.BindingAwareModelMap;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import jakarta.servlet.http.HttpServletResponse;
import pax.tecs.psconfig.web.record.Domain;
import pax.tecs.psconfig.web.record.S3Record;
import pax.tecs.psconfig.web.record.S3SearchCriteria;

@SpringBootTest(classes={S3LogController.class})
class S3LogControllerTest {

	@Autowired
	private S3LogController s3LogController;
	
	@MockBean
	private AmazonS3 s3Client;
	
	private S3SearchCriteria s3SearchCriteria = new S3SearchCriteria();
	
	private BindingResult bindingResult = mock(BindingResult.class);
	
	private HttpServletResponse response = mock(HttpServletResponse.class);
	
	private Model model = new BindingAwareModelMap();
	
	private  List<Domain> domains = new ArrayList();
	
	private ListObjectsV2Result v2Result =  mock(ListObjectsV2Result.class);
	
	private List<S3ObjectSummary> summaries = new ArrayList<>();
	
	private S3ObjectSummary summary = mock(S3ObjectSummary.class);
	
	private S3Object s3Object =mock(S3Object.class);
	
	private PrintWriter printWriter = mock(PrintWriter.class);
	
	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	
	private Date lastModifiedDate;
	
	private String contentStr = "{\"@timestamp\":\"2023-10-30 06:10:37.143\",\"thread\":\"queryExec-1\",\"logLevel\":"
			+ "\"INFO\",\"logClass\":\"pax.tecs.domain.workflow.impl.RunQueryTypeFilter\",\"origLogEntry\":\"[180]RunQry filter for RUN_NCIC_QRY "
			+ ">>> ALLOW >>> traceInfo= [SAVN, siteCode=L236, pkgId=5093166554, msgId=WLAR02A011:59c09f84540, transId=WLAR02A011 2023103005103697] payload class=pax.tecs.domain.workflow.msg.WfPersonQueryRequest\"}";
	
	@Test
	void testGetS3Log() {
		ModelAndView view=  s3LogController.s3Log();
		assertEquals("s3logs", view.getViewName());
		assertNotNull(view.getModel().get("criteria"));
	}
	
	@Test
	void testSearchS3log() throws Exception {
		
		domains.add(Domain.pi_land);
		s3SearchCriteria.setEntryType(domains);
		
		s3SearchCriteria.setStartDateTime("2023-10-30T06:00:01");
		s3SearchCriteria.setEndDateTime("2023-10-30T07:00:02");
		
		s3SearchCriteria.setSearchText1("ALLOW");
		s3SearchCriteria.setSearchText2("NCIC");
		s3SearchCriteria.setSearchText3("WLAR02A011");
		
		lastModifiedDate = dateFormat.parse("2023-10-30T06:35");
		when(summary.getLastModified()).thenReturn(lastModifiedDate);
		when(summary.getBucketName()).thenReturn("avfbst-pspd-tecs");
		when(summary.getKey()).thenReturn("logs/pi-land/20231030/piplandfluentdservice-deployment-7fbb46d55f-ktwk5_2023103006_0.gz");
		
		summaries.add(summary);
		when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(v2Result);
		when(v2Result.getObjectSummaries()).thenReturn(summaries);
		when(v2Result.isTruncated()).thenReturn(false);
		
		when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
		ByteArrayOutputStream out =  createS3ObjectContentOnFly();
		when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(out.toByteArray()), null));
		
		when(bindingResult.hasErrors()).thenReturn(false);
		s3LogController.searchS3Log(s3SearchCriteria, bindingResult, response, model);

		List<S3Record> rs = (List<S3Record>) model.getAttribute("searchResultsList");
		assertEquals("Pip Land", rs.get(0).getDomainDisplay());
		assertEquals("logs/pi-land/20231030/piplandfluentdservice-deployment-7fbb46d55f-ktwk5_2023103006_0.gz", rs.get(0).getS3ObjectSummary().getKey());
		assertTrue(rs.get(0).getLine().contains("2023-10-30 06:10:37.143"));
		
	}
	
	@Test
	void testSearchS3logDownload() throws Exception {
		
		domains.add(Domain.pi_land);
		s3SearchCriteria.setEntryType(domains);
		
		s3SearchCriteria.setStartDateTime("2023-10-30T06:00:01");
		s3SearchCriteria.setEndDateTime("2023-10-30T07:00:02");
		
		s3SearchCriteria.setSearchText1("ALLOW");
		s3SearchCriteria.setSearchText2("NCIC");
		s3SearchCriteria.setSearchText3("WLAR02A011");
		
		s3SearchCriteria.setNumOfRec(1);		
		s3SearchCriteria.setDownload("download");
		
		lastModifiedDate = dateFormat.parse("2023-10-30T06:35");
		when(summary.getLastModified()).thenReturn(lastModifiedDate);
		when(summary.getBucketName()).thenReturn("avfbst-pspd-tecs");
		when(summary.getKey()).thenReturn("logs/pi-land/20231030/piplandfluentdservice-deployment-7fbb46d55f-ktwk5_2023103006_0.gz");
		
		summaries.add(summary);
		when(s3Client.listObjectsV2(any(ListObjectsV2Request.class))).thenReturn(v2Result);
		when(v2Result.getObjectSummaries()).thenReturn(summaries);
		when(v2Result.isTruncated()).thenReturn(false);
		
		when(s3Client.getObject(any(GetObjectRequest.class))).thenReturn(s3Object);
		ByteArrayOutputStream out =  createS3ObjectContentOnFly();
		when(s3Object.getObjectContent()).thenReturn(new S3ObjectInputStream(new ByteArrayInputStream(out.toByteArray()), null));
		
		when(bindingResult.hasErrors()).thenReturn(false);
		when(response.getWriter()).thenReturn(printWriter);
		s3LogController.searchS3Log(s3SearchCriteria, bindingResult, response, model);
		
		List<S3Record> rs = (List<S3Record>) model.getAttribute("searchResultsList");
		assertEquals("Pip Land", rs.get(0).getDomainDisplay());
		assertEquals("logs/pi-land/20231030/piplandfluentdservice-deployment-7fbb46d55f-ktwk5_2023103006_0.gz", rs.get(0).getS3ObjectSummary().getKey());
		assertTrue(rs.get(0).getLine().contains("2023-10-30 06:10:37.143"));
		
	}


	private ByteArrayOutputStream createS3ObjectContentOnFly() throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try(GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out)){
			gzipOutputStream.write(contentStr.getBytes());
		}
		
		return out;
		
	}
}
