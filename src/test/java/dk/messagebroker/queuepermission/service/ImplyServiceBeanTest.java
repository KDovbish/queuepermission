package dk.messagebroker.queuepermission.service;

import org.junit.jupiter.api.Test;

import java.net.InetAddress;
import java.net.UnknownHostException;

import static org.junit.jupiter.api.Assertions.*;

class ImplyServiceBeanTest {

    ImplyService implyService = new ImplyServiceBean();

    @Test
    void IPSubnet() {

        assertFalse(implyService.IPSubnet("172.17.64.0", "172.17.64.0/24"));   // номер сети не является адресом
        assertFalse(implyService.IPSubnet("172.17.64.255", "172.17.64.0/24")); // широковещательный
        assertTrue(implyService.IPSubnet("172.17.64.10", "172.17.64.0/24"));
        assertFalse(implyService.IPSubnet("172.17.63.10", "172.17.64.0/24"));

        assertFalse(implyService.IPSubnet("172.17.64.0", "172.17.64.0/30"));   // номер сети не является адресом
        assertTrue(implyService.IPSubnet("172.17.64.1", "172.17.64.0/30"));
        assertTrue(implyService.IPSubnet("172.17.64.2", "172.17.64.0/30"));
        assertFalse(implyService.IPSubnet("172.17.64.3", "172.17.64.0/30"));   // широковещательный
        assertFalse(implyService.IPSubnet("172.17.64.4", "172.17.64.0/30"));
        /*
        Маска: FF.FF.FF.1111 11 00 (30 бит)

        172.17.64.0000 00 00    0   нумерация сети
        172.17.64.0000 00 01    1
        172.17.64.0000 00 10    2
        172.17.64.0000 00 11    3   широковещательный
         */

        assertFalse(implyService.IPSubnet("172.17.64.4", "172.17.64.4/30"));  // номер сети не является адресом
        assertTrue(implyService.IPSubnet("172.17.64.5", "172.17.64.4/30"));   // адрес 1 сети
        assertTrue(implyService.IPSubnet("172.17.64.6", "172.17.64.4/30"));   // адрес 2 сети
        assertFalse(implyService.IPSubnet("172.17.64.7", "172.17.64.4/30"));  // широковещательный
        // ip-адрес не входит в подсеть, поскольку биты покрываемые маской не соответствуют битам в подсети
        assertFalse(implyService.IPSubnet("172.17.64.8", "172.17.64.4/30"));
        /*
        Маска: FF.FF.FF.1111 11 00 (30 бит)

        172.17.64.0000 01 00    .4  0   нумерация сети
        172.17.64.0000 01 01    .5  1
        172.17.64.0000 01 10    .6  2
        172.17.64.0000 01 11    .7  3   широковещательный
         */
    }

    @Test
    void IPDomainName() throws UnknownHostException {
        System.out.println(InetAddress.getByName("127.0.0.1").getCanonicalHostName());
        System.out.println(InetAddress.getByName("192.168.0.106").getCanonicalHostName());
        System.out.println(InetAddress.getByName("142.251.39.14").getCanonicalHostName());

        assertTrue(implyService.IPDomainName("127.0.0.1", "127.0.0.1"));
        assertTrue(implyService.IPDomainName("142.251.39.14", "bud02s37-in-f14.1e100.net"));
        assertFalse(implyService.IPDomainName("142.251.39.14", "aaa.bud02s37-in-f14.1e100.net"));
        assertTrue(implyService.IPDomainName("142.251.39.14", "1e100.net"));
    }

    @Test
    void domainNameIP() throws UnknownHostException {
        System.out.println(InetAddress.getByName("bud02s37-in-f14.1e100.net").getHostAddress());
        System.out.println(InetAddress.getByName("cenzor.net").getHostAddress());
        System.out.println(InetAddress.getByName("docs.oracle.com").getHostAddress());

        assertTrue(implyService.DomainNameIP("cenzor.net", "64.190.63.111"));
    }

    @Test
    void domainNameSubnet() throws UnknownHostException {
        assertTrue(implyService.DomainNameSubnet("cenzor.net", "64.190.63.0/24"));
    }

    @Test
    void domainNameDomainName() {
        assertTrue(implyService.DomainNameDomainName("google.com","google.com"));
        // "а" является поддоменом google.com; поэтому, если дано разрешение для для google.com, то автоматически дается разрешение и для его поддоменов
        //  в данном случае, a.google.com является меньшим чем google.com
        assertTrue(implyService.DomainNameDomainName("a.google.com","google.com"));
        //  в данном случае, google.com большее множество, чем a.google.com; большее не может полностью входить в меньшее, поэтому false
        assertFalse(implyService.DomainNameDomainName("google.com","a.google.com"));    // в поддомен a.google.com нико
    }



    @Test
    void getByAddress() throws UnknownHostException {
        byte[] ip = {-84, 17, 64, 10};
        InetAddress inetAddress = InetAddress.getByAddress(ip);
        assertEquals("172.17.64.10", inetAddress.getHostAddress());

        ip[0] = -64; ip[1] = -88; ip[2] = 0; ip[3] = 103;
        assertEquals("192.168.0.103", InetAddress.getByAddress(ip).getHostAddress());
    }

    @Test
    void getCanonicalHostName() throws UnknownHostException {
        byte[] ip = {-64, -88, 0, 103};
        InetAddress inetAddress = InetAddress.getByAddress(ip);
        System.out.println("inetAddress.getHostAddress(): " + inetAddress.getHostAddress());
        System.out.println("inetAddress.getCanonicalHostName(): " + inetAddress.getCanonicalHostName());
    }

    @Test
    void endsWith() {
        String A = "a.google.com";
        assertTrue( A.endsWith("google.com") );
        assertTrue( A.endsWith("a.google.com"));
        assertTrue( A.endsWith("com"));
    }



}