package edu.yuferov.chat.server.services;

import edu.yuferov.chat.common.Utils;
import edu.yuferov.chat.server.security.SecurityContext;
import edu.yuferov.chat.server.security.SecurityContextHolder;
import edu.yuferov.chat.server.domain.User;
import edu.yuferov.chat.server.services.exceptions.ExistsAlreadyException;
import edu.yuferov.chat.server.services.exceptions.NotFoundException;
import edu.yuferov.chat.server.services.exceptions.ServiceException;
import edu.yuferov.chat.server.services.exceptions.WrongPasswordException;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Service
public class UserService extends AbstractService {
    @Autowired
    private SessionService sessionService;

    public void login(String name, String password) throws ServiceException {
        checkUserNotAuthenticated();
        checkCredentials(name, password);
        User user = userRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("user not found"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new WrongPasswordException("wrong password");
        }
        sessionService.create(user);
        notifyAboutNewUser(user);
    }

    public void register(String name, String password) throws ServiceException {
        checkUserNotAuthenticated();
        checkCredentials(name, password);
        User user = new User(name, passwordEncoder.encode(password), getDefaultRoom());
        try {
            userRepository.save(user);
        } catch (DataIntegrityViolationException e) {
            throw new ExistsAlreadyException("user with name " + name + " exists already");
        }
        sessionService.create(user);
        notifyAboutNewUser(user);
    }

    public void exit() {
        SecurityContext context = SecurityContextHolder.getContext();
        if (context.isAuthenticated()) {
            notifyUserExited(context.getPrincipal().getUser());
        }
        sessionService.exit();
    }

    private void checkCredentials(String name, String password) {
        Utils.validateUserName(name);
        Utils.validateUserPassword(password);
    }

    private void notifyAboutNewUser(User user) {
        String msgBody = String.format("user %s connected", user.getName());
        notify(user.getRoom(), msgBody);
    }

    private void notifyUserExited(User user) {
        String msgBody = String.format("user %s disconnected", user.getName());
        notify(user.getRoom(), msgBody);
    }
}
