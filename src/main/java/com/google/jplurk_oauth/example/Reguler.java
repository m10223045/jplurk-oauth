package com.google.jplurk_oauth.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.jplurk_oauth.skeleton.RequestException;

public class Reguler {
	public static void main(String[] args) throws RequestException, Exception {
		//String matchScore="<span class=\"ddd\">P</span>                       0-0                <spanclass=\"eeee\">P</span>";
		String matchScore="從小孩那聽到一句話<br />\"天生我材必有用，你這蠢材請保重\"<br />";
		Pattern p=Pattern.compile("<[^>]+>([^<]*)</[^>]+>");
		Matcher m = p.matcher(matchScore);
		while(m.find()){
			matchScore=matchScore.replaceFirst("<[^>]+>([^<]*)</[^>]+>",m.group(1).toString());
		}
		//System.out.println("A:"+matchScore);
		
		Connection con = null;
        PreparedStatement pst = null; 
        con = DriverManager.getConnection("jdbc:mysql://localhost:3306/plurk","root","idsl");
        String sql = "Select * from plurks_table limit 100";
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);
        while(rs.next()){
        	String temp=rs.getString("content");/*
        	m = p.matcher(temp);
    		while(m.find()){
    			temp=matchScore.replaceFirst("<[^>]+>([^<]*)</[^>]+>",m.group(1).toString());
    		}*/
        	System.out.println(formatHtml(temp));        	
        }		
	}
	
	
	public static String formatHtml(String str) {
		if (str == null) {
			return "";
		}
		str = str.toLowerCase();
		str = str.replaceAll("</?span[^>]*>", "");
		str = str.replaceAll("&#[^>]*;", "");
		str = str.replaceAll("</?marquee[^>]*>", "");
		str = str.replaceAll("</?object[^>]*>", "");
		str = str.replaceAll("</?param[^>]*>", "");
		str = str.replaceAll("</?embed[^>]*>", "");
		str = str.replaceAll("</?table[^>]*>", "");
		str = str.replaceAll("&nbsp;", "");
		str = str.replaceAll("&gt;", "");
		str = str.replaceAll("&lt;", "");
		str = str.replaceAll("</?tr[^>]*>", "");
		str = str.replaceAll("</?th[^>]*>", "");
		str = str.replaceAll("</?p[^>]*>", "");
		str = str.replaceAll("</?a[^>]*>", "");
		str = str.replaceAll("</?img[^>]*>", "");
		str = str.replaceAll("</?tbody[^>]*>", "");
		str = str.replaceAll("</?li[^>]*>", "");
		str = str.replaceAll("</?div[^>]*>", "");
		str = str.replaceAll("</?td[^>]*>", "");
		str = str.replaceAll("</?script[^>]*>", "");
		str = str.replaceAll("(javascript|jscript|vbscript|vbs):", "");
		str = str.replaceAll("on(mouse|exit|error|click|key)", "");
		str = str.replaceAll("<\\?xml[^>]*>", "");
		str = str.replaceAll("<\\?[a-z]+:[^>]*>", "");
		str = str.replaceAll("</?font[^>]*>", "");
		str = str.replaceAll("</?b[^>]*>", "");
		str = str.replaceAll("</?strike[^>]*>", "");
		str = str.replaceAll("</?u[^>]*>", "");
		str = str.replaceAll("</?i[^>]*>", "");
		str = str.replaceAll("</?strong[^>]*>", "");
		str = str.replaceAll("</?(a|A)( .*?>|>)", "");
		str = str.replaceAll("xdd*", "");
		str = str.replaceAll("ww*", "");
		str = str.replaceAll("[http|https]://", "");
		return str.trim();
	}
}
