package tdkw.hrmp.hrobs.formplugin.util;

/**
 * @Description ： 自定义消息返回值二元组
 * @ClassName ：SendTipTuple
 * @author xxx
 * @Date ：2023/8/4 10:24
 * @Version: 1.0
 */
public class SendTipTuple<item1,item2> {

    public  final item1  isSuccess;

    public final  item2  message;

    public  static  <item1,item2>  SendTipTuple<item1,item2> create(item1 isSuccess,item2 message){
        return new SendTipTuple(isSuccess,message);
    }


    public SendTipTuple(item1 result, item2 message) {
        this.isSuccess = result;
        this.message = message;
    }

}
