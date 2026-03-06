package tdkw.hrmp.hrobs.common.hrobs.util;

import kd.bos.logging.Log;
import kd.bos.logging.LogFactory;
import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

/**
 * 拼音工具类
 */
public class PinyinUtil {

    private static final Log logger = LogFactory.getLog(PinyinUtil.class);
    /**
     * 判断字符串是否包含字母
     *
     * @param str
     * @return
     */
    public static boolean containsLetters(String str) {
        return str.matches(".*[a-zA-Z]+.*");
    }
    /**
     * 将给定的中文字符串转换为拼音字符串。
     *
     * @param chinese 给定的中文字符串
     * @return 转换后的拼音字符串
     */
    public static String convertToPinyin(String chinese) {
        StringBuilder pinyinBuilder = new StringBuilder();
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE); // 小写格式
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE); // 不包含声调

        for (int i = 0; i < chinese.length(); i++) {
            char ch = chinese.charAt(i);
            try {
                // 对于每个汉字生成其拼音数组，注意这里使用了 format
                String[] pinyinArray = PinyinHelper.toHanyuPinyinStringArray(ch, format);
                if (pinyinArray != null) {
                    pinyinBuilder.append(pinyinArray[0]); // 只取一个发音，如果是多音字，就取第一个拼音
                } else {
                    pinyinBuilder.append(ch); // 如果不是汉字字符，直接添加原字符
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                // 异常处理
                logger.error("转换拼音异常:" + e.getMessage());
                pinyinBuilder.append(ch);
            }
        }
        return pinyinBuilder.toString();
    }


    /**
     * 获得汉语拼音首字母 大写
     *
     * @param chines 汉字
     * @return
     */
    public static String getAlpha2UpperCase(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    logger.error("获得汉语拼音首字母异常:" + e.getMessage());
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }

    /**
     * 将字符串中的中文转化为拼音,英文字符不变
     *
     * @param inputString 汉字
     * @return
     */
    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);
        String output = "";
        if (null != inputString && 0 < inputString.length() && !"null".equals(inputString)) {
            char[] input = inputString.trim().toCharArray();
            try {
                for (int i = 0; i < input.length; i++) {
                    if (Character.toString(input[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        String[] temp = PinyinHelper.toHanyuPinyinStringArray(input[i], format);
                        output += temp[0];
                    } else {
                        output += Character.toString(input[i]);
                    }
                }
            } catch (BadHanyuPinyinOutputFormatCombination e) {
                logger.error("将字符串中的中文转化为拼音,英文字符不变异常 :" + e.getMessage());
            }
        } else {
            return "";
        }
        return output;
    }

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpell(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.UPPERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    logger.error("汉字转换位汉语拼音首字母，英文字符不变异常 :" + e.getMessage());
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }

    /**
     * 汉字转换位汉语拼音首字母，英文字符不变(小写)
     *
     * @param chines 汉字
     * @return 拼音
     */
    public static String converterToFirstSpellSmal(String chines) {
        String pinyinName = "";
        char[] nameChar = chines.toCharArray();
        HanyuPinyinOutputFormat defaultFormat = new HanyuPinyinOutputFormat();
        defaultFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        defaultFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        for (int i = 0; i < nameChar.length; i++) {
            if (nameChar[i] > 128) {
                try {
                    if (Character.toString(nameChar[i]).matches("[\\u4E00-\\u9FA5]+")) {
                        pinyinName += PinyinHelper.toHanyuPinyinStringArray(nameChar[i], defaultFormat)[0].charAt(0);
                    } else {
                        pinyinName += nameChar[i];
                    }
                } catch (BadHanyuPinyinOutputFormatCombination e) {
                    logger.error("汉字转换位汉语拼音首字母，英文字符不变异常:" + e.getMessage());
                }
            } else {
                pinyinName += nameChar[i];
            }
        }
        return pinyinName;
    }


}
