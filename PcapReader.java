package dpi;
import java.io.*; import java.nio.*; import java.nio.file.*; import java.util.*;

public class PcapReader implements Closeable {
    public static final class RawPacket { public long tsSec,tsUsec,inclLen,origLen; public byte[] data; }
    private DataInputStream in; private boolean swap; private byte[] globalHeader;
    public boolean open(String file){ try { close(); in=new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
        globalHeader=in.readNBytes(24); if(globalHeader.length!=24) throw new IOException("Invalid PCAP header");
        int magic=readInt(globalHeader,0,false); if(magic==0xa1b2c3d4)swap=false; else if(magic==0xd4c3b2a1)swap=true; else throw new IOException("Unsupported PCAP magic: 0x"+Integer.toHexString(magic));
        return true; } catch(Exception e){System.err.println("Error: "+e.getMessage()); close(); return false;} }
    public byte[] getGlobalHeader(){return globalHeader==null?new byte[0]:globalHeader.clone();}
    public boolean isOpen(){return in!=null;} public void close(){if(in!=null)try{in.close();}catch(Exception ignored){} in=null;}
    public RawPacket next() throws IOException { if(in==null)return null; byte[] h=in.readNBytes(16); if(h.length==0)return null; if(h.length<16)throw new EOFException("Truncated packet header");
        RawPacket p=new RawPacket(); p.tsSec=uint(readInt(h,0,swap));p.tsUsec=uint(readInt(h,4,swap));p.inclLen=uint(readInt(h,8,swap));p.origLen=uint(readInt(h,12,swap));
        if(p.inclLen>64*1024*1024)throw new IOException("Invalid packet length: "+p.inclLen); p.data=in.readNBytes((int)p.inclLen); if(p.data.length!=p.inclLen)throw new EOFException("Truncated packet data"); return p; }
    private static int readInt(byte[] b,int o,boolean sw){return sw?((b[o]&255)<<24)|((b[o+1]&255)<<16)|((b[o+2]&255)<<8)|(b[o+3]&255):((b[o+3]&255)<<24)|((b[o+2]&255)<<16)|((b[o+1]&255)<<8)|(b[o]&255);}
    private static long uint(int x){return Integer.toUnsignedLong(x);}
}