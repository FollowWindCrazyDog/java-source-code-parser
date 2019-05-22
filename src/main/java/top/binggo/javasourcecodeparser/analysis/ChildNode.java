package top.binggo.javasourcecodeparser.analysis;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;
import top.binggo.javasourcecodeparser.utils.CamelNameProcessor;
import top.binggo.javasourcecodeparser.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author binggo
 */
public interface ChildNode {
    int PUBLIC_LEVEL = 3;
    int FRIENDLY_LEVEL = 2;
    int PROTECTED_LEVEL = 1;
    int PRIVATE_LEVEL = 0;
    Map<String, Integer> MAP = ImmutableMap.of(JavaKeyWords.PUBLIC, PUBLIC_LEVEL,
            JavaKeyWords.PROTECTED, PROTECTED_LEVEL,
            JavaKeyWords.PRIVATE, PRIVATE_LEVEL);
    Set<String> STRING_IMMUTABLE_SET = ImmutableSet.of(
            JavaKeyWords.CLASS, JavaKeyWords.INTERFACE,
            JavaKeyWords.ENUM, JavaKeyWords.ANNOTATION,
            JavaKeyWords.PUBLIC, JavaKeyWords.PROTECTED,
            JavaKeyWords.PRIVATE, JavaKeyWords.STATIC,
            JavaKeyWords.FINAL, JavaKeyWords.ABSTRACT
    );

    static int getAccessLevelFromRoot(ChildNode childNode) {
        ChildNode father = childNode;
        int ret = childNode.getAccessLevel();
        while ((father = father.getFather()) != null) {
            ret = Math.min(ret, father.getAccessLevel());
            if (ret == PRIVATE_LEVEL) {
                return 0;
            }
        }
        return ret;
    }

    static List<ChildNode> mergeList(List<? extends ChildNode>... lists) {
        ArrayList<ChildNode> ret = new ArrayList<>(64);
        for (List<? extends ChildNode> list : lists) {
            ret.addAll(list);
        }
        return ret;
    }

    static String getSymbolForClass(String head) {
        String[] strings = STRING_IMMUTABLE_SET.toArray(new String[0]);
        for (int i = 0; i < strings.length; i++) {
            strings[i] = strings[i] + " ";
        }
        String string = StringUtils.prettyString(head, 0, head.length());
        for (int i = 0; i < string.length(); ) {
            int i1 = StringUtils.startWithAnyFromIndex(string, i, strings);
            if (i1 != -1) {
                i += strings[i1].length();
            } else {
                int j;
                for (j = i; j < string.length(); j++) {
                    if (!Character.isJavaIdentifierPart(string.charAt(j))) {
                        break;
                    }
                }
                return string.substring(i, j);
            }
        }
        return string;
    }

    static String javaDocList2String(List<JavaDocs.JavaDoc> javaDocOverClass) {
        StringBuilder ret = new StringBuilder();

        for (JavaDocs.JavaDoc docOverClass : javaDocOverClass) {
            ret.append(docOverClass.rangeString.subString()).append(JavaKeyWords.ENTRY);
        }
        return ret.toString();

    }

    ChildNode getFather();

    /**
     * 对于class是friendly
     */
    default int getDefaultAccessLevelForChild() {
        return PUBLIC_LEVEL;
    }

    default int getAccessLevel() {
        String string = getAccessLevelString();
        for (Map.Entry<String, Integer> entry : MAP.entrySet()) {
            if (string.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        ChildNode father = getFather();
        return father == null ? PUBLIC_LEVEL : father.getDefaultAccessLevelForChild();

    }

    String getAccessLevelString();

    default String getId() {
        ChildNode father = getFather();
        String s = father == null ? "" : father.getId();
        s += splitString() + getSymbol();
        return s;
    }

    String splitString();

    String getSymbol();

    String getDescription();

    String getJavaDoc();

    default int getSymbolWeights() {
        return 5;
    }

    default String getIndexField() {
        StringBuilder ret = new StringBuilder();
        String symbol = getSymbol();
        String turn = CamelNameProcessor.turn(symbol);
        for (int i = 0; i < getSymbolWeights(); i++) {
            ret.append(turn).append(JavaKeyWords.ENTRY);
        }
        //todo prettyJavaDoc
        ret.append(getJavaDoc());
        List<ChildNode> children = getChildren();
        for (ChildNode child : children) {
            ret.append(child.getIndexField()).append(JavaKeyWords.ENTRY);
        }
        return ret.toString();
    }

    int getLocationInFile();

    List<ChildNode> getChildren();

    Type getType();

    enum Type {
        /**
         * .java源文件
         */
        SOURCE_FILE,
        CLASS,
        INTERFACE,
        ENUM,
        ANNOTATION,
        FIELD,
        METHOD,
        JAVADOC,
        ANNOTATION_USING,

    }
}
