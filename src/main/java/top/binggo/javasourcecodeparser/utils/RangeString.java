package top.binggo.javasourcecodeparser.utils;

import com.google.common.base.Preconditions;

/**
 * 表示一个String的[from,to)区间,用来传参
 *
 * @author binggo
 */
public final class RangeString {
    public final int from;
    public final int to;
    public String s;

    private RangeString(int from, int to, String s) {
        this.from = from;
        this.to = to;
        this.s = s;
    }

    public static RangeString of(int from, int to, String s) {
        Preconditions.checkPositionIndexes(from, to, s.length());
        return new RangeString(from, to, s);
    }

    public RangeString left() {
        return RangeString.of(this.to, this.s.length(), this.s);
    }

    public String subString() {
        return s.substring(from, to);
    }

    public boolean isEmpty() {
        return to <= from;
    }

    @Override
    public String toString() {
        return subString();
    }
}
