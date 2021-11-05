/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sft.softrest.connector;

import java.io.IOException;

import com.linuxense.javadbf.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.sql.Date;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import sft.consulta.ServiceRequest;

/**
 *
 * @author jose
 */
public class main {

    public static Date subtractDays(Date date, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.DATE, -days);
        return new Date(c.getTimeInMillis());
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
//        DBFReader reader = null;
        Date fechaFiltro = new Date(new java.util.Date().getTime());
        ArrayList<String> argumentos;
        String archivoConfiguracion = sft.consulta.Utils.ARCHIVO_CONFIGURACION;
        String nombreArchivo = sft.consulta.Utils.readProperty(archivoConfiguracion, "nombre_archivo", "tempcheques.dbf");
        if (args != null && args.length > 0) {
            archivoConfiguracion = args[0];
            nombreArchivo = sft.consulta.Utils.readProperty(archivoConfiguracion, "nombre_archivo", "tempcheques.dbf");
            if (args.length == 2) {
                fechaFiltro = sft.consulta.Utils.obtenDate(args[1], "yyyy-MM-dd");

            }
        }
        Integer diasMenos = sft.consulta.Utils.obtenInt(sft.consulta.Utils.readProperty(archivoConfiguracion, "dias_menos", "0"), 0);
        if (diasMenos > 0 && args.length < 2) {
            fechaFiltro = subtractDays(fechaFiltro, 1);
        }
        System.out.println("Procesando con archivo de configuración :" + archivoConfiguracion);
        System.out.println("fecha procesada :" + fechaFiltro);
        String sFolders = sft.consulta.Utils.readProperty(archivoConfiguracion, "bdd", "");//  "/home/jose/dbfs/Comalcalco/";
        if ("".equals(sFolders)) {
            System.out.println("Favor de verificar su archivo de configuración, no existe la dirección de las bases de datos.");
        }
        String[] folders = sFolders.split(";");
        try {

            JSONObject ventasJson = new JSONObject();
            JSONArray ventas = new JSONArray();
            for (Integer i = 0; i < folders.length; i++) {
                String sFolder = folders[i];
                File fldr;
                fldr = new File(sFolder);
                if (!fldr.exists()) {

                    System.out.println("No existe directorio" + sFolder);
                    continue;
                }
//                System.out.println("directorio" + sFolder);
                ArrayList<File> basesDeDatos = listFilesForFolder(fldr, nombreArchivo);
                for (File f : basesDeDatos) {
                    DBFReader reader = null;
                    try {
//                        System.out.println("Leer directorio :" + sFolder);
//                        System.out.println("archivo:" + f.getPath());
                        reader = new DBFReader(new FileInputStream(f));
                        Object[] rowObjects;
                        Integer jj = 0;

                        String Brincar = sft.consulta.Utils.readProperty(archivoConfiguracion, "brincar_registros", "0");
                        Double brDouble = sft.consulta.Utils.obtenDouble(Brincar);
                        if ((brDouble > 0 && brDouble < 1) && reader.getRecordCount() > 5000) {
                            reader.skipRecords(new Double(reader.getRecordCount() * brDouble).intValue());
                        }
                        while ((rowObjects = reader.nextRecord()) != null) {
//                            System.out.println(" Registro  : " + jj++);
                            JSONObject ventaJson = new JSONObject();
                            try {
                                java.util.Date fec = sft.consulta.Utils.obtenDate(rowObjects[3], sft.consulta.Utils.readProperty("formato_fecha", "dd/MM/yyyy"));
                                java.util.Date fec_cierre = sft.consulta.Utils.obtenDate(rowObjects[6], sft.consulta.Utils.readProperty("formato_fecha", "dd/MM/yyyy"));
                                Date fecha = new Date(fec.getTime());
                                Date fecha_cierre = new Date(fec_cierre.getTime());
                                if (sft.consulta.Utils.obtenerDiferenciaEnDias(fechaFiltro, fecha) != 0) {
//                                    System.out.println(" folio: " + rowObjects[2] + " fecha:" + fecha.toString());
                                    if (sft.consulta.Utils.obtenerDiferenciaEnDias(fechaFiltro, fecha) == -1) {
                                        if (sft.consulta.Utils.obtenerDiferenciaEnDias(fechaFiltro, fecha_cierre) == 0) {
                                            fecha = fecha_cierre;

                                        } else {
                                            continue;
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                                if (null == rowObjects[2] || "".equals(rowObjects[2])) {
//                                    System.out.println("folio null o vacio: ");
                                    continue;
                                }

                                String fop = "";
                                ventaJson.put("vfact_ticket", rowObjects[2]);
                                ventaJson.put("vfact_serie", rowObjects[1]);
                                ventaJson.put("vfact_fecha", fecha);
                                ventaJson.put("vfact_importe", rowObjects[49]);
                                ventaJson.put("vfact_pagado", rowObjects[10]);
                                ventaJson.put("vfact_cancelado", rowObjects[11]);
                                ventaJson.put("vfact_facturado", rowObjects[18]);
                                ventaJson.put("vfact_efectivo", rowObjects[70]);
                                ventaJson.put("vfact_tarjeta", rowObjects[71]);
                                ventaJson.put("vfact_total_impue", rowObjects[53]);
                                ventaJson.put("vfact_total", rowObjects[50]);
                                ventaJson.put("vfact_propina", rowObjects[50]);
                                ventaJson.put("vfact_descuento", rowObjects[15]);

                                if (ventaJson.optDouble("vfact_efectivo", 0.0) > 0.0) {
                                    fop += "01,";
                                }
                                if (ventaJson.optDouble("vfact_tarjeta", 0.0) > 0.0) {
                                    fop += "04,28";
                                }
                                ventaJson.put("vfact_fop", fop);
                                JSONArray impuestos = new JSONArray();
                                if (ventaJson.getDouble("vfact_total_impue") > 0) {
                                    JSONObject impuestoJson = new JSONObject();
                                    impuestoJson.put("ivf_clave_impuesto", sft.consulta.Utils.readProperty(archivoConfiguracion, "clave_impuesto", "002"));
                                    impuestoJson.put("ivf_tipo_impuesto", sft.consulta.Utils.readProperty(archivoConfiguracion, "tipo_impuesto", "IVA"));
                                    impuestoJson.put("ivf_valor_impuesto", sft.consulta.Utils.readProperty(archivoConfiguracion, "valor_impuesto", "0.16"));
                                    impuestoJson.put("ivf_naturaleza_impuesto", sft.consulta.Utils.readProperty(archivoConfiguracion, "naturaleza_impuesto", "TRAS"));
                                    impuestoJson.put("ivf_importe_impuesto", ventaJson.getDouble("vfact_total_impue"));
                                    impuestos.put(impuestoJson);

                                }
                                ventaJson.put("impuestos", impuestos);
                                ventas.put(ventaJson);

                            } catch (JSONException ex) {
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                                sft.consulta.Utils.notificaError(ex.getMessage(), sft.consulta.Utils.readProperty(archivoConfiguracion, "correoNotificacion", "jose.ibarra@sft.com.mx"));
                            } catch (Exception ex) {
                                sft.consulta.Utils.notificaError(ex.getMessage(), sft.consulta.Utils.readProperty(archivoConfiguracion, "correoNotificacion", "jose.ibarra@sft.com.mx"));
                                Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    } catch (FileNotFoundException ex) {
                        Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, ex);
                    } finally {
                        DBFUtils.close(reader);
                    }
                }
            }
            ventasJson.put("ventas", ventas);
//            ventasJson.toString()
            ServiceRequest.Sincronizar(sft.consulta.Utils.readProperty(archivoConfiguracion, "url_facturacion", "http://portal.sft-facturacion.com/webresources/CargaVentasWS/CargarSR"), ventasJson);
            if ("true".equals(sft.consulta.Utils.readProperty(archivoConfiguracion, "notificiar_ejecucion", "false"))) {
                
                sft.consulta.Utils.notificar("Registros guardados :" + ventasJson.toString(), "Tarea Ejecutada Correctamente", sft.consulta.Utils.readProperty(archivoConfiguracion, "correoNotificacion", "jose.ibarra@sft.com.mx"));
            }
        } catch (DBFException | IOException | JSONException e) {
            sft.consulta.Utils.notificaError(e.getMessage(), sft.consulta.Utils.readProperty(archivoConfiguracion, "correoNotificacion", "jose.ibarra@sft.com.mx"));
            Logger.getLogger(main.class.getName()).log(Level.SEVERE, null, e);
        } finally {
//            DBFUtils.close(reader);
        }
    }

    public static ArrayList<File> listFilesForFolder(final File folder, final String nombreArchivo) {
        ArrayList<File> bdds = new ArrayList<>();
        for (final File fileEntry : folder.listFiles()) {
            if (fileEntry.isDirectory()) {
                bdds.addAll(listFilesForFolder(fileEntry, nombreArchivo));
            } else {
                if (fileEntry.getName().toLowerCase().contains(nombreArchivo.toLowerCase()) && !fileEntry.getName().contains("lock.")) {
                    bdds.add(fileEntry);
//                    System.out.println(fileEntry.getName());
                }

            }
        }
        return bdds;
    }

}
//    public void readDBF() throws IOException, ParseException {
//        Charset stringCharset = Charset.forName("ISO-8859-1");
//
////        InputStream dbf = getClass().getClassLoader().getResourceAsStream("/home/jose/dbfs/Comalcalco/temptempcheques.dbf");
//
//    
////    }
//
//}
