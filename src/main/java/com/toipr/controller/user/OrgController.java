package com.toipr.controller.user;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.model.user.OrgInfo;
import com.toipr.service.org.OrgService;
import com.toipr.service.org.OrgServices;
import com.toipr.util.json.OrgInfoHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/org")
public class OrgController {
    @RequestMapping(path="create", method= RequestMethod.POST)
    public String doOrgCreate(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception{
        JSONObject ret = new JSONObject();
        ret.put("action", "create");
        resp.setContentType("application/json;utf-8");

        String uid = "uidcce5905ffc25b0a9e8a0803d36ffee49";
        OrgInfo org = OrgInfoHelper.fromJson(true, jsonText);
        if(org==null){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, org object is not right");
            return ret.toString();
        }
        org.setIpAddr(req.getRemoteAddr());
        org.setUidAdmin(uid);
        org.setUidCreate(uid);

        OrgService service = OrgServices.getInstance();
        if(service.addOrg(org)){
            ret.put("status", 200);
            ret.put("oid", org.getOid());
        } else {
            ret.put("status", 500);
            ret.put("error", "add org failed");
        }
        return ret.toString();
    }

    @RequestMapping(path="get", method= RequestMethod.POST)
    public String doOrgGet(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception{
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "get");
        resp.setContentType("application/json;utf-8");

        String oid = pobj.getString("oid");
        if(oid==null || oid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        OrgService service = OrgServices.getInstance();
        OrgInfo obj = service.getOrg(oid);
        if(obj!=null){
            ret.put("status", 200);
            ret.put("item", OrgInfoHelper.fromObject(obj));
        } else {
            ret.put("status", 404);
            ret.put("error", "org object is not found");
        }
        return ret.toString();
    }

    @RequestMapping(path="state", method= RequestMethod.POST)
    public String doOrgState(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String oid = pobj.getString("oid");
        if(oid==null || oid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        String str = pobj.getString("state");
        if(str==null || str.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, state is null");
            return ret.toString();
        }

        int state = Integer.parseInt(str);
        OrgService service = OrgServices.getInstance();
        if(service.setState(oid, state)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
            ret.put("error", "org set state failed");
        }
        return ret.toString();
    }

    @RequestMapping(path="delete", method= RequestMethod.POST)
    public String doOrgDelete(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String oid = pobj.getString("oid");
        if(oid==null || oid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        OrgService service = OrgServices.getInstance();
        if(service.removeOrg(oid)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="list", method= RequestMethod.POST)
    public String doOrgList(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception{
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "list");
        resp.setContentType("application/json;utf-8");

        Map<String, Object> params = new HashMap<String, Object>();
        if(pobj.containsKey("name")){
            params.put("name", pobj.getString("name"));
        }
        if(pobj.containsKey("contact")){
            params.put("contact", pobj.getString("contact"));
        }
        if(pobj.containsKey("pid")){
            params.put("pid", pobj.getString("pid"));
        }
        if(pobj.containsKey("state")){
            params.put("state", pobj.getInteger("state"));
        }

        int start = pobj.getInteger("start");
        int count = pobj.getInteger("count");

        OrgService service = OrgServices.getInstance();

        List<OrgInfo> rlist = new ArrayList<OrgInfo>();
        int total = service.getOrgList(params, start, count, rlist);
        ret.put("total", total);
        ret.put("status", 200);
        if(total>0){
            JSONArray myarr = new JSONArray(total);
            for(OrgInfo temp : rlist){
                myarr.add(OrgInfoHelper.fromObject(temp));
            }
            ret.put("docs", myarr);
        }
        return ret.toString();
    }
}
