package uz.alex2276564.leverlock.commands.subcommands.reload;

import uz.alex2276564.leverlock.LeverLock;
import uz.alex2276564.leverlock.commands.framework.builder.*;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfig;

public class ReloadSubCommand implements SubCommandProvider {

    @Override
    public SubCommandBuilder build(CommandBuilder parent) {
        return parent.subcommand("reload")
                .permission("leverlock.reload")
                .description("Reload plugin configuration")
                .argument(new ArgumentBuilder<>("type", ArgumentType.STRING)
                        .optional("config")
                        .suggestions("config", "all"))
                .executor((sender, context) -> {
                    String type = context.getArgument("type");

                    try {
                        LeverLock.getInstance().getConfigManager().reload();

                        MessagesConfig msg = LeverLock.getInstance().getConfigManager().getMessagesConfig();
                        String successMessage = msg.commands.reload.success.replace("{type}", type);

                        LeverLock.getInstance().getMessageManager().sendMessage(sender, successMessage);

                    } catch (Exception e) {
                        MessagesConfig msg = LeverLock.getInstance().getConfigManager().getMessagesConfig();
                        String errorMessage = msg.commands.reload.error.replace("{error}", e.getMessage());

                        LeverLock.getInstance().getMessageManager().sendMessage(sender, errorMessage);
                    }
                });
    }
}