package com.handwin.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by fangliang on 14/1/15.
 */
public class AdminCommand {

    public final static char SPACE = ' ';

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入命令：");
        Command preCommand = null;
        StringBuffer content = new StringBuffer();
        while (true) {
            String line;
            try {
                line = scanner.nextLine();
            } catch (Exception e) {
                System.out.println("No line error process finished !");
                break;
            }
            if (preCommand == null) {
                preCommand = Command.getCommand(line);
                if (preCommand == null) {
                    continue;
                }
                if (preCommand == Command.End) {
                    preCommand = null;
                    continue;
                }

                if (preCommand == Command.LIST) {
                    list();
                    preCommand = null;
                    continue;
                }

                if (preCommand == Command.QUIT) {
                    System.out.println("\nReceive quit command process finished !");
                    break;
                }
                continue;
            }
            Command command = Command.getCommand(line);
            if (command == null || (command != Command.QUIT && command != Command.End)) {
                content.append(line);
                content.append(SPACE);
            } else {
                if (command == Command.End) {
                    handle(preCommand, content);
                    preCommand = null;
                    content = new StringBuffer();
                    continue;
                } else {
                    System.out.println("\nReceive quit command process finished !");
                    break;
                }
            }
        }
    }

    public static void handle(Command command, StringBuffer content) {
        if (command == Command.REFRESH_HEART_ALL) {
            System.out.print("REFRESH_HEART_ALL : " + content.toString());
        }
    }

    public static void list() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("命令 QUIT 结束进程");
        buffer.append("\n");
        buffer.append("命令 REFRESH_HEART_ALL 通知TCP SERVER 转发所有心跳到 BIZ SERVER");
        buffer.append("\n");
        buffer.append("命令 END 结束当前命令 ");
        buffer.append("\n");
        buffer.append("具体使用  Command  回车  你想输入的命令追加的内容  END  执行命令 开始下一命令  QUIT 结束");
        System.out.println(buffer.toString());
    }

    public static enum Command {
        QUIT("QUIT"),

        REFRESH_HEART_ALL("REFRESH_HEART_ALL"),

        LIST("LIST"),

        End("END");

        private String commandStr;

        private Command(String commandStr) {
            this.commandStr = commandStr;
        }

        public String getCommandStr() {
            return commandStr;
        }

        static Map<String, Command> COMMAND_MAP = new HashMap<>();

        static {
            for (Command command : Command.values()) {
                COMMAND_MAP.put(command.getCommandStr(), command);
            }
        }

        public static Command getCommand(String commandStr) {
            return COMMAND_MAP.get(commandStr.toUpperCase());
        }

    }


}
