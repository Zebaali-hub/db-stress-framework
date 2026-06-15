package com.zebacodes.dbstress.controller;

import com.zebacodes.dbstress.model.StressTestConfig;
import com.zebacodes.dbstress.model.WorkloadType;
import com.zebacodes.dbstress.service.StressTestService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@Controller
public class DashboardController {

    private final StressTestService stressTestService;

    public DashboardController(StressTestService stressTestService) {
        this.stressTestService = stressTestService;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("config", new StressTestConfig());
        model.addAttribute("workloadTypes", WorkloadType.values());
        model.addAttribute("history", stressTestService.history());
        return "index";
    }

    @GetMapping("/dashboard/{id}")
    public String dashboard(@PathVariable UUID id, Model model) {
        model.addAttribute("test", stressTestService.getResult(id));
        model.addAttribute("testId", id);
        return "dashboard";
    }
}
