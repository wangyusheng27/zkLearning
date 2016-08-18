import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * Created by wys on 2016/8/17.
 */
public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener{
    String filename;
    String[] exec;
    ZooKeeper zk;
    DataMonitor dm;
    public Executor(String hostPort, String znode, String filename,
                    String exec[]) throws KeeperException, IOException {
        this.filename = filename;
        this.exec = exec;
        zk = new ZooKeeper(hostPort, 3000, this);
        dm = new DataMonitor(zk, znode, null, this);
        int i = 1;
    }

    public void run() {

    }

    public void process(WatchedEvent watchedEvent) {

    }

    public void exists(byte[] data) {

    }

    public void closing(int rc) {

    }
}
