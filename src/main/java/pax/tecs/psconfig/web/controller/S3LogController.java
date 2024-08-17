package pax.tecs.psconfig.web.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.GZIPInputStream;
import java.util.concurrent.Future;
import java.util.function.Function;

import jakarta.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;

import pax.tecs.psconfig.web.record.Domain;
import pax.tecs.psconfig.web.record.Period;
import pax.tecs.psconfig.web.record.S3Record;
import pax.tecs.psconfig.web.record.S3SearchCriteria;

@Controller
public class S3LogController {

	@Value("${S3_BUCKET_NAME}")
	private String bucketName;
	
	@Value("${dir_path:/data}")
	private String dataDir;

	private static String bucketPrefix = "logs";

	private Logger logger;

	private static final String S3_LOG_VIEW = "s3logs";

	private AmazonS3 s3Client;
	
	public S3LogController(AmazonS3 s3Client) {
		super();
		this.s3Client = s3Client;
		logger = LoggerFactory.getLogger(S3LogController.class);
	}

	@GetMapping(value = "/s3logs")
	public ModelAndView s3Log() {
		ModelAndView model = new ModelAndView();
		model.setViewName(S3_LOG_VIEW);
		model.addObject("criteria", new S3SearchCriteria());
		model.addObject("entries", Domain.buildMap());
		model.addObject("periods", Period.buildMap());

		return model;
	}

	@PostMapping(value = "/s3logs")
	public String searchS3Log(@ModelAttribute("criteria") @Valid S3SearchCriteria criteria, BindingResult bindingResult,
			HttpServletResponse response, Model model) {
		try {
			model.addAttribute("entries", Domain.buildMap());
			model.addAttribute("periods", Period.buildMap());
			model.addAttribute("criteria", criteria);

			if (!bindingResult.hasErrors()) {
				List<S3Record> records = s3LogSearch(criteria);
				List<S3Record> nRecords = records;

				if (!records.isEmpty()) {
					if (criteria.getNumOfRec() != null) {
						nRecords = records.stream().limit(criteria.getNumOfRec()).toList();
						model.addAttribute("recordsOfTotal",
								Math.min(criteria.getNumOfRec(), records.size()) + " of " + records.size());
					} else {
						model.addAttribute("recordsOfTotal", records.size());
					}
					if (criteria.getDownload() != null) {
						response.setContentType("text/plain");
						response.setHeader("Content-disposition", "attachment; filename=" + criteria.saveAsFileName());
						PrintWriter pw = response.getWriter();
						records.stream().forEach(e -> {
							pw.write(e.getDomainDisplay());
							pw.write("\t");
							pw.write(e.getFileName());
							pw.write("\t");
							pw.write(e.getLine());
							pw.write("\n");

						});
						pw.close();

					}
				}

				nRecords.stream().forEach(r -> 
					r.setLine(StringEscapeUtils.escapeXml11(r.getLine())));

				model.addAttribute("searchResultsList", nRecords);

				response.flushBuffer();
			}

		} catch (Exception e) {
			logger.error("An error happened here", e);
		}
		return S3_LOG_VIEW;
	}

	public List<S3Record> s3LogSearch(S3SearchCriteria criteria) throws ParseException {

		LocalTime before = LocalTime.now();

		List<String> searchDays = criteria.extractSearchDays();

		List<S3Record> allRecords = new ArrayList<>();
		
		String tmpDir = dataDir + File.separator + UUID.randomUUID().toString();
		Path tmpPath = Paths.get(tmpDir);
		
		try {
			
			Files.createDirectories(tmpPath);
		}catch (IOException ie) {
			logger.error("Error in creating directory {}", tmpPath);
		}

		criteria.getEntryType().stream().forEach(e -> {

			final List<S3ObjectSummary> ls = new ArrayList<>();
			searchDays.stream().forEach(day -> {
				ListObjectsV2Request request = new ListObjectsV2Request().withBucketName(bucketName)
						.withPrefix(bucketPrefix + "/" + e.name().replace("_", "-") + "/" + day);
				ListObjectsV2Result result = null;

				do {
					result = s3Client.listObjectsV2(request);
					ls.addAll(result.getObjectSummaries());
					request.setContinuationToken(result.getNextContinuationToken());

				} while (result.isTruncated());

				// remove probable duplication based on s3 object key
				List<S3ObjectSummary> candidateList = ls.stream()
						.collect(Collectors.toMap(S3ObjectSummary::getKey, Function.identity())).values().stream().toList();

				candidateList = candidateList.stream().filter(criteria::accept).toList();

				logger.info("Total number of candidate files: {}  for the domain {} ", candidateList.size(), e.getDisplayName());

				Stream<Path> files = Stream.empty();
				downloadS3Object(candidateList, tmpPath);
				
				try {
					files = Files.list(tmpPath);
				}catch(IOException ie){
					logger.error("Error exception in list files retrieved from S3 bucket", ie);
				}
				
				List<S3Record> records = new ArrayList<>();
				ExecutorService eService = Executors.newFixedThreadPool(6);
				List<Future<List<S3Record>>> futures = new ArrayList<>();

				files.forEach(p -> {
					Callable<List<S3Record>> callable = new S3ThreadWorker(p, criteria, e);
					Future<List<S3Record>> future = eService.submit(callable);
					futures.add(future);
				});

				futures.stream().forEach(f -> {
					try {
						records.addAll(f.get());
					} catch (InterruptedException | ExecutionException e1) {
						logger.info(e1.toString());
						Thread.currentThread().interrupt();
					}
				});

				records.sort((r1, r2) -> r1.getLogDate().compareTo(r2.getLogDate()));

				logger.info(
						"Total number of records found: {}  for the domain {}. ", records.size(), e.getDisplayName());

				allRecords.addAll(records);

			});
		});

		FileUtils.deleteDirectory(tmpPath.toFile());
		LocalTime after = LocalTime.now();

		logger.info("Total number of all records found {} ", allRecords.size());
		logger.info("Total execution time cost: {} seconds", before.until(after, ChronoUnit.SECONDS));

		return allRecords;
	}
	
	private void downloadS3Object(List<S3ObjectSummary> candidateList, Path tmpDirectory) {
		candidateList.stream().forEach(e->{
			String key = e.getKey();
			String parts = key.split("/");
			String fileName = parts[parts.length-1];
			Path filePath = tmpDirectory.resolve(fileName);
			s3Client.getObject(new GetObjectRequest(bucketName, key), filePath.toFile());
		});	
	}

	private class S3ThreadWorker implements Callable<List<S3Record>> {

		private Path path;
		private S3SearchCriteria criteria;
		private Domain current;

		public S3ThreadWorker(Path path, S3SearchCriteria criteria, Domain current) {
			this.path = path;
			this.criteria = criteria;
			this.current = current;
		}

		@Override
		public List<S3Record> call() throws Exception {
			List<S3Record> rs = new ArrayList<>();
			
			try(InputStream fileInputStream = Files.newInputStream(path); 
					GZIPInputStream in = new GZIPInputStream(fileInputStream)){
				String line = null;
				try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
					while ((line = reader.readLine()) != null) {
						S3Record s3Record = criteria.match(line);
						if (s3Record != null) {
							s3Record.setFileName(path.getFileName().toString());
							s3Record.setDomainDisplay(current.getDisplayName());
							rs.add(s3Record);
						}

					}
				} catch (Exception ie) {
					logger.info(ie.toString());
				} finally {
					in.close();
				}
			}
			return rs;
			}	
		}

	}
}
