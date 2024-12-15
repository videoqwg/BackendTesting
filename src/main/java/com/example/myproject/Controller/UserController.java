package com.example.myproject.Controller;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.example.myproject.Service.UserService;
import com.example.myproject.Util.JwtUtil;


@RestController
@RequestMapping("/api/user")
// 实现用户登录及用户注册功能
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public Result register(@RequestBody User user) {
        //先查询用户是否存在，如果存在则返回错误信息，不存在则注册
        if (userService.findUser(user.getUsername()) != null) {
            return Result.failure("用户名已存在");
        } else {
            return userService.register(user.getUsername(), user.getPassword());
        }
    }

    @PostMapping("/login")
    public Result login(@RequestBody User user) {
        if (userService.findUser(user.getUsername()) == null) {
            return Result.failure("用户名不存在");
        } else {
            return userService.login(user.getUsername(), user.getPassword());
        }
    }

    @GetMapping("/info")
    public Result info(@RequestParam("token") String token) {
        return userService.info(token);
    }

    @PostMapping("/logout")
    public Result logout() {
        return userService.logout();
    }

    @PostMapping("/test")
    public String test() {
        return "test";
    }

}
