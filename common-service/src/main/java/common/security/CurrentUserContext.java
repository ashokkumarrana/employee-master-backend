package common.security;

public class CurrentUserContext {

    private static final ThreadLocal<Long> CURRENT_USER_ID = new ThreadLocal<>();

    public static void setUserId(Long userId) {
        CURRENT_USER_ID.set(userId);
    }

    public static void clear() {
        CURRENT_USER_ID.remove();
    }
}

