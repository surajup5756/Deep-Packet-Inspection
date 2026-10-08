package dpi;
import java.io.*;import java.util.*;import java.util.concurrent.*;import java.nio.*;

public class DpiEngine {
 public static class Config{public int lbs=2,fpsPerLb=2,queueSize=10000;public boolean verbose;public String rulesFile;}
 private final Config cfg;private final RuleManager rules=new RuleManager();private final List<FastPath> fps=new ArrayList<>();private final List<LoadBalancer> lbs=new ArrayList<>();
 private long total,forwarded,dropped;
 public DpiEngine(Config c){cfg=c;}
 public boolean processFile(String input,String output)throws Exception{
   if(cfg.rulesFile!=null)rules.loadRules(cfg.rulesFile); for(int i=0;i<cfg.lbs*cfg.fpsPerLb;i++)fps.add(new FastPath(i,rules,(j,a)->{try{writePacket(output,j.raw);}catch(Exception e){throw new RuntimeException(e);}},cfg.queueSize));
   for(int l=0;l<cfg.lbs;l++){List<ThreadSafeQueue<Types.PacketJob>> qs=new ArrayList<>();for(int i=0;i<cfg.fpsPerLb;i++)qs.add(fps.get(l*cfg.fpsPerLb+i).queue());lbs.add(new LoadBalancer(l,qs,l*cfg.fpsPerLb,cfg.queueSize));}
   PcapReader r=new PcapReader();if(!r.open(input))return false;byte[] gh=r.getGlobalHeader();try(FileOutputStream o=new FileOutputStream(output)){o.write(gh);
     for(FastPath f:fps)f.start();for(LoadBalancer l:lbs)l.start();
     PcapReader.RawPacket raw;while((raw=r.next())!=null){Types.ParsedPacket p=new Types.ParsedPacket();if(!PacketParser.parse(raw,p))continue;total++;Types.PacketJob j=new Types.PacketJob(total,raw,p);long h=(new Types.FiveTuple(p.srcIp,p.destIp,p.srcPort,p.destPort,p.protocol)).hashCode()&0x7fffffff;lbs.get((int)(h%lbs.size())).queue().push(j);}
     r.close();for(LoadBalancer l:lbs)l.stop();for(FastPath f:fps)f.stop();
     forwarded=total-dropped;
   }finally{r.close();}System.out.println("\nDPI processing complete. Packets inspected: "+total);System.out.println("Rules: "+rules.stats());for(FastPath f:fps)System.out.println(f.stats());return true;
 }
 private synchronized void writePacket(String file,PcapReader.RawPacket p)throws IOException{try(RandomAccessFile raf=new RandomAccessFile(file,"rw")){raf.seek(raf.length());ByteBuffer b=ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);b.putInt((int)p.tsSec).putInt((int)p.tsUsec).putInt((int)p.inclLen).putInt((int)p.origLen);raf.write(b.array());raf.write(p.data);}}
 public RuleManager rules(){return rules;}
 public String report(){return "DPI Engine Report\\nPackets inspected: "+total+"\\nForwarded: "+forwarded+"\\nDropped: "+dropped+"\\n"+rules.stats();}
}