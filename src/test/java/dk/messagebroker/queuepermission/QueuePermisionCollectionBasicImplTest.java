package dk.messagebroker.queuepermission;

import lombok.ToString;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.net.SocketPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

class QueuePermisionCollectionBasicImplTest {

    @ToString
    private class QueuePermissionImpl2 extends QueuePermission {
        public QueuePermissionImpl2(String resourceName, String actions) {
            super(resourceName, actions);
        }
        @Override
        String[] normalizeAction(String actionAsString) {
            return new String[0];
        }

        @Override
        public boolean implies(Permission permission) {
            return false;
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public String getActions() {
            return null;
        }

        @Override
        boolean impliesResourceName(String otherResourceName) {
            return false;
        }

        @Override
        boolean impliesActions(String actions) {
            return false;
        }
    }

    @Test
    void add() {
        QueuePermissionCollection queuePermissionCollection = new QueuePermisionCollectionBasicImpl();

        QueuePermission queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher, puller");
        queuePermissionCollection.add(queuePermission);
        assertEquals(1, queuePermissionCollection.getPermissionsByName("172.17.64.10").size());

        //  Попытка добавить объект класса, реализующий Permission, а не QueuePermission.
        //  Добавление в коллекцию не должно пройти, т.к. суперклассом для SocketPermission не является класс QueuePermission
        Permission socketPermission = new SocketPermission("172.17.64.10", "connect");
        queuePermissionCollection.add(socketPermission);
        assertEquals(1, queuePermissionCollection.getPermissionsByName("172.17.64.10").size());

        //  Добавление другой реализации абстрактного класса QueuePermission
        QueuePermission queuePermission2 = new QueuePermissionImpl2("172.17.64.10", "pusher");
        queuePermissionCollection.add(queuePermission2);
        assertEquals(2, queuePermissionCollection.getPermissionsByName("172.17.64.10").size());

        //  Добавляется разрешение, но ключ уже, используемый в карте для его хранения, будет другой...
        queuePermissionCollection.add(new QueuePermissionBasicImpl("172.17.64.11", "pusher"));
        assertEquals(1, queuePermissionCollection.getPermissionsByName("172.17.64.11").size());
    }


    @Test
    void implies() {
        //  Создаем объект разрешения, который будем использовать для создания коллекцию разрешений.
        //  Добавляем этот же обект разрешения в созданную коллекцию
        QueuePermission qp1 = new QueuePermissionBasicImpl("172.17.64.0/24", "pusher, puller");
        PermissionCollection permissionCollection = qp1.newPermissionCollection();
        permissionCollection.add(qp1);

        //  А подразумевается ли разрешение qp2 теми разрешениями, которые храняться в permissionCollection?
        QueuePermission qp2 = new QueuePermissionBasicImpl("172.17.64.100", "puller");
        assertTrue(permissionCollection.implies(qp2));

        //  Используем в качестве проверяемого разрешения другую реализацию QueuePermission
        QueuePermission qp3 = Mockito.mock(QueuePermissionImpl2.class);
        Mockito.when(qp3.getName()).thenReturn("172.17.64.100");
        Mockito.when(qp3.getActions()).thenReturn("puller");
        assertTrue(permissionCollection.implies(qp3));
        //  Делаем заглушку с именем ресурса который не подразумевается разрешениями коллекции
        Mockito.when(qp3.getName()).thenReturn("172.17.63.100");
        Mockito.when(qp3.getActions()).thenReturn("puller");
        assertFalse(permissionCollection.implies(qp3));

        //  Добавляем в коллецию разрешение с новым именем ресурса
        permissionCollection.add(new QueuePermissionBasicImpl("172.17.63.25", "puller"));
        Mockito.when(qp3.getName()).thenReturn("172.17.63.25");
        Mockito.when(qp3.getActions()).thenReturn("puller");
        assertTrue(permissionCollection.implies(qp3));
        //  Делаем заглушку с именем ресурса, которого нет в коллекции
        Mockito.when(qp3.getName()).thenReturn("172.17.63.24");
        Mockito.when(qp3.getActions()).thenReturn("puller");
        assertFalse(permissionCollection.implies(qp3));
        //  Делаем заглушку с действиями, которые превышают действия для имени в коллекции
        Mockito.when(qp3.getName()).thenReturn("172.17.63.25");
        Mockito.when(qp3.getActions()).thenReturn("puller,pusher");
        assertFalse(permissionCollection.implies(qp3));
    }


    @Test
    void elements() {

        QueuePermission qp1 = new QueuePermissionBasicImpl("172.17.64.0/24", "pusher");
        //  Нисходящее преобразование...
        //  Я точно знаю, что qp1 - это объект с типом QueuePermissionBasicImpl.
        //  Я точно знаю, что метод qp1.nrePermissionCollection() отдает объект реализующий абстрактный
        //  класс QueuePermissionCollection, а именно QueuePermissionCollectionbasicImpl.
        QueuePermissionCollection queuePermissionCollection = (QueuePermissionCollection) qp1.newPermissionCollection();

        queuePermissionCollection.add(qp1);
        queuePermissionCollection.add(new QueuePermissionBasicImpl("172.17.64.0/24", "pusher, puller"));
        queuePermissionCollection.add(new QueuePermissionBasicImpl("10.0.0.1", "puller"));

        //  Получаем объект перечисления на базе всех разрешений хранящихся в коллекции...
        Enumeration<Permission> enumQueuePermission = queuePermissionCollection.elements();
        //  В коллекции было три объекта QueuePermission.
        //  Соответственно, вычитка трех элементов должна пройти без проблем. Вычитка же четвертого, несуществующего, приведет к исключению.
        assertTrue(enumQueuePermission.hasMoreElements());
        assertDoesNotThrow(enumQueuePermission::nextElement);
        assertDoesNotThrow(enumQueuePermission::nextElement);
        assertDoesNotThrow(enumQueuePermission::nextElement);
        assertFalse(enumQueuePermission.hasMoreElements());
        assertThrows(NoSuchElementException.class, enumQueuePermission::nextElement);

    }


}