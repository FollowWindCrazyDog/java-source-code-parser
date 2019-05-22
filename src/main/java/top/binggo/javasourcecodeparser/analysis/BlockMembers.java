package top.binggo.javasourcecodeparser.analysis;

import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 代码块
 *
 * @author binggo
 */
public class BlockMembers implements Creator {

    List<BlockMember> blockMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, BlockMembers.BlockMember.builder(), this.blockMemberList);

    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.blockMemberList));
    }

    public static class BlockMember implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;
        String head = "";

        public static BlockMembers.BlockMember.Builder builder() {
            BlockMember blockMember = new BlockMember();
            return blockMember.new Builder();
        }

        @Override
        public String toString() {
            return rangeString.toString();
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

        class Builder implements MatcherBuilder<BlockMembers.BlockMember> {
            /**
             * rangeString = '空白符{'|'空白符static{'
             */
            @Override
            public Pair<BlockMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                if (match(rangeString, JavaKeyWords.LEFT_CURLY_BRACES)) {
                    String trim = rangeString.s.substring(rangeString.from, rangeString.to - 1).trim();
                    boolean empty = trim.isEmpty();
                    if (empty || trim.contains(JavaKeyWords.STATIC)) {
                        if (!empty) {
                            BlockMember.this.head = JavaKeyWords.STATIC;
                        }
                        int to = StringUtils.indexOfMatchSymbol(rangeString.s, rangeString.to - JavaKeyWords.LEFT_CURLY_BRACES.length(), JavaKeyWords.BLOCKS) + JavaKeyWords.RIGHT_CURLY_BRACES.length();
                        RangeString of = RangeString.of(rangeString.from, to, rangeString.s);
                        BlockMember.this.rangeString = of;
                        BlockMember.this.fatherNode = fatherNode;
                        return Pair.of(BlockMember.this, rangeString.s);
                    }
                }
                return null;
            }
        }
    }

}
