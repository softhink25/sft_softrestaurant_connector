/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sft.consulta;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author HP
 */
public class ServiceRequest {

    public static JSONObject sendPostResponse(String sUrl) {
        try {
            JSONObject resultado = null;
            JSONObject respuesta;
            InputStream in = null;
            try {

                URL url = new URL(sUrl);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=ISO-8859-1");
                urlConnection.setRequestProperty("acceso", Utils.readProperty("acceso"));
                urlConnection.setConnectTimeout(5120000);
                urlConnection.setReadTimeout(5120000);
                in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    //reponsBuilder checa los errores
                    responseStrBuilder.append(inputStr);
                    //System.out.println(responseStrBuilder);
                }
                return new JSONObject(responseStrBuilder.toString());

            } catch (FileNotFoundException | SocketTimeoutException ex) {
                Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    resultado = new JSONObject();
                    resultado.put("success", Boolean.FALSE);
                    resultado.put("message", "Error, No se ha podido establecer conexi�n con el servidor.");
                } catch (JSONException ex1) {

                }
            } catch (IOException | JSONException e) {
                try {
                    resultado = new JSONObject();
                    resultado.put("success", Boolean.FALSE);
                    resultado.put("message", e.getMessage());

                } catch (JSONException ex2) {
                    Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex2);
                }

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }

                } catch (IOException e) {
                    Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            respuesta = new JSONObject();
            respuesta.put("result", resultado);
            return respuesta;

        } catch (JSONException ex) {
            Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JSONObject sendPostRequest(String sUrl, JSONObject jsonRequest) {
        try {

            InputStream in = null;
            JSONObject resultado = null;
            JSONObject respuesta;
            try {
                URL url = new URL(sUrl);
                URLConnection urlConnection = url.openConnection();
                urlConnection.setDoOutput(true);
                urlConnection.setRequestProperty("Content-Type", "application/json; charset=ISO-8859-1");
                urlConnection.setRequestProperty("acceso", Utils.readProperty("acceso"));
                urlConnection.setConnectTimeout(5120000);
                urlConnection.setReadTimeout(5120000);
                OutputStreamWriter out = new OutputStreamWriter(urlConnection.getOutputStream(), StandardCharsets.ISO_8859_1);
                out.write(jsonRequest.toString());
                out.flush();
                out.close();
                in = new BufferedInputStream(urlConnection.getInputStream());
                BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
                StringBuilder responseStrBuilder = new StringBuilder();
                String inputStr;
                while ((inputStr = streamReader.readLine()) != null) {
                    responseStrBuilder.append(inputStr);
                    //System.out.println(responseStrBuilder);
                }
                return new JSONObject(responseStrBuilder.toString());
            } catch (FileNotFoundException | SocketTimeoutException ex) {
                Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex);
                try {
                    resultado = new JSONObject();
                    resultado.put("success", Boolean.FALSE);
                    resultado.put("message", "Error, No se ha podido establecer conexi�n con el servidor.");
                } catch (JSONException ex1) {

                }
            } catch (IOException | JSONException e) {
                try {
                    resultado = new JSONObject();
                    resultado.put("success", Boolean.FALSE);
                    resultado.put("message", e.getMessage());

                } catch (JSONException ex2) {
                    Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex2);
                }

            } finally {
                try {
                    if (in != null) {
                        in.close();
                    }
                } catch (IOException e) {
                    Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, e);
                }
            }
            respuesta = new JSONObject();
            respuesta.put("result", resultado);
            return respuesta;

        } catch (JSONException ex) {
            Logger.getLogger(ServiceRequest.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JSONArray getJsonArrayRecursive(Object param) throws JSONException {
        List<Object> list = (List<Object>) param;
        JSONArray jsonArray = new JSONArray();
        for (Object object : list) {
            HashMap<String, Object> jsonMap = (HashMap<String, Object>) object;
            Set<String> keyset = jsonMap.keySet();
            for (String key : keyset) {
                Object jsonElement = jsonMap.get(key);
                if (jsonElement instanceof List) {
                    jsonArray.put(getJsonArrayRecursive(object));
                } else {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(key, jsonElement);
                    jsonArray.put(jsonObject);
                }
            }
        }
        return jsonArray;
    }

    public static void Sincronizar(String Url, JSONObject datos) {
        Runnable ejecucion = () -> {
            Logger.getLogger(ServiceRequest.class.getName()).log(Level.INFO, "envia:" + datos.toString());
            JSONObject x = sendPostRequest(Url, datos);
            Logger.getLogger(ServiceRequest.class.getName()).log(Level.INFO, "recibe:" + x.toString());
            if (x.has("result") && x.optJSONObject("result").optBoolean("success", false) == false) {
                sft.consulta.Utils.notificaError(x.optJSONObject("result").optString("message", x.optJSONObject("result").optString("mensaje", "error")), sft.consulta.Utils.readProperty("correoNotificacion", "jose.ibarra@sft.com.mx"));
            }
        };
        Thread t = new Thread(ejecucion);
        t.start();

    }

}
