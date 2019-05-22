package top.binggo.javasourcecodeparser.analysis;

import top.binggo.javasourcecodeparser.utils.RangeString;

/**
 * @author binggo
 */
public interface SourceCodeRange {

    /**
     * 获得所表示的代码区间
     *
     * @return 获得所表示的代码区间
     */
    RangeString getRangeString();


}
