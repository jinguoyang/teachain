package com.youzi.teaChain.common;

import java.io.*;
import java.net.*;
import java.util.Map;

public class HttpPostGet {

    public static String getPost(String aimUrl, Object obj) {
        String response = null;
        try {
            URL url = new URL(aimUrl);

            StringBuilder postData = new StringBuilder();
            byte[] postDataBytes = null;
            if (obj instanceof Map) {
                Map<String, Object> map = (Map) obj;
                // 开始访问
                for (Map.Entry<String, Object> param : map.entrySet()) {
                    if (postData.length() != 0)
                        postData.append('&');
                    postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
                    postData.append('=');
                    postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
                }

                postDataBytes = postData.toString().getBytes("UTF-8");
            } else if (obj instanceof String) {
                String xmlString = (String) obj;
                postDataBytes = xmlString.getBytes("UTF-8");
            }

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
//            conn.setRequestProperty("Content-Type", "application/json");
//			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//            conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(postDataBytes);

            Reader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));

            StringBuilder sb = new StringBuilder();
            for (int c; (c = in.read()) >= 0;)
                sb.append((char) c);
            response = sb.toString();
            System.out.println(response);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return response;
    }
}
