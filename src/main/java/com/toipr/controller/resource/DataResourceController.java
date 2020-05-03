package com.toipr.controller.resource;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataResource;
import com.toipr.service.resource.ResourceService;
import com.toipr.service.resource.ResourceServices;
import com.toipr.util.Utils;
import com.toipr.util.json.DataResourceHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/resource")
public class DataResourceController {
    @RequestMapping(path="create", method= RequestMethod.POST)
    public String doResCreate(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject ret = new JSONObject();
        ret.put("action", "create");
        resp.setContentType("application/json;utf-8");

        String uid = "uidcce5905ffc25b0a9e8a0803d36ffee49";
        DataResource robj = DataResourceHelper.fromJson(true, jsonText);
        if(robj==null){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, resource object is not right");
            return ret.toString();
        }
        robj.setOid(uid);
        robj.setUidCreate(uid);
        robj.setIpAddr(req.getRemoteAddr());

        ResourceService service = ResourceServices.getInstance();
        if(service.addResource(robj)){
            ret.put("status", 200);
            ret.put("rid", robj.getRid());
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="list", method= RequestMethod.POST)
    public String doResList(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "list");
        resp.setContentType("application/json;utf-8");

        Map<String, Object> params = new HashMap<String, Object>();
        Object objRet = Utils.getObject("oid", String.class, pobj);
        if(objRet!=null) {
            params.put("oid", objRet);
        }
        objRet = Utils.getObject("resource", String.class, pobj);
        if(objRet!=null) {
            params.put("resource", objRet);
        }
        objRet = Utils.getObject("state", int.class, pobj);
        if(objRet!=null) {
            params.put("state", objRet);
        }
        int start = pobj.getInteger("start");
        int count = pobj.getInteger("count");

        ResourceService service = ResourceServices.getInstance();

        List<DataResource> rlist = new ArrayList<DataResource>();
        int total = service.getResourceList(params, start, count, rlist);
        ret.put("status", 200);
        ret.put("total", total);
        if(total>0){
            JSONArray myarr = new JSONArray();
            for(DataResource temp : rlist){
                myarr.add(DataResourceHelper.fromObject(temp));
            }
            ret.put("docs", myarr);
        }
        return ret.toString();
    }

    @RequestMapping(path="delete", method= RequestMethod.POST)
    public String doResDelete(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String rid = pobj.getString("rid");
        if(rid==null || rid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, rid is empty");
            return ret.toString();
        }

        String oid = "uidcce5905ffc25b0a9e8a0803d36ffee49";
        ResourceService service = ResourceServices.getInstance();
        if(service.removeResource(oid, rid)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="state", method= RequestMethod.POST)
    public String doResState(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "state");
        resp.setContentType("application/json;utf-8");

        String rid = pobj.getString("rid");
        if(rid==null || rid.length()==0){
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
        ResourceService service = ResourceServices.getInstance();
        if(service.setState(rid, state)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }
}
