package tdkw.hrmp.hrobs.formplugin.util;

/**
 * @Description ： 自定义三元组
 * @ClassName ：ConfigIdTuple
 * @author xxx
 * @Date ：2023/7/24 10:14
 * @Version: 1.0
 */
public class ConfigIdTuple<A,B,C> {

    public final A result;

    public final B message;

    public final C congfigId;

    public ConfigIdTuple(A result, B message, C congfigId) {
        this.result = result;
        this.message = message;
        this.congfigId = congfigId;
    }
}
