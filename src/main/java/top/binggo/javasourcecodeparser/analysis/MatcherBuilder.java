package top.binggo.javasourcecodeparser.analysis;

import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

public interface MatcherBuilder<T> {
    Pair<T, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode);

    default boolean match(RangeString rangeString, String equal) {
        return StringUtils.endWithFromIndex(rangeString.s, rangeString.to - equal.length(), equal);
    }
}
