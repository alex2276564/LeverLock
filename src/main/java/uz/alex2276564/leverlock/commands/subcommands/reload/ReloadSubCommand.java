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

                    MessagesConfig msg = LeverLock.getInstance().getConfigManager().getMessagesConfig();
                    try {
                        LeverLock.getInstance().getConfigManager().reload();

                        LeverLock.getInstance().getMessageManager().sendMessage(sender, msg.commands.reload.success, "type", type);

                    } catch (Exception e) {

                        LeverLock.getInstance().getMessageManager().sendMessage(sender, msg.commands.reload.error, "error", e.getMessage());
                    }
                });
    }
}