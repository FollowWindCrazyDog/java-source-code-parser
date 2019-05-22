package top.binggo.javasourcecodeparser.analysis;

import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 注解的定义
 *
 * @author binggo
 */
public class AnnotationMembers implements Creator {
    List<AnnotationMember> annotationMemberList = new ArrayList<>();

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(annotationMemberList));

    }

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, AnnotationMember.builder(), this.annotationMemberList);
    }

    public static class AnnotationMember implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        String head;
        RangeString rangeString;
        AnnotationFieldMembers annotationFieldMembers = new AnnotationFieldMembers();
        JavaDocs javaDocs = new JavaDocs();
        AnnotationUsingMembers annotationUsingMembers = new AnnotationUsingMembers();

        ClassMembers classMembers = new ClassMembers();
        EnumMembers enumMembers = new EnumMembers();
        AnnotationMembers annotationMembers = new AnnotationMembers();

        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;

        Creator[] creators = {
                annotationFieldMembers,
                javaDocs,
                annotationUsingMembers,
                classMembers,
                enumMembers,
                annotationMembers
        };

        public static AnnotationMembers.AnnotationMember.Builder builder() {
            return new AnnotationMember().new Builder();
        }

        @Override
        public String toString() {
            return StringUtils.toString(StringUtils.list2String(javaDocOverField), StringUtils.list2String(annotationOverField), this.head, "{", this.annotationFieldMembers, this.classMembers, this.enumMembers, this.annotationMembers, "}\n");
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
            return JavaKeyWords.DOT;
        }

        @Override
        public String getSymbol() {
            return ChildNode.getSymbolForClass(head);
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
        @SuppressWarnings("unchecked")
        public List<ChildNode> getChildren() {
            return ChildNode.mergeList(this.annotationFieldMembers.annotationFieldMemberList, this.annotationMembers.annotationMemberList, this.classMembers.classMemberList, this.enumMembers.enumMemberList);
        }

        @Override
        public Type getType() {
            return Type.ENUM;
        }


        class Builder implements MatcherBuilder<AnnotationMembers.AnnotationMember> {

            /**
             * rangeString = '其他字符{'
             */
            @Override
            public Pair<AnnotationMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                if (match(rangeString, JavaKeyWords.LEFT_CURLY_BRACES) &&
                        StringUtils.checkClassType(rangeString, JavaKeyWords.ANNOTATION)) {
                    String s = StringUtils.prettyString(rangeString);
                    head = s.substring(0, s.length() - JavaKeyWords.LEFT_CURLY_BRACES.length());
                    AnnotationMember.this.javaDocOverField = javaDocs.getLastJavaDocList();
                    AnnotationMember.this.annotationOverField = annotationUsingMembers.getLastAnnotationUsingMemberList();
                    AnnotationMember.this.fatherNode = fatherNode;
                    Pair<Integer, String> endIndex = Creator.createClassOrInterface(RangeString.of(rangeString.to, rangeString.s.length(), rangeString.s),
                            AnnotationMember.this.creators, AnnotationMember.this.javaDocs, AnnotationMember.this.annotationUsingMembers, AnnotationMember.this, null);
                    AnnotationMember.this.rangeString = RangeString.of(rangeString.from, endIndex.getFirst(), rangeString.s);
                    return Pair.of(AnnotationMember.this, endIndex.getSecond());
                }
                return null;
            }
        }
    }


}

