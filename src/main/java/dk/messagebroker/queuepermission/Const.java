package dk.messagebroker.queuepermission;

class Const {
    static final String ACTION_DELIMITER = ",";

    /**
     * Регулярное выражение, описыввающее IP-адрес
     */
    static final String IP_REGEXP = "(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])";

    /**
     * Регулярное выражение, описывающее подсеть.
     * Пример: 172.17.64.0/24
     */
    static final String SUBNET_REGEXP = "(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])\\.(0|[1-9]\\d?|1\\d\\d|2[0-4]\\d|25[0-5])/(0|[1-2]\\d?|3[0-2]?)";

    /**
     * Регулярное выражение, описывающее доменное имя.
     *
     * Что реализовано:
     * Минимальная длина метки узла в DNS-дереве - 1 символ
     * Максимальная длина метки узла в DNS-дереве - 63 символа
     * Метка может начинаться и заканчиваться только буковой или цифрой
     * Внутри метки, помимо букв и цифр, могут быть тире
     *
     * Что не реализовано:
     * Максимальное количество уровней в DNS-дереве, т.е. меток - 127
     * Максимальная длина доменного имени - 255 символов
     */
    static final String DOMAINNAME_REGEXP = "^([a-zA-Z0-9]([a-zA-Z0-9\\-]{0,61}[a-zA-Z0-9])?\\.)+[a-zA-Z]{2,6}$";
}
