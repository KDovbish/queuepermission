package dk.messagebroker.queuepermission.service;

import org.apache.commons.net.util.SubnetUtils;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Одна из возможных реализаций сервисного класса для функционала "подразумевает ли"
 */
public class ImplyServiceBean implements ImplyService {
    @Override
    public boolean IPIP(String ipA, String ipB) {
        return ipB.equals(ipA);
    }

    @Override
    public boolean IPSubnet(String ip, String subnet) {
        return new SubnetUtils(subnet).getInfo().isInRange(ip);
    }

    @Override
    public boolean IPDomainName(String ip, String domainName) throws UnknownHostException {
        return DomainNameDomainName(InetAddress.getByName(ip).getCanonicalHostName(), domainName);
        //return domainName.equals(InetAddress.getByName(ip).getCanonicalHostName());
    }

    @Override
    public boolean SubnetSubnet(String subnetA, String subnetB) {
        return subnetB.equals(subnetA);
    }

    @Override
    public boolean DomainNameIP(String domainName, String ip) throws UnknownHostException {
        return ip.equals(InetAddress.getByName(domainName).getHostAddress());
    }

    @Override
    public boolean DomainNameSubnet(String domainName, String subnet) throws UnknownHostException {
        return IPSubnet( InetAddress.getByName(domainName).getHostAddress(), subnet );
    }

    @Override
    public boolean DomainNameDomainName(String domainNameA, String domainNameB) {
        return domainNameA.endsWith(domainNameB);
    }
}
