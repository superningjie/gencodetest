package tdkw.hrmp.hrobs.formplugin;

import kd.bos.form.ClientProperties;
import kd.bos.form.container.Container;
import kd.bos.form.control.Label;
import kd.bos.form.control.Vector;
import kd.bos.form.control.events.ItemClickEvent;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;

/**
 * @Descripe 员服-门户 常用工具类
 * @Autor ZZL
 * @Date 2023-05-19
 */
public class GateWayUtils {
    /**
     * @param evt 点击事件-参数
     * @return 返回 点击的标识
     */
    public String getClickKey(EventObject evt) {
        Object source = evt.getSource();
        String clickKey = "";
        if (source instanceof Label) {
            Label label = (Label) source;
            clickKey = label.getKey();
        }
        if (source instanceof Vector) {
            Vector vector = (Vector) source;
            clickKey = vector.getKey();
        }
        if (source instanceof Container) {
            Container container = (Container) source;
            clickKey = container.getKey();
        }
        return clickKey;
    }

    /**
     * @param dateStart 入职时间
     * @return 入职年数
     */
    public int getYear(Date dateStart) {
        int year = 0;
        Date dateNow = new Date();
        SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfM = new SimpleDateFormat("MM");
        SimpleDateFormat sdfD = new SimpleDateFormat("dd");
        int dateStartY = Integer.parseInt(sdfY.format(dateStart));
        int dateStartM = Integer.parseInt(sdfM.format(dateStart));
        int dateStartD = Integer.parseInt(sdfD.format(dateStart));

        int nowY = Integer.parseInt(sdfY.format(dateNow));
        int nowM = Integer.parseInt(sdfM.format(dateNow));
        int nowD = Integer.parseInt(sdfD.format(dateNow));
        year = nowY - dateStartY;
        if (year == 0) {
            return 0;
        }
        if (year > 0) {
            if (year == 1) {
                if (dateStartM < nowM) {
                    return 1;
                }
                if (dateStartM == nowM) {
                    if (dateStartD <= nowD) {
                        return 1;
                    }
                    if (dateStartD > nowD) {
                        return 0;
                    }
                }
                if (dateStartM > nowM) {
                    return 0;
                }
            }
            if (year > 1) {
                if (dateStartM < nowM) {
                    return year;
                }
                if (dateStartM == nowM) {
                    if (dateStartD <= nowD) {
                        return year;
                    }
                    if (dateStartD > nowD) {
                        return year - 1;
                    }
                }
                if (dateStartM > nowM) {
                    return year - 1;
                }
            }
        }
        return year;
    }

    /**
     * @param dateStart 入职时间
     * @return 入职月数
     */
    public int getMonth(Date dateStart) {
        int month = 0;
        Date dateNow = new Date();
        SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfM = new SimpleDateFormat("MM");
        SimpleDateFormat sdfD = new SimpleDateFormat("dd");
        int dateStartY = Integer.parseInt(sdfY.format(dateStart));
        int dateStartM = Integer.parseInt(sdfM.format(dateStart));
        int dateStartD = Integer.parseInt(sdfD.format(dateStart));

        int nowY = Integer.parseInt(sdfY.format(dateNow));
        int nowM = Integer.parseInt(sdfM.format(dateNow));
        int nowD = Integer.parseInt(sdfD.format(dateNow));
        month = nowM - dateStartM;
        if (dateStartY == nowY) {
            if (month > 0) {
                if (month == 1) {
                    if (dateStartD > nowD) {
                        return 0;
                    }
                    if (dateStartD <= nowD) {
                        return 1;
                    }
                }
                if (month > 1) {
                    if (dateStartD > nowD) {
                        return month - 1;
                    }
                    if (dateStartD <= nowD) {
                        return month;
                    }
                }
            }
            if (month == 0) {
                return 0;
            }
        }
        if (dateStartY < nowY) {
            //2022-09-10 2023-10-11
            //2022-09-10 2023-10-10
            //2022-09-10  2023-10-9
            if (month > 0) {
                if (dateStartD <= nowD) {
                    return month;
                }

                if (dateStartD > nowD) {
                    return month - 1;
                }
            }
            //2022-09-10 2023-9-11
            //2022-09-10 2023-9-10
            //2022-09-10  2023-9-9
            if (month == 0) {
                if (dateStartD <= nowD) {
                    return month;
                }
                if (dateStartD > nowD) {
                    return 11;
                }
            }
            //2022-10-10 2023-9-11
            //2022-10-10 2023-9-10
            //2022-10-10  2023-9-9
            if (month < 0) {
                if (dateStartD <= nowD) {
                    return 12 + month;
                }

                if (dateStartD > nowD) {
                    return 12 + month - 1;
                }
            }
        }

        return month;
    }

