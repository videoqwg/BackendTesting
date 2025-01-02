package com.example.myproject.Service;

import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import com.example.myproject.Repository.UserRepository;
import com.example.myproject.Util.JwtUtil;
import io.jsonwebtoken.Claims;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    private static final String UPLOAD_DIRECTORY = "D:/Desktop/vue/vue-admin-template-master/src/assets"; // 文件保存目录

    @Override
    public User findUser(String username) {
        User user = userRepository.findUser(username);
        return user;
    }

    @Override
    public Result register(String username, String password) {
        String encodedPassword = passwordEncoder.encode(password);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userRepository.addUser(user);
        return Result.success();
    }

    @Override
    public Result login(String userid, String password) {
        User user = userRepository.findUser(userid);
        if (user != null && passwordEncoder.matches(password, user.getPassword())) {
            List<String> roles = new ArrayList<>();
            roles.add(user.getRole());
            String token = jwtUtil.generateToken(user.getUserid(), user.getUsername(), user.getAvatar(), roles);
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
        String username = claims.get("userid", String.class);
        User user = findUser(username);
        List<String> roles = new ArrayList<>();
        roles.add(user.getRole());
        if (user != null) {
            info.put("username", user.getUsername());
            info.put("avatar", user.getAvatar());
            info.put("roles", roles);
            info.put("phone", user.getPhone());
            info.put("email", user.getEmail());
            info.put("introduction", user.getIntroduction());
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

    // 更新用户头像
    @Override
    public Result updateAvatar(String userid, MultipartFile avatar) {
        try {
            // 确保保存目录存在
            File uploadDir = new File(UPLOAD_DIRECTORY);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs(); // 创建保存目录
            }

            // 获取文件原始名，并构造保存路径
            String originalFilename = avatar.getOriginalFilename();
            String avtarFilename = "/api/user/getAvatar/" + originalFilename;
            File saveFile = new File(uploadDir, originalFilename);

            // 保存文件到指定路径
            avatar.transferTo(saveFile);
            userRepository.updateAvatar(userid, avtarFilename);
            Map<String, Object> avatarPath = new HashMap<>();
            avatarPath.put("avatar", avtarFilename);
            return Result.success(avatarPath);
        } catch (Exception e) {
            return Result.failure("上传失败");
        }
    }

    @Override
    public ResponseEntity<Resource> getAvatar(String filename) {
        try {
            Path filePath = Paths.get(UPLOAD_DIRECTORY).resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                    .body(resource);
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @Override
    public Result updateUserData(Map<String, String> userForm, User user) {
        String username = userForm.get("name");
        String phone = userForm.get("phone");
        String email = userForm.get("email");
        String introduction = userForm.get("introduction");
        try {
            userRepository.updateUserData(user.getUserid(), username, phone, email, introduction);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("修改失败");
        }
        return Result.success();
    }

    @Override
    public Result updateAccountData(Map<String, String> userForm, User user) {
        String username = userForm.get("username");
        if (userForm.containsKey("password")) {
            String password = userForm.get("password");
            String encodedPassword = passwordEncoder.encode(password);
            try {
                userRepository.updateUserName(user.getUserid(), username);
                userRepository.updateUserPassword(user.getUserid(), encodedPassword);
                return Result.success();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.failure("修改失败");
            }
        } else {
            try {
                userRepository.updateUserName(user.getUserid(), username);
                return Result.success();
            } catch (Exception e) {
                e.printStackTrace();
                return Result.failure("修改失败");
            }
        }
    }



}
