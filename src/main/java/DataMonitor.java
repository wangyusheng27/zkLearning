
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.KeeperException.Code;

import java.util.Arrays;

/**
 * Created by wys on 2016/8/17.
 */
public class DataMonitor implements Watcher, AsyncCallback.StatCallback{
    private ZooKeeper zk;
    String znode;
    Watcher chainedWatcher;
    DataMonitorListener listener;
    boolean dead;
    byte prevData[];

    public DataMonitor(ZooKeeper zk, String znode, Watcher chainedWatcher,
                       DataMonitorListener listener) {
        this.zk = zk;
        this.znode = znode;
        this.chainedWatcher = chainedWatcher;
        this.listener = listener;

        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        zk.exists(znode, true, this, null);
    }

    //implements from AsyncCallback.StatCallback
    public void processResult(int rc, String path, Object ctx, Stat stat) {
        System.out.println("step -1");
        System.out.println("rc={}, path={}, ctx={}, stat={}" + rc +","+ path +","+ ctx +","+ stat);
        boolean exists;
        switch (rc) {
            case Code.Ok:
                exists = true;
                break;
            case Code.NoNode:
                exists = false;
                System.out.println("error no node");
                break;
            case Code.SessionExpired:
            case Code.NoAuth:
                dead = true;
                listener.closing(rc);
                return;
            default:
                // Retry errors
                zk.exists(znode, true, this, null);
                return;
        }

        byte b[] = null;
        if (exists) {
            try {
                b = zk.getData(znode, false, null);
            } catch (KeeperException e) {
                // We don't need to worry about recovering now. The watch
                // callbacks will kick off any exception handling
                e.printStackTrace();
            } catch (InterruptedException e) {
                return;
            }
        }
        System.out.println("node exist b=" + b);
        System.out.println("step 2");

        if ((b == null && b != prevData)
                || (b != null && !Arrays.equals(prevData, b))) {
            listener.exists(b);
            prevData = b;
        }
    }

    //implements from Watcher
    public void process(WatchedEvent watchedEvent) {
        System.out.println("step 1");
        String path = watchedEvent.getPath();
        System.out.println("path:" + path);
        System.out.println("type:" + watchedEvent.getType());
        System.out.println("getState:" + watchedEvent.getState());
        if (watchedEvent.getType() == Event.EventType.None){
            switch (watchedEvent.getState()) {
                case SyncConnected:
                    // In this particular example we don't need to do anything
                    // here - watches are automatically re-registered with
                    // server and any watches triggered while the client was
                    // disconnected will be delivered (in order of course)
                    break;
                case Expired:
                    // It's all over
                    dead = true;
                    listener.closing(KeeperException.Code.SessionExpired);
                    break;
            }
        }else {
            if (path != null && path.equals(znode)){
                System.out.println("step 3");
                zk.exists(znode, true, this, null);
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher.process(watchedEvent);
        }
    }

    public interface DataMonitorListener {
        /**
         * The existence status of the node has changed.
         */
        void exists(byte data[]);

        /**
         * The ZooKeeper session is no longer valid.
         *
         * @param rc
         * the ZooKeeper reason code
         */
        void closing(int rc);
    }


}
