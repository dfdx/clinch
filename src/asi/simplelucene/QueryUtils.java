
package asi.simplelucene;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryUtils {

    
    public static String termValue(String query, String termName) {
        Pattern pattern = Pattern.compile(termName + ":[\\S]+");
        Matcher matcher = pattern.matcher(query);

        String value = "";
        if (matcher.find()) {
            value = matcher.group();
            value = value.replace(termName + ":", "");
        }

        return value;
    }

}
