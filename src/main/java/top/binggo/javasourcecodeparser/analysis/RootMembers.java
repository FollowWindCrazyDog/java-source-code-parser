package top.binggo.javasourcecodeparser.analysis;

import com.google.common.collect.ImmutableSet;
import lombok.NoArgsConstructor;
import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * @author binggo
 */
public class RootMembers implements Creator {
    static final Set<ChildNode.Type> TYPE_SET = ImmutableSet.of(
            ChildNode.Type.SOURCE_FILE,
            ChildNode.Type.CLASS,
            ChildNode.Type.INTERFACE,
            ChildNode.Type.ENUM,
            ChildNode.Type.ANNOTATION
    );
    List<RootMember> rootMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, RootMember.builder(), this.rootMemberList);
    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.rootMemberList));
    }

    @NoArgsConstructor
    static class RootMember implements SourceCodeRange, ChildNode {

        ChildNode fatherNode;
        RangeString rangeString;
        ClassMembers classMembers = new ClassMembers();
        EnumMembers enumMembers = new EnumMembers();
        AnnotationMembers annotationMembers = new AnnotationMembers();
        FieldMembers fieldMembers = new FieldMembers();
        JavaDocs javaDocs = new JavaDocs();
        AnnotationUsingMembers annotationUsingMembers = new AnnotationUsingMembers();
        Creator[] creators = {this.classMembers, this.enumMembers, this.annotationMembers, this.fieldMembers, this.javaDocs, this.annotationUsingMembers};

        public static RootMembers.RootMember.Builder builder() {
            return new RootMembers.RootMember().new Builder();
        }

        @Override
        public String toString() {
            return StringUtils.toString(this.fieldMembers, this.classMembers, this.enumMembers, this.annotationMembers);
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
            return "";
        }

        @Override
        public String getSymbol() {
            Optional<FieldMembers.FieldMember> first = this.fieldMembers.fieldMemberList.stream().filter(fieldMember -> StringUtils.prettyString(fieldMember.rangeString).startsWith(JavaKeyWords.PACKAGE)).findFirst();
            String ret = "";
            if (first.isPresent()) {
                String s = StringUtils.prettyString(first.get().rangeString).split(" ", 2)[1];
                ret = s.substring(0, s.length() - 1);
            }
            return ret;
        }

        @Override
        public String getDescription() {
            return "";
        }

        @Override
        public String getJavaDoc() {
            return "";
        }


        @Override
        public int getLocationInFile() {
            return 0;
        }

        @SuppressWarnings("unchecked")
        @Override
        public List<ChildNode> getChildren() {
            return ChildNode.mergeList(this.classMembers.classMemberList, this.enumMembers.enumMemberList, this.annotationMembers.annotationMemberList);
        }

        @Override
        public Type getType() {
            return Type.SOURCE_FILE;
        }

        @Override
        public int getDefaultAccessLevelForChild() {
            return FRIENDLY_LEVEL;
        }

        class Builder implements MatcherBuilder<RootMembers.RootMember> {

            /**
             * rangeString = '其他字符{'
             */
            @Override
            public Pair<RootMembers.RootMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                Pair<Integer, String> classOrInterface = Creator.createClassOrInterface(rangeString, creators, RootMember.this.javaDocs, RootMember.this.annotationUsingMembers, RootMember.this, null);
                RootMember.this.rangeString = RangeString.of(rangeString.from, classOrInterface.getFirst(), rangeString.s);
                RootMember.this.fatherNode = null;
                return Pair.of(RootMember.this, classOrInterface.getSecond());
            }
        }


    }
}
