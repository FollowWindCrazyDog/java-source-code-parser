package top.binggo.javasourcecodeparser.analysis;

import lombok.AllArgsConstructor;
import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 注解的使用
 *
 * @author binggo
 */
public class AnnotationUsingMembers implements Creator {

    List<AnnotationUsingMember> annotationUsingMemberList = new ArrayList<>();
    List<AnnotationUsingMember> lastAnnotationUsingMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        if (sourceCode.isEmpty()) {
            return Pair.of(-1, sourceCode.s);
        }
        Pair<AnnotationUsingMember, String> build = AnnotationUsingMember.builder().build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build != null) {
            annotationUsingMemberList.add(build.getFirst());
            lastAnnotationUsingMemberList.add(build.getFirst());
            return Pair.of(build.getFirst().getRangeString().to, build.getSecond());
        }
        return Pair.of(-1, sourceCode.s);
    }

    public List<AnnotationUsingMember> getLastAnnotationUsingMemberList() {
        ArrayList<AnnotationUsingMember> ret = new ArrayList<>(this.lastAnnotationUsingMemberList);
        lastAnnotationUsingMemberList.clear();
        return ret;
    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.annotationUsingMemberList));

    }

    @AllArgsConstructor
    public static class AnnotationUsingMember implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;

        public static AnnotationUsingMember.Builder builder() {
            return new Builder();
        }

        @Override
        public RangeString getRangeString() {
            return this.rangeString;
        }

        @Override
        public ChildNode getFather() {
            return this.fatherNode;
        }

        @Override
        public String getAccessLevelString() {
            return JavaKeyWords.PUBLIC;
        }

        @Override
        public String splitString() {
            return JavaKeyWords.WELL_NUMBER;
        }

        @Override
        public String getSymbol() {
            return rangeString.subString();
        }

        @Override
        public String getDescription() {
            return rangeString.subString();
        }

        @Override
        public String getJavaDoc() {
            return "";
        }

        @Override
        public int getLocationInFile() {
            return rangeString.from;
        }

        @Override
        public List<ChildNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public Type getType() {
            return Type.FIELD;
        }

        @Override
        public String toString() {
            return rangeString.toString();
        }

        static class Builder implements MatcherBuilder<AnnotationUsingMember> {
            private static int endIndex(RangeString rangeString) {
                int indexOf = rangeString.s.indexOf(JavaKeyWords.ANNOTATION_PREFIX, rangeString.from);
                //判断是否有参数
                boolean hasParam = false;
                int ret = -1;
                for (int i = indexOf; i < rangeString.s.length(); i++) {
                    if (Character.isWhitespace(rangeString.s.charAt(i))) {
                        ret = i + ("" + rangeString.s.charAt(i)).length();
                        break;
                    } else if (rangeString.s.charAt(i) == JavaKeyWords.CHAR_LEFT_BRACKETS) {
                        hasParam = true;
                        break;
                    }
                }
                if (hasParam) {
                    int index = StringUtils.indexOfMatchSymbol(rangeString.s, rangeString.to, new String[]{
                            JavaKeyWords.LEFT_BRACKETS, JavaKeyWords.RIGHT_BRACKETS
                    });
                    ret = index == -1 ? -1 : index + JavaKeyWords.RIGHT_BRACKETS.length();

                }
                return ret;
            }

            /**
             * rangeString = '空白符@'
             */
            @Override
            public Pair<AnnotationUsingMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                String s = StringUtils.prettyString(rangeString);
                int indexOf = rangeString.s.indexOf(JavaKeyWords.ANNOTATION_PREFIX, rangeString.from);
                if (s.startsWith(JavaKeyWords.ANNOTATION_PREFIX) && !(StringUtils.startWithFromIndex(rangeString.s, indexOf, JavaKeyWords.ANNOTATION) && Character.isWhitespace(rangeString.s.charAt(indexOf + JavaKeyWords.ANNOTATION.length())))) {
                    int endIndex = endIndex(rangeString);
                    return endIndex == -1 ? null : Pair.of(new AnnotationUsingMember(fatherNode, RangeString.of(rangeString.from, endIndex, rangeString.s)), rangeString.s);
                }
                return null;
            }
        }
    }
}