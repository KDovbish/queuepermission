package dk.messagebroker.queuepermission;

import lombok.ToString;

import java.security.Permission;
import java.util.*;

/**
 * Одна из возможных реализаций коллекции для хранения объектов, описывающих доступ к очереди.
 */
@ToString
public class QueuePermisionCollectionBasicImpl extends QueuePermissionCollection {

    private Map<String, List<QueuePermission>> innerCollection = new HashMap<>();


    //  Список, который будет использоваться в реализации интерфейса Enumeration и метода elements().
    //  Сначала в этот список, при вызове метода elements(), будут записаны все объекты из карты innerCollection.
    private List<QueuePermission> enumList = new ArrayList<>();
    //  Номер текущего элемента, который будет отдан методом nextElement() перечисления.
    private Integer enumPointer = 0;


    /*
    Реализация интерфейса Enumeraion, которая будет отдаваться методом elements()
     */
    private class InnerCollectionEnumeration implements Enumeration<Permission> {
        @Override
        public boolean hasMoreElements() {
            return (enumPointer <= enumList.size() - 1);
        }

        @Override
        public Permission nextElement() {
            if (hasMoreElements()) {
                return enumList.get(enumPointer++);
            } else {
                throw new NoSuchElementException();
            }
        }
    }


    /**
     * Добавление разрешения в коллекцию.
     * <br><br>
     * "...Subclass implementations of PermissionCollection should assume that they may be called simultaneously from
     * multiple threads, and therefore should be synchronized properly...". Именно в силу этих причин, этот метод оформлен
     * как synchronized.
     * @param permission the Permission object to add.
     */
    @Override
    public synchronized void add(Permission permission) {
        //  Пример: входит объект типа QueuePermissionBasicImpl; суперклассом данного класса является QueuePermission
        if (permission.getClass().getSuperclass().equals(QueuePermission.class)) {
            List<QueuePermission> permissionArray = innerCollection.get(permission.getName());
            if (permissionArray == null) {
                permissionArray = new ArrayList<>();
                innerCollection.put(permission.getName(), permissionArray);
            }
            //  Нисходящее преобразование типов. Я абсолютно точно знаю, что в этот метод заходит объект, тип которого наследует
            //  абстрактный класс QueuePermission.
            permissionArray.add((QueuePermission) permission);
        }
    }

    /**
     * Реализация функционала "подразумевает ли" для коллекции.
     * @param permission Объект разрешения, для которого выясняется, подразумевается ли это разрешение разрешениями
     *                   хранящимися в коллекции.
     * @return подразумевается(true)/не подразумевается(false)
     */
    @Override
    public boolean implies(Permission permission) {

        //  Не имеет смысл проводить какие либо проверки на предмет "подразумавает ли.." для разрешения permission, если
        //  класс времени выполнения, на который ссылается permission не порожден от абстрактного класса QueuePermission
        if (permission.getClass().getSuperclass().equals(QueuePermission.class) == false) return false;

        //  Получаем список всех разрешений для имени ресурса из permission
        List<QueuePermission> permissionList = innerCollection.get(permission.getName());
        if (permissionList != null) {
            //  Название ресурса в проверямом разрешении присутствует как ключ во внутренней коллекции.

            //  Если действия хотя бы одного разрешения из полученного списка, подразумевают действия проверяемого разрешения,
            //  значит можно считать, что содержимое коллекции подразумавает проверяемое разрешение
            if ( permissionList.stream().anyMatch(e -> e.impliesActions(permission.getActions())) ) {
                return true;
            }
        }

        //  В это точку выполнение алгоритма дойдет только в двух случаях:
        //  1.Во внутренней коллекции нет ключа с именем из проверяемого разрешения (permissionList будет null)
        //  2.Во внутренней коллекции есть ключ с именем из проверямого разрешения, но ни одно из разрешений, хранящихся в списке
        //    за этим ключом, не подразумевает проверяемое разрешение. Именно этот случай(когда permissionList не null) нужно проверять
        //    в дальнейшей логике...

        //  Получаю все ключи внутренней коллекции
        Set<String> keyList = innerCollection.keySet();
        for (String key: keyList) {
            //  Предполагается, что список разрешений из внутренней коллекции, закрепленный за именем из проверяемого разрешения(если он существовал),
            //  уже проверен на implies.
            //  Списки же разрешений за другими ключами проверям...
            if (key.equals(permission.getName())== false) {
                //  Получаем список разрешений за ключом, рассматриваемым в текущей иттерации цикла
                permissionList = innerCollection.get(key);
                //  Все разрешения в полученном списке хранят одно и то-же имя ресурса. Поэтому, нет необходимости делать проверку
                //  impliesResourceName() для каждого разрешения из списка. Достаточно сделать для одного. Например, для первого элемента списка.
                //  Перестраховываемся перед извлечением первого элемента списка и проверяем - а не пуст ли список вообще?
                if (permissionList.size() > 0) {
                    if (permissionList.get(0).impliesResourceName(permission.getName())) {
                        //  Итак...Имя проверяемого ресурса подразумевается в имени любого из разрешений анализируемого списка разрешений.
                        //  Применяем, поэтому, функционал "подразумевает ли?" уже для действий.
                        if ( permissionList.stream().anyMatch(e -> e.impliesActions(permission.getActions())) ) {
                            return true;
                        }
                    }
                }
            }
        }

        //  Самый крайний случай...
        //  Мы проверили абсолютно все разрешения из внутренней коллекции и не нашли ни одного, который бы подразумевал
        //  проверяемое разрешение.
        return false;
    }


    /**
     * Получить все элементы коллекции в виде объекта перечисления.
     * @return Элементы коллекции.
     */
    @Override
    public synchronized Enumeration<Permission> elements() {

        //  Оочистка списка по которому будет работать объект Enumeration
        //  и обнуление индекса текущего элемента для вычитки
        enumList.clear();
        enumPointer = 0;

        /*
        Все значения из внутренней карты innerCollection переносим в отдельную коллекцию enumList.
        Эта отдельная коллекция нужна для моей реализации интерфейса Enumeration.

        Взяли все значения из карты. Каждое значение - это список объектов QueuePermission.
        Превратили весь этот набор списков в стрим.
        Для каждого элемента стрима(т.е. для каждого списка) добавляем целиком этот список в отдельный список enumList.
         */
        innerCollection.values().stream().forEach(list -> enumList.addAll(list));

        //  Возвращаем объект, реализующий интерфейс Enumeration<Permission>
        return new InnerCollectionEnumeration();
    }

    @Override
    public List<QueuePermission> getPermissionsByName(String name) {
        return innerCollection.get(name);
    }


}
