package cn.sdutcs.mqtt.panel.controller;

import cn.sdutcs.mqtt.panel.model.Result;
import cn.sdutcs.mqtt.panel.model.User;
import cn.sdutcs.mqtt.panel.service.UserService;
import cn.sdutcs.mqtt.panel.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/login")
    public Result login(@RequestBody User user) {
        System.out.println("user = " + user);
        User u = userService.getUserByUnameAndPwd(user.getUsername(), user.getPassword());
        System.out.println("u = " + u);
        if (u == null) {
            return Result.failure("账号密码错误");
        }
        String jwt = JwtUtil.createJWT(u.getUsername(), u.getEmail(), 120 * 60 * 1000);
        return Result.success(new User(jwt, u.getUsername(), u.getEmail(), u.getNickName()));
    }

    @PostMapping("/auth/register")
    public Result register(@RequestBody User user) {
        System.out.println("user = " + user);
        if (userService.registerNewUser(user)) {
            return Result.success("注册成功");
        }
        return Result.success("注册失败");
    }
}