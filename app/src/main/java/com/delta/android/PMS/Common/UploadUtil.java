package com.delta.android.PMS.Common;

import com.delta.android.Core.Common.Global;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UploadUtil {

    private Global _global = null;
    public UploadUtil(Global global){
        _global = global;
    }
    /**
     * android上傳檔案到伺服器
     *
     * @param RequestURL 請求的url
     * @param arData     上傳的檔案資訊
     * @return 返回響應的內容
     */
    public String uploadFile(String RequestURL, ArrayList arData) {
        String result = "";
        final String BOUNDARY = "==================================";
        final String HYPHENS = "--";
        final String CRLF = "\r\n";

        Gson gson = new Gson();
        HashMap<String, String> data = new HashMap<>();
        data.put("UPLOAD_FILE_DATA", gson.toJson(arData));

        try {
            URL url = new URL(RequestURL);

            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setConnectTimeout(5 * 1000);
            urlConn.setRequestProperty("UPLOAD_FILE_DATA",gson.toJson(arData));
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.setRequestProperty("connection", "keep-alive");

            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            urlConn.setRequestProperty("Authorization",_global.getToken());
            //urlConn.setRequestProperty("Authorization","bearer"+_global.getToken());

            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());

            for (int i = 0; i < arData.size(); i++) {
                //將檔案上傳
                File file = new File(((HashMap) arData.get(i)).get("LOCAL_FILE_PATH").toString());
                StringBuffer sb = new StringBuffer();
                sb.append(HYPHENS);
                sb.append(BOUNDARY);
                sb.append(CRLF);
                sb.append("Content-Disposition: form-data; name=" + file.getName() + "\"; filename=\"" + file.getName() + "\"" + CRLF); //檔案名稱
                sb.append(CRLF);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = -1;
                while ((len = is.read(bytes)) != -1) {
                    dos.write(bytes, 0, len);
                }

                dos.write(CRLF.getBytes());
                is.close();
            }

            byte[] end_data = (HYPHENS + BOUNDARY + HYPHENS + CRLF).getBytes();
            dos.write(end_data);
            dos.flush();
            dos.close();

            if (urlConn.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {
                InputStreamReader isr = new InputStreamReader(urlConn.getInputStream());
                BufferedReader br = new BufferedReader(isr);
                String inputLine;

                while ((inputLine = br.readLine()) != null) {
                    result += inputLine;
                }

                //更新Token
                List<String> cookies = urlConn.getHeaderFields().get("Set-Cookie");
                for(String s : cookies){
                    if(HttpCookie.parse(s).get(0).getName().equalsIgnoreCase("jwt")) {
                        _global.setToken(HttpCookie.parse(s).get(0).getValue());
                    }
                }

                isr.close();
                urlConn.disconnect();

                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "ERROR";
    }

    public void deleteFile(String RequestURL, ArrayList arData){
        String strJsonInput;
        final String BOUNDARY = "==================================";

        Gson gson = new Gson();
        HashMap<String, String> data = new HashMap<>();
        data.put("DELETE_FILE_DATA", gson.toJson(arData));

        strJsonInput = gson.toJson(data);

        try {
            URL url = new URL(RequestURL);

            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();
            urlConn.setDoInput(true);
            urlConn.setDoOutput(true);
            urlConn.setUseCaches(false);
            urlConn.setConnectTimeout(5 * 1000);
            urlConn.setRequestMethod("POST");
            urlConn.setRequestProperty("Charset", "UTF-8");
            urlConn.setRequestProperty("connection", "keep-alive");
            urlConn.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + BOUNDARY);
            urlConn.setRequestProperty("Authorization",_global.getToken());

            DataOutputStream dos = new DataOutputStream(urlConn.getOutputStream());
            dos.writeBytes(strJsonInput);

            if (urlConn.getResponseCode() == java.net.HttpURLConnection.HTTP_OK) {

                //更新Token
                List<String> cookies = urlConn.getHeaderFields().get("Set-Cookie");
                for(String s : cookies){
                    if(HttpCookie.parse(s).get(0).getName().equalsIgnoreCase("jwt")) {
                        _global.setToken(HttpCookie.parse(s).get(0).getValue());
                    }
                }

                urlConn.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
