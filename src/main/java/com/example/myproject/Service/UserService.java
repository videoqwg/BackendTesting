package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;

public interface UserService {
    public User findUser(String username);
    public Result register(String username, String password);
    public Result login(String username, String password);
}
