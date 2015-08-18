package org.epiclouds.handler;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.epiclouds.bean.CategoryBean;
import org.epiclouds.bean.CommonData;
import org.epiclouds.bean.DataBean;
import org.epiclouds.bean.SearchBean;
import org.epiclouds.bean.TerapeakBean;
import org.epiclouds.handlers.AbstractNettyCrawlerHandler;
import org.epiclouds.spiders.dbstorage.condition.impl.EqualCondition;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean;
import org.epiclouds.spiders.dbstorage.manager.abstracts.StorageBean.OperationType;
import org.epiclouds.spiders.spiderobject.abstracts.AbstractSpiderObject;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import com.alibaba.fastjson.JSONObject;

public class TerapeakItemHandler extends AbstractNettyCrawlerHandler{
	private volatile int i=1;
	private volatile int days=80;
	private volatile int num=9;
	private volatile DataBean dbb=new DataBean();
	private volatile SearchBean sr;
	private volatile CategoryBean eb;
	public volatile static HashMap<String,String> hs;
	private volatile int errornum=0;
	private String database;
	private String collection;

	
	public SearchBean getSr() {
		return sr;
	}
	public CategoryBean getEb() {
		return eb;
	}
	public void setEb(CategoryBean eb) {
		this.eb = eb;
	}

	public int getDays() {
		return days;
	}
	public void setDays(int days) {
		this.days = days;
	}
	public int getNum() {
		return num;
	}
	public void setNum(int num) {
		this.num = num;
	}
	public void setSr(SearchBean sr) {
		this.sr = sr;
	}
	public int getI() {
		return i;
	}
	public void setI(int i) {
		this.i = i;
	}
	static{
		hs=new HashMap<String,String>();
		//headers
	    hs.put("Accept", "application/json, text/javascript, */*; q=0.01");
	    hs.put("Content-Type", "application/json");

	    hs.put("Origin","https://sell.terapeak.com");
	    hs.put("Referer","https://sell.terapeak.com/?page=eBayCategoryResearch");
	    hs.put("X-Requested-With", "XMLHttpRequest");
	}
	
	public TerapeakItemHandler(
			String host, String url, HttpMethod md,
			AbstractSpiderObject spider, int days, int num, final SearchBean sb, CategoryBean eb) {
		super(host, url, spider);
		this.setMd(md);
		this.setSchema("https");
		this.getHeaders().putAll(hs);
		this.days=days;
		this.num=num;
		this.sr=sb;
		this.eb=eb;
//		dbb.setData(new TerapeakBean());
		dbb.setId(eb.getId());
		dbb.setName(eb.getName());
		HashMap<String,String> pd=new HashMap<String,String>();
		pd.put(null, JSONObject.toJSONString(getSearchBean()));
	    this.setPostdata(pd);
	    database = this.getSpider().getConfigManager().getValue("spider_database",String.class);
	    collection = this.getSpider().getConfigManager().getValue("category_data_table",String.class);
	}
	
