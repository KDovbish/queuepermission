package dk.messagebroker.queuepermission;

import dk.messagebroker.queuepermission.service.ImplyService;
import dk.messagebroker.queuepermission.service.ImplyServiceBean;
import lombok.ToString;

import java.net.UnknownHostException;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Одна из возможных реализаций доступа к очереди.
 * <br><br>
 * Типы ресурсов, которые распознаются данной реализацией: IP-адрес, Подсеть, Доменное имя.<br>
 * Виды действий, которые распознаются данной реализацией: "pusher", "puller".<br>
 */
@ToString
public class QueuePermissionBasicImpl extends QueuePermission {

    //  Тип ресурса
    //  Распознается в конструкторе методом recognizeResourceType()
    //  Дефолтное значение - null
    private ResourceType resourceType;

    private ImplyService implyService = new ImplyServiceBean();


    public QueuePermissionBasicImpl(String resourceName, String actionAsString) {
        super(resourceName, actionAsString);
        this.resourceType = recognizeResourceType(resourceName);
    }


    /**
     * Нормализация передаваемого в класс списка действий в виде строки с разделителями во внутренний массив строк.
     * <br><br>
     * Допустимые типы действий указаны в перечислении {@link ActionType}.
     * В качестве разделителся должен использоваться разделитель, укзанный в {@link Const#ACTION_DELIMITER}.
     * @param action Список действий в виде строки. Пример: "pusher, puller"
     * @return Список действий в виде массива. Пример: {"pusher", "puller"}. В массив могут попасть только действия, присутствующие
     * в перечислении {@link ActionType}.
     */
    @Override
    String[] normalizeAction(String action) {
        return Arrays.stream(action.split(Const.ACTION_DELIMITER))
                .map(userAction -> userAction.trim().toLowerCase())
                .filter( userAction -> Arrays.stream(ActionType.values()).map(e->e.name()).anyMatch(e -> e.equals(userAction)) )
                .distinct()
                .collect(Collectors.toList())
                .toArray(new String[0]);
    }


    /**
     * Проверка того, подразумевается ли разрешение, переданное как параметр, текущим разрешением.
     * <br><br>
     * Осуществляется проверка следующих моментов:
     * <ul>
     *     <li>Входной объект имеет тот же тип времени выполнения, что и текущий объект. Т.е. тип QueuePermisiion</li>
     *     <li>IP-адрес ресурса, хранящийся во входном объекте, эквивалентен IP-адресу, хранящемуся в текущем объекте</li>
     *     <li>Все действия входного объекта присутствуют в действиях текущего объекта</li>
     * </ul>
     * @param permission the permission to check against
     * @return true/false
     */
    @Override
    public boolean implies(Permission permission) {
        boolean bImplies = false;
        if (permission.getClass().equals(this.getClass())) {
            if (impliesResourceName(permission.getName())) {
                return impliesActions(permission.getActions());
            }
        }
        return bImplies;
    }


    /**
     * Проверка на эквивалентность.
     * <br><br>
     * Объекты типа QueuePermission являются эквивалентными, если эквивалентны имена ресурсов в этих объектах, количество действий
     * идентично и все действия одного объекта присутствуют в другом объекте. Порядок хранения дейсвия может быть разным.
     * @param obj the object we are testing for equality with this object.
     * @return true/false
     */
    @Override
    public boolean equals(Object obj) {
        boolean bEquals = false;
        if (this.getClass().equals(obj.getClass())) {
            QueuePermissionBasicImpl otherObj = (QueuePermissionBasicImpl) obj;
            if (this.getName().equals(otherObj.getName())) {
                String[] otherObjAction = otherObj.getActions().split(Const.ACTION_DELIMITER);
                //  Очень важно! При проверке эквивалентности нужно сначала обязательно сравнить длины массивов.
                //  И только потом, сами действия.
                //  Т.е. действия двух объектов должны быть полностью одинаковы. Это и даст эквивалентность, в отличии от проверки "imply", где
                //  идентичность размеров массивов не важна, а важна только полная включнность действий "другого" объекта в массив действий текущего.
                if (getActionAsArray().length == otherObjAction.length) {
                    bEquals = List.of(getActionAsArray()).containsAll( List.of(otherObjAction) );
                }
            }
        }
        return bEquals;
    }


