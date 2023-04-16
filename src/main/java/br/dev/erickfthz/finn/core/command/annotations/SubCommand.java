package br.dev.erickfthz.finn.core.command.annotations;

import br.dev.erickfthz.finn.core.command.ICommand;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SubCommand {

    String name();

    String description();

    Option[] options() default {};

    Class<? extends ICommand> command();
}
