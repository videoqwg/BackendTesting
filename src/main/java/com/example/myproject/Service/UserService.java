package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface UserService {
    User findUser(String username);
    Result register(String username, String password);
    Result login(String username, String password);
    Result info(String token);
    Result logout();
    Result getRoles(User user);
    Result updateAvatar(String username, MultipartFile avatar);
    // Result getAvatar(User user);
    ResponseEntity<Resource> getAvatar(String filename);
    Result updateUserData(Map<String, String> userForm,User user);
    Result updateAccountData(Map<String, String> userForm,User user);
}