	public void handle(String content) throws Exception {
		// TODO Auto-generated method stub
	//	System.out.println("Id "+this.getEb().getId() + ": content="+content);
		System.out.println("No."+i+" Id "+this.getEb().getId() + " is in handle");
		errornum = 0;
		if(i<=num+1){
        	TerapeakBean tmp=JSONObject.parseObject(content,TerapeakBean.class);
        	formatTerapeakCategorySoldData(tmp);
        	formatTerapeakCategoryToalListingsData(tmp);
        	formatTerapeakCategoryRevenueData(tmp);
        	//System.err.println("ok");
/*        	if(tmp.getAverage_end_price().getData().size()>0&&i==num+1){
        		System.err.println(tmp.getAverage_end_price().getData().get(
        				tmp.getAverage_end_price().getData().size()-1)[0]+":::i="+i+":::num="+num+":::"
        				+JSONObject.toJSONString(sr)+":::"+new DateTime().toString("yyyy-MM-dd"));
        	}*/
        	TerapeakBean tb=dbb.getData();
        	DateTime otime=null;
        	if(tb!=null&&tb.getAverage_end_price()!=null&&tb.getAverage_end_price().getData().size()>0){
				Object[] obs=tb.getAverage_end_price().getData().get(
						tb.getAverage_end_price().getData().size()-1);
				otime=DateTime.parse(((String)obs[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));	
			}
        	if(otime!=null){
        		//System.err.println("otime is not null"+otime.toString("yyyy-MM-dd"));
        		Iterator<Object[]>  iter=tmp.getAverage_end_price().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		iter=tmp.getBids().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		
        		iter=tmp.getBids_per_listings().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		iter=tmp.getItems_sold().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		
        		
        		iter=tmp.getRevenue().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		
        		iter=tmp.getTotal_listings().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        		
        		iter=tmp.getSell_through().getData().iterator();
        		while(iter.hasNext()){
        			Object[] ob=iter.next();
        			DateTime ntime=DateTime.parse(((String)ob[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));
        			if(ntime.isAfter(otime)){
        				break;
        			}
        			iter.remove();
        		}
        	
        		dbb.getData().addAverage_end_price(tmp.getAverage_end_price());
        		dbb.getData().addBids(tmp.getBids());
        		dbb.getData().addBids_per_listings(tmp.getBids_per_listings());
        		dbb.getData().addItems_sold(tmp.getItems_sold());
        		dbb.getData().addRevenue(tmp.getRevenue());
        		dbb.getData().addTotal_listings(tmp.getTotal_listings());
        		dbb.getData().addSell_through(tmp.getSell_through());
        	}else{
        		dbb.setData(tmp);
        	}
        }
		if(i>num){
			System.err.println("CategorySpider "+this.getEb().getId() + " handle finished.");
			super.stop();
			return;
		}
		SearchBean sb=getSearchBean();
        HashMap<String,String> pd=new HashMap<String,String>();
        pd.put(null, JSONObject.toJSONString(sb));
		request("/services/ebay/categories/trends?token=4e5396e3fe80ee1249a0b8147c08c5636a"
				+ "95579b274624fc6ce568ef3d2cdde5", HttpMethod.POST, 
					hs, pd, "https");
	}

	@Override
	protected void onBefore() {
		// TODO Auto-generated method stub
		StorageBean.Builder builder = StorageBean.Builder.newBuilder(OperationType.FIND, database, collection);
		EqualCondition<Object> con = new EqualCondition<Object>("id", this.getEb().getId());
		builder.addConditon(con);			
		StorageBean sb=builder.build();
		List<DataBean> re = null;
		try {
			re = this.getSpider().getDbmanager().find(sb, DataBean.class);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (re == null || re.isEmpty()) {
			System.out.println("CategorySpider " + this.getEb().getId() + " new data in on before.");
			return;
		}
		if(re.size()>0){
			TerapeakBean tb=re.get(0).getData();
			dbb.setData(tb);
			if(tb.getAverage_end_price().getData().size()>0){
				Object[] obs=tb.getAverage_end_price().getData().get(tb.getAverage_end_price().getData().size()-1);
				DateTime ntime=new DateTime();
				
				DateTime otime=DateTime.parse(((String)obs[0]).split(" ")[0],DateTimeFormat.forPattern("yyyy/MM/dd"));							
				int days=ntime.getDayOfYear()-otime.getDayOfYear()+360*(ntime.getYear()-otime.getYear());
				if(days<=0){
					try {
						stop();
					} catch (Exception e) {
					}
					return;
				}
				int tt=(days-1)/this.getDays()+1;
				this.setNum(tt);
				this.setI(this.getI()-1);
				HashMap<String,String> pd=new HashMap<String,String>();
				pd.put(null, JSONObject.toJSONString(this.getSearchBean()));
			    this.setPostdata(pd);
			}
		}
		
		System.out.println("CategorySpider " + this.getEb().getId() + " on before.");
	}

	public void onError(Object response){
		FullHttpResponse cres=(FullHttpResponse)response;
		String ss=cres.content().toString(Charset.forName("utf-8"));
		if(cres!=null&&cres.status().code()==500&&
				(ss.contains("GetCategoryTrends is empty")||ss.contains("TrendData is empty"))){
			if(errornum <5) {
				requestSelf();
				System.out.println("Errornum"+errornum+": "+this.getEb().getId()+"'s another try");
				errornum ++;
				return;
			}
			else {
				if(i>num){
					super.stop();
					return;
				}
				errornum = 0;
				System.err.println("CategorySpider " + this.getEb().getId() + ": Page"+i+" is on error after five try.");
				SearchBean sb=getSearchBean();
		        HashMap<String,String> pd=new HashMap<String,String>();
		        pd.put(null, JSONObject.toJSONString(sb));
				try {
					request("/services/ebay/categories/trends?token=4"
							+ "e5396e3fe80ee1249a0b8147c08c5636a95579b274624fc6ce568ef3d2cdde5", HttpMethod.POST, 
							hs, pd, "https");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}else{
			super.onError(response);
		}
	}
	
	@Override
	protected void onNormalFinished() {
		// TODO Auto-generated method stub
		this.getSpider().finish();
	}

	
	@Override
	protected void onDataFinished() {
		try {
			TerapeakBean tb=dbb.getData();
			if((tb!=null&&tb.getAverage_end_price()!=null&&tb.getAverage_end_price().getData()!=null&&
					tb.getAverage_end_price().getData().size()>0)){
				//System.err.println(JSONObject.toJSONString(tb));
				StorageBean.Builder builder = StorageBean.Builder.newBuilder(OperationType.UPORINSERT, database, collection);		
				dbb.setCatch_time(new DateTime().toString("yyyy-MM-dd HH:mm:ss"));
				EqualCondition<Object> con = new EqualCondition<Object>("id", eb.getId());
				builder.addConditon(con);			
				this.getSpider().getDbmanager().storage(builder, dbb);
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("In onDataFinished: exception happens.");
		}
		System.err.println("CategorySpider " + this.getEb().getId() + " onDataFinished.");
	}
	
	private void formatTerapeakCategorySoldData(TerapeakBean tmp) {
		CommonData cd=tmp.getItems_sold();
		if(cd==null||cd.getData()==null||cd.getData().size()==0){
			return;
		}
		double even=(cd.getMaxValue()+cd.getMinValue())/2;
		double even_guess=0;
		for(Object[] obs:cd.getData()){
			even_guess+=Double.parseDouble(obs[1].toString());
		}
		even_guess/=cd.getData().size();
		even/=even_guess;
		for(Object[] obs:cd.getData()){
			obs[1]=even*Double.parseDouble(obs[1].toString());
		}
	}
	private void formatTerapeakCategoryToalListingsData(TerapeakBean tmp) {
		CommonData cd=tmp.getTotal_listings();
		if(cd==null||cd.getData()==null||cd.getData().size()==0){
			return;
		}
		double even=(cd.getMaxValue()+cd.getMinValue())/2;
		double even_guess=0;
		for(Object[] obs:cd.getData()){
			even_guess+=Double.parseDouble(obs[1].toString());
		}
		even_guess/=cd.getData().size();
		even/=even_guess;
		for(Object[] obs:cd.getData()){
			obs[1]=even*Double.parseDouble(obs[1].toString());
		}
	}
	private void formatTerapeakCategoryRevenueData(TerapeakBean tmp) {
		CommonData cd=tmp.getRevenue();
		if(cd==null||cd.getData()==null||cd.getData().size()==0){
			return;
		}
		double even=(cd.getMaxValue()+cd.getMinValue())/2;
		double even_guess=0;
		for(Object[] obs:cd.getData()){
			even_guess+=Double.parseDouble(obs[1].toString());
		}
		even_guess/=cd.getData().size();
		even/=even_guess;
		for(Object[] obs:cd.getData()){
			obs[1]=even*Double.parseDouble(obs[1].toString());
		}
	}
	/**
	 * 
	 * @return
	 */
	public SearchBean getSearchBean(){
		DateTime dt = new DateTime();
        DateTime dt5 = dt.minusDays(days*num-days*i);  
        sr.setDate_range(days);
        sr.setDate(dt5.toString("yyyy-MM-dd"));
        i++;
        return sr;
	}

}
