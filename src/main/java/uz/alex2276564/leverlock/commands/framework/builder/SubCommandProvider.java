package uz.alex2276564.leverlock.commands.framework.builder;

public interface SubCommandProvider {
    SubCommandBuilder build(CommandBuilder parent);
}