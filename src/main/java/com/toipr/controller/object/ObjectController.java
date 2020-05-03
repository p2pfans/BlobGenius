package com.toipr.controller.object;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataBlobIds;
import com.toipr.model.data.DataConst;
import com.toipr.model.data.DataObject;
import com.toipr.model.user.UserInfo;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.data.DataStoreService;
import com.toipr.service.data.DataStores;
import com.toipr.service.search.ObjectSearcher;
import com.toipr.service.search.SearchServices;
import com.toipr.service.search.SortField;
import com.toipr.service.token.TokenService;
import com.toipr.service.token.TokenServices;
import com.toipr.service.user.UserService;
import com.toipr.service.user.UserServices;
import com.toipr.util.HashHelper;
import com.toipr.util.Utils;
import com.toipr.util.json.DataObjectHelper;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.*;

@RestController
@RequestMapping("/object")
public class ObjectController {
    @RequestMapping(path="list", method= RequestMethod.POST)
    public String doObjectList(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        List<SortField> sorts = getQuerySorts(json);
        Map<String, Object> params = getQueryParams(json);
        if(!params.containsKey("rid")){
            params.put("rid", DataConst.defaultResource);
        }
        if(!params.containsKey("pid")){
            params.put("pid", DataConst.defaultDirectory);
        }

        String rid = (String)params.get("rid");
        String pid = (String)params.get("pid");

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            json.put("status", 401);
            json.put("error", "access denied");
            return json.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("list", token, rid, pid, ipAddr, rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        ObjectSearcher searcher = SearchServices.getSearcher("data_objects_v2");
        if(searcher==null){
            ret.put("status", 400);
            ret.put("error", "object searcher has not found");
            return ret.toString();
        }

        int start = 0;
        if(json.containsKey("start")) {
            start = json.getInteger("start");
        }
        int count = 20;
        if(json.containsKey("count")) {
            count = json.getInteger("count");
        }

        List<JSONObject> rlist = new ArrayList<JSONObject>();
        int total = searcher.queryObjects(params, start, count, sorts, rlist);
        ret.put("status", 200);
        ret.put("total", total);
        if(rlist.size()>0) {
            JSONArray myarr = new JSONArray();
            for (JSONObject item : rlist) {
                myarr.add(item);
            }
            ret.put("docs", myarr);
        }
        return ret.toString();
    }

    @RequestMapping(path="delete", method= RequestMethod.POST)
    public String doObjectDelete(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);

        String rid = json.getString("rid");
        String uuid = json.getString("uuid");
        return doObjectDeleteById(rid, uuid, req, resp);
    }

