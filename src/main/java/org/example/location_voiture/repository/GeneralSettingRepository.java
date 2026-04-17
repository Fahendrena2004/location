package org.example.location_voiture.repository;

import org.example.location_voiture.model.GeneralSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface GeneralSettingRepository extends JpaRepository<GeneralSetting, Long> {
    Optional<GeneralSetting> findBySettingKey(String key);
}
