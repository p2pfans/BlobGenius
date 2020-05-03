package com.toipr.service.rule.impl;

import com.toipr.service.rule.RuleRouter;
import com.toipr.util.Utils;
import com.toipr.util.bean.BeanFactory;
import com.toipr.util.bean.Beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DefaultRuleRouter implements RuleRouter {
    protected class RuleEntry{
        /**
         * 格式：字段名:开始位置-数据长度
         */
        protected String distKey;
        /**
         * 属性名称
         */
        protected String field;
        public String getField(){
            return this.field;
        }

        /**
         * 取值类型
         */
        protected String type;

        /**
         * 起始位置
         */
        protected int start = -1;
        /**
         * 截取长度
         */
        protected int length = -1;

        protected BeanFactory bean;

        public RuleEntry(String distKey){
            this.distKey = distKey;

            int pos = distKey.indexOf("=");
            if(pos>0){
                field = distKey.substring(0, pos).trim();
                distKey = distKey.substring(pos+1).trim();
            }

            pos = distKey.indexOf(":");
            if(pos>0){
                type = distKey.substring(0, pos).trim();
                distKey = distKey.substring(pos+1).trim();
            }

            pos = distKey.indexOf("-");
            if(pos>=0){
                if(pos>0){
                    String left = distKey.substring(0, pos).trim();
                    start = Integer.parseInt(left);
                }

                distKey = distKey.substring(pos+1).trim();
                if(distKey.length()>0) {
                    length = Integer.parseInt(distKey);
                }
            }
        }

        public String getKey(String text){
            if(text==null || text.length()==0){
                return null;
            }

            if(start>0 && length>0){
                return text.substring(start, start + length);
            }
            if(start>0){
                return text.substring(start);
            }
            if(length>0){
                return text.substring(0, length);
            }
            return text;
        }

        /**
         * 从自定义参数中获取映射主键
         * @param userData
         * @return
         */
        public String getKeyObject(Object userData){
            Class<?> clazz = userData.getClass();
            if(clazz.isPrimitive()){
                return getKey(userData.toString());
            }
            if(clazz.isInstance(String.class)){
                return getKey((String)userData);
            }
            if(clazz.isAssignableFrom(Map.class)){
                Map params = (Map)userData;
                Object objValue = params.get(field);
                if(objValue==null){
                    return null;
                }
                return getKey(objValue.toString());
            }

            if(bean==null){
                bean = Beans.newBeanFactory(clazz);
                if(bean==null){
                    return null;
                }
            }

            Object objValue = bean.getProperty(userData, field);
            if(objValue==null){
                return null;
            }
            return getKey(objValue.toString());
        }
    }

    protected boolean isFixTable;

    protected List<RuleEntry> nodeRules = new ArrayList<RuleEntry>();
    protected List<RuleEntry> tableRules = new ArrayList<RuleEntry>();

    public DefaultRuleRouter(){
    }

    /**
     * 初始化数据路由规则
     * @param nodeRule 节点路由规则
     * @param tableRule 数据表路由规则
     * @return 成功=true 失败=false
     */
    public boolean init(String nodeRule, String tableRule){
        if(!Utils.isNullOrEmpty(nodeRule) && nodeRule.compareTo("all")!=0) {
            if (!parseRuleText("node", nodeRule)) {
                return false;
            }
        }

        if(Utils.isNullOrEmpty(tableRule) || tableRule.compareToIgnoreCase("default")==0){
            isFixTable = true;
        } else {
            if (!parseRuleText("table", tableRule)) {
                return false;
            }
            isFixTable = false;
        }
        return true;
    }

    /**
     * 生成节点存储映射主键
     * @param doid 数字对象ID
     * @param oid 拥有者ID
     * @param userData 其它自定义参数
     * @return 节点映射主键
     */
    public String getNodeKey(String doid, String oid, Object userData){
        if(nodeRules.size()==0){
            return "";
        }
        return getRuleKey(doid, oid, userData, nodeRules);
    }

    /**
     * 生成数据表映射主键
     * @param doid 数字对象ID
     * @param oid 拥有者ID
     * @param userData 其它自定义参数
     * @return 数据表映射主键
     */
    public String getTableKey(String doid, String oid, Object userData){
        if(isFixTable || tableRules.size()==0){
            return "";
        }
        return getRuleKey(doid, oid, userData, tableRules);
    }

    protected String getRuleKey(String doid, String oid, Object userData, List<RuleEntry> rlist){
        StringBuilder str = new StringBuilder(256);
        for(int i=0; i<rlist.size(); i++){
            RuleEntry item = rlist.get(i);

            String key = null;
            switch(item.getField()){
                case "doid":
                    key = item.getKey(doid);
                    break;
                case "oid":
                    key = item.getKey(oid);
                    break;
                default:
                    if(userData!=null) {
                        key = item.getKeyObject(userData);
                    }
                    break;
            }
            if(key==null){
                return null;
            }
            str.append(key);
        }
        return str.toString();
    }

    protected boolean parseRuleText(String type, String text){
        String[] sArr = text.split(";;");
        for(int i=0; i<sArr.length; i++){
            RuleEntry item = new RuleEntry(sArr[i]);
            if(item.getField()==null){
                return false;
            }
            if(type.compareTo("node")==0){
                nodeRules.add(item);
            } else {
                tableRules.add(item);
            }
        }
        return true;
    }
}