    @RequestMapping(path="deleteById/{rid}/{uuid}")
    public String doObjectDeleteById(@PathVariable("rid") String rid, @PathVariable("uuid") String uuid, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        if(Utils.isNullOrEmpty(uuid) || Utils.isNullOrEmpty(rid)) {
            ret.put("status", 400);
            ret.put("error", "uuid or rid can not be empty in delete command");
            return ret.toString();
        }

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("delete", token, rid, uuid, ipAddr, rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String uid = (String)rights.get("uid");
        DataStoreService service = DataStores.getInstance();
        if(service.removeObject(rid, uid, uuid)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
            ret.put("error", "set object state failed");
        }
        return ret.toString();
    }

    @RequestMapping(path="auth", method= RequestMethod.POST)
    public String doObjectAuth(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        String uname = json.getString("uname");
        String upass = json.getString("upass");
        if(Utils.isNullOrEmpty(uname) || Utils.isNullOrEmpty(upass)) {
            ret.put("status", 400);
            ret.put("error", "uname or upass can not be empty in auth command");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService service = TokenServices.getInstance();
        String token = service.authUser(uname, upass, ipAddr, rights);
        if(token.compareTo("401")==0){
            ret.put("status", 401);
            ret.put("error", "Invalid user, access denied");
        } else if(token.compareTo("500")==0){
            ret.put("status", 500);
            ret.put("error", "token cache failed");
        } else {
            ret.put("status", 200);
            ret.put("token", token);
            ret.put("expire", (long)rights.get("expire"));
        }
        return ret.toString();
    }

    @RequestMapping(path="get", method= RequestMethod.POST)
    public String doObjectGet(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);

        String rid = json.getString("rid");
        String uuid = json.getString("uuid");
        return doObjectGetById(rid, uuid, req, resp);
    }

    @RequestMapping(path="getById/{rid}/{uuid}")
    public String doObjectGetById(@PathVariable("rid") String rid, @PathVariable("uuid") String uuid, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        if(Utils.isNullOrEmpty(uuid) || Utils.isNullOrEmpty(rid)) {
            ret.put("status", 400);
            ret.put("error", "doid or rid can not be empty in delete command");
            return ret.toString();
        }

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("state", token, rid, uuid, ipAddr, rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String uid = (String)rights.get("uid");
        DataStoreService service = DataStores.getInstance();
        DataObject obj = service.getObject(rid, uid, uuid);
        if(obj!=null){
            ret.put("status", 200);
            ret.put("item", DataObjectHelper.fromObject(obj));
        } else {
            ret.put("status", 404);
            ret.put("error", "object not found");
        }
        return ret.toString();
    }

    @RequestMapping(path="state", method= RequestMethod.POST)
    public String doObjectState(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        String rid = json.getString("rid");
        String uuid = json.getString("uuid");
        if(Utils.isNullOrEmpty(uuid) || Utils.isNullOrEmpty(rid) || !json.containsKey("state")) {
            ret.put("status", 400);
            ret.put("error", "doid or rid or state can not be empty in state command");
            return ret.toString();
        }

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("state", token, rid, uuid, ipAddr, rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return toString();
        }

        Integer state = json.getInteger("state");

        String uid = (String)rights.get("uid");
        DataStoreService service = DataStores.getInstance();
        if(service.setState(rid, uid, uuid, state)){
            ret.put("status", 200);
        } else {
            ret.put("status", 500);
            ret.put("error", "set object state failed");
        }
        return ret.toString();
    }

    @RequestMapping("blobids")
    protected void doDownObjectIds(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);

        String rid = json.getString("rid");
        String uuid = json.getString("uuid");
        if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(uuid)){
            resp.sendError(401, "invalid parameters, rid or doid can not be empty!");
            return;
        }
        doDownObjectIds2(rid, uuid, req, resp, session);
    }

    @RequestMapping("blobids/{rid}/{uuid}")
    protected String doDownObjectIds2(@PathVariable("rid") String rid, @PathVariable("uuid") String uuid,
                                      HttpServletRequest req, HttpServletResponse resp, HttpSession session){
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        JSONObject ret = new JSONObject();
        ret.put("rid", rid);
        ret.put("uuid", uuid);

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("download", token, rid, uuid, ipAddr, rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        DataStoreService store = DataStores.getInstance();
        List<DataBlobIds> oids = store.getBlobIds(rid, uuid);
        if(oids==null){
            ret.put("status", 404);
            ret.put("error", "oid not found");
        } else {
            ret.put("status", 200);
            ret.put("total", oids.size());

            JSONArray myarr = new JSONArray();
            for(DataBlobIds temp:oids){
                JSONObject obj = new JSONObject();
                obj.put("uuid", temp.getUuid());
                obj.put("boid", temp.getBoid());
                obj.put("hash", temp.getHash());
                obj.put("serial", temp.getSerial());
                obj.put("offset", temp.getOffset());
                obj.put("size", temp.getSize());
                myarr.add(obj);
            }
            ret.put("docs", myarr);
        }
        return ret.toString();
    }

    @RequestMapping(path="create", method= RequestMethod.POST)
    public String doObjectCreate(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp){
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        DataObject obj = getDataObject(false, ipAddr, json, ret);
        if(obj==null){
            return ret.toString();
        }
        return saveDataObject(obj, token, false, ret);
    }

    @RequestMapping(path="createDir", method= RequestMethod.POST)
    public String doObjectCreateDir(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        JSONObject json = JSONObject.parseObject(jsonText);
        JSONObject ret = new JSONObject();
        resp.setContentType("application/json");
        resp.setCharacterEncoding("utf-8");

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }

        String ipAddr = req.getRemoteAddr();
        DataObject obj = getDataObject(true, ipAddr, json, ret);
        if(obj==null){
            return ret.toString();
        }
        return saveDataObject(obj, token, true, ret);
    }

