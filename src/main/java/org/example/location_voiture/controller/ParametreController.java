package org.example.location_voiture.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/parametres")
public class ParametreController {

    @GetMapping
    public String index() {
        return "parametres/index";
    }
}
