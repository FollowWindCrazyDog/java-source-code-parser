package top.binggo.javasourcecodeparser.analysis;

import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author binggo
 */
public class EnumMembers implements Creator {
    List<EnumMember> enumMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, EnumMembers.EnumMember.builder(), this.enumMemberList);
    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.enumMemberList));
    }

    public static class EnumMember implements SourceCodeRange, ChildNode {
        String head = "";
        ChildNode fatherNode;
        RangeString rangeString;
        EnumFieldMembers enumFieldMembers = new EnumFieldMembers();
        MethodMembers methodMembers = new MethodMembers();
        FieldMembers fieldMembers = new FieldMembers();
        BlockMembers blockMembers = new BlockMembers();
        JavaDocs javaDocs = new JavaDocs();
        AnnotationUsingMembers annotationUsingMembers = new AnnotationUsingMembers();
        AnnotationMembers annotationMembers = new AnnotationMembers();

        ClassMembers classMembers = new ClassMembers();
        EnumMembers enumMembers = new EnumMembers();

        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;

        Creator[] creators = {
                this.javaDocs,
                this.annotationUsingMembers,
                new CommaAndSemicolonCreator(),
                this.methodMembers,
                this.fieldMembers,
                this.blockMembers,
                this.classMembers,
                this.enumMembers,
                this.annotationMembers,
                //要是最后一个
                this.enumFieldMembers,
        };

        public static EnumMembers.EnumMember.Builder builder() {
            return new EnumMembers.EnumMember().new Builder();
        }

        @Override
        public String toString() {
            return StringUtils.toString(StringUtils.list2String(javaDocOverField), StringUtils.list2String(annotationOverField), head, "{", this.enumFieldMembers, this.fieldMembers, this.blockMembers, this.methodMembers, this.classMembers, this.enumMembers, this.annotationMembers, "}\n");
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
        public int getDefaultAccessLevelForChild() {
            return FRIENDLY_LEVEL;
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
        public List<ChildNode> getChildren() {
            return ChildNode.mergeList(this.enumFieldMembers.enumFieldMemberList, this.classMembers.classMemberList, this.enumMembers.enumMemberList, this.annotationMembers.annotationMemberList);
        }

        @Override
        public Type getType() {
            return Type.ENUM;
        }


        class Builder implements MatcherBuilder<EnumMembers.EnumMember> {

            /**
             * rangeString = '其他字符{'
             */
            @Override
            public Pair<EnumMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                String s = StringUtils.prettyString(rangeString);
                RangeString of = RangeString.of(0, s.length(), s);
                if (s.endsWith(JavaKeyWords.LEFT_CURLY_BRACES) && StringUtils.checkClassType(of, JavaKeyWords.ENUM)) {
                    head = s.substring(0, s.length() - JavaKeyWords.LEFT_CURLY_BRACES.length());
                    javaDocOverField = javaDocs.getLastJavaDocList();
                    annotationOverField = annotationUsingMembers.getLastAnnotationUsingMemberList();
                    Pair<Integer, String> classOrInterface = Creator.createClassOrInterface(rangeString.left(), creators, EnumMember.this.javaDocs, EnumMember.this.annotationUsingMembers, EnumMember.this, EnumMember.this.enumFieldMembers);
                    EnumMember.this.rangeString = RangeString.of(rangeString.from, classOrInterface.getFirst(), rangeString.s);
                    return Pair.of(EnumMember.this, classOrInterface.getSecond());

                }
                return null;
            }
        }
    }

    static class CommaAndSemicolonCreator implements Creator {
        @Override
        public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
            String string = StringUtils.prettyString(sourceCode);
            if (string.equals(JavaKeyWords.COMMA) || string.equals(JavaKeyWords.SEMICOLON)) {
                return Pair.of(sourceCode.to, sourceCode.s);
            }
            return Pair.of(-1, sourceCode.s);
        }
    }
}
