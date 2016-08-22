import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by wys on 2016/8/21.
 */
public class ZkQueue extends SyncPrimitive{
    ZkQueue(String address, String name){
        super(address);
        this.root = name;
        if (zk != null){
            try{
                Stat stat = zk.exists(root, false);
                if (stat == null){
                    zk.create(root, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (InterruptedException e) {
                System.out.println("Interrupted exception");
            } catch (KeeperException e) {
                System.out.println("Keeper exception when instantiating queue: " + e.toString());
            }
        }
    }

    boolean produce(int i) throws KeeperException, InterruptedException {
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.putInt(i);
        byte[] value;
        value = buffer.array();
        zk.create(root + "/element", value, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);
        return true;
    }

    int consume() throws KeeperException, InterruptedException{
        int retvalue = -1;
        Stat stat = null;
        while(true){
            synchronized (mutex){
                List<String> list = zk.getChildren(root, true);
                if (list.size() == 0){
                    System.out.println("going to wait");
                    mutex.wait();
                }else {
                    System.out.println("consumer : list " + list);
                    Integer min = new Integer(list.get(0).substring(7));
                    for (String s: list){
                        Integer tempValue = new Integer(s.substring(7));
                        if (tempValue < min)
                            min = tempValue;
                    }
                    String path ;
                    if (min < 10){
                        path = root + "/element" + "000000000" + min;
                    }else
                        path = root + "/element" + "00000000" + min;
                    System.out.println("Temporary value : " + path);
                    byte[] b = zk.getData(path, false, stat);
                    zk.delete(path, 0);
                    ByteBuffer buffer = ByteBuffer.wrap(b);
                    retvalue = buffer.getInt();
                    return retvalue;
                }
            }
        }
    }

    public static void main(String args[]){
        String address = "10.235.100.22:2181";
        ZkQueue q = new ZkQueue(address, "/app1");

        System.out.println("Input: " + address);
        int i;
        Integer max = 100;

        if (false) {
            System.out.println("Producer");
            for (i = 0; i < max; i++)
                try{
                    q.produce(10 + i);
                } catch (KeeperException e){

                } catch (InterruptedException e){

                }
        } else {
            System.out.println("Consumer");

            for (i = 0; i < max; i++) {
                try{
                    int r = q.consume();
                    System.out.println("Item: " + r);
                } catch (KeeperException e){
                    i--;
                } catch (InterruptedException e){

                }
            }
        }
    }
}
