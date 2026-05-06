package org.example.location_voiture.service;

import org.example.location_voiture.model.GeneralSetting;
import org.example.location_voiture.repository.GeneralSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GeneralSettingService {

    @Autowired
    private GeneralSettingRepository generalSettingRepository;

    public String getSettingValue(String key, String defaultValue) {
        return generalSettingRepository.findBySettingKey(key)
                .map(GeneralSetting::getSettingValue)
                .orElse(defaultValue);
    }

    public void updateSetting(String key, String value) {
        GeneralSetting setting = generalSettingRepository.findBySettingKey(key)
                .orElse(new GeneralSetting());
        
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        generalSettingRepository.save(setting);
    }

    public double getExchangeRateEur() {
        String rate = getSettingValue("EXCHANGE_RATE_EUR", "4500");
        try {
            return Double.parseDouble(rate);
        } catch (NumberFormatException e) {
            return 4500.0;
        }
    }
}
