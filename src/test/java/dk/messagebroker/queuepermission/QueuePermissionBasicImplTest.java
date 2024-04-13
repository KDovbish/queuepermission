package dk.messagebroker.queuepermission;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;

class QueuePermissionBasicImplTest {

    @Test
    void recognizeResourceType() {

        //  Создаем загрушку с целью тестирования исключительно одного, нужного нам метода...
        QueuePermissionBasicImpl queuePermission = Mockito.mock(QueuePermissionBasicImpl.class);
        Mockito.doCallRealMethod().when(queuePermission).recognizeResourceType(anyString());

        assertEquals(ResourceType.IP, queuePermission.recognizeResourceType("1.1.1.1"));
        assertEquals(ResourceType.SUBNET, queuePermission.recognizeResourceType("172.17.64.0/24"));
        assertEquals(ResourceType.DOMAINNAME, queuePermission.recognizeResourceType("google.com"));

    }

    @Test
    void normalizeAction() {
        //  Создаем загрушку с целью тестирования исключительно одного, нужного нам метода...
        QueuePermissionBasicImpl queuePermission = Mockito.mock(QueuePermissionBasicImpl.class);
        Mockito.doCallRealMethod().when(queuePermission).normalizeAction(anyString());

        String[] actionAsArray = {"pusher", "puller"};
        assertArrayEquals(actionAsArray, queuePermission.normalizeAction("pusher,puller"));
        assertArrayEquals(actionAsArray, queuePermission.normalizeAction("pusher,unknown,puller"));
        assertArrayEquals(actionAsArray, queuePermission.normalizeAction("pusher,    unknown, puller"));

        //  В данном случае мы имеем массивы одинакового содержания.
        //  Но сравниваются они не как массивы, а как объекты.
        //  Любые массивы наследуют Object. Соответственно, для сравнения используется метод equals() класса Object.
        //  Т.е. идет сравнение ссылок. Более точно, адресов объектов. А адреса, естественно, разные.
        assertNotEquals(actionAsArray, queuePermission.normalizeAction("pusher,puller"));

        actionAsArray = new String[1]; actionAsArray[0] = "pusher";
        assertArrayEquals(actionAsArray, queuePermission.normalizeAction("pusher"));

    }

    @Test
    void impliesResourceName() {

        /*
        В конструкторе класса QueuePermission используется метод normalizeAction().
        В конструкторе класса QueuePermissionBasicImpl используется метод recognizeResourceType().
        Оба эти методы протестированы. Следовательно, для дальнейшего тестирования уже можно просто создавать объекта типа
        QueuePermissionBasicImpl, а не заглушки.
         */

        QueuePermissionBasicImpl queuePermission = new QueuePermissionBasicImpl("1.1.1.1", "pusher,puller");
        assertTrue(queuePermission.impliesResourceName("1.1.1.1"));

        queuePermission = new QueuePermissionBasicImpl("172.17.64.0/24", "pusher,puller");
        assertTrue(queuePermission.impliesResourceName("172.17.64.1"));
        assertFalse(queuePermission.impliesResourceName("172.17.63.1"));

        /*
        Подобные проверки могут не проходить на некоторых рабочих станциях. Поэтому, комментирую...

        queuePermission = new QueuePermissionBasicImpl("bud02s39-in-f14.1e100.net", "pusher,puller");
        assertTrue(queuePermission.impliesResourceName("142.251.39.78"));

        queuePermission = new QueuePermissionBasicImpl("142.251.39.78", "pusher,puller");
        assertTrue(queuePermission.impliesResourceName("bud02s39-in-f14.1e100.net"));

        queuePermission = new QueuePermissionBasicImpl("1e100.net", "pusher,puller");
        assertTrue(queuePermission.impliesResourceName("bud02s39-in-f14.1e100.net"));
        assertFalse(queuePermission.impliesResourceName("bud02s39-in-f14.net"));
         */
    }


    @Test
    void impiesActions() {

        QueuePermission queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher, puller, waiter");
        //  В объекте queuePermission будут храниться только два действия - pusher и puller. Действие waiter будет
        //  отброшено, поскольку его нет в перечислении допустимых действий ActionType

        assertTrue(queuePermission.impliesActions("pusher,puller"));
        assertFalse(queuePermission.impliesActions("waiter"));

        queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "puller");

        assertFalse(queuePermission.impliesActions("pusher,puller"));

    }

    @Test
    void implies() {

        QueuePermission queuePermission, otherPermission;

        queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher, puller");
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher");
        assertTrue(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.64.11", "pusher");
        assertFalse(queuePermission.implies(otherPermission));

        queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher");
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher,puller");
        assertFalse(queuePermission.implies(otherPermission));

        queuePermission = new QueuePermissionBasicImpl("172.17.64.0/24", "pusher");
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher");
        assertTrue(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.63.10", "pusher");
        assertFalse(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.63.10", "unknown");
        assertFalse(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.63.10", "");
        assertFalse(queuePermission.implies(otherPermission));

        /*
        Подобные проверки могут не проходить на некоторых рабочих станциях. Поэтому, комментирую...

        queuePermission = new QueuePermissionBasicImpl("1e100.net", "pusher");
        otherPermission = new QueuePermissionBasicImpl("142.251.39.78", "pusher");
        assertTrue(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("bud02s39-in-f14.1e100.net", "pusher");
        assertTrue(queuePermission.implies(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("bud02s39-in-f14.1e100.net", "puller");
        assertFalse(queuePermission.implies(otherPermission));
         */

    }


    @Test
    void equals() {
        QueuePermission queuePermission, otherPermission;

        queuePermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher, puller");
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "puller,pusher");
        assertTrue(queuePermission.equals(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "puller");
        assertFalse(queuePermission.equals(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.64.11", "pusher, puller");
        assertFalse(queuePermission.equals(otherPermission));

        queuePermission = new QueuePermissionBasicImpl("172.17.64.0/24", "pusher, puller");
        otherPermission = new QueuePermissionBasicImpl("172.17.64.10", "pusher, puller");
        assertFalse(queuePermission.equals(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.64.0/24", "puller,    pusher");
        assertTrue(queuePermission.equals(otherPermission));
        otherPermission = new QueuePermissionBasicImpl("172.17.64.0/24", "puller");
        assertFalse(queuePermission.equals(otherPermission));
    }

}