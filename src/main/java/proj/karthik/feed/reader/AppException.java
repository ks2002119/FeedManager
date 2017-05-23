package proj.karthik.feed.reader;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by ksubramanian on 5/19/17.
 */
public class AppException extends RuntimeException {
    private int code = -1;
    private String potentialFix;

    public AppException(String message, Throwable cause, int code, String potentialFix) {
        super(message, cause);
        this.code = code;
        this.potentialFix = potentialFix;
    }

    public AppException(String message, Throwable cause, String... args) {
        super(String.format(message, args), cause);
    }

    public AppException(int code, String message, Throwable cause, String... args) {
        super(String.format(message, args), cause);
        this.code = code;
    }

    public AppException(final String message, String... args) {
        super(String.format(message, args));
    }

    public AppException(int code, String message, String... args) {
        super(String.format(message, args));
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public String getPotentialFix() {
        return potentialFix;
    }

    public Map<String, String> getErrorMap() {
        Map<String, String> errorMap = new LinkedHashMap<>();
        errorMap.put("cause", getMessage());
        errorMap.put("Potential Fix", potentialFix == null? "" : potentialFix);
        return errorMap;
    }
}
