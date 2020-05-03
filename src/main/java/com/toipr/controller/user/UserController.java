package com.toipr.controller.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.model.user.UserInfo;
import com.toipr.service.user.UserServices;
import com.toipr.service.user.UserService;
import com.toipr.util.Utils;
import com.toipr.util.json.UserInfoHelper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/user")
public class UserController {
    @RequestMapping(value="regist", method= RequestMethod.POST)
    public String doUserRegist(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject ret = new JSONObject();
        ret.put("action", "regist");
        resp.setContentType("application/json;utf-8");

        UserInfo uobj = UserInfoHelper.fromJson(true, jsonText);
        if(uobj==null){
            ret.put("status", 400);
            ret.put("error", "User object is not right");
            return ret.toString();
        }

        UserService service = UserServices.getInstance();
        if(service.userExists(uobj.getUsername())!=null){
            ret.put("status", 400);
            ret.put("error", "username has existed");
            return ret.toString();
        }
        uobj.setIpAddr(req.getRemoteAddr());
        uobj.setLastIpAddr(uobj.getIpAddr());

        if(service.registUser(uobj)){
            ret.put("status", 200);
            ret.put("uid", uobj.getUid());
            ret.put("ipAddr", uobj.getIpAddr());
        } else {
            ret.put("status", 500);
            ret.put("error", "username regist failed");
        }
        return ret.toString();
    }

    @RequestMapping(value="login", method= RequestMethod.POST)
    public String doUserLogin(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session){
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "login");
        resp.setContentType("application/json;utf-8");

        String uname = json.getString("username");
        String upass = json.getString("password");
        if(Utils.isNullOrEmpty(uname) || Utils.isNullOrEmpty(upass)){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, username or password is not valid");
            return ret.toString();
        }

        UserService service = UserServices.getInstance();
        UserInfo user = service.checkUser(uname, upass);
        if(user==null){
            ret.put("status", 401);
            ret.put("error", "Invalid user, access denied");
        } else {
            service.incLoginAndLastAccess(user.getUid(), req.getRemoteAddr(), new Date());

            ret.put("status", 200);
            ret.put("uid", user.getUid());
            ret.put("oid", user.getOid());
            ret.put("nickname", user.getNickname());
            ret.put("username", user.getUsername());
        }

        /**
         * 单服务器方案，可以通过Redis缓存实现多服务器
         */
        session.setAttribute("user", user);
        return ret.toString();
    }

    @RequestMapping(value="logout", method= RequestMethod.POST)
    public String doUserLogout(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session){
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "logout");
        resp.setContentType("application/json;utf-8");

        String uid = json.getString("uid");
        if(uid==null || uid.length()<12){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, uid is not valid");
            return ret.toString();
        }

        UserInfo info = (UserInfo)session.getAttribute("user");
        if(info==null || uid.compareToIgnoreCase(info.getUid())!=0){
            ret.put("status", 401);
            ret.put("error", "Invalid parameter, uid is not valid");
            return ret.toString();
        }
        session.removeAttribute("user");
        ret.put("status", 200);
        return ret.toString();
    }

    @RequestMapping(path="get", method= RequestMethod.POST)
    public String doOrgGet(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception{
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "get");
        resp.setContentType("application/json;utf-8");

        String uid = pobj.getString("uid");
        if(Utils.isNullOrEmpty(uid)){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        UserService service = UserServices.getInstance();
        UserInfo obj = service.getUser(uid);
        if(obj!=null){
            ret.put("status", 200);
            ret.put("item", UserInfoHelper.fromObject(obj));
        } else {
            ret.put("status", 404);
            ret.put("error", "user object is not found");
        }
        return ret.toString();
    }

    @RequestMapping(path="state", method= RequestMethod.POST)
    public String doOrgState(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String uid = pobj.getString("uid");
        if(Utils.isNullOrEmpty(uid)){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        String str = pobj.getString("state");
        if(Utils.isNullOrEmpty(str)){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, state is null");
            return ret.toString();
        }

        int state = Integer.parseInt(str);
        UserService service = UserServices.getInstance();
        if(service.setState(uid, state)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
            ret.put("error", "org set state failed");
        }
        return ret.toString();
    }

    @RequestMapping(path="delete", method= RequestMethod.POST)
    public String doOrgDelete(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String uid = pobj.getString("uid");
        if(Utils.isNullOrEmpty(uid)){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        UserService service = UserServices.getInstance();
        if(service.removeUser(uid)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="list", method= RequestMethod.POST)
    public String doUserList(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception{
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "list");
        resp.setContentType("application/json;utf-8");

        Map<String, Object> params = new HashMap<String, Object>();
        Object objParam = Utils.getObject("username", String.class, pobj);
        if(objParam!=null){
            params.put("username", objParam);
        }
        objParam = Utils.getObject("org", String.class, pobj);
        if(objParam!=null){
            params.put("org", objParam);
        }
        objParam = Utils.getObject("phone", String.class, pobj);
        if(objParam!=null){
            params.put("phone", objParam);
        }
        objParam = Utils.getObject("email", String.class, pobj);
        if(objParam!=null){
            params.put("email", objParam);
        }
        objParam = Utils.getObject("oid", String.class, pobj);
        if(objParam!=null){
            params.put("oid", objParam);
        }
        objParam = Utils.getObject("state", String.class, pobj);
        if(objParam!=null){
            params.put("state", objParam);
        }
        int start = pobj.getInteger("start");
        int count = pobj.getInteger("count");

        UserService service = UserServices.getInstance();

        List<UserInfo> rlist = new ArrayList<UserInfo>();
        int total = service.getUserList(params, start, count, rlist);
        ret.put("total", total);
        ret.put("status", 200);
        if(total>0){
            JSONArray myarr = new JSONArray(total);
            for(UserInfo temp : rlist){
                myarr.add(UserInfoHelper.fromObject(temp));
            }
            ret.put("docs", myarr);
        }
        return ret.toString();
    }
}
