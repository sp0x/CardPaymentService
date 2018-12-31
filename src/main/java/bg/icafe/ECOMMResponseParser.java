package bg.icafe;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ECOMMResponseParser {
    /**
     * Parse a ECOMM response string to a hashmap
     * @param payload
     * @return
     */
    public Map<String, String> parse(String payload){
        String[] lines = payload.split("\n");
        Map<String, String> output = new HashMap<>();
        for (String line:
             lines) {
            String[] kvpair = line.split(": ");
            output.put(kvpair[0],kvpair[1]);
        }
        return output;
    }
}
