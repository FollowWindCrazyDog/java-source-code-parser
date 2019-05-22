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
public class FieldMembers implements Creator {
    List<FieldMember> fieldMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        if (sourceCode.isEmpty()) {
            return Pair.of(-1, sourceCode.s);
        }
        Pair<FieldMember, String> build = FieldMember.builder().build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build != null) {
            fieldMemberList.add(build.getFirst());
            return Pair.of(build.getFirst().rangeString.to, build.getSecond());
        }
        return Pair.of(-1, sourceCode.s);
    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.fieldMemberList));
    }

    @AllArgsConstructor
    public static class FieldMember implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;
        String head;
        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;

        public static FieldMember.Builder builder() {
            return new FieldMember.Builder();
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
            String s = this.head;
            StringBuilder ret = new StringBuilder(s.length());
            for (int i = s.length() - 1; i >= 0; i--) {
                if (!Character.isJavaIdentifierPart(s.charAt(i)) && ret.length() != 0) {
                    break;
                } else if (Character.isJavaIdentifierPart(s.charAt(i))) {
                    ret.append(s.charAt(i));
                }
            }
            return ret.reverse().toString();
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
            return Type.FIELD;
        }

        static class Builder implements MatcherBuilder<FieldMember> {


            /**
             * rangeString = '其他字符='|'其他字符;'
             */
            @Override
            public Pair<FieldMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                if (match(rangeString, JavaKeyWords.EQUAL)) {
                    int endIndex = StringUtils.indexOfCharNotInBlock(RangeString.of(rangeString.to, rangeString.s.length(), rangeString.s), JavaKeyWords.SEMICOLON.charAt(0));
                    if (endIndex == -1) {
                        System.out.println(rangeString);
                    }
                    String string = StringUtils.prettyString(rangeString);
                    String h = string.substring(0, string.length() - JavaKeyWords.EQUAL.length());
                    return Pair.of(new FieldMember(fatherNode, RangeString.of(rangeString.from, endIndex + JavaKeyWords.SEMICOLON.length(), rangeString.s), h, javaDocs.getLastJavaDocList(), annotationUsingMembers.getLastAnnotationUsingMemberList()), rangeString.s);
                } else if (match(rangeString, JavaKeyWords.SEMICOLON)) {
                    String string = StringUtils.prettyString(rangeString);
                    String h = string.substring(0, string.length() - JavaKeyWords.SEMICOLON.length());
                    return Pair.of(new FieldMember(fatherNode, RangeString.of(rangeString.from, rangeString.to, rangeString.s), h, javaDocs.getLastJavaDocList(), annotationUsingMembers.getLastAnnotationUsingMemberList()), rangeString.s);
                }
                return null;
            }


        }
    }

}
