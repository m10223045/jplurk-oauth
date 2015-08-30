package com.google.jplurk_oauth.example;

import java.sql.Connection;
//import java.sql.Date;
import java.util.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.microsoft.sqlserver.jdbc.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.jplurk_oauth.Offset;
import com.google.jplurk_oauth.Qualifier;
import com.google.jplurk_oauth.module.Polling;
import com.google.jplurk_oauth.module.Responses;
import com.google.jplurk_oauth.module.Search;
import com.google.jplurk_oauth.module.Timeline;
import com.google.jplurk_oauth.module.Users;
import com.google.jplurk_oauth.skeleton.Args;
import com.google.jplurk_oauth.skeleton.DateTime;
import com.google.jplurk_oauth.skeleton.PlurkOAuth;
import com.google.jplurk_oauth.skeleton.RequestException;

public class Example {
	Date dateEnd;
	SimpleDateFormat simple = new SimpleDateFormat();
	
    public static void main(String[] args) throws RequestException, Exception {
    	Properties prop = System.getProperties();
        PlurkOAuth auth = new PlurkOAuth(
        		"JfX4W3RzmAVq", "LlFThUL92Wo381pFRvOij16EHn8FUlR3", 
                "l19hgWWp3Mun", "AuZrdLduS5EMYlrihiTiEcOKqwKHIBri");
        Example e=new Example();
        
        //e.tagFined(auth); 
        e.carwlerPlurks(auth,"2014-09-09T23:59:59","2014-05-026T00:00:00");
        e.removeOutLimited("2014-05-026T00:00:00");
    }
    
    /**
     * 
     */
    public Example(){
    	simple.applyPattern("yyyy-MM-dd HH:mm:ss");    	    
    }
    
    /**
     * Remove out datetiime interval data.
     * @param limited
     * @throws RequestException
     * @throws Exception
     */
    public void removeOutLimited(String limited)throws RequestException, Exception {       
    	Connection con = null;
        PreparedStatement pst = null; 
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/plurk","root","idsl");
        String sql = "Select * from plurks_table";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        Date datelimited = simple.parse(limited.replaceAll("T", " "));
        
        while(rs.next()){
        	Date date = simple.parse(TransformDatatime(rs.getString("posted")).replaceAll("T", " "));
        	if(datelimited.compareTo(date)>0){
        		System.out.println(rs.getInt("id")+"     limited:"+datelimited+"    >     date:"+date);
        		
        		sql="DELETE FROM plurks_table WHERE id = ?";
            	pst = con.prepareStatement(sql);
            	pst.setInt(1, rs.getInt("id"));
            	pst.executeUpdate();      		
        	}       	
        }       
        con.close();
        System.out.println("remove End");
    }
    
    /**
     * 將以取得過噗浪資料的使用者做標記(True)，並且將無效帳號的也上標記(False)。
     * @param auth
     * @throws RequestException
     * @throws Exception
     */
    public void tagFined(PlurkOAuth auth)throws RequestException, Exception {
    	long user_id;  	 	
    	Connection con = null;
        PreparedStatement pst = null; 
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/plurk","root","idsl");
        String sql = "Select * from plurk_word where finded is null";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        
        while(rs.next()){
        	try{
        		user_id=getUserID(auth,rs.getString("plurk_word"));           	
            	sql = "Select * from plurks_table where user_id='"+user_id+"'";
            	Statement stmt2 = con.createStatement();
            	ResultSet rs2 = stmt2.executeQuery(sql);
            	
            	if(rs2.next()){
            		sql="UPDATE plurk_word SET finded=? where plurk_word=?";
                	pst = con.prepareStatement(sql);
                	pst.setString(1, "true");
                	pst.setString(2, rs.getString("plurk_word"));
                	pst.executeUpdate();
                	System.out.println("Update tagFined is True");
            	}
            	         	
            	System.out.println("nickname="+rs.getString("plurk_word")+" user_id="+user_id);       	
        	}catch(Exception e){ 
        		sql="UPDATE plurk_word SET finded=? where plurk_word=?";
            	pst = con.prepareStatement(sql);
            	pst.setString(1, "False");
            	pst.setString(2, rs.getString("plurk_word"));
            	pst.executeUpdate();
            	System.out.println("Update tagFined is False");       		
        		System.out.println("***Not Fined nick:"+rs.getString("plurk_word")+" ***"+e);
        	}
        	System.out.println("");
        }
        
        con.close();
    	pst.close();
    	System.out.println("**** Tag End ****");
    }
    
    /**
     * Start carwlerPlurks
     * @param auth
     * @param start
     * @param end
     * @throws RequestException
     * @throws Exception
     */
    public void carwlerPlurks(PlurkOAuth auth,String startDatetime,String endDatetime) throws RequestException, Exception {
    	long user_id;
        JSONObject json;
        Connection con = null;
        PreparedStatement pst = null; 
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/plurk","root","idsl");
        String sql = "Select * from plurk_word where finded is null";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
                   
        while(rs.next()){
        	try{
        		user_id=getUserID(auth,rs.getString("plurk_word"));            	           	
            	getPlurks(auth,user_id,startDatetime,endDatetime);
            	
            	System.out.println("nickname="+rs.getString("plurk_word")+" user_id="+user_id);       	
        	}catch(Exception e){        		
        		System.out.println("***Not Fined nick:"+rs.getString("plurk_word")+" ***"+e);
        	}
        	System.out.println("");
        }  
        
        con.close();
        System.out.println("**** crawelPlurks End ****");
    }
    
