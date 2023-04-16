package br.dev.erickfthz.finn.core.command.annotations;

import net.dv8tion.jda.api.interactions.commands.OptionType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {

    OptionType option();

    String name();

    String description();

    boolean isRequired();

    boolean isAutoComplete();
}
