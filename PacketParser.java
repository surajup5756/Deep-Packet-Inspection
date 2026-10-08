package dpi;
import java.util.*;

public final class PacketParser {
    private PacketParser(){}
    public static boolean parse(PcapReader.RawPacket raw, Types.ParsedPacket p){
        p.raw=raw.data;p.timestampSec=raw.tsSec;p.timestampUsec=raw.tsUsec;p.payloadOffset=0;p.payloadLength=0;
        byte[] d=raw.data;if(d.length<14)return false; p.destMac=mac(d,0);p.srcMac=mac(d,6);p.etherType=u16(d,12);int off=14;
        if(p.etherType==0x0800){ if(d.length<off+20)return false; int ihl=(d[off]&15)*4;if(ihl<20||d.length<off+ihl)return false;
            p.hasIp=true;p.protocol=u8(d,off+9);p.srcIp=u32(d,off+12);p.destIp=u32(d,off+16);off+=ihl;
            if(p.protocol==6){if(d.length<off+20)return false; p.hasTcp=true;p.srcPort=u16(d,off);p.destPort=u16(d,off+2);p.tcpFlags=u8(d,off+13);int h=(d[off+12]>>4&15)*4;if(h<20||d.length<off+h)return false;off+=h;}
            else if(p.protocol==17){if(d.length<off+8)return false;p.hasUdp=true;p.srcPort=u16(d,off);p.destPort=u16(d,off+2);off+=8;}
        } else if(p.etherType==0x86dd){ p.hasIp=true; } else return true;
        p.payloadOffset=off;p.payloadLength=Math.max(0,d.length-off);return true;
    }
    static int u8(byte[] d,int o){return d[o]&255;} static int u16(byte[]d,int o){return ((d[o]&255)<<8)|(d[o+1]&255);}
    static long u32(byte[]d,int o){return ((long)(d[o]&255)<<24)|((long)(d[o+1]&255)<<16)|((long)(d[o+2]&255)<<8)|(d[o+3]&255);}
    static String mac(byte[]d,int o){return String.format(Locale.ROOT,"%02x:%02x:%02x:%02x:%02x:%02x",d[o]&255,d[o+1]&255,d[o+2]&255,d[o+3]&255,d[o+4]&255,d[o+5]&255);}
    public static String ipToString(long ip){return (ip&255)+"."+((ip>>>8)&255)+"."+((ip>>>16)&255)+"."+((ip>>>24)&255);}
    public static String protocolToString(int p){return switch(p){case 1->"ICMP";case 6->"TCP";case 17->"UDP";default->"Unknown";};}
    public static String tcpFlagsToString(int f){StringBuilder s=new StringBuilder();if((f&2)!=0)s.append("SYN ");if((f&16)!=0)s.append("ACK ");if((f&1)!=0)s.append("FIN ");if((f&4)!=0)s.append("RST ");if((f&8)!=0)s.append("PSH ");if((f&32)!=0)s.append("URG ");return s.toString().trim();}
}