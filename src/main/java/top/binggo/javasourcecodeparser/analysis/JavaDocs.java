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
 * 用来创建和保存注释和JavaDoc
 *
 * @author binggo
 */
public class JavaDocs implements Creator {

    List<Comment> commentList = new ArrayList<>();
    List<JavaDoc> javaDocList = new ArrayList<>();
    List<JavaDoc> lastJavaDocList = new ArrayList<>();

    //...
    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.commentList), StringUtils.list2String(this.javaDocList));
    }

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        if (sourceCode.isEmpty()) {
            return Pair.of(-1, sourceCode.s);
        }
        JavaDoc.Builder builder = JavaDoc.builder();
        Pair<JavaDoc, String> build = builder.build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build != null) {
            javaDocList.add(build.getFirst());
            lastJavaDocList.add(build.getFirst());
            return Pair.of(build.getFirst().getRangeString().from, build.getSecond());
        }
        Pair<Comment, String> build1 = Comment.builder().build(sourceCode, javaDocs, annotationUsingMembers, fatherNode);
        if (build1 != null) {
            commentList.add(build1.getFirst());
            return Pair.of(build1.getFirst().getRangeString().from, build1.getSecond());
        }
        return Pair.of(-1, sourceCode.s);
    }

    public List<JavaDoc> getLastJavaDocList() {
        ArrayList<JavaDoc> ret = new ArrayList<>(this.lastJavaDocList);
        lastJavaDocList.clear();
        return ret;
    }

    @AllArgsConstructor
    public static class Comment implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;
        String comments;

        public static Comment.Builder builder() {
            return new Comment.Builder();
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
            return null;
        }

        @Override
        public String splitString() {
            return null;
        }

        @Override
        public String getSymbol() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getJavaDoc() {
            return null;
        }

        @Override
        public int getLocationInFile() {
            return 0;
        }

        @Override
        public List<ChildNode> getChildren() {
            return Collections.emptyList();
        }

        @Override
        public Type getType() {
            return null;
        }

        public String getComments() {
            return comments;
        }

        static class Builder implements MatcherBuilder<Comment> {
            ChildNode fatherNode;
            RangeString rangeString;
            String comments;

            public void init(ChildNode fatherNode, RangeString rangeString, String comments) {
                this.fatherNode = fatherNode;
                this.rangeString = rangeString;
                this.comments = comments;
            }

            /**
             * rangeString = '空白符//'|''空白符/*'
             */
            @Override
            public Pair<Comment, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                int next = rangeString.to + 1;
                if (next < rangeString.s.length() && StringUtils.prettyString(RangeString.of(rangeString.from, next, rangeString.s)).endsWith(JavaKeyWords.MULTIPLE_LINES_COMMENT_WITH_JAVA_DOC)) {
                    return null;
                }
                String s = StringUtils.prettyString(rangeString);
                String[][] targetStrings = {{JavaKeyWords.SINGLE_LINE_COMMENT, JavaKeyWords.ENTRY}, {JavaKeyWords.MULTIPLE_LINES_COMMENT, JavaKeyWords.MULTIPLE_LINES_COMMENT_END}};
                for (String[] targetString : targetStrings) {
                    if (s.endsWith(targetString[0])) {
                        RangeString of = RangeString.of(rangeString.to - targetString[0].length(), rangeString.s.indexOf(targetString[1], rangeString.to) + targetString[1].length(), rangeString.s);
                        init(fatherNode, of, of.subString());
                        String newRangeString = rangeString.s.substring(0, of.from) + rangeString.s.substring(of.to);
                        return Pair.of(new Comment(this.fatherNode, this.rangeString, this.comments), newRangeString);
                    }
                }
                return null;
            }
        }

    }

    @AllArgsConstructor
    public static class JavaDoc implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;
        String comments;

        public static Builder builder() {
            return new Builder();
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
            return null;
        }

        @Override
        public String splitString() {
            return null;
        }

        @Override
        public String getSymbol() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getJavaDoc() {
            return null;
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
            return null;
        }

        public String getComments() {
            return comments;
        }

        static class Builder implements MatcherBuilder<JavaDoc> {
            ChildNode fatherNode;
            RangeString rangeString;
            String comments;

            public void init(ChildNode fatherNode, RangeString rangeString, String comments) {
                this.fatherNode = fatherNode;
                this.rangeString = rangeString;
                this.comments = comments;
            }

            /**
             * rangeString = '空白符/**'
             */
            @Override
            public Pair<JavaDoc, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                String s = StringUtils.prettyString(rangeString);
                String[][] targetStrings = {{JavaKeyWords.MULTIPLE_LINES_COMMENT_WITH_JAVA_DOC, JavaKeyWords.MULTIPLE_LINES_COMMENT_END}};
                for (String[] targetString : targetStrings) {
                    if (s.endsWith(targetString[0])) {
                        int endIndex = rangeString.s.indexOf(targetString[1], rangeString.to);
                        RangeString of = RangeString.of(rangeString.to - targetString[0].length(), endIndex + targetString[1].length(), rangeString.s);
                        init(fatherNode, of, of.subString());
                        String newRangeString = rangeString.s.substring(0, of.from) + rangeString.s.substring(of.to);
                        return Pair.of(new JavaDoc(this.fatherNode, this.rangeString, this.comments), newRangeString);
                    }
                }
                return null;
            }
        }
    }
}

