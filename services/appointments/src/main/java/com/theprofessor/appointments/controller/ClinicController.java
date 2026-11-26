package com.theprofessor.appointments.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.theprofessor.appointments.config.ClinicProperties;
import com.theprofessor.appointments.service.ClinicInfoService;

@RestController
@RequestMapping("/clinic")
public class ClinicController {

    private final ClinicInfoService clinicInfoService;

    public ClinicController(ClinicInfoService clinicInfoService) {
        this.clinicInfoService = clinicInfoService;
    }

    @GetMapping
    public ClinicProperties getInfo() {
        return clinicInfoService.getInfo();
    }
}
