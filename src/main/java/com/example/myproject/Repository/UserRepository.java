package com.example.myproject.Repository;

import com.example.myproject.Model.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;


@Mapper
public interface UserRepository {
    //根据用户名查找用户
    @Select("select * from user where username=#{username}")
    public User findUser(String username);
    //插入用户信息，有用户名、密码、创建时间、更新时间
    @Insert("insert into user(username,password,created_at,updated_at) values(#{username},#{password},now(),now())")
    public void addUser(User user);
}
