import org.apache.log4j.Logger;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import utils.ZkLearningUtils;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by wys on 2016/9/2.
 * test create zk sync
 */
public class SessionWatch implements Watcher, Runnable{
    static Logger logger = Logger.getLogger(Executor.class);


    public static void main(String[] args) throws IOException {
        SessionWatch watch = new SessionWatch();
        ExecutorService service = Executors.newFixedThreadPool(2);
        service.submit(watch);
        logger.info("abcd");
    }

    @Override
    public void process(WatchedEvent watchedEvent) {
        logger.info("Event is" + watchedEvent);
    }

    @Override
    public void run() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(ZkLearningUtils.zkServerHost, 2000, this);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
