package com.example.myproject.Service;

import com.example.myproject.Model.Friends;
import com.example.myproject.Model.Groups;
import com.example.myproject.Model.Result;
import com.example.myproject.Model.User;
import com.example.myproject.Repository.FriendsRepository;
import com.example.myproject.Repository.GroupsRepository;
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
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FriendsRepository friendsRepository;

    @Autowired
    private GroupsRepository groupsRepository;

    @Autowired
    private SequenceGeneratorService sequenceGeneratorService;

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
        Friends friends = new Friends(username);
        User user = new User();
        user.setUsername(username);
        user.setPassword(encodedPassword);
        userRepository.addUser(user);
        friendsRepository.save(friends);
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

    /*
    * 以下为好友相关操作

     */

    @Override
    public boolean validateUser(String friendId) {
        try {
            User user = userRepository.findUser(friendId);
            if (user == null) {
                return false;
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    @Override
    public Result getFriends(String userId) {
        try {
            List<User> friends = new ArrayList<>();
            List<Friends.Friend> friendList = friendsRepository.findByUserId(userId).getFriends();
            for (Friends.Friend friend : friendList) {
                User user = userRepository.findUser(friend.getFriendId());
                friends.add(user);
            }
            return Result.success(friends);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("获取好友列表失败");
        }
    }

    @Override
    public Result addFriend(String userId, String friendId) {
        try {
            Friends friends = friendsRepository.findByUserId(userId);
            if(!validateUser(friendId)){
                return Result.failure("用户不存在");
            }

            for (Friends.Friend friend : friends.getFriends()) {
                if (friend.getFriendId().equals(friendId)) {
                    if (friend.getStatus().equals("pending")) {
                        return Result.failure("已经发送过添加好友请求");
                    } else if (friend.getStatus().equals("accepted")) {
                        return Result.failure("已经添加该好友");
                    }
                }
            }

            Friends.Friend friend = new Friends.Friend();
            friend.setFriendId(friendId);
            friend.setStatus("pending");
            friend.setCreatedAt(LocalDateTime.now());

            friends.getFriends().add(friend);
            friendsRepository.save(friends);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("添加好友失败");
        }
    }

    // 更新好友状态
    public Result updateFriendStatus(String userId, String friendId, String status) {
        Friends Friends = friendsRepository.findByUserId(userId);
        if(!validateUser(friendId)){
            return Result.failure("用户不存在");
        }
        if (Friends != null) {
            if(Objects.equals(status, "deny")){
                return removeFriend(userId, friendId);
            }else {
                Friends.getFriends().stream()
                        .filter(friend -> friend.getFriendId().equals(friendId))
                        .findFirst()
                        .ifPresent(friend -> friend.setStatus(status));
                friendsRepository.save(Friends);
                return Result.success();
            }
        }
        return Result.failure("更新好友状态失败");
    }

    // 删除好友
    public Result removeFriend(String userId, String friendId) {
        Friends Friends = friendsRepository.findByUserId(userId);
        if(!validateUser(friendId)){
            return Result.failure("用户不存在");
        }
        if (Friends != null) {
            Friends.getFriends().removeIf(friend -> friend.getFriendId().equals(friendId));
            friendsRepository.save(Friends);
            return Result.success();
        }else {
            return Result.failure("删除好友失败");
        }
    }


    /*

    * 以下为群组相关操作

     */
    public Result initGroups(String userId, String groupName) {
        try {
            Groups groups = new Groups();

            groups.setGroupId(sequenceGeneratorService.getNextSequence("groupId"));
            groups.setUserId(userId);
            groups.setGroupName(groupName);
            groups.setMembers(new ArrayList<>());
            groups.setCreatedAt(LocalDateTime.now());

            groups.setMembers(new ArrayList<>());
            Groups.Member member = new Groups.Member();
            member.setUserId(userId);
            member.setRole("member");
            member.setStatus("pending");
            member.setJoinedAt(LocalDateTime.now());
            groups.getMembers().add(member);

            groupsRepository.save(groups);
            return Result.success(groups);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("初始化群组失败");
        }
    }
    
    // 添加群组成员
    public Result addMember(String groupId, String userId) {
        Groups group = groupsRepository.findByGroupId(groupId);
        if(!validateUser(userId)) {
            return Result.failure("用户不存在");
        }

        Groups.Member member = new Groups.Member();
        member.setUserId(userId);
        member.setRole("member");
        member.setStatus("pending");
        member.setJoinedAt(LocalDateTime.now());

        group.getMembers().add(member);
        groupsRepository.save(group);

        return Result.success();
    }

    // 删除群组成员
    public Result removeMember(String groupId, String userId) {
        Groups group = groupsRepository.findByGroupId(groupId);
        if(!validateUser(userId)) {
            return Result.failure("用户不存在");
        }
        if (group != null) {
            group.getMembers().removeIf(member -> member.getUserId().equals(userId));
            groupsRepository.save(group);
            return Result.success();
        }
        return Result.failure("删除群组成员失败");
    }

    // 更新群成员状态
    public Result updateMemberStatus(String groupId, String userId, String status) {
        Groups group = groupsRepository.findByGroupId(groupId);
        if(!validateUser(userId)) {
            return Result.failure("用户不存在");
        }
        if (group != null) {
            group.getMembers().stream()
                    .filter(member -> member.getUserId().equals(userId))
                    .findFirst()
                    .ifPresent(member -> member.setStatus(status));
            groupsRepository.save(group);
            return Result.success();
        }
        return Result.failure("更新群组成员状态失败");
    }

    // 更新群组成员角色
    public Result updateMemberRole(String groupId, String userId, String role) {
        Groups group = groupsRepository.findByGroupId(groupId);
        if(!validateUser(userId)) {
            return Result.failure("用户不存在");
        }
        if (group != null) {
            group.getMembers().stream()
                    .filter(member -> member.getUserId().equals(userId))
                    .findFirst()
                    .ifPresent(member -> member.setRole(role));
            groupsRepository.save(group);
            return Result.success();
        }
        return Result.failure("更新群组成员角色失败");
    }

    // 查询群组成员
    /*
    public Result getMembers(String groupId) {
        try {
            List<User> members = new ArrayList<>();
            List<Groups.Member> memberList = groupsRepository.findByGroupId(groupId).getMembers();
            for (Groups.Member member : memberList) {
                User user = userRepository.findUser(member.getUserId());
                members.add(user);
            }
            return Result.success(members);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("获取群组成员失败");
        }
    }
    */
    
    // 查询用户所在的群组
    public Result getGroups(String userId) {
        try {
            List<Groups> groups = groupsRepository.findByMembersUserId(userId);
            return Result.success(groups);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("获取群组列表失败");
        }
    }

    public Result updateGroupName(String groupId, String groupName) {
        try {
            Groups group = groupsRepository.findByGroupId(groupId);
            group.setGroupName(groupName);
            groupsRepository.save(group);
            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.failure("修改群组名称失败");
        }
    }

}
