package sky.starter.ext;

import org.springframework.util.ObjectUtils;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.HashSet;
import java.util.Set;

/**
 * Parses and matches a single param expression to a request.
 */
public class ParamExpression extends AbstractNameValueExpression<String> {

    private final Set<String> namesToMatch = new HashSet<>(WebUtils.SUBMIT_IMAGE_SUFFIXES.length + 1);


    ParamExpression(String expression) {
        super(expression);
        this.namesToMatch.add(getName());
        for (String suffix : WebUtils.SUBMIT_IMAGE_SUFFIXES) {
            this.namesToMatch.add(getName() + suffix);
        }
    }

    @Override
    protected boolean isCaseSensitiveName() {
        return true;
    }

    @Override
    protected String parseValue(String valueExpression) {
        return valueExpression;
    }

    @Override
    protected boolean matchName(HttpServletRequest request) {
        for (String current : this.namesToMatch) {
            if (request.getParameterMap().get(current) != null) {
                return true;
            }
        }
        return request.getParameterMap().containsKey(this.name);
    }

    @Override
    protected boolean matchValue(HttpServletRequest request) {
        return ObjectUtils.nullSafeEquals(this.value, request.getParameter(this.name));
    }
}