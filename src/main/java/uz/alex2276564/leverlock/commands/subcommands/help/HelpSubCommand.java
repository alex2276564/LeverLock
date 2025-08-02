package uz.alex2276564.leverlock.commands.subcommands.help;

import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.commands.framework.builder.*;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfig;

public class HelpSubCommand implements SubCommandProvider {

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("help")
                .permission("leverlock.command")
                .description("Show help information")
                .executor((sender, context) -> {
                    LeverLock plugin = LeverLock.getInstance();
                    MessagesConfig msg = plugin.getConfigManager().getMessagesConfig();

                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.header);
                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.reloadLine);
                    plugin.getMessageManager().sendMessage(sender, msg.commands.help.helpLine);
                });
    }
}