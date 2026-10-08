package dpi;
import java.nio.charset.*;import java.util.*;import java.util.regex.*;

public final class SniExtractor {
 private SniExtractor(){}
 static int u16(byte[]d,int o){return ((d[o]&255)<<8)|(d[o+1]&255);} static int u24(byte[]d,int o){return ((d[o]&255)<<16)|((d[o+1]&255)<<8)|(d[o+2]&255);}
 public static boolean isTLSClientHello(byte[]d){return d.length>=9&&(d[0]&255)==0x16&&u16(d,1)>=0x0300&&u16(d,1)<=0x0304&&d[5]==1;}
 public static Optional<String> extract(byte[]d){try{if(!isTLSClientHello(d))return Optional.empty();int p=5+4;if(d.length<p+2+32+1)return Optional.empty();p+=2+32;int sid=d[p]&255;p+=1+sid;int cs=u16(d,p);p+=2+cs;int cm=d[p]&255;p+=1+cm;int extLen=u16(d,p);p+=2;int end=Math.min(d.length,p+extLen);
   while(p+4<=end){int type=u16(d,p),len=u16(d,p+2);p+=4;if(p+len>end)break;if(type==0&&len>=5){int q=p+2;int nameType=d[q]&255;int nl=u16(d,q+1);if(nameType==0&&q+3+nl<=p+len)return Optional.of(new String(d,q+3,nl,StandardCharsets.US_ASCII));}p+=len;} }catch(Exception ignored){}return Optional.empty();}
 public static Optional<String> httpHost(byte[]d){String s=new String(d,StandardCharsets.ISO_8859_1);Matcher m=Pattern.compile("(?im)^Host:\\s*([^\\r\\n]+)").matcher(s);return m.find()?Optional.of(m.group(1).trim()):Optional.empty();}
 public static boolean isHttp(byte[]d){String s=new String(d,0,Math.min(d.length,16),StandardCharsets.ISO_8859_1);return s.matches("(?s)(GET|POST|PUT|DELETE|HEAD|OPTIONS|PATCH) .*");}
 public static Optional<String> dnsQuery(byte[]d){try{if(d.length<13)return Optional.empty();int flags=u16(d,2);if((flags&0x8000)!=0)return Optional.empty();int qd=u16(d,4);if(qd<1)return Optional.empty();int p=12;StringBuilder s=new StringBuilder();while(p<d.length&&d[p]!=0){int n=d[p++]&255;if(n==0||p+n>d.length)break;if(s.length()>0)s.append('.');s.append(new String(d,p,n,StandardCharsets.US_ASCII));p+=n;}return s.length()>0?Optional.of(s.toString()):Optional.empty();}catch(Exception e){return Optional.empty();}}
}