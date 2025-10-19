package com.kaiburr.task1.util;

import org.springframework.stereotype.Component;
import java.util.Arrays;

@Component
public class CommandValidator {

    // A simple blacklist of potentially harmful commands
    private static final String[] BLOCKED_COMMANDS = {"rm", "shutdown", "reboot", "del", "format"};

    public boolean isCommandSafe(String command) {
        if (command == null || command.trim().isEmpty()) {
            return false;
        }
        String lowerCaseCommand = command.toLowerCase();
        return Arrays.stream(BLOCKED_COMMANDS).noneMatch(lowerCaseCommand::contains);
    }
}