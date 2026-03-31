package tdkw.hrmp.hrobs.formplugin.emputils;

import java.util.*;

/**
 * 日期区间合并
 */
public class DateIntervalMerger {



    // 合并日期区间方法
    public static List<Interval> mergeIntervals(List<Date> startDateList, List<Date> endDateList) {
        List<Interval> intervals = new ArrayList<>();
        // 将起始日期和结束日期列表转换成Interval对象列表
        for (int i = 0; i < startDateList.size(); i++) {
            intervals.add(new Interval(startDateList.get(i), endDateList.get(i)));
        }

        if (intervals.isEmpty()) return intervals;

        // 使用List.sort进行排序，按照起始日期进行比较
        intervals.sort(Comparator.comparingLong(a -> a.start.getTime()));
        LinkedList<Interval> merged = new LinkedList<>();

        // 遍历日期区间列表
        for (Interval interval : intervals) {
            // 如果合并后的列表为空，或者当前区间与上一个区间不重叠且间隔超过一天，则直接将当前区间加入合并后的列表
            if (merged.isEmpty() || merged.getLast().end.getTime() + 86400000 < interval.start.getTime()) {
                merged.add(interval);
            } else {
                // 否则，将当前区间与上一个区间进行合并
                merged.getLast().end = new Date(Math.max(merged.getLast().end.getTime(), interval.end.getTime()));
            }
        }
        return merged;
    }


}
