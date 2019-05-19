package edu.yuferov.chat.server.security;

public class SecurityContextHolder {
    private static final ThreadLocal<SecurityContext> CONTEXT = new ThreadLocal<>();

    public static SecurityContext getContext() {
        SecurityContext context = CONTEXT.get();
        if (context == null) {
            context = createContext();
            CONTEXT.set(context);
        }
        return context;
    }

    public static void clearContext() {
        CONTEXT.remove();
    }

    private static SecurityContext createContext() {
        return new SecurityContext();
    }
}
