package edu.yuferov.chat.server.security;

import lombok.Getter;
import lombok.Setter;

public class SecurityContext {
    @Getter
    @Setter
    private UserPrincipal principal;

    public boolean isAuthenticated() {
        return principal != null;
    }
}
