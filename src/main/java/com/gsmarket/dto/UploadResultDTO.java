package com.gsmarket.dto;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UploadResultDTO {
	
	private String fileName;
	private String uuid;
	private String folderPath;
	
	//나중에 이미지를 업로드시 이미지 패스 경로를 리턴하는 메서드하나 정의합니다.
	public String getImageURL() {
		try {
			return URLEncoder.encode(folderPath+"/"+uuid+"_"+fileName,"UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		return "";
	}
	public String getThumbnailURL(){
	     try {
	         return URLEncoder.encode(folderPath+"/s_"+uuid+"_"+fileName,"UTF-8");
	     } catch (UnsupportedEncodingException e) {
	         e.printStackTrace();
	     }
	     return "";
	 }

}
