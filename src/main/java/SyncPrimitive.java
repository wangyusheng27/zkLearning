import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by wys on 2016/8/21.
 */
public class SyncPrimitive implements Watcher{
    static ZooKeeper zk = null;
    static Integer mutex;
    String root;
    SyncPrimitive(String address){
        if(zk == null){
            try{
                System.out.println("Starting zk:");
                zk = new ZooKeeper(address, 3000, this);
                mutex = new Integer(-1);
                System.out.println("Finshed starting zk:" + zk);
            } catch (IOException e) {
                System.out.println(e.toString());
                zk = null;
            }
        }
    }

    synchronized public void process(WatchedEvent watchedEvent) {
        synchronized (mutex){
            mutex.notify();
        }
    }
}
