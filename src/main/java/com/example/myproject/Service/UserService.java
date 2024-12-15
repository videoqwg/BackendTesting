package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;

public interface UserService {
    User findUser(String username);
    Result register(String username, String password);
    Result login(String username, String password);
    Result info(String token);
    Result logout();
}
