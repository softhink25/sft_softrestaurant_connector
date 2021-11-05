/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sft.softrest.connector;

import com.dbschema.xbase.DbfJdbcDriver;
import java.sql.Connection; 
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger; 
/**
 *
 * @author jose
 */
public class jdbc {

    public static void main(String[] args) {
        try { 
            new DbfJdbcDriver(); 
            Connection con = DriverManager.getConnection("jdbc:dbschema:dbf:/home/jose/dbfs/cheques?version=dbase_5");
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("select * from cheques");
            while (rs.next()) {
                System.out.println("" + rs.getInt(0));
            }
         } catch (SQLException  ex) {
            Logger.getLogger(jdbc.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
