import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import utils.ZkLearningUtils;

import java.io.IOException;
import java.util.List;

public class ChildrenCallbackMonitor {

    /**
     * @param args
     * @throws IOException
     * @throws InterruptedException
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final ZooKeeper zookeeper = new ZooKeeper(ZkLearningUtils.zkServerHost, 2000, null);

        final AsyncCallback.ChildrenCallback callback = new AsyncCallback.ChildrenCallback() {

            @Override
            public void processResult(int rc, String path, Object ctx, List<String> children) {
                System.out.println(children);

            }

        };

        Watcher watcher = new Watcher() {

            @Override
            public void process(WatchedEvent event) {
                System.out.println("Event is " + event);
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    System.out.println("Changed " + event);
                    zookeeper.getChildren("/workers", this, callback, null);
                }
            }
        };


        System.out.println("begin finish");
        Thread.sleep(200000);
        System.out.println("finish");

    }

}