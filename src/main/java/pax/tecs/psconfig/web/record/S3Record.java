package pax.tecs.psconfig.web.record;

import java.util.Date;

import com.amazonaws.services.s3.model.S3ObjectSummary;

public class S3Record {

	private String line;
	private S3ObjectSummary s3ObjectSummary;
	private Date logDate;
	private String domainDisplay;

	public void setLine(String line) {
		this.line = line;
		
	}
	
	public S3ObjectSummary getS3ObjectSummary() {
		return s3ObjectSummary;
	}

	public void setS3ObjectSummary(S3ObjectSummary s3ObjectSummary) {
		this.s3ObjectSummary = s3ObjectSummary;
	}

	public String getLine() {
		return line;
	}
	
	public Date getLogDate() {
		return logDate;
	}
	
	public void setLogDate(Date logDate) {
		this.logDate = logDate;
	}
	
	public String getDomainDisplay() {
		return domainDisplay;
	}
	
	public void setDomainDisplay(String domainDisplay) {
		this.domainDisplay = domainDisplay;
	}
	
}
