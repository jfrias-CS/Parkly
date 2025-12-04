package parkly;

import java.util.HashMap;
import java.util.Map;

// Used to authenticate valid users to enter program
public class AuthenticationService {
	// Use maps for O(1) look up
	private static final Map<String, String> VALID_EMPLOYEES = new HashMap<>();
	private static final Map<String, String> VALID_GATE_IDS = new HashMap<>();
	static {
        // Initialize valid users and their passwords
        VALID_EMPLOYEES.put("manager", "admin123"); 
        VALID_EMPLOYEES.put("employee1", "parkly456");
        VALID_EMPLOYEES.put("gatekeeper", "exit789");
        // Initialize valid Entry / Exit pairs
        VALID_GATE_IDS.put("G1", "G2");
		VALID_GATE_IDS.put("G3", "G4");
		VALID_GATE_IDS.put("G5", "G6");
    }
	
	public static boolean verifyCredentials(String username, String password) {
		return password != null && password.equals(VALID_EMPLOYEES.get(username));
	}
	
	public static boolean verifyGateId(String entryGate) {
		return VALID_GATE_IDS.containsKey(entryGate);
	}
	
	public static String getExitGateId(String entryGate) {
		return VALID_GATE_IDS.get(entryGate);		
	}
}
