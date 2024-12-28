package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import com.example.myproject.Repository.UserRepository;
import com.example.myproject.Util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public User findUser(String username) {
        User user = userRepository.findUser(username);
        return user;
    }

    @Override
    public Result register(String username, String password) {
        System.out.println(password);
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userRepository.addUser(user);
        return Result.success();
    }

    @Override
    public Result login(String username, String password) {
        User user = userRepository.findUser(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            List<String> roles = new ArrayList<>();
            roles.add(user.getRole());
            String token = jwtUtil.generateToken(user.getUsername(),user.getAvatar(),roles);
            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            return Result.success(data);
        } else {
            return Result.failure("用户名或密码错误");
        }
    }

    @Override
    public Result info(String token) {
        Claims claims = jwtUtil.validateToken(token);
        Map<String, Object> info = new HashMap<>();
        String username = claims.get("username", String.class);
        User user = findUser(username);
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole());
        if (user != null) {
            info.put("username", user.getUsername());
            info.put("avatar", user.getAvatar());
            info.put("roles", roles);
            return Result.success(info);
        } else {
            return Result.failure("用户不存在");
        }
    }

    @Override
    public Result logout() {
        return Result.success();
    }

    @Override
    public Result getRoles(User user) {
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole());
        return Result.success(roles);
    }
}
