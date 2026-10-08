package dpi;
import java.util.concurrent.*;import java.util.*;

public class ThreadSafeQueue<T> {
 private final BlockingQueue<T> q; private volatile boolean shutdown;
 public ThreadSafeQueue(int max){q=new ArrayBlockingQueue<>(max);}
 public void push(T x)throws InterruptedException{if(!shutdown)q.put(x);} public boolean tryPush(T x){return !shutdown&&q.offer(x);}
 public T pop()throws InterruptedException{return q.take();} public T poll(long ms)throws InterruptedException{return q.poll(ms,TimeUnit.MILLISECONDS);}
 public int size(){return q.size();} public void shutdown(){shutdown=true;} public boolean isShutdown(){return shutdown;}
}