    /**
     * Use nickname search user_id.
     * @param auth
     * @param nickname
     * @return Long
     * @throws Exception
     */    
    public Long getUserID(PlurkOAuth auth,String nickname)throws Exception { 
    	JSONObject json;  	
    	json=auth.using(Search.class).userSearch(nickname);
    	return Long.parseLong(json.getJSONArray("users").getJSONObject(0).get("id").toString());    	
    }
    
    /**
     * Get user plurk data in a time interval.
     * @param auth
     * @param uid
     * @param StartDatetime
     * @param EndDatetime
     * @throws Exception
     */       
    public void getPlurks(PlurkOAuth auth,Long uid,String StartDatetime,String EndDatetime) throws Exception{
    	SimpleDateFormat simple = new SimpleDateFormat();
        simple.applyPattern("yyyy-MM-dd HH:mm:ss");
        Date datelimit = simple.parse(EndDatetime.replaceAll("T", " "));       
        String startDT=StartDatetime;
        
        do{
        	Args optional= new Args();
            optional.add("offset", startDT);
            optional.add("limit", "20");            
            JSONObject json=auth.using(Timeline.class).getPublicPlurks(uid, optional);
        	
            startDT =  insertDB(json.getJSONArray("plurks")); 
        	dateEnd = simple.parse(startDT.replaceAll("T", " "));    	      	
        }while(dateEnd.compareTo(datelimit)>0);      
        System.out.println("limit="+datelimit+"  dateEnd="+dateEnd);  	
    }
    
    /**
     * insert Plurk Data to DB and return final plurk Datetime.
     * @param jary
     * @return datetime 
     */  
    public String insertDB(JSONArray jary) throws SQLException, JSONException, ParseException{
    	String sql;
    	Connection con = DriverManager.getConnection("jdbc:mysql://140.125.84.76:3306/plurk","root","idsl");
        PreparedStatement pst = null;
                    
    	for (int i = 0; i < jary.length()-1; i++) {
        	JSONObject json2 = jary.getJSONObject(i);
        	       	
        	sql="insert into plurks_table("
        			+ "replurked,replurkers_count,favorers,qualifier_translated,replurker_id,"
        			+ "replurkable,qualifier,favorite,posted,lang,content,anonymous,plurk_type,limited_to,"
        			+ "replurkers,response_count,owner_id,content_raw,user_id,favorite_count,plurk_id,is_unread,"
        			+ "responses_seen,no_comments)  Values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)"; 
        	
        	pst = con.prepareStatement(sql);
        	pst.setString(1,json2.get("replurked").toString());//
        	pst.setInt(2, Integer.parseInt(json2.get("replurkers_count").toString()));
        	pst.setString(3,json2.get("favorers").toString());
        	pst.setString(4,json2.get("qualifier_translated").toString());
        	pst.setString(5,json2.get("replurker_id").toString());
        	pst.setString(6,json2.get("replurkable").toString());
        	pst.setString(7,json2.get("qualifier").toString());
        	pst.setString(8,json2.get("favorite").toString());
        	pst.setString(9,json2.get("posted").toString());
        	pst.setString(10,json2.get("lang").toString());
        	pst.setString(11,json2.get("content").toString());
        	pst.setString(12,json2.get("anonymous").toString());
        	pst.setString(13,json2.get("plurk_type").toString());
        	pst.setString(14,json2.get("limited_to").toString());
        	pst.setString(15,json2.get("replurkers").toString());
        	pst.setInt(16, Integer.parseInt(json2.get("response_count").toString()));
        	pst.setString(17,json2.get("owner_id").toString());
        	pst.setString(18,json2.get("content_raw").toString());
        	pst.setString(19,json2.get("user_id").toString());
        	pst.setInt(20, Integer.parseInt(json2.get("favorite_count").toString()));
        	pst.setString(21,json2.get("plurk_id").toString());
        	pst.setString(22,json2.get("is_unread").toString());
        	pst.setString(23,json2.get("responses_seen").toString());
        	pst.setString(24,json2.get("no_comments").toString());
        	pst.executeUpdate(); 
        	
        	if(i==0)System.out.println(json2.names()+"\n");
        	System.out.print(i+"  "+json2.get("posted"));
        	System.out.println("   "+json2.get("content"));
        }
	
    	pst.close();
    	con.close();   	
    	return TransformDatatime(jary.getJSONObject(jary.length()-2).get("posted").toString());
    }
    
    /**
     * Transform Plurk Datetime format to Java Datetime format.
     * @param datetime
     * @return datetime
     */      
    public String TransformDatatime(String datatime){
    	String[] temp=datatime.split(",");
    	temp=temp[1].trim().split(" ");
      	
    	String month="";    	
    	if(temp[1].equals("Jan")){
    		month="01";
    	}else if(temp[1].equals("Feb")){
    		month="02";
    	}else if(temp[1].equals("Mar")){
    		month="03";
    	}else if(temp[1].equals("Apr")){
    		month="04";
    	}else if(temp[1].equals("May")){
    		month="05";
    	}else if(temp[1].equals("Jun")){
    		month="06";
    	}else if(temp[1].equals("Jul")){
    		month="07";
    	}else if(temp[1].equals("Aug")){
    		month="08";
    	}else if(temp[1].equals("Sep")){
    		month="09";
    	}else if(temp[1].equals("Oct")){
    		month="10";
    	}else if(temp[1].equals("Nov")){
    		month="11";
    	}else if(temp[1].equals("Dec")){
    		month="12";
    	}  	  	
    	return temp[2]+"-"+month+"-"+temp[0]+"T"+temp[3];
    }   
}
