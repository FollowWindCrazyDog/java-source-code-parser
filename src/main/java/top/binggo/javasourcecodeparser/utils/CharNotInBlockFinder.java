package top.binggo.javasourcecodeparser.utils;

/**
 * @author binggo
 */
public class CharNotInBlockFinder {
    protected int count;
    protected int lastIndex = 0;
    private String blockLeft;
    private String blockRight;
    private String escapeString;

    public CharNotInBlockFinder(String blockLeft, String blockRight, String escapeChar) {
        this.blockLeft = blockLeft;
        this.blockRight = blockRight;
        this.escapeString = escapeChar;
    }

    public boolean notIn() {
        return blockLeft == blockRight ? count % 2 == 0 : count == 0;
    }

    /**
     * [Math.max(rangeString.from, lastIndex),index) 是否以某个操作符为结尾
     */
    public void next(RangeString rangeString, int index) {
        int from = Math.max(rangeString.from, lastIndex);
        String substring = rangeString.s.substring(from, index);
        if (substring.endsWith(blockLeft) && !hasBeanTransfer(rangeString, index, blockLeft)) {
            lastIndex = index;
            count++;
        } else if (substring.endsWith(blockRight) && !hasBeanTransfer(rangeString, index, blockRight)) {
            if (count > 0) {
                clear(index);
            }
        }
    }

    protected void clear(int index) {
        count--;
        lastIndex = index;
    }

    private boolean hasBeanTransfer(RangeString rangeString, int index, String target) {
        if (notIn() || escapeString == null) {
            return false;
        }
        int max = Math.max(rangeString.from, lastIndex);
        return stringCountFromEnd(rangeString.s, index - target.length(), max, escapeString) % 2 == 1;
    }

    /**
     * [start,end)中从end到start中有几个连续的target
     */
    private int stringCountFromEnd(String s, int end, int start, String target) {
        int ret = 0;
        for (int i = end; i - target.length() >= start; i--) {
            int from = i - target.length();
            if (StringUtils.startWithFromIndex(s, from, target)) {
                ret++;
            } else {
                break;
            }
        }
        return ret;
    }

}


