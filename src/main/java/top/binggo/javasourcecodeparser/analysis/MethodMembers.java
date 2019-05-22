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
 * @author binggo
 */
public class MethodMembers implements Creator {
    List<MethodMember> methodMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, MethodMembers.MethodMember.builder(), this.methodMemberList);
    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.methodMemberList));
    }


    @AllArgsConstructor
    public static class MethodMember implements SourceCodeRange, ChildNode {
        String head;
        ChildNode fatherNode;
        RangeString rangeString;
        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;

        public static MethodMember.Builder builder() {
            return new MethodMember.Builder();
        }

        @Override
        public String toString() {
            return StringUtils.toString(StringUtils.list2String(javaDocOverField), StringUtils.list2String(annotationOverField), rangeString);
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
            return head;
        }

        @Override
        public String splitString() {
            return JavaKeyWords.WELL_NUMBER;
        }

        @Override
        public String getSymbol() {
            String string = new StringBuilder(head).reverse().toString();
            int i = StringUtils.indexOfMatchSymbol(string, 0, JavaKeyWords.BRACKET_PAIR);
            String nextJavaIdentifierWord = StringUtils.getNextJavaIdentifierWord(string, i);
            return new StringBuilder(nextJavaIdentifierWord).reverse().toString();
        }

        /**
         * 因为方法存在重载
         */
        @Override
        public String getId() {
            ChildNode father = getFather();
            String s = father == null ? "" : father.getId();
            String string = new StringBuilder(head).reverse().toString();
            int i = StringUtils.indexOfMatchSymbol(string, 0, JavaKeyWords.BRACKET_PAIR);
            String nextJavaIdentifierWord = StringUtils.getNextJavaIdentifierWord(string, i);
            String s1 = new StringBuilder(nextJavaIdentifierWord).reverse().toString() + new StringBuilder(string.substring(0, i + 1)).reverse().toString();
            return s + splitString() + s1;
        }

        @Override
        public String getDescription() {
            return head;
        }

        @Override
        public String getJavaDoc() {
            return ChildNode.javaDocList2String(this.javaDocOverField);
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
            return Type.METHOD;
        }

        static class Builder implements MatcherBuilder<MethodMember> {

            private boolean isMethod(RangeString rangeString, ChildNode fatherNode) {
                String substring = rangeString.s.substring(rangeString.from, rangeString.to - JavaKeyWords.LEFT_CURLY_BRACES.length());
                String s = StringUtils.prettyString(substring, 0, substring.length());
                return s.contains(JavaKeyWords.SPACE) || ((fatherNode.getType() == Type.CLASS || fatherNode.getType() == Type.ENUM) && s.equals(fatherNode.getSymbol()));
            }

            /**
             * rangeString = '其他字符)'
             */
            @Override
            public Pair<MethodMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                if (match(rangeString, JavaKeyWords.LEFT_BRACKETS) && isMethod(rangeString, fatherNode)) {
                    int index = -1;
                    String[] targets = {JavaKeyWords.LEFT_CURLY_BRACES, JavaKeyWords.SEMICOLON};
                    String t = "";
                    for (String target : targets) {
                        int endIndex = rangeString.s.indexOf(target, rangeString.to);
                        if (endIndex != -1 && (index == -1 || endIndex < index)) {
                            index = endIndex;
                            t = target;
                        }
                    }
                    if (index == -1) {
                        return null;
                    }

                    int to = JavaKeyWords.LEFT_CURLY_BRACES.equals(t) ? StringUtils.indexOfMatchSymbol(rangeString.s, index, new String[]{JavaKeyWords.LEFT_CURLY_BRACES, JavaKeyWords.RIGHT_CURLY_BRACES}) + JavaKeyWords.RIGHT_CURLY_BRACES.length() : index + t.length();
                    return Pair.of(new MethodMember(StringUtils.prettyString(rangeString.s, rangeString.from, index), fatherNode, RangeString.of(rangeString.from, to, rangeString.s), javaDocs.getLastJavaDocList(), annotationUsingMembers.getLastAnnotationUsingMemberList()), rangeString.s);
                }
                return null;
            }
        }
    }

}
