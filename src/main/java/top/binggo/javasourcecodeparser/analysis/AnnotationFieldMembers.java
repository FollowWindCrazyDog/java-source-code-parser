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
 * 用于描述注解的成员变量
 *
 * @author binggo
 */
public class AnnotationFieldMembers implements Creator {

    List<AnnotationFieldMember> annotationFieldMemberList = new ArrayList<>();
    List<AnnotationFieldMember> normalFieldMemberList = new ArrayList<>();

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(annotationFieldMemberList), StringUtils.list2String(normalFieldMemberList));
    }

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        if (sourceCode.isEmpty()) {
            return Pair.of(-1, sourceCode.s);
        }
        Pair<AnnotationFieldMember, String> build = AnnotationFieldMember.builder().build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build != null) {
            if (build.getFirst().fieldType == AnnotationFieldMember.FieldType.ANNOTATION_FIELD) {
                annotationFieldMemberList.add(build.getFirst());
            } else {
                normalFieldMemberList.add(build.getFirst());
            }
            return Pair.of(build.getFirst().getRangeString().to, sourceCode.s);
        }
        return Pair.of(-1, sourceCode.s);
    }

    @AllArgsConstructor
    public static class AnnotationFieldMember implements SourceCodeRange, ChildNode {
        FieldType fieldType;
        ChildNode fatherNode;
        RangeString rangeString;
        String head;
        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;

        public static AnnotationFieldMembers.AnnotationFieldMember.Builder builder() {
            return new AnnotationFieldMembers.AnnotationFieldMember.Builder();
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
            return JavaKeyWords.PUBLIC;
        }

        @Override
        public String splitString() {
            return null;
        }

        @Override
        public String getSymbol() {
            return JavaKeyWords.WELL_NUMBER;
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

        private enum FieldType {
            /**
             * Annotation Field
             */
            ANNOTATION_FIELD,
            NORMAL_FIELD
        }

        static class Builder implements MatcherBuilder<AnnotationFieldMembers.AnnotationFieldMember> {

            /**
             * rangeString = '其他字符('|'其他字符='
             */
            @Override
            public Pair<AnnotationFieldMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                boolean normalField;
                if ((normalField = match(rangeString, JavaKeyWords.EQUAL)) || match(rangeString, JavaKeyWords.LEFT_BRACKETS)) {
                    int endIndex = StringUtils.indexOfCharNotInBlock(rangeString.left(), JavaKeyWords.SEMICOLON.charAt(0));
                    String string = StringUtils.prettyString(rangeString);
                    String h = string.substring(0, string.length() - (normalField ? JavaKeyWords.EQUAL.length() : JavaKeyWords.LEFT_BRACKETS.length()));
                    return Pair.of(new AnnotationFieldMember(normalField ? FieldType.NORMAL_FIELD : FieldType.ANNOTATION_FIELD, fatherNode, RangeString.of(rangeString.from, endIndex + JavaKeyWords.SEMICOLON.length(), rangeString.s), h, javaDocs.getLastJavaDocList(), annotationUsingMembers.getLastAnnotationUsingMemberList()), rangeString.s);
                }
                return null;
            }
        }
    }

}
