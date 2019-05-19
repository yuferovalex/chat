package edu.yuferov.chat.server.controllers;

import edu.yuferov.chat.common.dto.requests.NamePasswordRequestParams;
import edu.yuferov.chat.common.dto.responses.SessionIdResponseBody;
import edu.yuferov.chat.server.controllers.meta.RequestBody;
import edu.yuferov.chat.server.controllers.meta.RequestMapping;
import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @RequestMapping("/login")
    public SessionIdResponseBody login(@RequestBody NamePasswordRequestParams params) throws Exception {
        if (params == null || params.getName() == null || params.getPassword() == null) {
            throw new IllegalArgumentException("parameters name and password can not be null");
        }
        userService.login(params.getName().toLowerCase(), params.getPassword());
        return new SessionIdResponseBody(SecurityContextHolder.getContext().getPrincipal().getSession().getId()
                .toString());
    }

    @RequestMapping("/register")
    public SessionIdResponseBody register(@RequestBody NamePasswordRequestParams params) throws Exception {
        if (params == null || params.getName() == null || params.getPassword() == null) {
            throw new IllegalArgumentException("parameters name and password can not be null");
        }
        userService.register(params.getName().toLowerCase(), params.getPassword());
        return new SessionIdResponseBody(SecurityContextHolder.getContext().getPrincipal().getSession().getId()
                .toString());
    }

    @RequestMapping("/exit")
    public void exit() {
        userService.exit();
    }
}
