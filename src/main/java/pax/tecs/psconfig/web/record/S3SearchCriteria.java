package pax.tecs.psconfig.web.record;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringEscapeUtils;

import com.amazonaws.services.s3.model.S3ObjectSummary;

import pax.tecs.psconfig.web.record.Domain;
import pax.tecs.psconfig.web.record.Period;

public class S3SearchCriteria {

	@NotNull(message = "Must select a domain type.")
	private List<Domain> entryType;

	private Period period;
	
	private String startDateTime;

	private String endDateTime;

	private Integer numOfRec;

	private String fileName;

	@NotEmpty(message = "Must enter a search text.")
	private String searchText1;

	private String searchText2;
	
	private String searchText3;

	private String download;

	private DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

	private DateFormat logDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	
	private DateTimeFormatter dateTimeFormat = DateTimeFormatter.ofPattern("yyyyMMdd");

	private static final String PATTERN = "(.*\"@timestamp\"\\:)(\"\\d{4}\\-\\d{2}\\-\\d{2} \\d{2}\\:\\d{2}\\:\\d{2}\\.\\d{3}\")(.*)";

	public Period getPeriod() {
		return period;
	}

	public void setPeriod(Period period) {
		this.period = period;
	}

	public List<Domain> getEntryType() {
		return entryType;
	}

	public void setEntryType(List<Domain> entryType) {
		this.entryType = entryType;
	}

	public Integer getNumOfRec() {
		return numOfRec;
	}

	public void setNumOfRec(Integer numOfRec) {
		this.numOfRec = numOfRec;
	}

	public String getSearchText1() {
		return searchText1;
	}

	public void setSearchText1(String searchText1) {
		this.searchText1 = searchText1;
	}

	public String getSearchText2() {
		return searchText2;
	}

	public void setSearchText2(String searchText2) {
		this.searchText2 = searchText2;
	}

	public String getSearchText3() {
		return searchText3;
	}

	public void setSearchText3(String searchText3) {
		this.searchText3 = searchText3;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStartDateTime() {
		return startDateTime;
	}

	public void setStartDateTime(String startDateTime) {
		this.startDateTime = startDateTime;
	}

	public String getEndDateTime() {
		return endDateTime;
	}

	public void setEndDateTime(String endDateTime) {
		this.endDateTime = endDateTime;
	}

	public Date getStartDate() throws ParseException {
		return Pattern.matches("yyyy-MM-dd'T'HH:mm:ss", startDateTime) ? dateFormat.parse(startDateTime)
				: new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(startDateTime);
	}

	public Date getEndDate() throws ParseException {
		return Pattern.matches("yyyy-MM-dd'T'HH:mm:ss", endDateTime) ? dateFormat.parse(endDateTime)
				: new SimpleDateFormat("yyyy-MM-dd'T'HH:mm").parse(endDateTime);
	}

	public String getDownload() {
		return download;
	}

	public void setDownload(String download) {
		this.download = download;
	}
	
	public S3SearchCriteria() {
		Date d = new Date();
		this.startDateTime = dateFormat.format(d);
		this.endDateTime = dateFormat.format(d);
	}

	public String saveAsFileName() {
		String saveAsFileName = fileName;

		if (fileName == null || fileName.isEmpty()) {
			saveAsFileName = this.getEntryNames().concat("_")
					.concat(searchText1.substring(0, Math.min(5, searchText1.length()))).concat("_");
			if (searchText2 != null && !searchText2.isEmpty()) {
				saveAsFileName = saveAsFileName.concat(searchText2.substring(0, Math.min(5, searchText2.length())))
						.concat("_");
			}
			if (searchText3 != null && !searchText3.isEmpty()) {
				saveAsFileName = saveAsFileName.concat(searchText3.substring(0, Math.min(5, searchText3.length())))
						.concat("_");
			}
			
			if (period != null) {
				saveAsFileName = saveAsFileName.concat(period.name());
			} else {
				saveAsFileName = saveAsFileName.concat(startDateTime.replace("\\s+", "_"))
						.concat(endDateTime.replace("\\s+", "_"));
			}
		}
		return saveAsFileName + ".txt";

	}

	protected String getEntryNames() {
		String names = entryType.stream().map(e -> e.name() + "_").collect(Collectors.joining());
		return names.substring(0, names.length() - 1);
	}

	public boolean accept(S3ObjectSummary s3Object) {
		boolean accept = true;

		try {
			Date lastModifiedDate = s3Object.getLastModified();

			if (period != null) {
				return lastModifiedDate.compareTo(period.getDate(LocalDateTime.now()))>= 0;
			} else {
				if (lastModifiedDate.compareTo(getStartDate()) < 0 || lastModifiedDate.compareTo(getEndDate()) > 0) {
					return false;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return accept;
	}

	public S3Record match(String line) throws ParseException {
		if (textMacthed(line) && timeMatched(line)) {
			S3Record s3Record = new S3Record();
			s3Record.setLogDate(extractLogDate(line));
			s3Record.setLine(line);
			return s3Record;
		} else {
			return null;
		}
	}

	protected boolean textMacthed(String line) {
		return line.contains(searchText1)
				&& (searchText2 == null || searchText2.isEmpty()
						|| (line.contains(searchText2)))
				&& (searchText3 == null || searchText3.isEmpty()
						|| (line.contains(searchText3)));
	}

	protected boolean timeMatched(String line) throws ParseException {
		Date logDate = extractLogDate(line);
		if (logDate != null) {
			if (period != null && logDate.compareTo(period.getDate(LocalDateTime.now())) < 0) {
				return false;
			} else {
				if (logDate.compareTo(getStartDate()) <0  || logDate.compareTo(getEndDate()) >0 ) {
					return false;
				}
			}
			return true;
		}
		return false;
	}

	protected Date extractLogDate(String line) throws ParseException {

		Pattern p = Pattern.compile(PATTERN);

		Matcher matcher = p.matcher(line);
		String timestamp = null;
		if (matcher.find()) {
			timestamp = matcher.group(2);
		}
		Date logDate = null;
		if (timestamp != null) {
			logDate = logDateFormat.parse(timestamp.replace("\"", ""));
		}
		
		return logDate;

	}

	public List<String> extractSearchDays() throws ParseException {
		
		List<LocalDate> dates= new ArrayList<>();
		LocalDate lStart = getStartDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate lEnd = getEndDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		while (lStart.compareTo(lEnd)<=0) {
			dates.add(lStart);
			lStart = lStart.plusDays(1l);
		}
		return dates.stream().map(d-> dateTimeFormat.format(d)).toList();
	}

}
