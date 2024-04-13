package dk.messagebroker.queuepermission.service;

import java.net.UnknownHostException;

/**
 * Интерфейс для сервисного класса, хранящего методы для реализации функционала "подразумавает ли"
 */
public interface ImplyService {
    /**
     * Сравнить два IP-адреса
     * @param ipA
     * @param ipB
     * @return true(адреса совпадают)/false(адреса не сопадают)
     */
    boolean IPIP(String ipA, String ipB);

    /**
     * Входит ли IP-адрес в подсеть
     * @param ip
     * @param subnet
     * @return true(входит)/false(не входит)
     */
    boolean IPSubnet(String ip, String subnet);

    /**
     * Сравнить два доменных имени, после преобразования IP в доменное имя
     * @param ip
     * @param domainName
     * @return true(доменное имя из IP входит(или полностью совпадает) в domainName)/false(доменное имя из IP не подразумевается в domainName)
     */
    boolean IPDomainName(String ip, String domainName) throws UnknownHostException;

    /**
     * Сравнить две подсети
     * @param subnetA
     * @param subnetB
     * @return true(подсети совпадают)/false(подсети не совпадают)
     */
    boolean SubnetSubnet(String subnetA, String subnetB);

    /**
     * Сравнить два IP-адреса, после преобразования доменного имени в IP
     * @param domainName
     * @param ip
     * @return true(IP-адрес полученный после разрешения domainName совпадает с ip)/false(IP-адрес полученный после разрешения domainName не совпадает с ip)
     */
    boolean DomainNameIP(String domainName, String ip) throws UnknownHostException;

    /**
     * Проверить, входит ли IP, полученное после разрешения доменного имени в подсеть
     * @param domainName
     * @param subnet
     * @return true(входит)/false(не входит)
     */
    boolean DomainNameSubnet(String domainName, String subnet) throws UnknownHostException;

    /**
     * Проверить, входит(или полностью соответствует) ли один домен в другой
     * @param domainNameA
     * @param domainNameB
     * @return true(domainNameA является поддоменом или полностью соответствует domainNameB)/false(domainNameA никаким образом не связан с domainNameB)
     */
    boolean DomainNameDomainName(String domainNameA, String domainNameB);

}
