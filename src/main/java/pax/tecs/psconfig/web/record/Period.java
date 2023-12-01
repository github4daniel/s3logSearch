package pax.tecs.psconfig.web.record;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public enum Period {
	
	m30("last 30 minutes"), h1("last 1 hour"),  h2("last 2 hours"), h4("last 4 hours");

	private String displayName;

	private Period(String displayName) {
		this.displayName=displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}



	public Date getDate(LocalDateTime ldt) {
		
		Date dt ;
		switch (this) {
		case m30:
			dt = Date.from(ldt.minusMinutes(30).atZone(ZoneId.systemDefault()).toInstant());
			break;
		case h1:
			dt = Date.from(ldt.minusHours(1l).atZone(ZoneId.systemDefault()).toInstant());
			break;
		case h2:
			dt = Date.from(ldt.minusHours(2l).atZone(ZoneId.systemDefault()).toInstant());
			break;
		case h4:
			dt = Date.from(ldt.minusHours(4l).atZone(ZoneId.systemDefault()).toInstant());
			break;
		default:
			throw new RuntimeException("The input date is wrong");
		}
		return dt;
	}

	public static Map<String, String> buildMap() {
		return Arrays.asList(Period.values()).stream()
				.collect(Collectors.toMap(Period::name, Period::getDisplayName, (a,b) -> a, LinkedHashMap::new));
	}

}
