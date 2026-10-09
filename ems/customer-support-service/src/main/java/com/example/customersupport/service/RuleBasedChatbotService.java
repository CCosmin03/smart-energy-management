package com.example.customersupport.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class RuleBasedChatbotService {

    public String getAutomaticResponse(String message) {

        if (message == null || message.isBlank()) {
            return null;
        }

        String msg = message.toLowerCase(Locale.ROOT);

        // ====== RULE 1 ======
        if (msg.contains("salut") || msg.contains("buna")) {
            return "Salut! Cu ce te pot ajuta?";
        }

        // ====== RULE 2 ======
        if (msg.contains("ajutor") || msg.contains("help")) {
            return "Sunt aici sa te ajut. Spune-mi problema ta.";
        }

        // ====== RULE 3 ======
        if (msg.contains("consum")) {
            return "Poti verifica consumul dispozitivelor tale in sectiunea Monitoring.";
        }

        // ====== RULE 4 ======
        if (msg.contains("dispozitiv") || msg.contains("device")) {
            return "Dispozitivele tale pot fi gestionate din meniul Devices.";
        }

        // ====== RULE 5 ======
        if (msg.contains("eroare") || msg.contains("problema")) {
            return "Imi pare rau pentru problema. Poti oferi mai multe detalii?";
        }

        // ====== RULE 6 ======
        if (msg.contains("cont") || msg.contains("account")) {
            return "Setarile contului sunt disponibile in profilul tau.";
        }

        // ====== RULE 7 ======
        if (msg.contains("parola") || msg.contains("password")) {
            return "Pentru resetarea parolei, foloseste optiunea 'Forgot password'.";
        }

        // ====== RULE 8 ======
        if (msg.contains("admin")) {
            return "Un administrator va prelua conversatia daca este necesar.";
        }

        // ====== RULE 9 ======
        if (msg.contains("multumesc") || msg.contains("merci")) {
            return "Cu placere! Daca mai ai intrebari, sunt aici.";
        }

        // ====== RULE 10 ======
        if (msg.contains("la revedere") || msg.contains("bye")) {
            return "La revedere! O zi frumoasa!";
        }

        // Nicio regula potrivita → fallback AI
        return null;
    }
}
