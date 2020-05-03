package com.toipr.controller.data;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataObject;
import com.toipr.model.user.UserInfo;
import com.toipr.service.data.DataStoreService;
import com.toipr.service.data.DataStores;
import com.toipr.service.token.TokenService;
import com.toipr.service.token.TokenServices;
import com.toipr.util.HashHelper;
import com.toipr.util.Utils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/data/upload")
public class DataUploadController {
    @PostMapping("file/{rid}")
    public String doFileUpload(@PathVariable("rid") String rid, HttpServletRequest req, HttpServletResponse resp, HttpSession ses) throws Exception {
        return doFileUpload2(rid, null, req, resp, ses);
    }

    @PostMapping("file/{rid}/{pid}")
    public String doFileUpload2(@PathVariable("rid") String rid, @PathVariable("pid") String pid, HttpServletRequest req, HttpServletResponse resp, HttpSession ses) throws Exception {
        JSONObject json = new JSONObject();
        resp.setContentType("application/json;charset=utf-8");

        String sType = req.getContentType();
        if(sType.indexOf("multipart")<0){
            json.put("status", 400);
            json.put("error", "file upload only support multipart/form-data");
            return json.toString();
        }

        Collection<Part> plist = req.getParts();
        if(plist==null || plist.size()==0){
            json.put("status", 400);
            json.put("error", "no file selected");
            return json.toString();
        }

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            json.put("status", 401);
            json.put("error", "access denied");
            return json.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("upload", token, rid, pid, ipAddr, rights)){
            json.put("status", 401);
            json.put("error", "access denied");
            return json.toString();
        }

        String oid = (String)rights.get("oid");//"DUrI9FMi";
        String uid = (String)rights.get("uid");//"IRX8qYa3";

        DataStoreService store = DataStores.getInstance();
        JSONArray myarr = new JSONArray();
        for(Part part : plist){
            sType = part.getContentType();
            if(sType==null || sType.length()==0){
                continue;
            }

            JSONObject file = new JSONObject();
            file.put("name", part.getSubmittedFileName());
            file.put("size", part.getSize());
            file.put("type", part.getContentType());

            DataObject dobj = getDataObject(rid, uid, oid, pid, ipAddr, part);
            if(store.storeObject(part.getInputStream(), dobj)){
                file.put("status", 200);
                file.put("hash", dobj.getHash());
                file.put("doid", dobj.getDoid());
            } else {
                file.put("status", 500);
                file.put("error", "file upload failed");
            }
            myarr.add(file);
        }
        json.put("status", 200);
        json.put("files", myarr);
        return json.toString();
    }

    @PostMapping("blob")
    public String doFileBlob(HttpServletRequest req, HttpServletResponse resp, HttpSession ses) throws Exception {
        JSONObject json = new JSONObject();
        resp.setContentType("application/json;charset=utf-8");

        Collection<Part> plist = req.getParts();
        if(plist==null || plist.size()==0){
            json.put("status", 400);
            json.put("error", "no file selected");
            return json.toString();
        }

        byte[] data = null;
        Map<String, String> params = new HashMap<String ,String>(32);
        for(Part part : plist){
            String name = part.getName();
            Object value = Utils.getMutltiPart(part.getInputStream(), part.getContentType());
            if(value instanceof String) {
                params.put(name, (String)value);
            } else {
                data = (byte[])value;
            }
        }
        if(data==null || !params.containsKey("uuid") || !params.containsKey("rid") || !params.containsKey("index")){
            json.put("status", 400);
            json.put("error", "invalid parameters, uuid or rid or data can not be empty");
            return json.toString();
        }

        int index = 0, size = 0;
        if(params.containsKey("size")){
            size = Integer.parseInt(params.get("size"));
        }
        if(size!=data.length){
            json.put("status", 400);
            json.put("error", "invalid parameters, size is not right");
            return json.toString();
        }
        if(params.containsKey("index")){
            index = Integer.parseInt(params.get("index"));
        }

        int flags = 0;
        if(params.containsKey("flags")){
            flags = Integer.parseInt(params.get("flags"));
        }

        String rid = params.get("rid");
        String uuid = params.get("uuid");
        String hash = params.get("hash");

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            json.put("status", 401);
            json.put("error", "access denied");
            return json.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("upload", token, rid, uuid, ipAddr, rights)){
            json.put("status", 401);
            json.put("error", "access denied");
            return json.toString();
        }

        String uid = (String)rights.get("uid");//"IRX8qYa3";
        DataStoreService store = DataStores.getInstance();
        if(store.addBlob(rid, uid, uuid, flags, index, hash, data)) {
            json.put("status", 200);
        } else {
            json.put("status", 500);
            json.put("error", "add blob failed");
        }
        return json.toString();
    }

    @RequestMapping("welcome")
    public String doTestAlive(){
        return "Welcome to file upload service";
    }

    protected DataObject getDataObject(String rid, String uid, String oid, String pid, String ipAddr, Part part){
        DataObject obj = new DataObject();
        obj.setName(part.getSubmittedFileName());
        obj.setSize((int)part.getSize());
        obj.setMimeType(part.getContentType());
        obj.setIpAddr(ipAddr);
        obj.setRid(rid);
        obj.setOid(oid);
        obj.setUid(uid);
        if(pid!=null && pid.length()>0){
            obj.setPid(pid);
        } else {
            obj.setPid("root");
        }
        obj.setTimeCreate(new Date());
        obj.setLastAccess(new Date());
        return obj;
    }
}
