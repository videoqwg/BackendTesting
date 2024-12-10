package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import com.example.myproject.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public User findUser(String username) {
        User user = userRepository.findUser(username);
        return user;
    }

    public Result register(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userRepository.addUser(user);
        return Result.success();
    }
}
