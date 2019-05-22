package top.binggo.javasourcecodeparser.utils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import top.binggo.javasourcecodeparser.constant.JavaKeyWords;

import java.util.List;

/**
 * @author binggo
 */
public class StringUtils {
    public static List<CharNotInBlockFinder> getCharNotInBlockFinderList() {
        return Lists.newArrayList(
                new CharNotInBlockFinder("\"", "\"", "\\"),
                new CharNotInBlockFinder("'", "'", "\\"),
                new CharNotInBlockFinder("/*", "*/", null) {
                    @Override
                    protected void clear(int index) {
                        this.count = 0;
                        this.lastIndex = index;
                    }
                },
                new CharNotInBlockFinder("//", "\n", null) {
                    @Override
                    protected void clear(int index) {
                        this.count = 0;
                        this.lastIndex = index;
                    }
                },
                new CharNotInBlockFinder("(", ")", null),
                new CharNotInBlockFinder("{", "}", null)
        );
    }

    public static int startWithAnyFromIndex(String s, int index, String[] targets) {
        for (int i = 0; i < targets.length; i++) {
            String target = targets[i];
            if (startWithFromIndex(s, index, target)) {
                return i;
            }
        }
        return -1;
    }

    public static boolean startWithFromIndex(String s, int index, String target) {
        int r = target.length();
        if (s.length() - index < target.length()) {
            return false;
        }
        for (int i = 0; i < r; i++) {
            if (s.charAt(index + i) != target.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    /**
     * "   \nSSSS\t\n  SS S SS  " ->"SSSS SS S SS"
     */
    public static String prettyString(String s, int fromIndex, int toIndex) {
        //todo 参数合法检查
        Preconditions.checkPositionIndexes(fromIndex, toIndex, s.length());
        boolean lastCharIsWhitespace = false;
        char replaceChar = ' ';
        boolean isPrefix = true;
        StringBuilder ret = new StringBuilder(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            char ch = s.charAt(i);
            if (Character.isWhitespace(ch)) {
                if (!lastCharIsWhitespace && !isPrefix) {
                    ret.append(replaceChar);
                }
                lastCharIsWhitespace = true;
            } else {
                ret.append(ch);
                isPrefix = false;
                lastCharIsWhitespace = false;
            }
        }
        return ret.toString().trim();
    }

    public static String prettyStringWithFormat(String s, int fromIndex, int toIndex) {
        //todo 参数合法检查
        Preconditions.checkPositionIndexes(fromIndex, toIndex, s.length());
        char lastChar = 'X';
        StringBuilder ret = new StringBuilder(toIndex - fromIndex);
        for (int i = fromIndex; i < toIndex; i++) {
            char ch = s.charAt(i);
            if (!(ch == lastChar && Character.isWhitespace(ch))) {
                ret.append(ch);
            }
            lastChar = ch;
        }
        return ret.toString().trim();
    }

    public static String prettyString(RangeString rangeString) {
        return prettyString(rangeString.s, rangeString.from, rangeString.to);
    }

    public static boolean checkClassType(RangeString rangeString, String classType) {
        String s = StringUtils.prettyString(rangeString);
        int indexOf = s.indexOf(classType);
        return indexOf != -1 && (indexOf == 0 || (s.charAt(indexOf - 1) + "").equals(JavaKeyWords.SPACE));
    }

    public static int indexOfMatchSymbol(String s, int fromIndex, String[] symbols) {
        List<CharNotInBlockFinder> prohibitedInterval = getCharNotInBlockFinderList();
        prohibitedInterval.remove(prohibitedInterval.size() - 1);
        prohibitedInterval.remove(prohibitedInterval.size() - 1);
        return indexOfMatchSymbol(s, fromIndex, prohibitedInterval, symbols);
    }

    public static int indexOfMatchSymbol(String s, int fromIndex, List<CharNotInBlockFinder> prohibitedInterval, String[] symbols) {
        RangeString rangeString = RangeString.of(fromIndex, s.length(), s);

        int count = 0;
        boolean isBegin = true;
        //默认括号都是正确匹配的
        //todo 通过堆栈验证括号匹配时候合法
        for (int i = fromIndex + Math.min(symbols[0].length(), symbols[1].length()); i <= rangeString.to; i++) {
            boolean effective = true;
            for (CharNotInBlockFinder finder : prohibitedInterval) {
                if (!finder.notIn()) {
                    finder.next(rangeString, i);
                    effective = false;
                    break;
                }
            }
            if (effective) {
                if (rangeString.from <= i - symbols[0].length() && StringUtils.endWithFromIndex(rangeString.s, i - symbols[0].length(), symbols[0])) {
                    count++;
                    isBegin = false;
                } else if (rangeString.from <= i - symbols[1].length() && StringUtils.endWithFromIndex(rangeString.s, i - symbols[1].length(), symbols[1])) {
                    count--;
                }
                if (!isBegin && count == 0) {
                    return i - 1;
                }
                for (CharNotInBlockFinder finder : prohibitedInterval) {
                    finder.next(rangeString, i);
                }
            }
        }
        return -1;
    }

    public static boolean endWithFromIndex(String s, int i, String equal) {
        Preconditions.checkPositionIndex(i, s.length());
        int i1 = i + equal.length();
        if (i1 > s.length()) {
            return false;
        }
        for (int j = i; j < i1; j++) {
            char c = s.charAt(j);
            if (c != equal.charAt(j - i)) {
                return false;
            }
        }
        return true;
    }

    public static int indexOfCharNotInBlock(RangeString rangeString, List<CharNotInBlockFinder> prohibitedInterval, String c) {
        prohibitedInterval = prohibitedInterval == null ? getCharNotInBlockFinderList() : prohibitedInterval;
        for (int i = rangeString.from + c.length(); i <= rangeString.to; i++) {
            boolean effective = true;

            for (CharNotInBlockFinder finder : prohibitedInterval) {
                if (!finder.notIn()) {
                    effective = false;
                    finder.next(rangeString, i);
                    break;
                }
            }
            if (effective) {
                if (StringUtils.endWithFromIndex(rangeString.s, i - c.length(), c)) {
                    return i - 1;
                } else {
                    for (CharNotInBlockFinder finder : prohibitedInterval) {
                        finder.next(rangeString, i);
                    }
                }
            }
        }
        return -1;

    }

    public static int endWithAnyOfStrings(String s, String[] strings) {
        for (int i = 0; i < strings.length; i++) {
            if (s.endsWith(strings[i])) {
                return i;
            }
        }
        return -1;
    }


    public static int indexOfCharNotInBlock(RangeString rangeString, char c) {
        return indexOfCharNotInBlock(rangeString, null, "" + c);
    }

    public static String list2String(List<?> list) {
        if (list == null) {
            return "";
        }
        StringBuilder ret = new StringBuilder();
        for (Object o : list) {
            ret.append(o).append("\n");
        }
        return ret.toString();
    }

    public static String toString(Object... objects) {
        StringBuilder ret = new StringBuilder();
        for (Object o : objects) {
            ret.append(o).append("\n");
        }
        return ret.toString();
    }

    public static boolean allJavaIdentity(String substring) {
        for (int i = 0; i < substring.length(); i++) {
            if (Character.isJavaIdentifierPart(substring.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String getNextJavaIdentifierWord(String string, int i) {
        StringBuilder ret = new StringBuilder();
        for (int j = i; j < string.length(); j++) {
            char ch = string.charAt(j);
            if (Character.isJavaIdentifierPart(ch)) {
                ret.append(ch);
            } else if (ret.length() != 0) {
                break;
            }
        }
        return ret.toString();

    }
}
