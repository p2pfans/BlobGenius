package com.toipr.controller.node;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.model.node.DataNode;
import com.toipr.service.node.NodeService;
import com.toipr.service.node.NodeServices;
import com.toipr.util.Utils;
import com.toipr.util.json.DataNodeHelper;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/node")
public class NodeController {
    @RequestMapping(path="create", method= RequestMethod.POST)
    public String doHostCreate(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject ret = new JSONObject();
        ret.put("action", "create");
        resp.setContentType("application/json;utf-8");

        DataNode node = DataNodeHelper.fromJson(true, jsonText);
        if(node==null){
            ret.put("status", 400);
            ret.put("error", "parameter is not correct");
            return ret.toString();
        }

        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        String hid = service.nodeExists(node.getProtocol(), node.getDataType(), node.getHost());
        if(hid!=null){
            ret.put("status", 200);
            ret.put("hid", hid);
        } else {
            if (!service.addNode(node)) {
                ret.put("status", 500);
                ret.put("error", "add node failed");
            } else {
                ret.put("status", 200);
                ret.put("hid", node.getHid());
            }
        }
        return ret.toString();
    }

    @RequestMapping(path="test", method= RequestMethod.POST)
    public String doHostTest(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "test");
        resp.setContentType("application/json;utf-8");

        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        String retMsg = "";
        String hid = pobj.getString("hid");
        if(hid!=null && hid.length()>10){
            retMsg = service.testNode(hid);
        } else {
            DataNode node = DataNodeHelper.fromJson(true, pobj);
            if(node==null){
                ret.put("status", 400);
                ret.put("error", "parameter is not correct");
                return ret.toString();
            }
            retMsg = service.testNode(node);
        }
        if(retMsg.compareToIgnoreCase("success")==0){
            ret.put("status", 200);
        }
        ret.put("error", retMsg);
        return ret.toString();
    }

    @RequestMapping(path="delete", method= RequestMethod.POST)
    public String doHostDelete(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "delete");
        resp.setContentType("application/json;utf-8");

        String hid = pobj.getString("hid");
        if(hid==null || hid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, hid is null");
            return ret.toString();
        }

        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        if(service.removeNode(hid)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="get", method= RequestMethod.POST)
    public String doHostGet(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject) JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "get");
        resp.setContentType("application/json;utf-8");

        String hid = pobj.getString("hid");
        if (hid == null || hid.length() == 0) {
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, hid is null");
            return ret.toString();
        }

        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        DataNode node = service.getNode(hid);
        if(node==null){
            ret.put("status", 404);
            ret.put("error", "node is not found");
        } else {
            ret.put("status", 200);
            ret.put("item", DataNodeHelper.fromObject(node));
        }
        return ret.toString();
    }

    @RequestMapping(path="state", method= RequestMethod.POST)
    public String doHostState(@RequestBody String params, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(params);
        JSONObject ret = new JSONObject();
        ret.put("action", "state");
        resp.setContentType("application/json;utf-8");

        String hid = pobj.getString("hid");
        if(hid==null || hid.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, hid is null");
            return ret.toString();
        }

        String str = pobj.getString("state");
        if(str==null || str.length()==0){
            ret.put("status", 400);
            ret.put("error", "Invalid parameter, state is null");
            return ret.toString();
        }

        int state = Integer.parseInt(str);
        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        if(service.setState(hid, state)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
        }
        return ret.toString();
    }

    @RequestMapping(path="list", method= RequestMethod.POST)
    public String doHostList(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject pobj = (JSONObject)JSONObject.parse(jsonText);
        JSONObject ret = new JSONObject();
        ret.put("action", "list");

        NodeService service = NodeServices.getInstance();
        if(service==null){
            ret.put("status", 500);
            ret.put("error", "Node service is null");
            return ret.toString();
        }

        Map<String, Object> params = new HashMap<String, Object>();
        Object retObj = Utils.getObject("rid", String.class, pobj);
        if(retObj!=null){
            params.put("rid", retObj);
        }
        retObj = Utils.getObject("dataType", String.class, pobj);
        if(retObj!=null){
            params.put("dataType", retObj);
        }
        retObj = Utils.getObject("state", int.class, pobj);
        if(retObj!=null){
            params.put("state", retObj);
        }
        int start = pobj.getInteger("start");
        int count = pobj.getInteger("count");
        List<DataNode> nodeList = new ArrayList<DataNode>();
        int total = service.getNodeList(params, start, count, nodeList);

        ret.put("total", total);
        if(total>0){
            JSONArray myarr = new JSONArray();
            for(DataNode node : nodeList){
                myarr.add(DataNodeHelper.fromObject(node));
            }
            ret.put("docs", myarr);
        }
        resp.setContentType("application/json");
        ret.put("status", 200);
        return ret.toString();
    }
}
