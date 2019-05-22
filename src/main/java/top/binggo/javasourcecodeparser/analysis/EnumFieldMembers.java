package top.binggo.javasourcecodeparser.analysis;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.util.Pair;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.RangeString;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AAA{}
 *
 * @author binggo
 */
public class EnumFieldMembers implements Creator {
    List<EnumFieldMember> enumFieldMemberList = new ArrayList<>();

    @Override
    public Pair<Integer, String> create(RangeString sourceCode, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
        return Creator.defualt(sourceCode, javaDocs, annotationUsingMembers, fatherNode, EnumFieldMembers.EnumFieldMember.builder(), this.enumFieldMemberList);

    }

    @Override
    public String toString() {
        return StringUtils.toString(StringUtils.list2String(this.enumFieldMemberList));
    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    public static class EnumFieldMember implements SourceCodeRange, ChildNode {
        ChildNode fatherNode;
        RangeString rangeString;
        List<JavaDocs.JavaDoc> javaDocOverField;
        List<AnnotationUsingMembers.AnnotationUsingMember> annotationOverField;
        String head;

        public static EnumFieldMembers.EnumFieldMember.Builder builder() {
            return new EnumFieldMember().new Builder();
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
            return JavaKeyWords.WELL_NUMBER;
        }

        @Override
        public String getSymbol() {
            StringBuilder ret = new StringBuilder(head.length());
            for (int i = 0; i < head.length(); i++) {
                char ch = head.charAt(i);
                if (Character.isJavaIdentifierPart(ch)) {
                    ret.append(ch);
                } else {
                    break;
                }
            }
            return ret.toString();
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

        class Builder implements MatcherBuilder<EnumFieldMembers.EnumFieldMember> {
            /**
             * @param rangeString 没有结尾字符如;|,|{|(
             * @return null不是enum field,否则返回对应的field
             */
            private String isEnumField(RangeString rangeString) {
                String string = StringUtils.prettyString(rangeString);
                if (string.isEmpty() || string.equals(JavaKeyWords.STATIC)) {
                    return null;
                }
                String[] split = string.split(JavaKeyWords.SPACE);
                //todo check "AAA"=>[AAA]?
                if (split.length != 1) {
                    return null;
                } else {
                    return split[0];
                }
            }

            /**
             * rangeString = '变量定义字符 空白符* ('|'变量定义字符 空白符* ,'|'变量定义字符 空白符* {'|'变量定义字符 空白符* ;'
             */
            @Override
            public Pair<EnumFieldMembers.EnumFieldMember, String> build(RangeString rangeString, JavaDocs javaDocs, AnnotationUsingMembers annotationUsingMembers, ChildNode fatherNode) {
                // 寻找'('对应的')',后面的','|';'|'{'|'}'中最近的一个,如果是'{',匹配到'}'
                String[] targetStrings = {
                        JavaKeyWords.COMMA, JavaKeyWords.SEMICOLON, JavaKeyWords.LEFT_CURLY_BRACES,
                };
                if (match(rangeString, JavaKeyWords.LEFT_BRACKETS)) {
                    String enumField = isEnumField(RangeString.of(rangeString.from, rangeString.to - JavaKeyWords.LEFT_CURLY_BRACES.length(), rangeString.s));
                    if (enumField != null) {
                        EnumFieldMembers.EnumFieldMember.this.head = enumField;
                        javaDocOverField = javaDocs.getLastJavaDocList();
                        annotationOverField = annotationUsingMembers.getLastAnnotationUsingMemberList();
                        //')' 对应位置
                        int i = StringUtils.indexOfMatchSymbol(rangeString.s, rangeString.to - JavaKeyWords.LEFT_BRACKETS.length(), JavaKeyWords.BRACKET_PAIR);
//                        int min = -1;
//                        String targetString = null;
//                        for (String string : targetStrings) {
//                            int indexOf = StringUtils.indexOfCharNotInBlock(RangeString.of(i,rangeString.s.length(),rangeString.s),string.charAt(0));
//                            if (indexOf != -1) {
//                                if (min == -1 || indexOf < min) {
//                                    min = indexOf;
//                                    targetString = string;
//                                }
//                            }
//                        }
                        EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, i + JavaKeyWords.RIGHT_BRACKETS.length(), rangeString.s);

//                        if (targetString == null) {
//                            return null;
//                        } else {
//                            boolean comma = JavaKeyWords.COMMA.equals(targetString);
//                            if (comma || JavaKeyWords.SEMICOLON.equals(targetString)) {
//                                EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, min +  (comma ? JavaKeyWords.COMMA.length() : JavaKeyWords.SEMICOLON.length()), rangeString.s);
//                            } else if (targetString.equals(JavaKeyWords.LEFT_CURLY_BRACES)) {
//                                int to = StringUtils.indexOfMatchSymbol(rangeString.s, min, JavaKeyWords.BLOCKS) + JavaKeyWords.RIGHT_CURLY_BRACES.length();
//                                EnumFieldMembers.EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, to, rangeString.s);
//                                //最近的{|,|;|}
//                            } else {
//                                EnumFieldMembers.EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, min-JavaKeyWords.RIGHT_CURLY_BRACES.length(), rangeString.s);
//                            }
//                        }
                        EnumFieldMembers.EnumFieldMember.this.fatherNode = fatherNode;
                        return Pair.of(EnumFieldMembers.EnumFieldMember.this, rangeString.s);
                    }
                    // , | ; 直接结束
                    // { 找到匹配的'}'
                } else {
                    int stringIndex = StringUtils.endWithAnyOfStrings(rangeString.subString(), targetStrings);
                    if (stringIndex != -1) {
                        javaDocOverField = javaDocs.getLastJavaDocList();
                        annotationOverField = annotationUsingMembers.getLastAnnotationUsingMemberList();


                        String enumField = isEnumField(RangeString.of(rangeString.from, rangeString.to - targetStrings[stringIndex].length(), rangeString.s));
                        if (enumField == null) {
                            return null;
                        }
                        EnumFieldMembers.EnumFieldMember.this.head = enumField;
                        if (stringIndex != 2) {
                            EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, rangeString.to, rangeString.s);

                        } else {
                            int to = StringUtils.indexOfMatchSymbol(rangeString.s, rangeString.to - JavaKeyWords.LEFT_CURLY_BRACES.length(), JavaKeyWords.BLOCKS) + JavaKeyWords.RIGHT_CURLY_BRACES.length();
                            EnumFieldMember.this.rangeString = RangeString.of(rangeString.from, to, rangeString.s);
                        }
                        EnumFieldMember.this.fatherNode = fatherNode;
                        return Pair.of(EnumFieldMembers.EnumFieldMember.this, rangeString.s);
                    }
                }
                return null;
            }
        }
    }

}
