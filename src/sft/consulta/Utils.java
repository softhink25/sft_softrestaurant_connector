/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package sft.consulta;

import java.awt.Component;
import java.awt.Container;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Morfeus, INC. Jos Ibarra Martnez Jopen Marzo 2010
 * @version 0.1 Beta
 */
public class Utils {

    public static final String ARCHIVO_CONFIGURACION = "system.properties";
    public static final DecimalFormat formatoNumerico = new DecimalFormat("###,##0.00");
    public static final DecimalFormat formatoMoneda = new DecimalFormat("$###,##0.00");
    public static final DecimalFormat formatoPorcentaje = new DecimalFormat("#0.00");
    public static final SimpleDateFormat formatoFecha = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat formatoTimeStamp = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public static byte[] byteArrayFromInputStream(InputStream is) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        return buffer.toByteArray();
    }

    public static Double calcularImporteImpuestos(JSONArray impuestos, Double importe, Integer tipo) {
        Double dImpuestos = 0.0;
        if (impuestos != null) {
            for (int i = 0; i < impuestos.length(); i++) {
                try {
                    JSONObject impuesto = impuestos.getJSONObject(i);
                    if (impuesto.optInt("ima_ambito") == tipo || impuesto.optInt("ima_ambito") == 3) {
                        Integer factor = 0;
                        if ("Traslado".equalsIgnoreCase(impuesto.getString("ima_traslado_retencion"))) {
                            factor = 1;
                        } else if ("Retencion".equalsIgnoreCase(impuesto.getString("ima_traslado_retencion"))) {
                            factor = -1;
                        }
                        Double nuevoImpuesto = impuesto.getDouble("ima_valor");
                        if ("Tasa".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                            dImpuestos += Utils.ObtenerImportePorcentaje(importe, nuevoImpuesto) * factor;
                            //dImpuestosPrecioNormal += Utils.ObtenerImportePorcentaje(dPrecio, nuevoImpuesto * factor);
                        } else if ("Cuota".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                            dImpuestos += (nuevoImpuesto * factor);
                            //dImpuestosPrecioNormal += (nuevoImpuesto * factor);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return dImpuestos;
    }

    public static Double calcularImporteImpuesto(JSONArray impuestos, Double importe, Integer tipo) {
        Double dImpuestos = 0.0;
        if (impuestos != null) {
            for (int i = 0; i < impuestos.length(); i++) {
                try {
                    JSONObject impuesto = impuestos.getJSONObject(i);
                    if (impuesto.optInt("ima_ambito") == tipo) {
                        Integer factor = 0;
                        if ("Traslado".equalsIgnoreCase(impuesto.getString("ima_traslado_retencion"))) {
                            factor = 1;
                        } else if ("Retencion".equalsIgnoreCase(impuesto.getString("ima_traslado_retencion"))) {
                            factor = -1;
                        }
                        Double nuevoImpuesto = impuesto.getDouble("ima_valor");
                        if ("Tasa".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                            dImpuestos += Utils.ObtenerImportePorcentaje(importe, nuevoImpuesto) * factor;
                            //dImpuestosPrecioNormal += Utils.ObtenerImportePorcentaje(dPrecio, nuevoImpuesto * factor);
                        } else if ("Cuota".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                            dImpuestos += (nuevoImpuesto * factor);
                            //dImpuestosPrecioNormal += (nuevoImpuesto * factor);
                        }
                    }
                } catch (JSONException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return dImpuestos;
    }

    public static Double calcularPrecioSinImpuestos(JSONArray impuestos, Double importe, Integer tipo) {
        Double dImpuestos = 0.0;
        if (impuestos != null) {
            for (int i = 0; i < impuestos.length(); i++) {
                try {
                    JSONObject impuesto = impuestos.getJSONObject(i);
                    if (impuesto.optInt("ima_ambito") == (tipo == 1 ? 2 : 1)) {
                        continue;
                    }
                    Integer factor = -1;
                    Double nuevoImpuesto = impuesto.getDouble("ima_valor");
                    if ("Tasa".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                        dImpuestos += Utils.ObtenerImporteReduccionPorcentaje(importe, nuevoImpuesto) * factor;
                        //dImpuestosPrecioNormal += Utils.ObtenerImportePorcentaje(dPrecio, nuevoImpuesto * factor);
                    } else if ("Cuota".equalsIgnoreCase(impuesto.optString("ima_rango_fijo"))) {
                        dImpuestos += (nuevoImpuesto * factor);
                        //dImpuestosPrecioNormal += (nuevoImpuesto * factor);
                    }

                } catch (JSONException ex) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        }
        return importe + dImpuestos;
    }

    public static String obtenConfiguracion(String sConfiguraciones, String sConfiguracion) {
        ResourceBundle rs = ResourceBundle.getBundle(sConfiguraciones);
        if (rs.containsKey(sConfiguracion)) {
            return rs.getString(sConfiguracion);
        } else {
            return "Configuraci�n no encontrada";
        }
    }

    public static String etiqueta(String sConfiguracion) {
        String sConfiguraciones = "proyecto.configuracion.etiquetas";
        ResourceBundle rs = ResourceBundle.getBundle(sConfiguraciones);
        if (rs.containsKey(sConfiguracion)) {
            return rs.getString(sConfiguracion);
        } else {
            return "Configuraci�n no encontrada";
        }
    }

    /**
     * Leer recurso en un archivo .properties
     *
     * @param sArchivo nombre del archivo properties
     * @param sRecurso nombre del recurso a ser leeido
     * @return String valor de la cadena.
     */
    public static String _fLeerRecurso(String sArchivo, String sRecurso) {
        ResourceBundle rs = ResourceBundle.getBundle(sArchivo);
        if (rs.containsKey(sRecurso)) {
            String sCadena = rs.getString(sRecurso);

            if (sCadena.contains("{") & sCadena.contains("}")) {
                List<String> parametros = new ArrayList<String>();
                ObtenerParametros(sCadena, parametros);
                for (String parametro : parametros) {
                    try {
                        String ValorParametro = rs.getString(parametro.substring(1, parametro.length() - 1));
                        sCadena = sCadena.replace(parametro, ValorParametro);
                    } catch (java.util.MissingResourceException e) {

                    }
                }
                return sCadena;
            } else {
                return rs.getString(sRecurso);
            }
        } else {
            return sRecurso;
        }

    }

    public static String Encriptar(String texto) {
        try {
            MessageDigest md;
            md = MessageDigest.getInstance("MD5");
            byte[] md5hash;
            md.update(texto.getBytes("iso-8859-1"), 0, texto.length());
            md5hash = md.digest();
            return convertToHex(md5hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    private static void ObtenerParametros(String cadena, List<String> parametros) {
        if (cadena.contains("{") & cadena.contains("}")) {
            String subCadena = cadena.substring(cadena.indexOf("{"), cadena.indexOf("}") + 1);
            parametros.add(subCadena);
            String cadenaRestante = cadena.substring(cadena.indexOf("}") + 1, cadena.length());
            if (cadenaRestante.contains("{") & cadenaRestante.contains("}")) {
                ObtenerParametros(cadenaRestante, parametros);
            }
        }
    }

    /**
     * Obtener una cadena.
     *
     * @param oValor Object
     * @return
     */
    public static String obtenString(Object oValor) {
        if (oValor == null) {
            return "";
        } else if (oValor.toString().equals("null")) {
            return "";
        } else if (oValor.getClass() == String.class) {
            return (String) oValor;
        }
        return String.valueOf(oValor);
    }

    /**
     *
     * @param separador
     * @param valores
     * @return
     */
    public static String obtenStringSeparado(String separador, Object... valores) {
        String resultado = "";
        for (Object valor : valores) {
            if (valor != null) {
                resultado += !"".equals(resultado) ? Utils.obtenString(valor) : "," + Utils.obtenString(valor);
            }
        }
        return resultado;
    }

    public static String obtenString(Object oValor, String sDefault) {
        if (oValor == null) {
            return sDefault;
        } else if (oValor.getClass() == String.class) {
            return (String) oValor;
        }
        return String.valueOf(oValor);
    }

    /**
     * Obtener fecha del día de hoy Formato sql
     *
     * @return Date fecha del día de hoy
     *
     */
    public static Date fObtenFechaSql() {
        java.util.Date utilFecha = new java.util.Date();
        Date sqlFecha = new Date(utilFecha.getTime());
        return sqlFecha;
    }

    public static Timestamp fObtenTimestamp() {
        java.util.Date utilFecha = new java.util.Date();
        Timestamp sqlFecha = new Timestamp(utilFecha.getTime());
        return sqlFecha;
    }

    /**
     * Obtener un Objeto de session.
     *
     * @param req objecto request
     * @param sObjeto nombre del objeto
     * @return Object Objeto Obtenido de session
     */
    /**
     * Obtener un entero
     *
     * @param oValor Objeto
     * @return Entero.
     */
    public static int obtenInt(Object oValor) {
        try {
            if (oValor == null) {
                return 0;
            } else if (oValor.getClass() == int.class) {
                return (Integer) oValor;
            } else if (oValor.getClass() == Double.class) {
                Double val = (Double) oValor;
                return val.intValue();

            }
            return oValor.equals("") ? 0 : Integer.parseInt(oValor.toString());
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    /**
     * obtener entero con un valor por default
     *
     * @param oValor
     * @param valorDefault
     * @return
     */
    public static int obtenInt(Object oValor, int valorDefault) {
        try {
            if (oValor == null) {
                return valorDefault;
            } else if (oValor.getClass() == int.class) {
                return (Integer) oValor;
            }
            return oValor.equals("") ? valorDefault : Integer.parseInt(oValor.toString());
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    /**
     * Obtener Long
     *
     * @param oValor
     * @return
     */
    public static Long obtenLong(Object oValor) {
        try {
            if (oValor == null) {
                return 0L;
            } else if (oValor.getClass() == Long.class) {
                return (Long) oValor;
            }
            return oValor.equals("") ? 0 : Long.parseLong(oValor.toString());
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    /**
     * Obtener Long
     *
     * @param oValor
     * @param valorDefault
     * @return
     */
    public static Long obtenLong(Object oValor, Long valorDefault) {
        try {
            if (oValor == null) {
                return valorDefault;
            } else if (oValor.getClass() == Long.class) {
                return (Long) oValor;
            }
            return oValor.equals("") ? valorDefault : Long.parseLong(oValor.toString());
        } catch (NumberFormatException e) {
            return valorDefault;
        }
    }

    /**
     * Obtener un doble
     *
     * @param oValor
     * @return
     */
    public static Double obtenDouble(Object oValor) {
        try {
            if (oValor instanceof String) {
                oValor = ((String) oValor).replace(",", "");
            }
            if (oValor == null) {
                return 0.0;
            } else if (oValor.getClass() == Double.class) {
                return (Double) oValor;
            }
            return Double.parseDouble(oValor.toString());
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static Double obtenDouble(Object oValor, Double defaultValue) {
        try {
            if (oValor == null) {
                return defaultValue;
            } else if (oValor.getClass() == Double.class) {
                return (Double) oValor;
            }
            return Double.parseDouble(oValor.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static Number obtenNumber(Object oValor) {
        try {
            if (oValor == null) {
                return 0;
            } else if (oValor instanceof Number) {
                return (Number) oValor;
            }
            return (Number) oValor;
        } catch (Exception e) {
            return 0.0;
        }
    }

    public static Number obtenNumber(Object oValor, Number defaultValue) {
        try {
            if (oValor == null) {
                return defaultValue;
            } else if (oValor instanceof Number) {
                return (Number) oValor;
            }
            return Double.parseDouble(oValor.toString());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Obtener InputStram
     *
     * @param oValor
     * @return
     */
    static InputStream obtenInputStream(Object oValor) {
        try {
            if (oValor == null) {
                return null;
            } else if (oValor instanceof InputStream) {
                return (InputStream) oValor;
            }
            return (InputStream) oValor;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Obtener Objeto
     *
     * @param oValor
     * @return
     */
    public static Object obtenObject(Object oValor) {
        return oValor != null ? oValor : null;
    }

    /**
     * funci�n para subir subir archivo
     *
     * @param sDireccion carpeta de alojamiento.
     * @param request
     * @param response
     * @return direcci�n completa del archivo en servidor.
     * @throws FileUploadException
     * @throws Exception
     */
    /**
     * obtener fecha formateada
     *
     * @param patternIn
     * @param patternOut
     * @param fecha
     * @return
     */
    public static String formatFecha(String patternIn, String patternOut, Object fecha) {
        String fechaFormat = "";
        if (fecha != null) {
            try {
                SimpleDateFormat formato = new SimpleDateFormat(patternIn);
                java.util.Date f1 = formato.parse(String.valueOf(fecha));
                formato = new SimpleDateFormat(patternOut);
                fechaFormat = formato.format(f1);
            } catch (ParseException e) {
                return "";
            }
        }
        return fechaFormat;
    }

    public static String formatFecha(String patternOut, java.util.Date fecha) {
        String fechaFormat = "";
        if (fecha != null) {
            SimpleDateFormat formato;
            formato = new SimpleDateFormat(patternOut);
            fechaFormat = formato.format(fecha);
        }
        return fechaFormat;
    }

    public static String formatFecha(String patternOut, Date fecha) {
        String fechaFormat = "";
        if (fecha != null) {
            SimpleDateFormat formato;
            formato = new SimpleDateFormat(patternOut);
            fechaFormat = formato.format(fecha);
        }
        return fechaFormat;
    }

    /**
     * obtener fecha
     *
     * @param oValor
     * @return
     */
    public static Date obtenDate(Object oValor) {
        return obtenDate(oValor, "yyyy-MM-dd HH:mm:ss");

    }

    public static Date obtenDate(Object oValor, String Format) {
        DateFormat format = new SimpleDateFormat(Format);
        if (oValor == null) {
            return null;
        } else if (oValor.getClass() == String.class) {
            try {
                java.util.Date fecha = format.parse(oValor.toString());
                return new Date(fecha.getTime());
            } catch (ParseException ex) {
                Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }

        } else if (oValor.getClass() == Timestamp.class) {
            Timestamp ts = (Timestamp) oValor;
            return new Date(ts.getTime());
        } else if (oValor.getClass() == java.util.Date.class) {
            return new Date(((java.util.Date) oValor).getTime());
        } else if (oValor.getClass() == Date.class) {
            return (Date) oValor;
        }

        return Date.valueOf(oValor.toString());
    }

    public static Date obtenDate(String oValor) {
        return obtenDate(oValor, "yyyy-MM-dd HH:mm:ss");
    }

    public static Date obtenDate(String oValor, String formato) {
        if (oValor == null) {
            return null;
        }
        DateFormat format = new SimpleDateFormat(formato);
        try {
            return new Date(format.parse(oValor).getTime());
        } catch (ParseException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public static Timestamp obtenTimestamp(Object oValor) {
        if (oValor == null) {
            return null;
        } else if (oValor.getClass() == Timestamp.class) {
            Timestamp ts = (Timestamp) oValor;
            return ts;
        }
        return Timestamp.valueOf(oValor.toString());
    }

    public static Boolean obtenBoolean(Object object) {
        if (object == null) {
            return null;
        } else if (object.getClass() == Boolean.class) {
            return (Boolean) object;
        } else if (object.getClass() == int.class || object.getClass() == Integer.class) {
            return Utils.obtenInt(object, 0) == 1;
        } else if (object.getClass() == String.class) {
            if ("1".equals(object.toString())) {
                return true;
            } else if ("0".equals(object.toString())) {
                return false;
            } else {
                return Boolean.valueOf(object.toString());
            }
        }
        return Boolean.valueOf(object.toString());
    }

    public static Boolean obtenBoolean(Object object, Boolean defaultValue) {
        if (object == null) {
            return defaultValue;
        } else if (object.getClass() == Boolean.class) {
            return (Boolean) object;
        } else if (object.getClass() == int.class || object.getClass() == Integer.class) {
            return Utils.obtenInt(object, 0) == 1;
        } else if (object.getClass() == String.class) {
            if ("1".equals(object.toString())) {
                return true;
            } else if ("0".equals(object.toString())) {
                return false;
            } else {
                return Boolean.valueOf(object.toString());
            }
        }
        return Boolean.valueOf(object.toString());

    }

    /**
     * concatenar un campo de una lista de entidades
     *
     * @param lista
     * @param sCampo
     * @param sSeparador
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws NoSuchMethodException
     */
    /**
     * Concatenar un arraylist<String>
     *
     * @param lista
     * @param sSeparador
     * @return cadena separadas por un caracter
     */
    public static String ConcatenarPorCampoEntidad(ArrayList<String> lista, String sSeparador) {
        int i = 0;
        String resultado = "";
        for (String val : lista) {
            if (i == 0) {
                resultado = val;
            } else {
                resultado += "," + val;
            }
            i++;
        }

        return resultado;
    }

    public static String obtenerFormatoMoneda(Double dCantidad) {

        return obtenerFormatoNumero(dCantidad, "$#,##0.00");
    }

    public static String obtenerFormatoNumero(Double dCantidad, String sFormato) {
        DecimalFormat df = new DecimalFormat(sFormato);
        return df.format(dCantidad);
    }

    public static String obtenerFormatoPorcentaje(Double dCantidad) {
        NumberFormat percentFormat = NumberFormat.getPercentInstance();
        percentFormat.setMaximumFractionDigits(2);
        return percentFormat.format(dCantidad);
    }

    public static String reemplazaToJSON(String cadena) {
        cadena = cadena.replace("\n", "\\\n ");
        return cadena;
    }

    public static java.util.Date obtenerPrimerDiaMes(java.util.Date fecha) {
        try {
            if (fecha == null) {
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMinimum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static java.util.Date obtenerPrimerDiaAno(java.util.Date fecha) {
        try {
            if (fecha == null) {
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(cal.get(Calendar.YEAR), Calendar.JANUARY, 1);
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static java.util.Date obtenerPrimerDiaAno(java.sql.Date fecha) {
        try {
            java.util.Date f = new java.util.Date();
            f.setTime(fecha.getTime());
            return obtenerPrimerDiaAno(f);
        } catch (Exception e) {
            return null;
        }
    }

    public static java.util.Date obtenerUltimoDiaMes(java.util.Date fecha) {
        try {
            if (fecha == null) {
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH));
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static java.util.Date obtenerUltimoDiaAno(java.util.Date fecha) {
        try {
            if (fecha == null) {
                return null;
            }
            Calendar cal = Calendar.getInstance();
            cal.setTime(fecha);
            cal.set(cal.get(Calendar.YEAR), Calendar.DECEMBER, 31);
            //cal.set(Calendar.DAY_OF_YEAR, Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_YEAR)); 
            return cal.getTime();
        } catch (Exception e) {
            return null;
        }
    }

    public static java.util.Date obtenerUltimoDiaAno(java.sql.Date fecha) {
        try {
            try {
                java.util.Date f = new java.util.Date();
                f.setTime(fecha.getTime());
                return obtenerUltimoDiaAno(f);
            } catch (Exception e) {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static Integer obtenerDiferenciaEnDias(Date fechaIn, Date fechaFin) {
        try {
            SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
            Period period = Period.between(LocalDate.parse(sdf.format(fechaIn)), LocalDate.parse(sdf.format(fechaFin)));
            return (period.getYears()*365)+(period.getMonths()*30)+ period.getDays();

        } catch (Exception e) {
            return 0;
        }
    }

    public static Integer obtenerNumeroMeses(java.util.Date fechaIn, java.util.Date fechaFin) {
        try {
            Calendar c1 = Calendar.getInstance();
            c1.setTime(fechaIn);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(fechaFin);
            int diff = 1;
            if (c2.after(c1)) {
                while (c2.after(c1)) {
                    c1.add(Calendar.MONTH, 1);
                    if (c2.after(c1)) {
                        diff++;
                    }
                }
            } else if (c2.before(c1)) {
                while (c2.before(c1)) {
                    c1.add(Calendar.MONTH, -1);
                    if (c1.before(c2)) {
                        diff--;
                    }
                }
            }
            return diff;
        } catch (Exception e) {
            return 0;
        }
    }

    public static String generateString(int length) {
        Random random = new Random();
        String baseString = "QWERTYUIOPASDFGHJKLZXCVBNMqwertyuiopasdfghjkklzxcvbnm1234567890!#$%&/";
        char[] text = new char[length];
        for (int i = 0; i < length; i++) {
            text[i] = baseString.charAt(random.nextInt(baseString.length()));
        }
        return new String(text);
    }

    public static String obtenerValorJson(String value) {
        if (value == null || value.length() == 0) {
            return "\"\"";
        }

        char c = 0;
        int i;
        int len = value.length();
        StringBuilder sb = new StringBuilder(len + 4);
        String t;

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            c = value.charAt(i);
            switch (c) {
                case '\\':
                case '"':
                    sb.append('\\');
                    sb.append(c);
                    break;
                case '/':
                    //                if (b == '<') {
                    sb.append('\\');
                    //                }
                    sb.append(c);
                    break;
                case '\b':
                    sb.append("\\b");
                    break;
                case '\t':
                    sb.append("\\t");
                    break;
                case '\n':
                    sb.append("\\n");
                    break;
                case '\f':
                    sb.append("\\f");
                    break;
                case '\r':
                    sb.append("\\r");
                    break;
                default:
                    if (c < ' ') {
                        t = "000" + Integer.toHexString(c);
                        sb.append("\\u" + t.substring(t.length() - 4));
                    } else {
                        sb.append(c);
                    }
            }
        }
        sb.append('"');
        return sb.toString();
    }

    public static List<Component> getAllComponents(final Container c) {
        Component[] comps = c.getComponents();
        List<Component> compList = new ArrayList<>();
        for (Component comp : comps) {
            compList.add(comp);
            if (comp instanceof Container) {
                compList.addAll(getAllComponents((Container) comp));
            }
        }
        return compList;
    }

    public static Double SumarPorcentaje(Double importe, Double porcentaje) {
        Double resultado = 0.0;
        if (porcentaje > 0.0) {
            resultado = (importe * (1.0 + porcentaje));
        }
        return resultado;
    }

    public static Double ObtenerImporteReduccionPorcentaje(Double importe, Double porcentaje) {
        Double reduccion = 0.0;
        if (porcentaje > 0.0) {
            reduccion = importe / (1.0 + (porcentaje / 100));
        }
        return importe - reduccion;
    }

    public static Double ObtenerImporteReduccionPorcentaje(Double importe, Integer porcentaje) {
        Double reduccion = 0.0;
        if (porcentaje > 0.0) {
            reduccion = importe / (1.0 + (porcentaje / 100));
        }
        return importe - reduccion;
    }

    public static Double ObtenerImportePorcentaje(Double importe, Integer porcentaje) {
        BigDecimal resultado = null;
        resultado = new BigDecimal(importe * porcentaje / 100);
        if (resultado == null) {
            return 0.0;
        } else {
            return resultado.doubleValue();
        }
    }

    public static Double ObtenerImportePorcentaje(Double importe, Double porcentaje) {
        BigDecimal resultado = null;
        if (porcentaje > 0.0 && importe > 0.0) {
            resultado = new BigDecimal(importe * (porcentaje / 100));//.setScale(3, RoundingMode.UP);
        }
        if (resultado == null) {
            return 0.0;
        } else {
            return resultado.doubleValue();
        }
    }

    public static Double RedondearDecimales(Double Valor, Double factor, Double tolerancia) {
        if (Valor == null || factor == null || tolerancia == null) {
            return 0.0;
        }
        Long longValue = Valor.longValue();
        Double decimal = (Valor - longValue);
        if (decimal <= tolerancia) {
            return longValue.doubleValue();
        } else if (decimal > factor) {
            return longValue + 1.0;
        } else {
            return factor + longValue;
        }
    }

    public static Double Redondear(Double dValor, Integer decimales) {
        if (dValor == null) {
            return null;
        }
        BigDecimal resultado = new BigDecimal(dValor).setScale(decimales, RoundingMode.HALF_EVEN);
        return resultado.doubleValue();
    }

    public static String formatoNumerico(Double dValor, Integer decimales) {

        try {
            String format = String.format("#0.%0" + decimales + "d", 0);
            DecimalFormat df = new DecimalFormat(format, new DecimalFormatSymbols(Locale.US));
            if (dValor == null || Double.isNaN(dValor)) {
                dValor = 0.0;
            }
            //BigDecimal resultado = new BigDecimal(dValor).setScale(decimales, RoundingMode.HALF_EVEN);
            return df.format(dValor);
        } catch (NumberFormatException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "";
    }

    public static String formatoMoneda(Double dValor, Integer decimales) {

        try {
            String format = String.format("$###,##0.%0" + decimales + "d", 0);
            DecimalFormat df = new DecimalFormat(format);
            if (dValor == null || Double.isNaN(dValor)) {
                dValor = 0.0;
            }
            BigDecimal resultado = new BigDecimal(dValor).setScale(decimales, RoundingMode.HALF_EVEN);
            return df.format(resultado.doubleValue());
        } catch (NumberFormatException e) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
        }
        return "";
    }

    public static void writeProperty(String key, String value) {
        writeProperty(ARCHIVO_CONFIGURACION, key, value);
    }

    public static void writeProperty(String archivo, String key, String value) {
        Properties prop = new Properties();
        OutputStream output = null;
        InputStream input = null;
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                f.createNewFile();
            }
            input = new FileInputStream(f);
            prop.load(input);
            prop.setProperty(key, value);
            output = new FileOutputStream(f);
            prop.store(output, null);
            output.flush();
        } catch (IOException io) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, io);
            //io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
    }

    public static void deleteProperty(String key) {
        deleteProperty(ARCHIVO_CONFIGURACION, key);
    }

    public static void deleteProperty(String archivo, String key) {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                f.createNewFile();
                return;
            }
            input = new FileInputStream(f);
            prop.load(input);
            if (prop.containsKey(key)) {
                prop.remove(key);
            } else {

            }

        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }

    }

    public static String readProperty(String key) {
        return readProperty(key, "");
    }

    public static String readProperty(String key, String defaulValue) {
        return readProperty(ARCHIVO_CONFIGURACION, key, defaulValue);
    }

    public static String readProperty(String archivo, String key, String defaulValue) {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                f.createNewFile();
                writeProperty(key, defaulValue);
                return defaulValue;
            }
            input = new FileInputStream(f);
            prop.load(input);
            if (prop.containsKey(key)) {
                return prop.getProperty(key);
            } else {
                writeProperty(key, defaulValue);
                return defaulValue;
            }

        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
        return "";
    }

    public static Boolean hasProperty(String key) {
        return hasProperty(ARCHIVO_CONFIGURACION, key);
    }

    public static Boolean hasProperty(String archivo, String key) {
        InputStream input = null;
        Properties prop = new Properties();
        try {
            File f = new File(archivo);
            if (!f.exists()) {
                f.createNewFile();
                return false;
            }
            input = new FileInputStream(f);
            prop.load(input);
            if (prop.containsKey(key)) {
                return true;
            } else {
                return false;
            }

        } catch (IOException ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, e);
                }
            }
        }
        return false;
    }

    public static InputStream obtenerInputStream(byte[] array) {
        return new ByteArrayInputStream(array);
    }

    public static InputStream getPath(String archivo) throws URISyntaxException {
        InputStream url = Utils.class.getClassLoader().getResourceAsStream(archivo);
        if (url == null) {
            url = Utils.class.getClassLoader().getResourceAsStream(archivo);
        }
        if (url == null) {
            return null;
        }
        return url;
    }

    public static String generateNumberSigns(int n) {
        String s = "";
        for (int i = 0; i < n; i++) {
            s += "#";
        }
        return s;
    }

    public static void notificaError(String error, String destinatario) {
        try {
            SendEmail sendMail;
            sendMail = new SendEmail(Utils.class.getResourceAsStream("/mx/proyecto/configuracion/correo.properties"));
            sendMail.EnviarEmail(error, "Ha ocurrido un error en la importación. SFT-Facturación.com", destinatario, "notification@softhink.mx");
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void notificar(String mensaje, String titulo, String destinatario) {
        try {
            SendEmail sendMail;
            sendMail = new SendEmail(Utils.class.getResourceAsStream("/mx/proyecto/configuracion/correo.properties"));
            sendMail.EnviarEmail(mensaje, titulo, destinatario, "notification@softhink.mx");
        } catch (Exception ex) {
            Logger.getLogger(Utils.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