    protected DataObject getDataObject(boolean isDir, String ipAddr, JSONObject json, JSONObject ret){
        String name = json.getString("name");
        if(Utils.isNullOrEmpty(name)){
            ret.put("status", 400);
            ret.put("error", "invalid parameters, directory name is empty.");
            return null;
        }

        String rid = json.getString("rid");
        if(Utils.isNullOrEmpty(rid)){
            rid = DataConst.defaultResource;
        }
        String pid = json.getString("pid");
        if(Utils.isNullOrEmpty(pid)){
            pid = DataConst.defaultDirectory;
        }

        int size = 0;
        if(json.containsKey("size")){
            size = json.getInteger("size");
        }

        String hash = null;
        if(json.containsKey("hash")){
            hash = json.getString("hash");
        }

        String doid = null;
        if(json.containsKey("doid")){
            doid = json.getString("doid");
        }
        String uuid = null;
        if(json.containsKey("uuid")){
            uuid = json.getString("uuid");
        }

        String path = null;
        if(json.containsKey("path")){
            path = json.getString("path");
        }
        String tag = "master";
        if(json.containsKey("tag")){
            tag = json.getString("tag");
        }
        int version = 1;
        if(json.containsKey("version")){
            version = json.getInteger("version");
        }

        String mimeType=null;
        if(isDir){
            mimeType = "dir";
        } else {
            if(json.containsKey("mimeType")) {
                mimeType = json.getString("mimeType");
            }
            if (Utils.isNullOrEmpty(mimeType)) {
                int pos = name.lastIndexOf('.');
                if (pos > 0) {
                    mimeType = name.substring(pos + 1);
                }
            }
        }

        int flags = 0;
        if(isDir){
            flags = DataConst.DataFlags_Directory;
        }
        flags = getFlags(flags, json);

        DataObject obj = new DataObject();
        if(!Utils.isNullOrEmpty(uuid)) {
            obj.setUuid(uuid);
        }
        if(!Utils.isNullOrEmpty(doid)) {
            obj.setDoid(doid);
        }
        obj.setName(name);
        if(!Utils.isNullOrEmpty(path)) {
            obj.setPath(path);
        }
        obj.setFlags(flags);
        if(!isDir) {
            obj.setState(DataConst.Object_Incomplete);
        }
        obj.setPid(pid);
        obj.setRid(rid);
        obj.setSize(size);
        obj.setBlobSize(DataConst.DataBlob_DefSize);
        if(!Utils.isNullOrEmpty(hash)) {
            obj.setHash(hash);
        }
        obj.setMimeType(mimeType);
        if(version>0){
            obj.setVersion(version);
        }
        if(!Utils.isNullOrEmpty(tag)) {
            obj.setTag(tag);
        }
        obj.setIpAddr(ipAddr);
        obj.setTimeCreate(new Date());
        obj.setLastAccess(new Date());
        obj.setLastModify(new Date());
        return obj;
    }

    protected String saveDataObject(DataObject obj, String token, boolean isDir, JSONObject ret){
        String action = isDir ? "createDir" : "create";
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed(action, token, obj.getRid(), obj.getPid(), obj.getIpAddr(), rights)){
            ret.put("status", 401);
            ret.put("error", "access denied");
            return ret.toString();
        }
        obj.setUid((String)rights.get("uid"));
        obj.setOid((String)rights.get("oid"));

