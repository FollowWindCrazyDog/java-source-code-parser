package top.binggo.javasourcecodeparser.analysis;

import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.List;

/**
 * @author binggo
 */
public interface Creator {
    /**
     * sourceCode = '类的内部}'
     */
    static Pair<Integer, String> createClassOrInterface(RangeString sourceCode, Creator[] creators, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode, EnumFieldMembers enumFieldMembers) {
        int from = sourceCode.from;
        int i;
        for (i = sourceCode.from + 1; i <= sourceCode.s.length() && sourceCode.s.charAt(i - 1) != JavaKeyWords.RIGHT_CURLY_BRACES.charAt(0); ) {
            boolean success = false;
            for (Creator creator : creators) {
                Pair<Integer, String> integerStringPair = creator.create(RangeString.of(from, i, sourceCode.s), javaDocs, annotationUsingMembers, fatherNode);
                int i1 = integerStringPair.getFirst();
                if (i1 != -1) {
                    sourceCode.s = integerStringPair.getSecond();
                    if (i1 < i) {
                        i = i1 + 1;
                    } else {
                        from = i1;
                        i = from + 1;
                    }
                    success = true;
                    break;
                }
            }
            if (!success) {
                i++;
            }
        }
        if (i - 1 < sourceCode.s.length() && sourceCode.s.charAt(i - 1) == JavaKeyWords.RIGHT_CURLY_BRACES.charAt(0) && enumFieldMembers != null) {
            String substring = sourceCode.s.substring(from, i - 1);
            substring = StringUtils.prettyString(substring, 0, substring.length());
            if (StringUtils.allJavaIdentity(substring)) {
                EnumFieldMembers.EnumFieldMember e = new EnumFieldMembers.EnumFieldMember();
                e.head = substring;
                e.rangeString = RangeString.of(from, i - 1, sourceCode.s);
                e.fatherNode = fatherNode;
                e.javaDocOverField = ((JavaDocs) creators[0]).lastJavaDocList;
                e.annotationOverField = ((AnnotationUsingMembers) creators[1]).getLastAnnotationUsingMemberList();
                enumFieldMembers.enumFieldMemberList.add(e);
            }
        }
        return Pair.of(Math.min(i + JavaKeyWords.RIGHT_CURLY_BRACES.length(), sourceCode.s.length()), sourceCode.s);
    }

    @SuppressWarnings("unchecked")
    static Pair<Integer, String> defualt(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode, MatcherBuilder<? extends SourceCodeRange> matcherBuilder, List list) {
        if (sourceCode.isEmpty()) {
            return Pair.of(-1, sourceCode.s);
        }
        Pair<? extends SourceCodeRange, String> build = matcherBuilder.build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build != null) {
            list.add(build.getFirst());
            return Pair.of(build.getFirst().getRangeString().to, build.getSecond());
        }
        return Pair.of(-1, sourceCode.s);
    }

    /**
     * @return pair.first = 结束坐标loc ; pair.second = 后面要处理的String
     */
    Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode);
}
