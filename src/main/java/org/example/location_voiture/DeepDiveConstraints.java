package org.example.location_voiture;
import java.sql.*;

public class DeepDiveConstraints {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/location_voiture_db";
        String user = "postgres";
        String password = "fafana";

        try (Connection conn = DriverManager.getConnection(url, user, password)) {
            System.out.println("Searching for all constraints on 'locations' table...");
            String sql = "SELECT conname, pg_get_constraintdef(oid) " +
                         "FROM pg_constraint " +
                         "WHERE conrelid = 'locations'::regclass";
            
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                
                while (rs.next()) {
                    System.out.println("Constraint: " + rs.getString(1));
                    System.out.println("Definition: " + rs.getString(2));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
