package org.example.location_voiture.service;

import org.example.location_voiture.model.GeneralSetting;
import org.example.location_voiture.repository.GeneralSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class SettingService {

    @Autowired
    private GeneralSettingRepository settingRepository;

    @PostConstruct
    public void init() {
        if (!settingRepository.findBySettingKey("EXCHANGE_RATE_EUR_MGA").isPresent()) {
            GeneralSetting rate = new GeneralSetting();
            rate.setSettingKey("EXCHANGE_RATE_EUR_MGA");
            rate.setSettingValue("5000"); // Par défaut 1€ = 5000 Ar
            rate.setDescription("Taux de change Euro vers Ariary");
            settingRepository.save(rate);
        }
    }

    public double getExchangeRate() {
        return Double.parseDouble(settingRepository.findBySettingKey("EXCHANGE_RATE_EUR_MGA")
                .map(GeneralSetting::getSettingValue)
                .orElse("5000"));
    }

    public void updateExchangeRate(double rate) {
        GeneralSetting setting = settingRepository.findBySettingKey("EXCHANGE_RATE_EUR_MGA")
                .orElseGet(() -> {
                    GeneralSetting s = new GeneralSetting();
                    s.setSettingKey("EXCHANGE_RATE_EUR_MGA");
                    return s;
                });
        setting.setSettingValue(String.valueOf(rate));
        settingRepository.save(setting);
    }
}
