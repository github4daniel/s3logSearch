package pax.tecs.psconfig.web.record;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public enum Domain {
	pi_land("Pip Land"), pi_airsea("Pip Air Sea"), vi("Pip Vehicle");

	private String displayName;

	Domain(String displayName) {
		this.displayName = displayName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public static Map<String, String> buildMap() {
		LinkedHashMap<String, String> lmap = new LinkedHashMap<>();
		lmap.put("", "Select at least one");
		Arrays.asList(Domain.values()).stream().forEach(e ->
			lmap.put(e.name(), e.displayName)
		);
		return lmap;

	}

}
