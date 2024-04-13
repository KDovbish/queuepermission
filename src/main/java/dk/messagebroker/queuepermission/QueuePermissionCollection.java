package dk.messagebroker.queuepermission;

import java.security.PermissionCollection;
import java.util.List;

/**
 * Абстрактный класс, который должен использоваться для реализации коллекции, хранящей объекты разрешений к
 * очереди(т.е. объекты, реализующие абстрактный класс QueuePermission)
 */
public abstract class QueuePermissionCollection extends PermissionCollection {
    /**
     * Получить все разрешения из коллекции для заданного имени ресурса
     * @param name Имя ресурса
     * @return Список с объектами разрешений
     */
    public abstract List<QueuePermission> getPermissionsByName(String name);
}
