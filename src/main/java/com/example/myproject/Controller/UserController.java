package com.example.myproject.Controller;

import com.example.myproject.Model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.myproject.Service.UserService;

@RestController
@RequestMapping("/user")
// 实现用户登录及用户注册功能
public class UserController {
    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result register(String username, String password) {
        //先查询用户是否存在，如果存在则返回错误信息，不存在则注册
        if (userService.findUser(username) != null) {
            return Result.failure("用户名已存在");
        }else {
            userService.register(username, password);
            return Result.success();
        }
    }


}
