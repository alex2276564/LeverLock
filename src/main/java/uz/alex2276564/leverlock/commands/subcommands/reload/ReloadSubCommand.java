package uz.alex2276564.leverlock.commands.subcommands.reload;

import uz.alex2276564.leverlock.LeverLockServices;
import uz.alex2276564.leverlock.commands.framework.builder.ArgumentBuilder;
import uz.alex2276564.leverlock.commands.framework.builder.ArgumentType;
import uz.alex2276564.leverlock.commands.framework.builder.CommandBuilder;
import uz.alex2276564.leverlock.commands.framework.builder.SubCommandBuilder;
import uz.alex2276564.leverlock.commands.framework.builder.SubCommandProvider;
import uz.alex2276564.leverlock.config.configs.messagesconfig.MessagesConfig;

public class ReloadSubCommand implements SubCommandProvider {

    private final LeverLockServices services;

    public ReloadSubCommand(LeverLockServices services) {
        this.services = services;
    }

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

                    var configManager = services.configManager();
                    var messageManager = services.messageManager();

                    MessagesConfig msg = configManager.getMessagesConfig();
                    try {
                        configManager.reload();

                        messageManager.sendMessageKeyed(sender,
                                "commands.reload.success",
                                msg.commands.reload.success,
                                "type", type);

                    } catch (Exception e) {
                        String error = (e.getMessage() != null) ? e.getMessage() : "unknown";
                        messageManager.sendMessageKeyed(sender,
                                "commands.reload.error",
                                msg.commands.reload.error,
                                "error", error);
                    }
                });
    }
}