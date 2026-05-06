package org.example.location_voiture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parametres")
public class ParametreController {

    @org.springframework.beans.factory.annotation.Autowired
    private org.example.location_voiture.service.GeneralSettingService generalSettingService;

    @GetMapping
    public String index(org.springframework.ui.Model model) {
        model.addAttribute("exchangeRateEur", generalSettingService.getExchangeRateEur());
        return "parametres/index";
    }

    @org.springframework.web.bind.annotation.PostMapping("/update-rate")
    public String updateRate(@org.springframework.web.bind.annotation.RequestParam("rate") String rate, 
                             org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        generalSettingService.updateSetting("EXCHANGE_RATE_EUR", rate);
        redirectAttributes.addFlashAttribute("message", "Taux de change mis à jour avec succès !");
        return "redirect:/parametres";
    }
}
