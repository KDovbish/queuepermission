package dk.messagebroker.queuepermission;

/**
 * Допустимые типы ресурсов.
 */
enum ResourceType { /** IP-адрес */ IP,
                    /** Подсеть. <br>Пример формата, который должен быть использован: 172.17.64.0/24 */ SUBNET,
                    /** Доменное имя */ DOMAINNAME}