        DataStoreService service = DataStores.getInstance();
        if(service.storeObject(obj)){
            ret.put("status", 200);
            ret.put("item", DataObjectHelper.fromObject(obj));
        } else {
            ret.put("status", 500);
            ret.put("error", "store object failed");
        }
        return ret.toString();
    }

    protected int getFlags(int flags, JSONObject json){
        if(json.containsKey("flags")){
            flags |= json.getInteger("flags");
        }

        boolean cipher = false;
        if(json.containsKey("cipher")) {
            cipher = json.getBoolean("cipher");
        }
        boolean encode = false;
        if(json.containsKey("cipher")) {
            encode = json.getBoolean("encode");
        }
        boolean compress = false;
        if(json.containsKey("cipher")) {
            compress = json.getBoolean("compress");
        }

        if(cipher){
            flags |= DataConst.DataFlags_Cipher;
        }
        if(encode){
            flags |= DataConst.DataFlags_Encode;
        }
        if(compress){
            flags |= DataConst.DataFlags_Compress;
        }
        return flags;
    }

    protected List<SortField> getQuerySorts(JSONObject json){
        List<SortField> list = new ArrayList<SortField>();
        JSONArray myarr = json.getJSONArray("sorts");
        if(myarr!=null && myarr.size()>0){
            for(int i=0; i<myarr.size(); i++){
                JSONObject temp = myarr.getJSONObject(i);

                SortField item = new SortField();
                item.field = temp.getString("field");
                item.desc = temp.getBoolean("desc");
                list.add(item);
            }
        }
        return list;
    }

    protected Map<String, Object> getQueryParams(JSONObject json){
        Map<String, Object> params = new HashMap<String, Object>();

        String str = json.getString("rid");
        if(!Utils.isNullOrEmpty(str)){
            params.put("rid", str);
        }
        str = json.getString("pid");
        if(!Utils.isNullOrEmpty(str)){
            params.put("pid", str);
        }
        str = json.getString("oid");
        if(!Utils.isNullOrEmpty(str)){
            params.put("oid", str);
        }
        str = json.getString("uid");
        if(!Utils.isNullOrEmpty(str)){
            params.put("uid", str);
        }
        str = json.getString("doid");
        if(!Utils.isNullOrEmpty(str)){
            params.put("doid", str);
        }

        /**
         * 多字段逻辑检索
         */
        JSONArray myarr = json.getJSONArray("fields");
        if(myarr!=null && myarr.size()>0){
            for(int i=0; i<myarr.size(); i++){
                JSONObject temp = myarr.getJSONObject(i);

                str = temp.getString("field");
                if(str.compareTo("org")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("org", str);
                    }
                } else if(str.compareTo("name")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("name", str);
                    }
                } else if(str.compareTo("nickname")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("nickname", str);
                    }
                } else if(str.compareTo("resource")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("resource", str);
                    }
                } else if(str.compareTo("mimeType")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("mimeType", str);
                    }
                } else if(str.compareTo("path")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("path", str);
                    }
                } else if(str.compareTo("tag")==0){
                    str = temp.getString("value");
                    if(!Utils.isNullOrEmpty(str)){
                        params.put("tag", str);
                    }
                }
            }
        }

        /**
         * 对象状态
         */
        if(json.containsKey("state")){
            int state = json.getInteger("state");
            if(state>=0) {
                params.put("state", state);
            }
        }

        /**
         * 版本号
         */
        JSONObject temp = json.getJSONObject("version");
        if(temp!=null){
            String from = temp.getString("from");
            String to = temp.getString("to");
            if(!Utils.isNullOrEmpty(from) || !Utils.isNullOrEmpty(to)) {
                str = String.format("[%s TO %s]", from, to);
                params.put("version", str);
            }
        }

        /**
         * 创建时间
         */
        temp = json.getJSONObject("timeCreate");
        if(temp!=null){
            String from = temp.getString("from");
            String to = temp.getString("to");
            if(!Utils.isNullOrEmpty(from) || !Utils.isNullOrEmpty(to)) {
                str = String.format("[%s TO %s]", from, to);
                params.put("timeCreate", str);
            }
        }
        return params;
    }
}
