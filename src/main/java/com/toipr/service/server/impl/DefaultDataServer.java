package com.toipr.service.server.impl;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONObject;
import com.toipr.conf.MybatisConfig;
import com.toipr.mapper.data.DataBlobMapper;
import com.toipr.mapper.data.DataBlobRefMapper;
import com.toipr.mapper.data.DataObjectMapper;
import com.toipr.mapper.data.DatabaseMapper;
import com.toipr.model.data.*;
import com.toipr.model.node.DataNode;
import com.toipr.service.cache.CacheServer;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.data.BlobStore;
import com.toipr.service.data.BlobStores;
import com.toipr.service.data.DataHandler;
import com.toipr.service.data.DataHandlers;
import com.toipr.service.node.NodeService;
import com.toipr.service.node.NodeServices;
import com.toipr.service.rule.RuleRouter;
import com.toipr.service.rule.RuleRouters;
import com.toipr.service.server.DataServer;

import com.toipr.util.Utils;
import com.toipr.util.json.DataObjectHelper;
import com.toipr.util.threads.ThreadPoolWorker;
import org.apache.ibatis.session.SqlSession;

import javax.sql.DataSource;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class DefaultDataServer implements DataServer {
    protected class VisitReport implements Runnable {
        protected Date lastAccess;
        protected int currVisit = 0;

        protected DataNode node;
        public VisitReport(DataNode node){
            this.node = node;
        }

        public void incVisit(){
            this.lastAccess = new Date();
            ++this.currVisit;
        }

        @Override
        public void run(){
            if(this.currVisit>0){
                int visit = this.currVisit;
                this.currVisit = 0;
                try {
                    NodeService service = NodeServices.getInstance();
                    service.incVisitAndLastAccess(node.getHid(), visit, lastAccess);
                }catch(Exception ex){
                    ex.printStackTrace();
                }
            }
        }
    }

    protected DataRule rule;
    protected DataNode config;
    protected MybatisConfig mybatis;

    protected DataServer nextServer;
    protected DataServer prevServer;
    protected DataServer primaryServer;

    protected VisitReport visit;

    protected BlobStore blobStore;

    protected Set<String> tableSet = new HashSet<String>();

    public DefaultDataServer(DataNode config, DataRule rule){
        this.config = config;
        this.rule = rule;
        this.visit = new VisitReport(config);
    }

    public boolean init(String[] mappers){
        if(Utils.isNullOrEmpty(rule.getDbName())){
            return false;
        }
        if(Utils.isNullOrEmpty(rule.getTblName())){
            return false;
        }

        if(!Utils.isNullOrEmpty(config.getFilePath())){
            /**
             * 文件块数据存储器
             */
            String key = config.getRid() + "_" + config.getHid();
            this.blobStore = BlobStores.getBlobStore(key);
            if(this.blobStore==null){
                return false;
            }
        }

        DataSource dataSource = getDataSource();
        mybatis = new MybatisConfig(dataSource);
        if(!mybatis.init(mappers)){
            return false;
        }

        try(SqlSession session=mybatis.getSession()){
            DatabaseMapper mapper = session.getMapper(DatabaseMapper.class);

            /**
             * 检查数据库是否存在，不存在则创建数据库
             */
            int ret = mapper.dbExists(rule.getDbName());
            if(ret==0){
                mapper.dbCreate(rule.getDbName());
                ret = mapper.dbExists(rule.getDbName());
                if(ret==0){
                    return false;
                }
            }

            /**
             * 获取数据库中已存在的数据表
             */
            List<String> rlist = mapper.getTables(rule.getDbName());
            if(rlist!=null && rlist.size()>0){
                if(tableSet==null){
                    tableSet.addAll(rlist);
                }
            }
        }
        /**
         * 每隔10秒汇报一次节点访问次数
         */
        ThreadPoolWorker.scheduleWithFixedDelay(visit, 10, 10, TimeUnit.SECONDS);
        return true;
    }

    protected DataSource getDataSource() {
        DruidDataSource obj = new DruidDataSource();
        String jdbcUrl = String.format("jdbc:%s://%s/%s?useUnicode=%s&serverTimezone=%s",
                config.getDbType(), config.getHost(), rule.getDbName(), config.getUseUnicode(), config.getTimeZone());
        obj.setUrl(jdbcUrl);
        obj.setUsername(config.getDbUser());
        obj.setPassword(config.getDbPass());
        obj.setDriverClassName(config.getDriverClass());
        obj.setDefaultAutoCommit(false);
        return obj;
    }

    /**
     * 设置下一个处理的数据服务器，责任链设计模式
     * 解决数据多节点复制 与 失败多点重试问题
     * @param type -1=前一个 1=后一个 0=主服务器
     * @param server 数据服务器
     */
    public void setServer(int type, DataServer server) {
        if(type==DataServer.nextNode) {
            this.nextServer = server;
        } else if(type==DataServer.prevNode) {
            this.prevServer = server;
        } else {
            this.primaryServer = server;
        }
    }

    /**
     * 获取服务器节点配置DataNode
     * @return DataNode对象
     */
    public Object getNode(){
        return this.config;
    }

    /**
     * 获取数据映射规则DataRule
     * @return DataRule对象
     */
    public Object getRule(){
        return this.rule;
    }

    /**
     * 数据块是否存在
     * @param bid 数据块ID
     * @return 数据记录号，不唯一，大于0成功
     */
    public Object blobExists(String bid){
        return blobExists(bid, DataServer.biChain);
    }

    public Object blobExists(String bid, int direction){
        visit.incVisit();

        Object ret = null;
        try {
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                ret = mapper.blobExists(bid, table);
            }
        } catch(Exception ex){
            if(!onCallException("blobExists", new Object[]{bid}, false, ex)){
                return ret;
            }
        }

        if(ret==null){
            if(nextServer!=null && isRightDirection(direction, DataServer.nextChain)){
                ret = nextServer.blobExists(bid, DataServer.nextChain);
                if(ret!=null){
                    copyDataBlob(bid, DataServer.nextChain, nextServer);
                }
            }
            if(ret==null && prevServer!=null && isRightDirection(direction, DataServer.prevChain)){
                ret = prevServer.blobExists(bid, DataServer.prevChain);
                if(ret!=null){
                    copyDataBlob(bid, DataServer.prevChain, prevServer);
                }
            }
            if(ret==null && primaryServer!=null && isRightDirection(direction, DataServer.priChain)){
                ret = primaryServer.blobExists(bid, DataServer.noChain);
                if(ret!=null){
                    copyDataBlob(bid, DataServer.noChain, primaryServer);
                }
            }
        }
        return ret;
    }

    protected void copyDataBlob(String bid, int direction, DataServer server){
        /**
         * 对象复制到本节点，下一次可正常使用
         * 解决问题1：新加入数据节点
         * 解决问题2：数据同步延迟导致临时缺失
         * 解决问题3：服务器故障或程序故障导致的数据异常
         */
        DataBlob blob = server.getBlob(bid, true, direction);
        if(blob!=null){
            addBlob(blob, true, DataServer.noChain);
        }
    }

    /**
     * 根据ID获取数据块
     * @param bid 数据块ID
     * @return 数据块对象
     */
    public DataBlob getBlob(String bid){
        return getBlob(bid, false, DataServer.biChain);
    }

    /**
     * 根据ID获取数据块
     * @param bid 数据块ID
     * @param isCopy 是否直接复制，不处理解压/解密等操作
     * @param direction 操作传导方向
     * @return 数据块对象
     */
    public DataBlob getBlob(String bid, boolean isCopy, int direction){
        visit.incVisit();
        DataBlob blob = null;
        try {
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                blob = mapper.getBlob(bid, table);
                if(blob.getData()==null && blobStore!=null){
                    /**
                     * 数据块数据与数据块对象存储分离
                     * 数据块对象与描述字段存储到数据库，数据存储到文件系统或其他位置
                     */
                    byte[] data = blobStore.getData(bid);
                    if(data==null){
                        return null;
                    }
                    blob.setData(data);
                }
            }
        } catch(Exception ex){
            if(!onCallException("getBlob", new Object[]{bid, isCopy}, false, ex)){
                return blob;
            }
        }

        if(blob==null){
            /**
             * 从主服务器复制数据块
             */
            if(primaryServer!=null){
                blob = primaryServer.getBlob(bid, true, DataServer.noChain);
            }
            /**
             * 从兄弟节点复制
             */
            if(blob==null && nextServer!=null && isRightDirection(direction, DataServer.nextChain)){
                blob = nextServer.getBlob(bid, true, DataServer.nextChain);
            }
            if(blob==null && prevServer!=null && isRightDirection(direction, DataServer.prevChain)){
                blob = prevServer.getBlob(bid, true, DataServer.prevChain);
            }
            if(blob!=null){
                /**
                 * 对象复制到本节点，下一次可正常读取
                 * 解决问题1：新加入数据节点
                 * 解决问题2：数据同步延迟导致临时缺失
                 * 解决问题3：服务器故障或程序故障导致的数据异常
                 */
                addBlob(blob, true, DataServer.noChain);
            }
        }

        if(!isCopy && blob!=null && blob.getFlags()>0){
            int len = blob.getSize();
            byte[] data = blob.getData();
            DataHandler handler = DataHandlers.getChainHandler(false);
            if(handler==null){
                return null;
            }
            handler.process(data, 0, len, blob.getFlags());
        }
        return blob;
    }

    /**
     * 添加数据块
     * @param blob 数据块对象
     * @return 数据记录号，不唯一，大于0成功
     */
    public int addBlob(DataBlob blob){
        return addBlob(blob, false, DataServer.biChain);
    }

    /**
     * 添加数据块
     * @param blob 数据块对象
     * @param isCopy 是否直接复制，不处理压缩/加密等
     * @param direction 传导方向
     * @return 成功=1 失败=0
     */
    public int addBlob(DataBlob blob, boolean isCopy, int direction){
        visit.incVisit();
        /**
         * 数据复制时，不编码解码，降低性能消耗
         */
        if(!isCopy && blob.getFlags()>0) {
            int len = blob.getSize();
            byte[] data = blob.getData();
            DataHandler handler = DataHandlers.getChainHandler(true);
            if(handler==null){
                return 0;
            }
            data = handler.process(data, 0, len, blob.getFlags());
            if(data==null){
                return 0;
            }
            blob.setData(data);
        }

        int ret = 0;
        try {
            if((direction&DataServer.jobChain)!=0){
                int copy = blob.getCopy();
                /**
                 * 处理数据复制复本数限制
                 */
                if(rule.getMaxCopy()==0 || (rule.getMaxCopy()>0 && copy<rule.getMaxCopy())) {
                    blob.setCopy(copy + 1);
                }
            }

            String table = getTableName(blob.getBoid(), null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                if(blobStore==null) {
                    ret = mapper.addBlob(blob, table);
                } else {
                    /**
                     * 数据块数据与数据块对象存储分离
                     * 数据块对象与描述字段存储到数据库，数据存储到文件系统或其他位置
                     */
                    if(!blobStore.saveBlob(blob)) {
                        /**
                         * 失败后临时存储到数据库
                         */
                        ret = mapper.addBlob(blob, table);
                    } else {
                        //字节数组设置为空，只存储数据块描述信息
                        blob.setData(null);
                        ret = mapper.addBlob(blob, table);
                    }
                }

                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("addBlob", new Object[]{blob, isCopy}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            /**
             * 主服务器写失败后，交给自动写到从服务器
             */
            if(ret==0 && nextServer!=null){
                ret = nextServer.addBlob(blob, true, DataServer.nextChain);
            }
        } else {
            /**
             * 复制数据块到兄弟节点
             */
            doChainJob("addBlob", null, null, 0, null, direction, blob);
        }
        return ret;
    }

    /**
     * 根据ID删除数据块
     * @param bid 数据块ID
     * @return 影响记录数，大于0成功
     */
    public int removeBlob(String bid){
        return removeBlob(bid, DataServer.biChain);
    }
    public int removeBlob(String bid, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                ret = mapper.removeBlob(bid, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("removeBlob", new Object[]{bid}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.removeBlob(bid, DataServer.nextChain);
            }
        } else {
            doChainJob("removeBlob", bid, null, 0, null, direction, null);
        }
        return ret;
    }

    /**
     * 增加数据块引用数
     * @param bid 数据块ID
     * @return 影响记录数，大于0成功
     */
    public int incBlobRefs(String bid){
        return incBlobRefs(bid, DataServer.biChain);
    }

    public int incBlobRefs(String bid, int direction) {
        visit.incVisit();
        int ret = 0;
        try{
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try(SqlSession session = mybatis.getSession()){
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                ret = mapper.incBlobRefs(bid, table);
                if(ret>0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("incBlobRefs", new Object[]{bid}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.incBlobRefs(bid, DataServer.nextChain);
            }
        } else {
            doChainJob("incBlobRefs", bid, null, 0, null, direction, null);
        }
        return ret;
    }

    /**
     * 减少数据块引用数
     * @param bid 数据块ID
     * @return 影响记录数，大于0成功
     */
    public int decBlobRefs(String bid){
        return decBlobRefs(bid, DataServer.biChain);
    }
    public int decBlobRefs(String bid, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                ret = mapper.decBlobRefs(bid, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        }catch(Exception ex){
            if(!onCallException("decBlobRefs", new Object[]{bid}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.decBlobRefs(bid, DataServer.nextChain);
            }
        } else {
            doChainJob("decBlobRefs", bid, null, 0, null, direction, null);
        }
        return ret;
    }

    /**
     * 增加下载次数，设置最后访问时间
     * @param bid 数据块ID
     * @param lastAccess 访问时间戳
     * @return 影响记录数,成功等于1
     */
    public int incBlobDown(String bid, Date lastAccess){
        return incBlobDown(bid, lastAccess, DataServer.biChain);
    }
    public int incBlobDown(String bid, Date lastAccess, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(bid, null, DataConst.DataType_Blob, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                ret = mapper.incBlobDown(bid, new Date(), table);
                if (ret > 0) {
                    session.commit();
                }
            }
        }catch(Exception ex){
            if(!onCallException("incBlobDown", new Object[]{bid, lastAccess}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.incBlobDown(bid, lastAccess, DataServer.nextChain);
            }
        } else {
            doChainJob("incBlobDown", bid, null, 0, lastAccess, direction, null);
        }
        return ret;
    }

    /**
     * 判断数据对象的数据块ID串是否存在
     * @param doid 数据对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    public Object blobIdsExists(String doid, String boid){
        return blobIdsExists(doid, boid, DataServer.biChain);
    }
    public Object blobIdsExists(String doid, String boid, int direction){
        visit.incVisit();
        Object ret = null;
        try {
            String table = getTableName(doid, null, DataConst.DataType_BlobRef, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                ret = mapper.objectExists(doid, table);
            }
        } catch(Exception ex){
            if(!onCallException("blobIdsExists", new Object[]{doid, boid}, false, ex)){
                return ret;
            }
        }

        if(ret==null){
            if(nextServer!=null && isRightDirection(direction, DataServer.nextChain)){
                ret = nextServer.blobIdsExists(doid, boid, DataServer.nextChain);
            }
            if(ret==null && prevServer!=null && isRightDirection(direction, DataServer.prevChain)){
                ret = prevServer.blobIdsExists(doid, boid, DataServer.prevChain);
            }
            if(ret==null && primaryServer!=null){
                ret = primaryServer.blobIdsExists(doid, boid, DataServer.noChain);
            }
        }
        return ret;
    }

    /**
     * 添加数据对象的数据块ID串
     * @param obj 数据对象ID串
     * @return 数据记录号，不唯一，大于0成功
     */
    public int addBlobIds(DataBlobRef obj){
        return addBlobIds(obj, DataServer.biChain);
    }
    public int addBlobIds(DataBlobRef obj, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(obj.getUuid(), null, DataConst.DataType_BlobRef, obj);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                ret = mapper.addObject(obj, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("addBlobIds", new Object[]{obj}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.addBlobIds(obj, DataServer.nextChain);
            }
        } else {
            doChainJob("addBlobIds", null, null, 0, null, direction, obj);
        }
        return ret;
    }

    /**
     * 删除数据对象ID串
     * @param doid 数据对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    public int removeBlobIds(String doid){
        return removeBlobIds(doid, DataServer.biChain);
    }
    public int removeBlobIds(String doid, int direction) {
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(doid, null, DataConst.DataType_BlobRef, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                ret = mapper.removeObject(doid, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("removeBlobIds", new Object[]{doid}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.removeBlobIds(doid, DataServer.nextChain);
            }
        } else {
            doChainJob("removeBlobIds", doid, null, 0, null, direction, null);
        }
        return ret;
    }

    /**
     * 根据数据对象ID获取数据块ID串
     * @param doid 数据对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    public DataBlobRef getBlobIds(String doid, String boid){
        return getBlobIds(doid, boid, DataServer.biChain);
    }
    public DataBlobRef getBlobIds(String doid, String boid, int direction){
        visit.incVisit();
        DataBlobRef ret = null;
        try {
            String table = getTableName(doid, null, DataConst.DataType_BlobRef, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                ret = mapper.getObject(doid, boid, table);
            }
        } catch(Exception ex){
            if(!onCallException("getBlobIds", new Object[]{doid, boid}, true, ex)){
                return ret;
            }
        }

        if(ret==null){
            if (ret==null && nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                ret = nextServer.getBlobIds(doid, boid, DataServer.nextChain);
            }
            if (ret==null && prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                ret = prevServer.getBlobIds(doid, boid, DataServer.prevChain);
            }
            if(ret==null && primaryServer!=null && isRightDirection(direction, DataServer.priChain)){
                ret = primaryServer.getBlobIds(doid, boid, DataServer.nextChain);
            }
            if(ret!=null){
                /**
                 * 对象复制到本节点
                 * 解决问题1：新加入数据节点
                 * 解决问题2：数据同步延迟导致临时缺失
                 * 解决问题3：服务器故障或程序故障导致的数据异常
                 */
                addBlobIds(ret, DataServer.noChain);
            }
        }
        return ret;
    }

    /**
     * 获取数字对象doid的数据块索引列表
     * @param doid 数字对象ID
     * @return 成功=数据块索引列表
     */
    public List<DataBlobRef> getBlobIds(String doid){
        return getBlobIds(doid, DataServer.allChain);
    }
    public List<DataBlobRef> getBlobIds(String doid, int direction){
        visit.incVisit();
        List<DataBlobRef> rlist = null;
        try {
            String table = getTableName(doid, null, DataConst.DataType_BlobRef, null);
            try (SqlSession session = mybatis.getSession()) {
                DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                rlist = mapper.getObjects(doid, table);
            }
        } catch(Exception ex){
            if(!onCallException("getBlobIds", new Object[]{doid}, true, ex)){
                return rlist;
            }
        }

        if(rlist==null){
            if (rlist==null && nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                rlist = nextServer.getBlobIds(doid, DataServer.nextChain);
            }
            if (rlist==null && prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                rlist = prevServer.getBlobIds(doid, DataServer.prevChain);
            }
            if(rlist==null && primaryServer!=null && isRightDirection(direction, DataServer.priChain)){
                rlist = primaryServer.getBlobIds(doid, DataServer.nextChain);
            }
            if(rlist!=null){
                /**
                 * 对象复制到本节点
                 * 解决问题1：新加入数据节点
                 * 解决问题2：数据同步延迟导致临时缺失
                 * 解决问题3：服务器故障或程序故障导致的数据异常
                 */
                for(DataBlobRef temp:rlist) {
                    addBlobIds(temp, DataServer.noChain);
                }
            }
        }
        return rlist;
    }

    /**
     * 数据对象是否存在
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 数据记录号，不唯一，大于0成功
     */
    public Object objectExists(String oid, String uuid){
        return objectExists(oid, uuid, DataServer.biChain);
    }
    public Object objectExists(String oid, String uuid, int direction){
        visit.incVisit();
        Object ret = null;
        try {
            String table = getTableName(uuid, oid, DataConst.DataType_Object, null);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.objectExists(uuid, table);
            }
        } catch(Exception ex){
            if(!onCallException("blobIdsExists", new Object[]{oid,uuid}, false, ex)){
                return ret;
            }
        }

        if(ret==null){
            if(nextServer!=null && isRightDirection(direction, DataServer.nextChain)){
                ret = nextServer.objectExists(oid, uuid, DataServer.nextChain);
                if(ret!=null){
                    copyDataObject(oid, uuid, DataServer.nextChain, nextServer);
                }
            }
            if(ret==null && prevServer!=null && isRightDirection(direction, DataServer.prevChain)){
                ret = prevServer.objectExists(oid, uuid, DataServer.prevChain);
                if(ret!=null){
                    copyDataObject(oid, uuid, DataServer.prevChain, prevServer);
                }
            }
            if(ret==null && primaryServer!=null && isRightDirection(direction, DataServer.priChain)){
                ret = primaryServer.objectExists(oid, uuid, DataServer.noChain);
                if(ret!=null){
                    copyDataObject(oid, uuid, DataServer.noChain, primaryServer);
                }
            }
        }
        return ret;
    }

    protected void copyDataObject(String oid, String uuid, int direction, DataServer server){
        /**
         * 对象复制到本节点，下一次可正常使用
         * 解决问题1：新加入数据节点
         * 解决问题2：数据同步延迟导致临时缺失
         * 解决问题3：服务器故障或程序故障导致的数据异常
         */
        DataObject obj = nextServer.getObject(oid, uuid, direction);
        if(obj!=null){
            addObject(obj, DataServer.noChain);
        }
    }

    /**
     * 添加数据对象
     * @param obj 数据对象
     * @return 影响记录数，大于0成功
     */
    public int addObject(DataObject obj){
        return addObject(obj, DataServer.biChain);
    }
    public int addObject(DataObject obj, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(obj.getUuid(), obj.getOid(), DataConst.DataType_Object, obj);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.addObject(obj, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("addObject", new Object[]{obj}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            /**
             * 主服务器写入失败，尝试写入从服务器,自动责任切换
             */
            if(ret==0 && nextServer!=null){
                ret = nextServer.addObject(obj, DataServer.nextChain);
            }
        } else {
            doChainJob("addObject", null, null, 0, null, direction, obj);
        }

        if(ret>0 && (direction&DataServer.jobChain)==0){
            /**
             * 发布消息,ES索引、数据统计、计费程序等处理
             */
            pubAddObject(obj);
        }
        return ret;
    }

    /**
     * 发布添加数字对象消息
     * @param obj
     */
    protected void pubAddObject(DataObject obj){
        CacheServer service = CacheServices.getServer("events", obj.getDoid(), true, obj);
        if(service==null){
            return;
        }

        JSONObject json = new JSONObject();
        JSONObject jobj = DataObjectHelper.fromObject(obj);
        json.put("action", "add");
        json.put("object", jobj.toString());
        service.publish(DataConst.DataType_Object, json.toString());
    }

    /**
     * 根据ID删除数据对象
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 影响记录数，大于0成功
     */
    public int removeObject(String oid, String uuid){
        return removeObject(oid, uuid, DataServer.biChain);
    }
    public int removeObject(String oid, String uuid, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(uuid, oid, DataConst.DataType_Object, null);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.removeObject(uuid, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("removeObject", new Object[]{oid, uuid}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.removeObject(oid, uuid, DataServer.nextChain);
            }
        } else {
            doChainJob("removeObject", uuid, oid, 0, null, direction, null);
        }
        if(ret>0){
            onObjectChanged("remove", true, oid, uuid);
        }
        return ret;
    }

    /**
     * 根据ID获取数据对象
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @return 数据对象
     */
    public DataObject getObject(String oid, String uuid){
        return getObject(oid, uuid, DataServer.allChain);
    }
    public DataObject getObject(String oid, String uuid, int direction){
        visit.incVisit();
        DataObject ret = null;
        try {
            String table = getTableName(uuid, oid, DataConst.DataType_Object, null);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.getObject(uuid, table);
            }
        } catch(Exception ex){
            if(!onCallException("getObject", new Object[]{oid, uuid}, false, ex)){
                return ret;
            }
        }

        if(ret==null){
            /**
             * 数据缺失，从兄弟节点、主节点读取数据对象，解决数据同步异常问题
             */
            if(nextServer!=null && isRightDirection(direction, DataServer.nextChain)){
                ret = nextServer.getObject(oid, uuid, DataServer.nextChain);
            }
            if(ret==null && prevServer!=null && isRightDirection(direction, DataServer.prevChain)){
                ret = prevServer.getObject(oid, uuid, DataServer.prevChain);
            }
            if(ret==null && primaryServer!=null && isRightDirection(direction, DataServer.priChain)){
                ret = primaryServer.getObject(oid, uuid, DataServer.noChain);
            }
            if(ret!=null){
                /**
                 * 对象复制到本节点，下一次可正常使用
                 * 解决问题1：新加入数据节点
                 * 解决问题2：数据同步延迟导致临时缺失
                 * 解决问题3：服务器故障或程序故障导致的数据异常
                 */
                addObject(ret, DataServer.noChain);
            }
        }
        return ret;
    }

    /**
     * 设置梳子对象状态
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @param state 对象状态
     * @return 成功=1 失败=0
     */
    public int setState(String oid, String uuid, int state){
        return setState(oid, uuid, state, DataServer.allChain);
    }
    public int setState(String oid, String uuid, int state, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(uuid, oid, DataConst.DataType_Object, null);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.setState(uuid, state, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("setState", new Object[]{oid, uuid, state}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.setState(oid, uuid, state, DataServer.nextChain);
            }
        } else {
            doChainJob("setState", uuid, oid, state, null, direction, null);
        }

        if(ret>0){
            onObjectChanged("update", false, oid, uuid);
        }
        return ret;
    }

    protected void onObjectChanged(String action, boolean isDel, String oid, String uuid){
        CacheServer service = CacheServices.getServer("events", uuid, true, null);
        if(service==null){
            return;
        }

        JSONObject json = new JSONObject();
        if(isDel){
            json.put("action", action);
            json.put("uuid", uuid);
        } else {
            DataObject obj = getObject(oid, uuid);
            if(obj==null){
                return;
            }

            JSONObject jobj = DataObjectHelper.fromObject(obj);
            json.put("action", "update");
            json.put("object", jobj.toString());
        }
        service.publish("objects", json.toString());
    }

    /**
     * 增加文件下载计数，设置最后下载时间
     * @param oid 用户ID
     * @param uuid 数据对象ID
     * @param lastAccess 最后下载时间
     * @return 影响记录数，大于0成功
     */
    public int incObjectDown(String oid, String uuid, Date lastAccess){
        return incObjectDown(oid, uuid, lastAccess, DataServer.allChain);
    }
    public int incObjectDown(String oid, String uuid, Date lastAccess, int direction){
        visit.incVisit();
        int ret = 0;
        try {
            String table = getTableName(uuid, oid, DataConst.DataType_Object, null);
            try (SqlSession session = mybatis.getSession()) {
                DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                ret = mapper.incObjectDown(uuid, lastAccess, table);
                if (ret > 0) {
                    session.commit();
                }
            }
        } catch(Exception ex){
            if(!onCallException("incObjectDown", new Object[]{oid, uuid, lastAccess}, true, ex)){
                return ret;
            }
        }

        if(config.getMaster() && primaryServer==this){
            if(ret==0 && nextServer!=null){
                ret = nextServer.incObjectDown(oid, uuid, lastAccess, DataServer.nextChain);
            }
        } else {
            doChainJob("incObjectDown", uuid, oid, 0, lastAccess, direction, null);
        }

        if(ret>0){
            onObjectChanged("update", false, oid, uuid);
        }
        return ret;
    }

    protected void doChainJob(String action, String uuid, String oid, int state, Date lastAccess, int direction, Object target){
        if((direction&DataServer.jobChain)==0) {
            doChainJobServer(action, uuid, oid, state, lastAccess, direction, target);
            return;
        }

        switch(action){
            case "incBlobDown":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.incBlobDown(uuid, lastAccess, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.incBlobDown(uuid, lastAccess, DataServer.prevChain);
                }
                break;
            case "incBlobRefs":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.incBlobRefs(uuid, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.incBlobRefs(uuid, DataServer.prevChain);
                }
                break;
            case "addBlob":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.addBlob((DataBlob)target, true, DataServer.nextChain|DataServer.jobChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.addBlob((DataBlob)target, true, DataServer.prevChain|DataServer.jobChain);
                }
                break;
            case "decBlobRefs":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.decBlobRefs(uuid, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.decBlobRefs(uuid, DataServer.prevChain);
                }
                break;
            case "removeBlob":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.removeBlob(uuid, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.removeBlob(uuid, DataServer.prevChain);
                }
                break;

            case "addBlobIds":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.addBlobIds((DataBlobRef)target, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.addBlobIds((DataBlobRef)target, DataServer.prevChain);
                }
                break;
            case "removeBlobIds":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.removeBlobIds(uuid, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.removeBlobIds(uuid, DataServer.prevChain);
                }
                break;

            case "incObjectDown":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.incObjectDown(oid, uuid, lastAccess, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.incObjectDown(oid, uuid, lastAccess, DataServer.prevChain);
                }
                break;
            case "addObject":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.addObject((DataObject)target, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.addObject((DataObject)target, DataServer.prevChain);
                }
                break;
            case "setState":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.setState(oid, uuid, state, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.setState(oid, uuid, state, DataServer.prevChain);
                }
                break;
            case "removeObject":
                if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
                    nextServer.removeObject(oid, uuid, DataServer.nextChain);
                }
                if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
                    prevServer.removeObject(oid, uuid, DataServer.prevChain);
                }
                break;
        }
    }

    protected void doChainJobServer(String action, String uuid, String oid, int state, Date lastAccess, int direction, Object target){
        if (nextServer != null && isRightDirection(direction, DataServer.nextChain)) {
            (new DataWorker(target, DataServer.nextChain, nextServer)).setAction(action)
                    .setUuid(uuid).setOid(oid).setState(state).setLastAccess(lastAccess).submit();
        }
        if (prevServer != null && isRightDirection(direction, DataServer.prevChain)) {
            (new DataWorker(target, DataServer.prevChain, prevServer)).setAction(action)
                    .setUuid(uuid).setOid(oid).setState(state).setLastAccess(lastAccess).submit();
        }
    }

    /**
     * 判断是否要向责任链传导操作
     * @param inDir 允许传导方向
     * @param theDir 需要判断的方向
     * @return true=允许 false=不允许
     */
    protected boolean isRightDirection(int inDir, int theDir){
        if((inDir&theDir)!=0){
            return true;
        }
        return false;
    }

    /**
     * 获取数据对象存储的表名称，支持分表存储逻辑
     * @param doid 数字对象ID
     * @param oid 拥有者ID
     * @param dataType 数据类型
     * @param userData 自定义参数
     * @return 数据表名称
     */
    protected String getTableName(String doid, String oid, String dataType, Object userData){
        RuleRouter router = RuleRouters.getRuleRouter(rule.getRid(), dataType);
        if(router==null){
            return null;
        }

        String table = rule.getTblName();
        //获取分表存储映射键值
        String sKey = router.getTableKey(doid, oid, userData);
        if(!Utils.isNullOrEmpty(sKey)){
            table = table + "_" + sKey;
        }
        //表是否存在
        if(tableSet.contains(table)){
            return table;
        }
        //表不存在，创建新表
        if(!createTable(table, dataType)){
            return null;
        }
        return table;
    }

    /**
     * 创建数据表
     * @param tblName 数据表名称
     * @param dataType 数据类型
     * @return 成功=true 失败=false
     */
    protected synchronized boolean createTable(String tblName, String dataType){
        if(tableSet==null){
            tableSet = new HashSet<String>();
        }
        if(tableSet.contains(tblName)){
            return true;
        }

        try {
            int ret = 0;
            try (SqlSession session = mybatis.getSession()) {
                if(dataType.compareTo(DataConst.DataType_Blob)==0){
                    DataBlobMapper mapper = session.getMapper(DataBlobMapper.class);
                    //表是否存在，存在ret=1
                    ret = mapper.tableExists(rule.getDbName(), tblName);
                    if(ret==0){
                        //创建数据表，成功ret=1
                        mapper.createTable(tblName);
                    }
                } else if(dataType.compareTo(DataConst.DataType_BlobRef)==0){
                    DataBlobRefMapper mapper = session.getMapper(DataBlobRefMapper.class);
                    ret = mapper.tableExists(rule.getDbName(), tblName);
                    if(ret==0){
                        mapper.createTable(tblName);
                    }
                } else if(dataType.compareTo(DataConst.DataType_Object)==0){
                    DataObjectMapper mapper = session.getMapper(DataObjectMapper.class);
                    ret = mapper.tableExists(rule.getDbName(), tblName);
                    if(ret==0){
                        mapper.createTable(tblName);
                    }
                }
                tableSet.add(tblName);
                return true;
            }
        } catch(Exception ex){
            onCallException("createTable", new Object[]{tblName, dataType}, true, ex);
        }
        return false;
    }

    /**
     * 方法执行失败回调
     * @param method 方法名称
     * @param args 方法参数
     * @param isUpdate true=更新操作
     * @param ex 异常对象
     * @return true=继续执行 false=终止执行
     */
    protected boolean onCallException(String method, Object[] args, boolean isUpdate, Exception ex){
        ex.printStackTrace();
        return true;
    }
}
