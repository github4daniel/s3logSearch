package pax.tecs.psconfig.web.record;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.Test;

class DomainTest {

	@Test
	void testBuildMap() {
		Map<String, String> m = Domain.buildMap(); 
		assertTrue(m.containsKey(Domain.pi_airsea.name()));
		assertTrue(m.containsValue(Domain.pi_airsea.getDisplayName()));
	}
}