    /**
     * Получить хэш-код объекта.
     * <br><br>
     * Вычисляется как хэшкод строки, состоящей из имени ресурса + все действия в отсортированном порядке
     * @return хэш-код
     */
    @Override
    public int hashCode() {
        return (getName() + Arrays.stream(getActionAsArray()).sorted().reduce("", (s1, s2) -> (s1 + s2))).hashCode();
    }


    /**
     * Получить массив действий, хранящийся в объекте
     * @return
     */
    @Override
    public String getActions() {
        return String.join(Const.ACTION_DELIMITER, getActionAsArray());
    }


    @Override
    public PermissionCollection newPermissionCollection() {
        return new QueuePermisionCollectionBasicImpl();
    }


    /**
     * Распознать тип ресурса
     * @param resourceName Имя ресурса
     * @return Или IP или Подсеть или Имя хоста; или null, если тип ресурса не распознан
     */
    ResourceType recognizeResourceType(String resourceName) {
        if (resourceName.matches(Const.IP_REGEXP)) return ResourceType.IP;
        if (resourceName.matches(Const.SUBNET_REGEXP)) return ResourceType.SUBNET;
        if (resourceName.matches(Const.DOMAINNAME_REGEXP)) return ResourceType.DOMAINNAME;
        return null;
    }


    /**
     * Получить тип ресурса, который прописан в объекте
     * @return Один из типов ресурсов, прописанных в перичеслении {@link ActionType}
     */
    ResourceType getResourceType() {
        return this.resourceType;
    }


    /**
     * Подразумевает ли имя ресурса, передаваемое в метод, что речь идет о ресурсе, описанном в текущем объекте.
     * <br><br>
     * Именем ресурса может быть IP.
     * Именем ресурса может быть доменное имя.
     * Именем ресурса может быть подсеть.
     * @param otherResourceName Имя ресурса для проверки
     * @return true/false (подразумевает/не подразумевает)
     */
    @Override
    boolean impliesResourceName(String otherResourceName) {

        if (getResourceType() == null) return false;

        ResourceType otherResourceNameType = recognizeResourceType(otherResourceName);
        if (otherResourceNameType == null) return false;

        if (otherResourceNameType == ResourceType.IP) {
            switch (getResourceType()) {
                case IP: return implyService.IPIP(otherResourceName, this.getName());
                case SUBNET: return implyService.IPSubnet(otherResourceName, this.getName());
                case DOMAINNAME:
                    try {
                        return implyService.IPDomainName(otherResourceName, this.getName());
                    } catch (UnknownHostException e) {
                        return false;
                    }
            }
        }

        if (otherResourceNameType == ResourceType.SUBNET) {
            switch (getResourceType()) {
                case IP: return false;
                case SUBNET: return implyService.SubnetSubnet(otherResourceName, this.getName());
                case DOMAINNAME: return false;
            }
        }

        if (otherResourceNameType == ResourceType.DOMAINNAME) {
            switch (getResourceType()) {
                case IP:
                    try {
                        return implyService.DomainNameIP(otherResourceName, this.getName());
                    } catch (UnknownHostException e) {
                        return false;
                    }
                case SUBNET:
                    try {
                        return implyService.DomainNameSubnet(otherResourceName, this.getName());
                    } catch (UnknownHostException e) {
                        return false;
                    }
                case DOMAINNAME: return implyService.DomainNameDomainName(otherResourceName, this.getName());
            }
        }

        //  В эту точку алгоритм никогда не дойдет. Но java этого не знает и требует сделать возврат значения
        //  без каких либо условий. А то, что условия выше, предусматривают все возможные варианты, ее это не интересует.
        return false;
    }


    /**
     * Подразумевают ли действия, хранящиеся в объекте, действия, передаваемые в метод.
     * @param actions Список действий для проверки
     * @return true(подразумевают)/false(не подразумевают)
     */
    @Override
    boolean impliesActions(String actions) {
        return List.of(getActionAsArray()).containsAll( List.of(actions.split(Const.ACTION_DELIMITER)) );
    }


}
