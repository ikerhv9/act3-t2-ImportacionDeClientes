package org.example;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

    public static void main( String[] args ) {
        boolean realizado = crearTablaClientes2();
        if (realizado){
            traspasaClientes();
            importaClientes();
        }
    }

    private static void importaClientes() {
        Connection con = null;
        PreparedStatement insert;
        List<Cliente> listaClientes = null;
        try {
            con = ConexionBD.getConexion();
            listaClientes = cargarClientes();
            con.setAutoCommit(false);
            insert = con.prepareStatement("INSERT INTO clientes2 (nombre, direccion, ciudad, pais, telefono, contacto, password) VALUES (?, ?, ?, ?, ?, ?, ?)");
            for (Cliente c : listaClientes) {
                insert.setString(1, c.getNombre());
                insert.setString(2, c.getDireccion());
                insert.setString(3, c.getCiudad());
                insert.setString(4, c.getPais());
                insert.setString(5, c.getTelefono());
                insert.setString(6, c.getContacto());
                insert.setString(7, c.getPassword());
                insert.addBatch();
            }
            insert.executeBatch();
            con.commit();
        }catch (SQLException e){
            System.out.println("Error Batch: " + e.getMessage());
            try {
                con.rollback();
            }catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }

    private static ArrayList<Cliente> cargarClientes (){
        BufferedReader br = null;
        Connection con = null;
        ArrayList<Cliente> clientes = null;
        Cliente cliente = null;
        CSVParser parser = new CSVParserBuilder().withSeparator(';').build();
        try (Reader fileReader = new FileReader("nuevosclientes.csv");
             CSVReader csv = new CSVReaderBuilder(fileReader).withCSVParser(parser).withSkipLines(1).build()){
            String[] linea = csv.readNext();
            clientes = new ArrayList<>();

            while (linea != null){
                cliente = new Cliente();
                cliente.setNombre(linea[1]);
                cliente.setDireccion(linea[4]);
                cliente.setCiudad(linea[5]);
                cliente.setPais(linea[7]);
                cliente.setTelefono(linea[8]);
                cliente.setContacto(linea[2]);
                cliente.setPassword(generaContrasenya());
                clientes.add(cliente);
                linea = csv.readNext();
            }

        }catch (Exception e){
            System.out.println(e.getMessage());
        }
        return clientes;
    }

    private static void traspasaClientes() {
        Connection con = null;
        PreparedStatement insert;
        List<Cliente> listaClientes = null;
        try {
            try {
                con = ConexionBD.getConexion();
                listaClientes = getClientes(con);
                con.setAutoCommit(false);
                String sqlInsert = "INSERT INTO clientes2 (nombre, direccion, ciudad, pais, telefono, contacto, password) VALUES (?, ?, ?, ?, ?, ?, ?)";
                insert = con.prepareStatement(sqlInsert);
                for (Cliente c : listaClientes) {
                    insert.setString(1, c.getNombre());
                    insert.setString(2, c.getDireccion());
                    insert.setString(3, c.getCiudad());
                    insert.setString(4, c.getPais());
                    insert.setString(5, c.getTelefono());
                    insert.setString(6, c.getContacto());
                    insert.setString(7, c.getPassword());
                    insert.executeUpdate();
                }
                con.commit();
            }catch (SQLException e){
                assert con != null;
                con.rollback();
                System.out.println(e.getMessage());
            }finally {
                assert con != null;
                con.setAutoCommit(true);
            }
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private static List<Cliente> getClientes(Connection con) {
        PreparedStatement select;
        List<Cliente> listaClientes = null;
        try {
            String sql = "SELECT * FROM clientes";
            con = ConexionBD.getConexion();
            select = con.prepareStatement(sql);
            listaClientes = new ArrayList<>();
            ResultSet rs = select.executeQuery();
            while (rs.next()) {
                listaClientes.add(new Cliente(rs.getInt("id"), rs.getString("nombre"), rs.getString("direccion"),generaContrasenya()));
            }
            rs.close();
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return listaClientes;
    }

    private static boolean crearTablaClientes2() {
        Connection con = null;
        PreparedStatement create = null;
        PreparedStatement drop = null;
        int creada = 0;
        try {
            String sqlDrop = "drop table if exists clientes2";
            String sql = "create table if not exists clientes2(\n" +
                    "\tid int auto_increment primary key,\n" +
                    "    nombre varchar(60),\n" +
                    "    direccion varchar(50),\n" +
                    "    ciudad varchar(30),\n" +
                    "    pais varchar(30),\n" +
                    "    telefono varchar(30),\n" +
                    "    contacto varchar(30),\n" +
                    "    password char(10)\n" +
                    ")";
            con = ConexionBD.getConexion();
            drop = con.prepareStatement(sqlDrop);
            drop.executeUpdate();
            create = con.prepareStatement(sql);
            create.executeUpdate();
            return true;
        }catch (SQLException e){
            System.out.println(e.getMessage());
        }
        return false;
    }

    private static String generaContrasenya() {
        StringBuilder sb;
        sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            int c = (int) Math.floor(Math.random() * 85 + 40);
            if (c==94 || c==96) continue;
            sb.append((char) c);
        }
        return sb.toString();
    }
}
