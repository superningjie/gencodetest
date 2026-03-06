package tdkw.cosmic.debug;



/**
 * 启动本地应用程序(微服务节点)
 */
public class ApplicationSale {

    public static void main(String[] args) {
        Launcher cosmic = new Launcher();
        cosmic.setClusterNumber("ierp-cluster");
        cosmic.setTenantNumber("ierp-tenant");
        // 本地缓存指向
        /*cosmic.set("redis.serversForCache","172.18.96.106:6379/Cosmic@3839");
        cosmic.set("redis.serversForSession","172.18.96.106:6379/Cosmic@3839");
        cosmic.set("algo.storage.redis.url","172.18.96.106:6379/Cosmic@3839");*/
        cosmic.setAppName("cosmic-sss-8353");
//        cosmic.setWebResPath("D:\\mydemo701\\static-file-service");
        cosmic.setWebResPath("D://mydemo0108//static-file-service");
        cosmic.setConfigUrl("172.18.96.105:2181/?user=zookeeper&password=d@f*g:SGVsbG8==sQOcJsSjiibDSojtJ7wRODdTS/tgutPPKoBsJQAPQeVOa2RwYXNzd29yZA==");
        // 调度中心
        cosmic.set("Schedule.zk.server", "172.18.96.105:2181/?user=zookeeper&password=d@f*g:SGVsbG8==sQOcJsSjiibDSojtJ7wRODdTS/tgutPPKoBsJQAPQeVOa2RwYXNzd29yZA==");
        cosmic.setMcServerUrl("http://172.18.96.108:8090/");
        cosmic.set("mqConfigFiles.config", "consumermqconfig.xml");
        // 调试mq消费的代码块,该参数为false，本节点将不会消费mq消息  会影响本地工作流
        cosmic.set("mq.consumer.register", "true");
        cosmic.set("mq.debug.queue.tag", "zhanglei8353_queue");
        //标注环境为轻量级
        cosmic.set("lightweightdeploy", "true");
        //轻量级不能用lua，设为false
        cosmic.set("redismodelcache.enablelua", "false");
        cosmic.set("dubbo.service.lookup.local", "true");
        cosmic.setStartWithQing(false);
        cosmic.setCosmicWepPort(8888);
        cosmic.start();

    }
}