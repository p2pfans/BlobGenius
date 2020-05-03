package com.toipr.controller.data;

import com.alibaba.fastjson.JSONObject;
import com.toipr.model.data.DataBlob;
import com.toipr.model.data.DataObject;
import com.toipr.service.data.DataStoreService;
import com.toipr.service.data.DataStores;

import com.toipr.service.token.TokenService;
import com.toipr.service.token.TokenServices;
import com.toipr.util.Utils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/data/download")
public class DataDownController {
    @RequestMapping("object")
    protected void doDownObject(@RequestBody String jsonText, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        JSONObject json = (JSONObject)JSONObject.parse(jsonText);

        String rid = json.getString("rid");
        String uuid = json.getString("uuid");
        if(Utils.isNullOrEmpty(rid) || Utils.isNullOrEmpty(uuid)){
            resp.sendError(400, "invalid parameters, rid or doid can not be empty!");
            return;
        }
        doDownObject2(rid, uuid, req, resp, session);
    }

    @RequestMapping("object/{rid}/{uuid}")
    protected void doDownObject2(@PathVariable("rid") String rid, @PathVariable("uuid") String uuid,
                                HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        DataStoreService store = DataStores.getInstance();

        String token = Utils.getToken(req.getHeader("Cookie"));
        if(token==null){
            resp.sendError(401, "access denied");
            return;
        }

        String ipAddr = req.getRemoteAddr();
        Map<String, Object> rights = new HashMap<String, Object>();
        TokenService tokenService = TokenServices.getInstance();
        if(!tokenService.isActionAllowed("download", token, rid, uuid, ipAddr, rights)){
            resp.sendError(401, "access denied");
            return;
        }

        String uid = (String)rights.get("uid");//"IRX8qYa3";
        DataObject dobj = store.getObject(rid, uid, uuid);
        if(dobj==null){
            resp.sendError(404, "object not found");
            return;
        }

        /**
         * 处理断点续传的Content-Range参数
         */
        int begPos = 0, endPos = 0;
        String sRange = req.getHeader("Content-Range");
        if(sRange!=null && sRange.length()>0){
            int pos = sRange.indexOf('-');
            if(pos>=0){
                if(pos>0) {
                    String left = sRange.substring(0, pos).trim();
                    begPos = Integer.parseInt(left);
                }

                String right = sRange.substring(pos+1).trim();
                if(right.length()>0){
                    endPos = Integer.parseInt(right);
                }
            }
            if(begPos<0 || (endPos>0 && endPos<begPos) || begPos>=dobj.getSize()){
                resp.sendError(400, String.format("invalid content ranges:%s", sRange));
                return;
            }
        }

        InputStream ins = store.getInputStream(dobj.getRid(), dobj.getOid(), dobj.getDoid());
        if(ins==null){
            resp.sendError(500, "internal error, object stream creation failed");
            return;
        }

        ServletOutputStream sos = resp.getOutputStream();

        int readLen = 0, wantLen = 0;
        if(endPos>0){
            wantLen = endPos - begPos;
        }
        if(begPos>0){
            //支持断点续传的起始位置
            ins.skip(begPos);
        }

        byte[] buff = new byte[16384];
        int length = ins.read(buff);
        while(length>0){
            sos.write(buff, 0 , length);
            readLen += length;
            if(wantLen>0 && readLen>=wantLen){
                //到达断点续传结束位置
                break;
            }
            length = ins.read(buff);
        }
        ins.close();

        /**
         * 增加下载次数，设置最后下载时间
         */
        store.incObjectDown(rid, uid, uuid);

        String fileExt = "";
        String fileName = dobj.getName();
        int pos = fileName.lastIndexOf('.');
        if(pos>0){
            fileExt = fileName.substring(pos+1);
            fileName = fileName.substring(0, pos);
        }
        /**
         * 注意文件名编码，处理中文乱码问题
         */
        fileName = URLEncoder.encode(fileName, "utf-8");
        String sHeader = String.format("attachment;filename=%s.%s", fileName, fileExt);
        resp.addHeader("Content-Disposition", sHeader);
        String mimeType = dobj.getMimeType();
        if(mimeType.indexOf("/")>0) {
            resp.addHeader("Content-Type", dobj.getMimeType());
        } else {
            resp.setContentType("application/octet-stream");
        }
        resp.setContentLength(readLen);
    }

    @RequestMapping("blob/{rid}/{boid}")
    protected void doDownBlob(@PathVariable("rid") String rid, @PathVariable("boid") String boid, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        DataStoreService store = DataStores.getInstance();
        DataBlob blob = store.getBlob(rid, boid);
        if(blob==null){
            resp.sendError(404, "blob not found");
            return;
        }

        resp.setContentType("application/octet-stream");
        resp.setContentLength(blob.getSize());
        resp.getOutputStream().write(blob.getData());
    }

    @RequestMapping("error/{rid}/{boid}")
    protected void doBlobError(@PathVariable("rid") String rid, @PathVariable("boid") String boid, HttpServletRequest req, HttpServletResponse resp, HttpSession session) throws Exception {
        resp.sendError(500, "report blob error not support now");
    }

    @RequestMapping("welcome")
    public String doTestAlive(){
        return "Welcome to data object download service";
    }
}
