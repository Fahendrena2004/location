package org.example.location_voiture.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class SchemaUpdateRunner implements CommandLineRunner {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute(
                    "ALTER TABLE clients ADD COLUMN IF NOT EXISTS avec_chauffeur BOOLEAN DEFAULT FALSE NOT NULL;");
            jdbcTemplate.execute("ALTER TABLE voitures DROP COLUMN IF EXISTS image_url;");
            System.out.println(">>> Database schema updated: Column changes applied.");
        } catch (Exception e) {
            System.err.println(">>> Schema update note: " + e.getMessage());
            // Probably already exists or other minor issue, ignoring to let app start
        }
    }
}
