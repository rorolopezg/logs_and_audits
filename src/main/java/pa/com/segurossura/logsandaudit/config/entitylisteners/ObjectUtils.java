package pa.com.segurossura.logsandaudit.config.entitylisteners;

import java.util.Collection;

public class ObjectUtils {

    /**
     * Verifica si un objeto es null, o si es una List o Set vacía.
     *
     * @param obj El objeto a verificar.
     * @return true si el objeto es null, o una List vacía, o un Set vacío;
     * false en caso contrario.
     */
    public static boolean isNullOrEmptyCollection(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        return false;
    }
}