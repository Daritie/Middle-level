package me.darity.mid.util

/**
 * Аннотация, которой помечаются методы, возвращающие LiteralCommandNode<CommandSourceStack> для регистрации
 * @param pathToAliases Путь в конфиге до списка названий команды
 * @param aliases Массив названий команды
 * Команда должна регистрироваться с названиями, указанными и в конфиге, и в параметре aliases. Если ничего не указано, команда должна быть проигнорирована
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class CommandRegistrar(
    val pathToAliases: String = "",
    val aliases: Array<String> = []
)
