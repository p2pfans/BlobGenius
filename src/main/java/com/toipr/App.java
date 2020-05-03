package com.toipr;

import com.toipr.client.example.DataClientExample;
import com.toipr.conf.MySettings;
import com.toipr.model.data.DataConst;
import com.toipr.service.cache.CacheServices;
import com.toipr.service.conf.SettingsServices;
import com.toipr.service.data.DataHandler;
import com.toipr.service.data.DataHandlers;
import com.toipr.service.node.NodeServices;
import com.toipr.service.resource.ResourceServices;
import com.toipr.service.search.SearchServices;
import com.toipr.service.server.DataServers;
import com.toipr.service.org.OrgServices;
import com.toipr.service.token.TokenServices;
import com.toipr.service.user.UserServices;
import com.toipr.socket.SocketServices;
import com.toipr.util.HashHelper;
import com.toipr.util.threads.ThreadPoolWorker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ImportResource;

import java.util.Random;


@SpringBootApplication
@ImportResource(locations="classpath:/conf/beans.xml")
public class App {

	public static void main(String[] args) throws Exception {
		ApplicationContext ctx = SpringApplication.run(App.class, args);
		if(ctx!=null) {
			/**
			 * 启动线程池，完成发布订阅、数据索引等操作
			 */
			ThreadPoolWorker.initThreadPool(5);

			/**
			 * 初始化Redis缓存服务
			 */
			String[] mappers = new String[]{"classpath:/mapper/node/*.xml"};
			if(!CacheServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化系统配置服务
			 */
			mappers = new String[]{"classpath:/mapper/conf/*.xml"};
			if(!SettingsServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			MySettings settings = (MySettings)ctx.getBean("mysettings");
			/**
			 * 初始化数据节点管理服务
			 */
			mappers = new String[]{"classpath:/mapper/node/*.xml"};
			if(!NodeServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化资源管理服务
			 */
			mappers = new String[]{"classpath:/mapper/resource/*.xml"};
			if(!ResourceServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化机构管理服务
			 */
			mappers = new String[]{"classpath:/mapper/org/*.xml"};
			if(!OrgServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化用户管理服务
			 */
			mappers = new String[]{"classpath:/mapper/user/*.xml"};
			if(!UserServices.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化数据处理器，编码/加密/压缩
			 */
			int flags = DataConst.DataFlags_Encode | DataConst.DataFlags_Cipher | DataConst.DataFlags_Compress;
			if(!DataHandlers.createHandler(true, -1, flags)){
				System.exit(-1);
				return;
			}
			if(!DataHandlers.createHandler(false, -1, flags)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化TCP文件块存储服务器
			 */
			if(!SocketServices.createBlobServer(ctx)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化ElasticSearch数据索引与查询服务
			 */
			String text = (String)settings.getProperty("es.server");
			if(text==null || text.length()<6){
				System.exit(-1);
				return;
			}
			String[] hosts = text.split(";");
			String collection = (String)settings.getProperty("es.collection");
			if(!SearchServices.createIndexer("objects", collection, hosts)){
				System.exit(-1);
				return;
			}
			if(!SearchServices.createSearcher(collection, hosts)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化数据存储结点与调度路由器服务
			 */
			mappers = new String[]{"classpath:/mapper/data/*.xml"};
			if(!DataServers.createInstance(ctx, mappers)){
				System.exit(-1);
				return;
			}

			/**
			 * 初始化权限管理服务
			 */
			if(!TokenServices.createInstance("users")){
				System.exit(-1);
				return;
			}
			//doTestClient();
		}
	}

	/**
	 * 测试客户端上传下载样例
	 */
	protected static void doTestClient(){
		String[] params = {
				"-u=18810537529",
				"-p=123456",
				"-s=http://localhost:8100"
		};

		DataClientExample myapp = new DataClientExample();
		if(!myapp.init(params)){
			return;
		}
		myapp.runTest();
		System.out.println("client run test completed");
	}

	protected static void doCreateHostid(String host, String dataType) throws Exception {
		String rid = "Ky4bJyO3";
		String protocol="jdbc";
		String idstr = String.format("%s_%s_%s_%s", protocol, rid, dataType, host);
		try {
			byte[] hashBytes = HashHelper.computeHashBytes(idstr.getBytes("utf-8"), "SHA-256");
			idstr = HashHelper.getShortHashStr(hashBytes, 8);
			System.out.println(host + " id= " + idstr);
		}catch(Exception ex){
			ex.printStackTrace();
		}
	}

	protected static void doTestShortHashStr() throws Exception {
		Random rand = new Random();
		byte[] data = new byte[1024*1024];
		rand.nextBytes(data);
		byte[] hashArr = HashHelper.computeHashBytes(data, "SHA-256");
		String idstr = HashHelper.getShortHashStr(hashArr, 12);
		System.out.println(idstr);
	}

	protected static void doTestDataHandler(){
		Random rand = new Random();
		byte[] data = new byte[1024];
		rand.nextBytes(data);

		int flags = 7;
		DataHandlers.createHandler(true, -1, flags);
		DataHandler handler = DataHandlers.getChainHandler(true);
		byte[] data2 = handler.process(data, 0, data.length, flags);

		DataHandlers.createHandler(false, -1, flags);
		handler = DataHandlers.getChainHandler(false);
		byte[] data3 = handler.process(data2, 0, data2.length, flags);
		System.out.println("test complete");
	}
}
