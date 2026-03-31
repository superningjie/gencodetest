package tdkw.hrmp.hrobs.formplugin.util;

import com.alibaba.fastjson.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

/**
 * @author xxx
 * @version 1.0
 * @date 2023/7/26-15:53
 * @description TODO
 */
public class HttpUtils {
    public static String sendHttpPost(String url, Map<String, String> headers, JSONObject body) throws Exception {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(url);
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(5000).setConnectionRequestTimeout(1000).setSocketTimeout(5000).build();
        post.setConfig(requestConfig);
        if (headers != null) {
            Iterator var6 = headers.entrySet().iterator();

            while(var6.hasNext()) {
                Map.Entry<String, String> entry = (Map.Entry)var6.next();
                post.setHeader((String)entry.getKey(), (String)entry.getValue());
            }
        }

        StringEntity entity = new StringEntity(body.toString(), ContentType.APPLICATION_JSON);
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        HttpEntity responseEntity = response.getEntity();
        return EntityUtils.toString(responseEntity, StandardCharsets.UTF_8);
    }
}
