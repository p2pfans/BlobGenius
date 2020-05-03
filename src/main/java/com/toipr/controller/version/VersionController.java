package com.toipr.controller.version;

import com.alibaba.fastjson.JSONObject;
import com.toipr.util.Utils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@RestController
@RequestMapping("/version")
public class VersionController {
    @RequestMapping("create")
    protected String doProjectCreate(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        return ret.toString();
    }

    @RequestMapping("delete")
    protected String doProjectDelete(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        return ret.toString();
    }

    @RequestMapping("lock")
    protected String doProjectLock(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        return ret.toString();
    }

    @RequestMapping("unlock")
    protected String doProjectUnlock(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        return ret.toString();
    }
}