    /**
     * 入职天数
     *
     * @param dateStart 入职时间
     * @return 返回计算的入职天数
     */
    public int getDay(Date dateStart) {
        int day = 0;
        Date dateNow = new Date();
        SimpleDateFormat sdfY = new SimpleDateFormat("yyyy");
        SimpleDateFormat sdfM = new SimpleDateFormat("MM");
        SimpleDateFormat sdfD = new SimpleDateFormat("dd");
        int dateStartY = Integer.parseInt(sdfY.format(dateStart));
        int dateStartM = Integer.parseInt(sdfM.format(dateStart));
        int dateStartD = Integer.parseInt(sdfD.format(dateStart));

        int nowY = Integer.parseInt(sdfY.format(dateNow));
        int nowM = Integer.parseInt(sdfM.format(dateNow));
        int nowD = Integer.parseInt(sdfD.format(dateNow));
        day = nowD - dateStartD;
        //2023-05-23 2023-05-23
        //2023-04-23 2023-05-23
        //2022-11-23 2023-05-23
        if (day == 0) {
            return 1;
        }
        //2023-05-25 2023-05-24
        //2023-04-25 2023-05-24
        //2022-11-25 2023-05-24
        if (day < 0) {
            List<Integer> day31 = Arrays.asList(1, 2, 4, 6, 8, 9, 11);
            List<Integer> day30 = Arrays.asList(5, 7, 10, 12);
            if (day31.contains(nowM)) {
                return 31 - dateStartD + nowD + 1;
            }
            if (day30.contains(nowM)) {
                return 30 - dateStartD + nowD + 1;
            }
            if (nowM == 3) {
                if (nowY % 4 == 0) {
                    return 29 - dateStartD + nowD + 1;
                } else {
                    return 28 - dateStartD + nowD + 1;
                }
            }
        }
        if (day > 0) {
            return day + 1;
        }
        return day;
    }

    // 获得某天最大时间 2022-12-09 23:59:59
    public static Date getEndOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime endOfDay = localDateTime.with(LocalTime.MAX);
        return Date.from(endOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    // 获得某天最小时间 2022-12-09 00:00:00
    public static Date getStartOfDay(Date date) {
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(date.getTime()), ZoneId.systemDefault());
        LocalDateTime startOfDay = localDateTime.with(LocalTime.MIN);
        return Date.from(startOfDay.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Set<Long> getExs() {
        Set<Long> exs = new HashSet<>();
        //  exs.add(1320384650887095296L); // 晚走
        //  exs.add(1320384483332989952L); //早到
        exs.add(1320384356908327936L);//缺卡
        exs.add(1320384239123833856L);//旷工
        exs.add(1320384079815828480L);//早退
        exs.add(1320383951981782016L);// 迟到
        return exs;
    }

    public static Map<String, Object> redColor() {
        Map<String, Object> map = new HashMap<>();//红色
        map.put("bc", "rgba(255,82,87,0.2)");
        map.put(ClientProperties.ForeColor, "#fb2323");
        return map;
    }

    public static Map<String, Object> blueColor() {
        Map<String, Object> map1 = new HashMap<>();
        map1.put("bc", "rgba(27,168,84,0.2)");
        map1.put(ClientProperties.ForeColor, "#1ba854");
        return map1;
    }
}
