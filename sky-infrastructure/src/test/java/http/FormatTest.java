package http;

import cn.hutool.core.date.format.FastDateFormat;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.Locale;


/**
 * @author <a href="mailto:likelovec@gmail.com">fzdwx</a>
 * @date 2022/5/30 16:15
 */
public class FormatTest {

    public static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    @Test
    void test() throws ParseException {
        final String s = "Mon, 30 May 2022 08:11:05 GMT";
        final FastDateFormat instance = FastDateFormat.getInstance(HTTP_DATE_FORMAT, Locale.US);
        instance.parse(s);
    }
}