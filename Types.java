package dpi;
import java.util.*;

public final class Types {
    private Types() {}
    public record FiveTuple(long srcIp,long dstIp,int srcPort,int dstPort,int protocol) {
        public FiveTuple reverse(){ return new FiveTuple(dstIp,srcIp,dstPort,srcPort,protocol); }
        public String toString(){return PacketParser.ipToString(srcIp)+":"+srcPort+" -> "+PacketParser.ipToString(dstIp)+":"+dstPort+" ("+(protocol==6?"TCP":protocol==17?"UDP":"?")+")";}
    }
    public enum AppType { UNKNOWN, HTTP, HTTPS, DNS, TLS, QUIC, GOOGLE, FACEBOOK, YOUTUBE, TWITTER, INSTAGRAM, NETFLIX, AMAZON, MICROSOFT, APPLE, WHATSAPP, TELEGRAM, TIKTOK, SPOTIFY, ZOOM, DISCORD, GITHUB, CLOUDFLARE }
    public enum ConnectionState { NEW, ESTABLISHED, CLASSIFIED, BLOCKED, CLOSED }
    public enum PacketAction { FORWARD, DROP, INSPECT, LOG_ONLY }
    public static String appTypeToString(AppType a){ return switch(a){
        case UNKNOWN->"Unknown"; case HTTP->"HTTP"; case HTTPS->"HTTPS"; case DNS->"DNS"; case TLS->"TLS"; case QUIC->"QUIC";
        case GOOGLE->"Google"; case FACEBOOK->"Facebook"; case YOUTUBE->"YouTube"; case TWITTER->"Twitter"; case INSTAGRAM->"Instagram";
        case NETFLIX->"Netflix"; case AMAZON->"Amazon"; case MICROSOFT->"Microsoft"; case APPLE->"Apple"; case WHATSAPP->"WhatsApp";
        case TELEGRAM->"Telegram"; case TIKTOK->"TikTok"; case SPOTIFY->"Spotify"; case ZOOM->"Zoom"; case DISCORD->"Discord";
        case GITHUB->"GitHub"; case CLOUDFLARE->"Cloudflare"; }; }
    public static AppType sniToAppType(String s){ if(s==null)return AppType.UNKNOWN; String x=s.toLowerCase(Locale.ROOT);
        if(x.contains("google"))return AppType.GOOGLE; if(x.contains("facebook"))return AppType.FACEBOOK; if(x.contains("youtube")||x.contains("googlevideo"))return AppType.YOUTUBE;
        if(x.contains("twitter")||x.contains("x.com"))return AppType.TWITTER; if(x.contains("instagram"))return AppType.INSTAGRAM; if(x.contains("netflix"))return AppType.NETFLIX;
        if(x.contains("amazon"))return AppType.AMAZON; if(x.contains("microsoft")||x.contains("live.com"))return AppType.MICROSOFT; if(x.contains("apple"))return AppType.APPLE;
        if(x.contains("whatsapp"))return AppType.WHATSAPP; if(x.contains("telegram"))return AppType.TELEGRAM; if(x.contains("tiktok"))return AppType.TIKTOK;
        if(x.contains("spotify"))return AppType.SPOTIFY; if(x.contains("zoom"))return AppType.ZOOM; if(x.contains("discord"))return AppType.DISCORD;
        if(x.contains("github"))return AppType.GITHUB; if(x.contains("cloudflare"))return AppType.CLOUDFLARE; return AppType.UNKNOWN; }
    public static final class ParsedPacket {
        public long timestampSec; public long timestampUsec; public byte[] raw;
        public String srcMac="", destMac=""; public int etherType; public boolean hasIp,hasTcp,hasUdp;
        public long srcIp,destIp; public int protocol,srcPort,destPort,tcpFlags; public int payloadOffset,payloadLength;
        public byte[] payload(){ return raw==null?new byte[0]:Arrays.copyOfRange(raw,payloadOffset,Math.min(raw.length,payloadOffset+payloadLength));}
    }
    public static final class PacketJob {
        public final long id; public final PcapReader.RawPacket raw; public final ParsedPacket parsed;
        public PacketJob(long id,PcapReader.RawPacket raw,ParsedPacket parsed){this.id=id;this.raw=raw;this.parsed=parsed;}
    }
    public static final class Connection {
        public FiveTuple tuple; public ConnectionState state=ConnectionState.NEW; public AppType appType=AppType.UNKNOWN; public String sni="";
        public long packetsIn,packetsOut,bytesIn,bytesOut; public long firstSeen=System.nanoTime(),lastSeen=firstSeen; public PacketAction action=PacketAction.FORWARD;
        public boolean synSeen,synAckSeen,finSeen;
    }
}