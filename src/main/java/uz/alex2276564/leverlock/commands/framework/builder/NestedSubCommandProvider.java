package uz.alex2276564.leverlock.commands.framework.builder;

public interface NestedSubCommandProvider {
    SubCommandBuilder build(SubCommandBuilder parent);
}