package top.binggo.javasourcecodeparser.utils;

import org.springframework.lang.NonNull;

import java.util.function.Predicate;

/**
 * @author binggo
 */
public class CamelNameProcessor {

    private CamelNameProcessor() {

    }

    /**
     * 用来将原先使用驼峰命名的名称转换成以空格为分词的短语
     * <p>
     * example:
     * CamelNameProcessor -> camel name processor
     * getPredicate -> get predicate
     * </p>
     * 或者是将以下划线分割的名称转换成以空格为分词的短语
     * <p>
     * example:
     * CAMEL_NAME_PROCESSOR -> camel name processor
     * </p>
     */
    @NonNull

    public static String turn(@NonNull String ori) {
        StringBuilder ret = new StringBuilder(ori.length() + 5);
        int lastIndex = 0;
        Predicate<Character> predicate = getPredicate(ori);
        for (int i = 1; i < ori.length(); i++) {
            if (predicate.test(ori.charAt(i))) {
                ret.append(ori, lastIndex, i).append(" ");
                lastIndex = i;
            }
        }
        ret.append(ori, lastIndex, ori.length());
        return ret.toString().toLowerCase().replaceAll("_", "");
    }

    @NonNull
    public static String[] split(@NonNull String ori) {
        return turn(ori).split("\\s");
    }

    /**
     * 判断一个变量是ABC_ABC的命名方式还是abcAbc的命名方式
     */
    static Predicate<Character> getPredicate(String ori) {
        boolean allLetterUpperCase = true;
        for (int i = 0; i < ori.length(); i++) {
            if (Character.isLowerCase(ori.charAt(i))) {
                allLetterUpperCase = false;
                break;
            }
        }
        if (!allLetterUpperCase) {
            return Character::isUpperCase;
        } else {
            return character -> character.equals('_');
        }
    }


}
